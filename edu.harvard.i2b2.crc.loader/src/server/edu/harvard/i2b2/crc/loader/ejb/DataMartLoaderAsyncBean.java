package edu.harvard.i2b2.crc.loader.ejb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.loader.dao.ILoaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.dao.UniqueKeyException;
import edu.harvard.i2b2.crc.loader.dao.UploadStatusDAOI;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.InputOptionListType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishDataRequestType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.SetStatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.StatusType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType.DataFileLocationUri;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DataMartLoaderAsyncBean implements DataMartLoaderAsyncBeanRemote,
		DataMartLoaderAsyncBeanLocal {

	DTOFactory dtoFactory = new DTOFactory();
	Connection connection = null;
	@Resource(mappedName = "ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	@Resource
	private SessionContext sc;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadrunner")
	private static Queue queue;
	@Resource(mappedName = "jms/edu.harvard.i2b2.crc.loader.loadresponse")
	private static Queue responseQueue;

	@Resource
	private UserTransaction utx;

	// get this
	public static final String LOG_REFERENCE_PREFIX = "";

	static final Log log = LogFactory.getLog("DataMartLoaderBean");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.harvard.i2b2.crc.loader.ejb.IDataMartLoaderBean#load(java.lang.String
	 * )
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public LoadDataResponseType load(DataSourceLookup dataSourceLookup,
			String publishMessage, SecurityType i2b2SecurityType, long timeout,
			String fileSystemDefaultStorageResource) throws I2B2Exception {
		LoadDataResponseType response = null;
		String userId = null, password = null;
		int uploadId = 0;
		Exception exception = null;

		// //String hiveId, String projectId, String ownerId
		LoaderDAOFactoryHelper daoHelper = new LoaderDAOFactoryHelper(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		ILoaderDAOFactory loaderDaoFactory = daoHelper.getDAOFactory();
		IUploaderDAOFactory uploaderDaoFactory = loaderDaoFactory
				.getUpLoaderDAOFactory();
		try {
			if (i2b2SecurityType == null) {
				String errorMsg = "DataMartLoaderAsyncBean.load: input security type is null";
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg);
			} else {
				userId = i2b2SecurityType.getUsername();
				password = i2b2SecurityType.getPassword();

			}
			PublishDataRequestType publishType = null;

			try {

				JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
						.getJAXBUtil();

				JAXBElement<?> jaxbElement = jaxbUtil
						.unMashallFromString(publishMessage);
				publishType = (PublishDataRequestType) jaxbElement.getValue();

			} catch (JAXBUtilException jaxbEx) {

				throw new I2B2Exception("Error proacessing request message "
						+ jaxbEx.getMessage(), jaxbEx);
			}

			utx.begin();
			uploadId = createUploadStatus(uploaderDaoFactory, publishType,
					userId);
			log.info("Created Upload Status: uploadId=" + uploadId);
			utx.commit();

			utx.begin();
			response = sendAndGetQueueResponse(uploaderDaoFactory, uploadId,
					i2b2SecurityType, publishMessage, timeout,
					fileSystemDefaultStorageResource);
			utx.commit();
			// return processUploadMessage(publishType);
		} catch (I2B2Exception e) {
			exception = e;
		} catch (NotSupportedException e) {
			exception = e;
		} catch (SystemException e) {
			exception = e;
		} catch (SecurityException e) {
			exception = e;
		} catch (IllegalStateException e) {
			exception = e;
		} catch (RollbackException e) {
			exception = e;
		} catch (HeuristicMixedException e) {
			exception = e;
		} catch (HeuristicRollbackException e) {
			exception = e;
		} finally {
			if (exception != null) {
				try {
					if (utx.getStatus() == Status.STATUS_ACTIVE) {
						utx.rollback();
					}
				} catch (Exception e) {
				}

				try {
					utx.begin();
					// update status

					StringWriter stringWriter = new StringWriter();
					exception.printStackTrace(new PrintWriter(stringWriter));
					updateUploadStatus(uploaderDaoFactory, uploadId,
							"INCOMPLETE", stringWriter.toString());
					// build response
					response = buildResponse(uploaderDaoFactory, uploadId);
					utx.commit();
				} catch (Exception e) {
					try {
						utx.rollback();
					} catch (Exception e1) {

					}
				}

				String errorMsg = "LoadDataResponseType.load:Error "
						+ exception.getMessage();
				log.error(errorMsg);
				throw new I2B2Exception(errorMsg, exception);

			}

		}

		return response;

	}

	private int createUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			PublishDataRequestType publishType, String userId)
			throws I2B2Exception {
		InputOptionListType inputOptionType = publishType.getInputList();
		String loadFileName = inputOptionType.getDataFile().getLocationUri()
				.getValue();
		String sourceSystemCd = inputOptionType.getDataFile()
				.getSourceSystemCd();

		String status = "QUEUED";
		String uploadLabel = inputOptionType.getDataFile().getLoadLabel();
		UploadStatus uploadStatus = new UploadStatus();
		uploadStatus.setInputFileName(loadFileName);
		uploadStatus.setUploadLabel(uploadLabel);
		uploadStatus.setSourceCd(sourceSystemCd);
		uploadStatus.setLoadStatus(status);
		uploadStatus.setUserId(userId);
		uploadStatus.setLoadDate(new Date(System.currentTimeMillis()));

		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		return uploadStatusDAO.insertUploadStatus(uploadStatus);

	}

	private void updateUploadStatus(IUploaderDAOFactory uploaderDaoFactory,
			int uploadId, String status, String message) throws I2B2Exception {
		UploadStatusDAOI uploadStatusDAO = uploaderDaoFactory
				.getUploadStatusDAO();
		// try {
		UploadStatus uploadStatus;
		try {
			uploadStatus = uploadStatusDAO.findById(uploadId);
			uploadStatus.setLoadStatus(status);
			if (message != null) {
				int length = (message.length() > 3995) ? 3995 : message
						.length();
				uploadStatus.setMessage(message.substring(0, length));
			}
			uploadStatus.setEndDate(new Date(System.currentTimeMillis()));
			uploadStatusDAO.updateUploadStatus(uploadStatus);
			// uploadStatusDAO.calculateUploadStatus(uploadId);

		} catch (UniqueKeyException unqEx) {
			unqEx.printStackTrace();
			log.error("Error while upload status update", unqEx);
		}
	}

	private LoadDataResponseType sendAndGetQueueResponse(
			IUploaderDAOFactory uploaderDaoFactory, int uploadId,
			SecurityType securityType, String publishMessage, long timeout,
			String fileSystemDefaultStorageResource) {
		Session session = null;
		MessageProducer producer = null;
		MessageConsumer receiver = null;
		TextMessage message = null;
		LoadDataResponseType response = null;
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(queue);
			message = session.createTextMessage();

			message.setJMSCorrelationID(String.valueOf(uploadId));
			message.setIntProperty(DataMartLoaderBeanMDB.UPLOAD_ID, uploadId);
			message.setStringProperty(DataMartLoaderBeanMDB.I2B2_USER_ID,
					securityType.getUsername());
			message.setStringProperty(DataMartLoaderBeanMDB.I2B2_PASSWORD,
					securityType.getPassword());

			message.setStringProperty(
					DataMartLoaderBeanMDB.DS_LOOKUP_DOMAIN_ID,
					uploaderDaoFactory.getDataSourceLookup().getDomainId());
			message.setStringProperty(DataMartLoaderBeanMDB.DS_LOOKUP_OWNER_ID,
					uploaderDaoFactory.getDataSourceLookup().getOwnerId());
			message.setStringProperty(
					DataMartLoaderBeanMDB.DS_LOOKUP_PROJECT_ID,
					uploaderDaoFactory.getDataSourceLookup().getProjectPath());
			message.setStringProperty(
					DataMartLoaderBeanMDB.IROD_FILESYSTEM_STORAGE_RESOURCE,
					fileSystemDefaultStorageResource);

			message.setText(publishMessage);
			log.info("DataMartLoaderSync: Sending " + "message text to: "
					+ message.getText());
			producer.send(message);

			//
			String selector = "JMSCorrelationID='" + uploadId + "'";
			receiver = session.createConsumer(responseQueue, selector);

			connection.start();

			TextMessage inMessage = (TextMessage) receiver.receive(timeout);
			if (inMessage != null) {
				System.out.println("Received text message from response queue"
						+ inMessage.getText());

			}
			response = buildResponse(uploaderDaoFactory, uploadId);
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.sendAndGetQueueResponse: "
					+ "Exception: " + t.toString());

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			try {
				buildResponse(uploaderDaoFactory, uploadId);
			} catch (I2B2Exception e) {
				StatusType statusType = new StatusType();
				StatusType.Condition condition = new StatusType.Condition();
				condition.setType("ERROR");
				condition.setValue(sw.toString());
				statusType.getCondition().add(condition);
				response.setStatus(statusType);
			}
		} finally {
			if (session != null) {
				try {
					session.close();
				} catch (JMSException e) {
				}
			}

		}
		return response;

	}

	private LoadDataResponseType buildResponse(
			IUploaderDAOFactory uploaderDaoFactory, int uploadId)
			throws I2B2Exception {
		LoadDataResponseType response = new LoadDataResponseType();
		try {
			UploadStatusDAOI statusDao = uploaderDaoFactory
					.getUploadStatusDAO();

			UploadStatus uploadStatus = statusDao.findById(uploadId);
			// build response
			DataFileLocationUri fileLoc = new DataFileLocationUri();
			fileLoc.setValue(uploadStatus.getInputFileName());
			response.setDataFileLocationUri(fileLoc);
			response.setLoadStatus(uploadStatus.getLoadStatus());
			response.setUploadId(String.valueOf(uploadStatus.getUploadId()));
			response.setUserId(uploadStatus.getUserId());
			response.setMessage(uploadStatus.getMessage());
			response.setTransformerName(uploadStatus.getTransformName());
			response.setStartDate(dtoFactory
					.getXMLGregorianCalendar(uploadStatus.getLoadDate()
							.getTime()));
			if (uploadStatus.getEndDate() != null) {
				response.setEndDate(dtoFactory
						.getXMLGregorianCalendar(uploadStatus.getEndDate()
								.getTime()));
			}

			List<UploadSetStatus> setStatusList = statusDao
					.getUploadSetStatusByLoadId(uploadId);
			for (UploadSetStatus setStatus : setStatusList) {
				SetStatusType responseSetStatusType = new SetStatusType();
				responseSetStatusType.setIgnoredRecord(setStatus
						.getNoOfRecord()
						- setStatus.getLoadedRecord());
				responseSetStatusType.setInsertedRecord(setStatus
						.getLoadedRecord());
				responseSetStatusType.setMessage(setStatus.getMessage());
				responseSetStatusType.setTotalRecord(setStatus.getNoOfRecord());
				if (setStatus.getSetTypeId() == 1) {
					response.setEventSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 2) {
					response.setPatientSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 3) {
					response.setConceptSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 4) {
					response.setObserverSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 5) {
					response.setObservationSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 6) {
					response.setPidSet(responseSetStatusType);
				} else if (setStatus.getSetTypeId() == 7) {
					response.setEventidSet(responseSetStatusType);
				}

			}
		} catch (I2B2Exception i2b2Ex) {
			throw new I2B2Exception(
					"DataMartLoaderAsync.buildResponse:Exception"
							+ i2b2Ex.getMessage(), i2b2Ex);
		}
		return response;
	}

	/**
	 * Creates the connection.
	 */
	@PostConstruct
	public void makeConnection() {
		try {
			connection = connectionFactory.createConnection();
		} catch (Throwable t) {
			// JMSException could be thrown
			log.error("DataMartLoaderAsync.makeConnection:" + "Exception: "
					+ t.toString());
		}
	}

	/**
	 * Closes the connection.
	 */
	@PreDestroy
	public void endConnection() throws RuntimeException {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
