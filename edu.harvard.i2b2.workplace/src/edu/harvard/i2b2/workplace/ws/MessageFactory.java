/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Raj Kuttan
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageControlIdType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ProcessingIdType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.workplace.datavo.wdo.FoldersType;
import edu.harvard.i2b2.workplace.util.WorkplaceJAXBUtil;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringReader;
import java.io.StringWriter;

import java.math.BigDecimal;

import java.util.Date;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Factory class to create request/response message objects.
 *
 */
public class MessageFactory {
    private static Log log = LogFactory.getLog(MessageFactory.class);

    /**
     * Function creates Workplace response OMElement from xml string
     * @param xmlString
     * @return OMElement
     * @throws XMLStreamException
     */
    public static OMElement createResponseOMElementFromString(String xmlString) 
        throws I2B2Exception {
        OMElement returnElement = null;

        try {
        	StringReader strReader = new StringReader(xmlString);
        	XMLInputFactory xif = XMLInputFactory.newInstance();
        	XMLStreamReader reader = xif.createXMLStreamReader(strReader);

        	StAXOMBuilder builder = new StAXOMBuilder(reader);
        	returnElement = builder.getDocumentElement();

        } catch (XMLStreamException e) {
            log.error("xml stream response WDO to OMElement");
            throw new I2B2Exception("XML Stream error ", e);
        } catch (Exception e) {
            log.error("Error while converting Workplace response WDO to OMElement");
            throw new I2B2Exception("Response OMElement creation error ", e);
        } 
        return returnElement;
    }



    /**
     * Function to build workplaceData body type
     *
     * @param obsSet
     *            Observation fact set to be returned to requester
     * @return BodyType object
     */
    public static BodyType createBodyType(FoldersType workplaceData) {

        edu.harvard.i2b2.workplace.datavo.wdo.ObjectFactory of = new edu.harvard.i2b2.workplace.datavo.wdo.ObjectFactory();
        BodyType bodyType = new BodyType();
        bodyType.getAny().add(of.createFolders(workplaceData));

        return bodyType;
    }

