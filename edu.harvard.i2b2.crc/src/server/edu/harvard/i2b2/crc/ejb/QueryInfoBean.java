/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.crc.dao.setfinder.IQueryResultTypeDao;
import edu.harvard.i2b2.crc.datavo.PSMFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.db.QtQueryResultType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryResultTypeType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.RequestXmlType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultTypeResponseType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.UserRequestType;

/**
 * Ejb manager class for query operation
 * 
 * @author rkuttan
 * 
 * @ejb.bean description="QueryTool Query Info"
 *           display-name="QueryTool Query Info"
 *           jndi-name="ejb.querytool.QueryInfo"
 *           local-jndi-name="ejb.querytool.QueryInfoLocal"
 *           name="querytool.QueryInfo" type="Stateless" view-type="both"
 *           transaction-type="Container"
 * 
 * 
 * 
 * @ejb.interface remote-class="edu.harvard.i2b2.crc.ejb.QueryInfoRemote"
 * 
 * 
 */
public class QueryInfoBean implements SessionBean {
	private static Log log = LogFactory.getLog(QueryInfoBean.class);

	/**
	 * Function to return master query list for the given user id
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param userRequestType
	 *            user_id
	 * 
	 * @return String publish response XML
	 */
	public MasterResponseType getQueryMasterListFromUserId(
			DataSourceLookup dataSourceLookup, UserRequestType userRequestType)
			throws I2B2DAOException {

		String userId = userRequestType.getUserId();
		int fetchSize = userRequestType.getFetchSize();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao.getQueryMasterByUserId(
				userId, fetchSize);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
	}

	/**
	 * Function to return master query list for the give group id
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param userRequestType
	 *            group_id
	 * 
	 * @return String publish response XML
	 * @throws I2B2DAOException
	 */
	public MasterResponseType getQueryMasterListFromGroupId(
			DataSourceLookup dataSourceLookup, UserRequestType userRequestType)
			throws I2B2DAOException {
		String groupId = userRequestType.getGroupId();
		int fetchSize = userRequestType.getFetchSize();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();
		List<QtQueryMaster> masterList = queryMasterDao
				.getQueryMasterByGroupId(groupId, fetchSize);
		MasterResponseType masterResponseType = buildMasterResponseType(masterList);
		return masterResponseType;
	}

	/**
	 * Function to publish patients using publish message format.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return String publish response XML
	 */
	public MasterResponseType getRequestXmlFromMasterId(
			DataSourceLookup dataSourceLookup, String userId,
			MasterRequestType masterRequestType) throws I2B2Exception {
		String queryMasterId = masterRequestType.getQueryMasterId();
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

		QtQueryMaster qtQueryMaster = queryMasterDao
				.getQueryDefinition(queryMasterId);
		MasterResponseType masterResponseType = new MasterResponseType();
		if (qtQueryMaster != null) {
			QueryMasterType queryMasterType = new QueryMasterType();
			queryMasterType.setQueryMasterId(qtQueryMaster.getQueryMasterId());
			queryMasterType.setName(qtQueryMaster.getName());
			queryMasterType.setUserId(qtQueryMaster.getUserId());
			String requestXml = qtQueryMaster.getRequestXml();

			if (requestXml != null) {
				Document doc = null;
				RequestXmlType requestXmlType = new RequestXmlType();
				try {
					/*
					 * //get jaxb object JAXBContext jc1 =
					 * JAXBContext.newInstance
					 * (edu.harvard.i2b2.crc.datavo.setfinder
					 * .query.ObjectFactory.class); Unmarshaller unMarshaller =
					 * jc1.createUnmarshaller(); JAXBElement jaxbElement =
					 * (JAXBElement)unMarshaller.unmarshal(new
					 * StringReader(requestXml)); QueryDefinitionType
					 * queryDefinition =
					 * (QueryDefinitionType)jaxbElement.getValue();
					 * 
					 * 
					 * //marshall to dom JAXBContext jc =
					 * JAXBContext.newInstance
					 * (edu.harvard.i2b2.crc.datavo.setfinder
					 * .query.QueryDefinitionType.class); Marshaller m =
					 * jc.createMarshaller(); DocumentBuilderFactory f =
					 * DocumentBuilderFactory.newInstance(); DocumentBuilder
					 * builder = f.newDocumentBuilder(); doc =
					 * builder.newDocument(); m.marshal((new
					 * edu.harvard.i2b2.crc
					 * .datavo.setfinder.query.ObjectFactory(
					 * )).createQueryDefinition(queryDefinition), doc);
					 */

					doc = edu.harvard.i2b2.common.util.xml.XMLUtil
							.convertStringToDOM(requestXml);
					log.debug("query definition xml prefix "
							+ doc.getDocumentElement().getPrefix());
					requestXmlType.getContent().add(doc.getDocumentElement());
				} catch (Exception i2b2) {
					i2b2.printStackTrace();
					throw new I2B2Exception(
							"Error converting request xml to dom "
									+ i2b2.getMessage(), i2b2);
				}
				queryMasterType.setRequestXml(requestXmlType);
			}
			masterResponseType.getQueryMaster().add(queryMasterType);
		} else {
			throw new I2B2Exception("Could not find query for masterId: ["
					+ queryMasterId + "]");
		}
		return masterResponseType;
	}

