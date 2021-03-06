/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.loader.ws;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.SecurityType;

/**
 * Class to hold helper functions to 
 * pack and unwrap xml payload 
 * @author rkuttan
 */
public abstract class CRCAxisAbstract {
	public static MessageHeaderType generateMessageHeader() {
		MessageHeaderType messageHeader = new MessageHeaderType();
		messageHeader.setI2B2VersionCompatible(new BigDecimal("1.0"));
		messageHeader.setHl7VersionCompatible(new BigDecimal("2.4"));
		edu.harvard.i2b2.crc.loader.datavo.i2b2message.ApplicationType appType = new edu.harvard.i2b2.crc.loader.datavo.i2b2message.ApplicationType();
		appType.setApplicationName("i2b2 Project Management");
		appType.setApplicationVersion("1.0"); 
		messageHeader.setSendingApplication(appType);
		Date currentDate = new Date();
		DTOFactory factory = new DTOFactory();
		messageHeader.setDatetimeOfMessage(factory.getXMLGregorianCalendar(currentDate.getTime()));
		messageHeader.setAcceptAcknowledgementType("AL");
		messageHeader.setApplicationAcknowledgementType("AL");
		messageHeader.setCountryCode("US");
		SecurityType securityType = new SecurityType();
		securityType.setDomain("demo");
		securityType.setUsername("demo");
		securityType.setPassword("demouser");
		messageHeader.setSecurity(securityType);
		return messageHeader;
	}
	
	public static RequestHeaderType generateRequestHeader() {
		RequestHeaderType reqHeaderType = new RequestHeaderType(); 
		reqHeaderType.setResultWaittimeMs(90000);
		return reqHeaderType;
	}
	
	public static String getQueryString(String filename) throws Exception  { 
		StringBuffer queryStr = new StringBuffer();
		DataInputStream dataStream = new DataInputStream(new FileInputStream(filename));
		while(dataStream.available()>0) {
			queryStr.append(dataStream.readLine() + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();	
	}
	
	public static ServiceClient getServiceClient(String serviceUrl) throws Exception {
		Options options = new Options();
		EndpointReference endpointReference = new EndpointReference(serviceUrl);
		options.setTo(endpointReference);
		options.setTimeOutInMilliSeconds(1800000);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		
		//Constants.s
		//options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE); 
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, "/tmp"); 
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "8000");
		options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
		//options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		return sender;
	}
	
	
	public static ServiceClient getRestServiceClient(String serviceUrl) throws Exception {
		Options options = new Options();
		EndpointReference endpointReference = new EndpointReference(serviceUrl);
		options.setTo(endpointReference);
		options.setTimeOutInMilliSeconds(1800000);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		
		//Constants.s
		options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
		//options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		//options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE); 
		//options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR, "/tmp"); 
		//options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD, "8000");
		//options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
		//options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		return sender;
	}
	
	public static OMElement convertStringToOMElement(String requestXmlString) throws Exception { 
		StringReader strReader = new StringReader(requestXmlString);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = xif.createXMLStreamReader(strReader);

        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement lineItem = builder.getDocumentElement();
        return lineItem;
	}
	
}