    /**
     * Function to create response  message header based
     * on request message header
     *
     * @return MessageHeader object
     */
    public static MessageHeaderType createResponseMessageHeader(MessageHeaderType reqMsgHeader) {
        MessageHeaderType messageHeader = new MessageHeaderType();

        messageHeader.setI2B2VersionCompatible(new BigDecimal("1.1"));
		messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));

        ApplicationType appType = new ApplicationType();
        appType.setApplicationName("Workplace Cell");
        appType.setApplicationVersion("1.0");
        messageHeader.setSendingApplication(appType);

        FacilityType facility = new FacilityType();
        facility.setFacilityName("i2b2 Hive");
        messageHeader.setSendingFacility(facility);

        if (reqMsgHeader != null) {
        	ApplicationType recvApp = new ApplicationType();
        	recvApp.setApplicationName(reqMsgHeader.getSendingApplication().getApplicationName());
        	recvApp.setApplicationVersion(reqMsgHeader.getSendingApplication().getApplicationVersion());
        	messageHeader.setReceivingApplication(recvApp);
        	
        	FacilityType recvFac = new FacilityType();
        	recvFac.setFacilityName(reqMsgHeader.getSendingFacility().getFacilityName());
        	messageHeader.setReceivingFacility(recvFac);

        	messageHeader.setSecurity(reqMsgHeader.getSecurity());
        }

        Date currentDate = new Date();
        DTOFactory factory = new DTOFactory();
        messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(
                currentDate.getTime()));

        MessageControlIdType mcIdType = new MessageControlIdType();
        mcIdType.setInstanceNum(1);

        if (reqMsgHeader != null) {
            if (reqMsgHeader.getMessageControlId() != null) {
                mcIdType.setMessageNum(reqMsgHeader.getMessageControlId()
                                                        .getMessageNum());
                mcIdType.setSessionId(reqMsgHeader.getMessageControlId()
                                                       .getSessionId());
            }
        }

        messageHeader.setMessageControlId(mcIdType);

        ProcessingIdType proc = new ProcessingIdType();
        proc.setProcessingId("P");
        proc.setProcessingMode("I");
        messageHeader.setProcessingId(proc);

        messageHeader.setAcceptAcknowledgementType("AL");
        messageHeader.setApplicationAcknowledgementType("AL");
        messageHeader.setCountryCode("US");
        if (reqMsgHeader != null) {
        	messageHeader.setProjectId(reqMsgHeader.getProjectId());
        }
        return messageHeader;
    }

    /**
     * Function to create response message type
     * @param messageHeader
     * @param respHeader
     * @param bodyType
     * @return ResponseMessageType
     */
    public static ResponseMessageType createResponseMessageType(
        MessageHeaderType messageHeader, ResponseHeaderType respHeader,
        BodyType bodyType) {
        ResponseMessageType respMsgType = new ResponseMessageType();
        respMsgType.setMessageHeader(messageHeader);
        respMsgType.setMessageBody(bodyType);
        respMsgType.setResponseHeader(respHeader);

        return respMsgType;
    }

    /**
     * Function to convert ResponseMessageType to string
     * @param respMessageType
     * @return String
     * @throws Exception
     */
    public static String convertToXMLString(ResponseMessageType respMessageType)
        throws I2B2Exception {
        StringWriter strWriter = null;

        try {
            strWriter = new StringWriter();

            edu.harvard.i2b2.workplace.datavo.i2b2message.ObjectFactory objectFactory = new edu.harvard.i2b2.workplace.datavo.i2b2message.ObjectFactory();
            WorkplaceJAXBUtil.getJAXBUtil().marshaller(objectFactory.createResponse(respMessageType),
                strWriter);
        } catch (JAXBUtilException e) {
        	 log.error(e.getMessage());
            throw new I2B2Exception(
                "Error converting response message type to string " +
                e.getMessage(), e);
        }

        return strWriter.toString();
    }

    /**
     * Function to build Response message type and return it as an XML string
     *
     * @param folders   The set of Workplace folders that match request
     *
     * @return A String data type containing the ResponseMessage in XML format
     * @throws Exception
     */
    public static ResponseMessageType createBuildResponse(
        MessageHeaderType messageHeaderType,
        FoldersType folders) {
        ResponseMessageType respMessageType = null;
        BodyType bodyType = null;
        
        ResponseHeaderType respHeader = createResponseHeader("DONE",
                "Workplace processing completed");
        if(folders != null)
        	bodyType = createBodyType(folders);
        
        respMessageType = createResponseMessageType(messageHeaderType, respHeader,
                bodyType);
        
        return respMessageType;
    }

    /**
     * Function to get i2b2 Request message header
     *
     * @return RequestHeader object
     */
    public static RequestHeaderType getRequestHeader() {
        RequestHeaderType reqHeader = new RequestHeaderType();
        reqHeader.setResultWaittimeMs(120000);

        return reqHeader;
    }

    /**
     * Function to create Response with given error message
     * @param messageHeaderType
     * @param errorMessage
     * @return
     * @throws Exception
     */
    public static ResponseMessageType doBuildErrorResponse(
        MessageHeaderType messageHeaderType, String errorMessage) {
        ResponseMessageType respMessageType = null;

        MessageHeaderType messageHeader = createResponseMessageHeader(messageHeaderType);
        ResponseHeaderType respHeader = createResponseHeader("ERROR",
                errorMessage);
        respMessageType = createResponseMessageType(messageHeader, respHeader,
                null);

        return respMessageType;
    }

    /**
     * Creates ResponseHeader for the given type and value
     * @param type
     * @param value
     * @return
     */
    private static ResponseHeaderType createResponseHeader(String type,
        String value) {
        ResponseHeaderType respHeader = new ResponseHeaderType();
        StatusType status = new StatusType();
        status.setType(type);
        status.setValue(value);

        ResultStatusType resStat = new ResultStatusType();
        resStat.setStatus(status);
        respHeader.setResultStatus(resStat);

        return respHeader;
    }
   
}