	/**
	 * Function to delete master query
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param string
	 *            user id
	 * @param int master id
	 * 
	 * @return String Master Query response XML
	 */
	public MasterResponseType deleteQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId)
			throws I2B2Exception {

		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

		queryMasterDao.deleteQuery(masterId);

		MasterResponseType masterResponseType = new MasterResponseType();
		QueryMasterType queryMasterType = new QueryMasterType();
		queryMasterType.setQueryMasterId(masterId);
		masterResponseType.getQueryMaster().add(queryMasterType);
		return masterResponseType;
	}

	/**
	 * Function to rename master query
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 * @param int session id publish request XML fileName
	 * 
	 * @return Master Query response XML
	 */
	public MasterResponseType renameQueryMaster(
			DataSourceLookup dataSourceLookup, String userId, String masterId,
			String queryNewName) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryMasterDao queryMasterDao = sfDaoFactory.getQueryMasterDAO();

		queryMasterDao.renameQuery(masterId, queryNewName);

		MasterResponseType masterResponseType = new MasterResponseType();
		QueryMasterType queryMasterType = new QueryMasterType();
		queryMasterType.setQueryMasterId(masterId);
		queryMasterType.setUserId(userId);
		queryMasterType.setName(queryNewName);
		masterResponseType.getQueryMaster().add(queryMasterType);
		return masterResponseType;
	}

	/**
	 * Function to return all query result type
	 * 
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * 
	 */
	public ResultTypeResponseType getAllResultType(
			DataSourceLookup dataSourceLookup) throws I2B2Exception {
		SetFinderDAOFactory sfDaoFactory = this.getSetFinderDaoFactory(
				dataSourceLookup.getDomainId(), dataSourceLookup
						.getProjectPath(), dataSourceLookup.getOwnerId());
		IQueryResultTypeDao resultTypeDao = sfDaoFactory
				.getQueryResultTypeDao();
		List<QtQueryResultType> queryResultTypeList = resultTypeDao
				.getAllQueryResultType();
		ResultTypeResponseType resultTypeResponseType = new ResultTypeResponseType();
		List<QueryResultTypeType> returnQueryResultType = new ArrayList<QueryResultTypeType>();
		for (QtQueryResultType queryResultType : queryResultTypeList) {
			returnQueryResultType.add(PSMFactory
					.buildQueryResultType(queryResultType));
		}
		resultTypeResponseType.getQueryResultType().addAll(
				returnQueryResultType);
		return resultTypeResponseType;
	}

	// -------------------------------------------------
	// private functions
	// -------------------------------------------------
	private MasterResponseType buildMasterResponseType(
			List<QtQueryMaster> masterList) {
		MasterResponseType masterResponseType = new MasterResponseType();
		// masterResponseType
		DTOFactory dtoFactory = new DTOFactory();
		for (QtQueryMaster queryMaster : masterList) {
			QueryMasterType queryMasterType = new QueryMasterType();
			queryMasterType.setQueryMasterId(queryMaster.getQueryMasterId());
			java.util.Date createDate = queryMaster.getCreateDate();
			queryMasterType.setCreateDate(dtoFactory
					.getXMLGregorianCalendar(createDate.getTime()));
			java.util.Date deleteDate = queryMaster.getDeleteDate();
			if (deleteDate != null) {
				queryMasterType.setDeleteDate(dtoFactory
						.getXMLGregorianCalendar(deleteDate.getTime()));
			}
			queryMasterType.setName(queryMaster.getName());
			queryMasterType.setGroupId(queryMaster.getGroupId());
			queryMasterType.setUserId(queryMaster.getUserId());
			masterResponseType.getQueryMaster().add(queryMasterType);
		}
		return masterResponseType;
	}

	private SetFinderDAOFactory getSetFinderDaoFactory(String domainId,
			String projectPath, String ownerId) throws I2B2DAOException {
		DAOFactoryHelper helper = new DAOFactoryHelper(domainId, projectPath,
				ownerId);
		SetFinderDAOFactory sfDaoFactory = helper.getDAOFactory()
				.getSetFinderDAOFactory();
		return sfDaoFactory;
	}

	// --------------------------------
	// ejb functions
	// --------------------------------
	public void ejbCreate() throws CreateException {
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext arg0) throws EJBException,
			RemoteException {
	}
}
