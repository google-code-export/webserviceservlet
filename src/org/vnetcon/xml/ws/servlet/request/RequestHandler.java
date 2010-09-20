/*
 *    This file is part of WebServiceServlet (WSS).
 *
 *    WSS is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    WSS is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with WSS.  If not, see <http://www.gnu.org/licenses/>.
 *    
 *    Copyright(c) 2010 Michael Kankkonen
 *    http://www.vnetcon.org
 */


package org.vnetcon.xml.ws.servlet.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;

import org.vnetcon.xml.ws.servlet.dao.MethodParameter;
import org.vnetcon.xml.ws.servlet.dao.SoapMethodCall;
import org.vnetcon.xml.ws.servlet.dao.WebMethod;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;

/**
 * This class is responsible for handling received SOAP calls.
 * doPost method in WebServiceServlet will only read the soap message
 * from requests input stream and pass this request as a string 
 * to this class.
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public class RequestHandler {
	
	JAXBContext jcEnvelope = null;
	JAXBContext jcBody = null;
	
	String wsUrl = null;
	String soapRequest = null;
	String nameSpace = null;
	HashMap<String, WebMethod> hashWebMethods = null;
	
	Class<?> wsClass = null;
	
	/**
	 * Constructor for RequestHandler
	 * 
	 * @param soapRequest       this is the actual request received from WebService client.
	 * @param wsClass           the users class which public methods are treated as WebService methods
	 * @param hashWebMethods    HashMap containing information of WebSerivce's public methods
	 * @param wsUrl             WebService url this WebService is served
	 * @throws Exception
	 */
	public RequestHandler(String soapRequest, Class<?> wsClass, HashMap<String, WebMethod> hashWebMethods, String wsUrl) throws Exception {
		String parts[] = null;
		String delim = "";
		int i = 0;
		this.soapRequest = soapRequest.replaceAll("</", "\n</").replaceAll("\n\n", "\n");
		this.soapRequest = this.soapRequest.replaceAll(">", ">\n").replaceAll("\n\n", "\n");
//		System.out.println("SOAP request: " + this.soapRequest);
		this.wsClass = wsClass;
		this.hashWebMethods = hashWebMethods;
		this.wsUrl = wsUrl;
		this.jcEnvelope = JAXBContext.newInstance(org.xmlsoap.schemas.soap.envelope.Envelope.class);
		this.jcBody = JAXBContext.newInstance(org.xmlsoap.schemas.soap.envelope.Body.class);
		
		this.nameSpace = this.wsClass.getName();
		parts = this.nameSpace.split("\\.");
		i = parts.length - 2;
		this.nameSpace = "";
		while(i >= 0){
			this.nameSpace += delim + parts[i];
			delim = ".";
			i--;
		}
	}

	/**
	 * All received requests are transformed to native java objects using
	 * JAXB Unmarshaller. To get this work properly without namespace "problems"
	 * all the namspace "tags" (e.g. ns2:) are removed using this method. 
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private String cleanNamespaces(String xml) throws Exception {
		String strBuf = xml;
		strBuf = strBuf.replaceAll("\n</.?.*(0-10).:", "\n</");
		strBuf = strBuf.replaceAll("\n<.?.*(0-10).:", "\n<");
		return strBuf;
	}

	/**
	 * This is called by RequestHandler creator and calling this
	 * will start the actual "job to be done".
	 * 
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {
		String strRet = null;
		Envelope env = this.xmlToEnvelope(this.soapRequest);
		Body body = env.getBody();
		String bodyXml = this.bodyToXML(body);
		SoapMethodCall smc = this.parseSoapBody(bodyXml);
		String methodRequest = smc.getMethodName();
		WebMethod webMethod = this.hashWebMethods.get(methodRequest);
		strRet = this.doNativeMethodCall(webMethod, smc);
		return strRet;
	}
	
	/**
	 * This method is called after all the information is prepared for actual
	 * native WebService method call (= Users WebService class's public method call)
	 * Method call is done using reflection classes.
	 * 
	 * @param webMethod
	 * @param smc
	 * @return
	 * @throws Exception
	 */
	private String doNativeMethodCall(WebMethod webMethod, SoapMethodCall smc) throws Exception {
		String strRet = "";
		List<MethodParameter> params = webMethod.getParameters();
		Class<?> partytypes[] = new Class[params.size()];
		Method callMethod = null;
		Object wsObject = this.wsClass.newInstance();
		Object oParams[] = new Object[params.size()];
		Object retObject = null;
		int i = 0;

		
		while(i < params.size()){
			MethodParameter param = params.get(i);
			String parameterClass = param.getParameterClassAsString();
			String parameterClassForXmlRetrieve = "arg" + i + "_" + parameterClass.replaceAll("\\.", "_");
			Object nativeObject = null;
			String paramClassAsString = param.getParameterClassAsString();

			// get class for name if class not java class and not native (= contains . in class as package separator)
			if(!paramClassAsString.startsWith("java.") && !paramClassAsString.startsWith("javax.") && paramClassAsString.indexOf(".") > -1){
				partytypes[i] = Class.forName(paramClassAsString);
			}

			// now we are dealing with native types like int, long, float etc.
			if(paramClassAsString.indexOf(".") == -1){
				
				if(paramClassAsString.equals("int")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Integer.parseInt(xml);
					partytypes[i] = Integer.TYPE;
				}

				if(paramClassAsString.equals("long")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Long.parseLong(xml);
					partytypes[i] = Long.TYPE;
				}

				if(paramClassAsString.equals("short")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Short.parseShort(xml);
					partytypes[i] = Short.TYPE;
				}

				if(paramClassAsString.equals("float")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Float.parseFloat(xml);
					partytypes[i] = Float.TYPE;
				}
				
				if(paramClassAsString.equals("double")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Double.parseDouble(xml);
					partytypes[i] = Double.TYPE;
				}

				if(paramClassAsString.equals("boolean")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Boolean.parseBoolean(xml);
					partytypes[i] = Boolean.TYPE;
				}
				
				if(paramClassAsString.equals("byte")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Byte.parseByte(xml);
					partytypes[i] = Byte.TYPE;
				}
				
				if(paramClassAsString.equals("long")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = Long.parseLong(xml);
					partytypes[i] = Long.TYPE;
				}
				
			}

			// now we are dealing with java types like String, BigInteger, GregorianCalendar etc.
			if(paramClassAsString.indexOf(".") > -1 && (paramClassAsString.startsWith("java.") || paramClassAsString.startsWith("javax."))){

				if(paramClassAsString.equals("java.lang.String")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = new String(xml);
					partytypes[i] = String.class;
				}

				if(paramClassAsString.equals("java.math.BigInteger")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = new java.math.BigInteger(xml);
					partytypes[i] = java.math.BigInteger.class;
				}

				if(paramClassAsString.equals("java.math.BigDecimal")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = new java.math.BigDecimal(xml);
					partytypes[i] = java.math.BigDecimal.class;
				}

				if(paramClassAsString.equals("javax.xml.datatype.XMLGregorianCalendar")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					javax.xml.datatype.XMLGregorianCalendar gcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(xml);
					nativeObject = gcal;
					partytypes[i] = javax.xml.datatype.XMLGregorianCalendar.class;
				}

				if(paramClassAsString.equals("javax.xml.namespace.QName")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					nativeObject = new javax.xml.namespace.QName(xml);
					partytypes[i] = javax.xml.namespace.QName.class;
				}

				if(paramClassAsString.equals("javax.xml.datatype.Duration")){
					String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
					javax.xml.datatype.Duration dur = DatatypeFactory.newInstance().newDuration(xml);
					nativeObject = dur;
					partytypes[i] = javax.xml.datatype.Duration.class;
				}
				
				
			}
			
			
			if(nativeObject == null){
				String parts[] = parameterClass.split("\\.");
				String parameterPlainClassName = parts[parts.length - 1];
				parameterPlainClassName = parameterPlainClassName.substring(0, 1).toLowerCase() + parameterPlainClassName.substring(1);
				String xml = smc.getParameterXml(parameterClassForXmlRetrieve);
				xml = this.cleanNamespaces(xml);
				xml = "<" + parameterPlainClassName + ">" + xml + "</" + parameterPlainClassName + ">";
				oParams[i] = this.unmarshall(parameterClass, xml);
			}else{
				oParams[i] = nativeObject;
			}
			i++;
		}

		callMethod = this.wsClass.getMethod(webMethod.getMethodName(), partytypes);
		retObject = callMethod.invoke(wsObject, oParams);
		String retObjectClass = retObject.getClass().getName();
//		System.out.println("retObject: " + retObject);

		if(retObjectClass.indexOf(".") > -1 && !retObjectClass.startsWith("java.") && !retObjectClass.startsWith("javax.")){
			retObject = this.marshal(retObject);
			
			strRet += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n";
			strRet += " <soap:Body>\n";
			strRet += "      <tns:" + smc.getMethodName() + "Response xmlns:tns=\"" + this.nameSpace + "\" targetNamespace=\"" + this.nameSpace + "\" >\n";
			strRet += "               <return>" + retObject + "</return>\n";
			strRet += "      </tns:" + smc.getMethodName() + "Response>\n";
			strRet += " </soap:Body>\n";
			strRet += "</soap:Envelope>\n";		
			
//			System.out.println("objectRet: " + strRet);
			return strRet;
		}		
		
		
		strRet += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n";
		strRet += " <soap:Body>\n";
		strRet += "      <tns:" + smc.getMethodName() + "Response xmlns:tns=\"" + this.nameSpace + "\" targetNamespace=\"" + this.nameSpace + "\">\n";
		strRet += "               <return>" + retObject + "</return>\n";
		strRet += "      </tns:" + smc.getMethodName() + "Response>\n";
		strRet += " </soap:Body>\n";
		strRet += "</soap:Envelope>\n";		
		
//		System.out.println(strRet);
		
		return strRet;
	}

	/**
	 * This method unmarshall xml element retrieved from SOAP call to java object.
	 * Usually this is a method parameter used in WebService method calls.
	 * 
	 * @param className
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private Object unmarshall(String className, String xml) throws Exception {
		Object o = null;
		JAXBContext jc = JAXBContext.newInstance(Class.forName(className));
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		ByteArrayInputStream bIn = new ByteArrayInputStream(xml.getBytes());
		unmarshaller.setSchema(null);
//		o = ((JAXBElement)unmarshaller.unmarshal(bIn)).getValue();
		o = unmarshaller.unmarshal(bIn);
		return o;
	}

	/**
	 * This method will marshal methods return object for SOAP reply
	 * 
	 * @param xmlObject  methods return object
	 * @return           return marshaled and little modified xml for SOAP reply
	 * @throws Exception
	 */
	private String marshal(Object xmlObject) throws Exception {
		String strRet = null;
		String strClassName = xmlObject.getClass().getName();
		String parts[] = strClassName.split("\\.");
		strClassName = parts[parts.length - 1];
		strClassName = strClassName.substring(0, 1).toLowerCase() + strClassName.substring(1);
		JAXBContext jc = JAXBContext.newInstance(xmlObject.getClass());
		Marshaller marshaller = jc.createMarshaller();
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		marshaller.setSchema(null);
		// o = ((JAXBElement)unmarshaller.unmarshal(bIn)).getValue();
		marshaller.marshal(xmlObject, bOut);
		strRet = new String(bOut.toByteArray());
		strRet = strRet.replaceAll("<\\?.*.\\?>", "");
		strRet = strRet.replaceAll("<" + strClassName + ">", "");
		strRet = strRet.replaceAll("</" + strClassName + ">", "");
		return strRet;
	}
	
	
	/**
	 * Helper method for parsing method call information from SOAP message.
	 * This information will be stored into SoapMethodCall object.
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private SoapMethodCall parseSoapBody(String xml) throws Exception {
		SoapMethodCall smc = null;
		String lines[] = xml.split("\n");
		boolean inBody = false;
		int i = 0;
		int iArgCounter = 0;
		while(i < lines.length){
			String line = lines[i].trim();
			
			if(line.toLowerCase().indexOf("body ") > -1){
				i++;
				line = lines[i].trim();
				smc = new SoapMethodCall(this.getSoapMethodName(line));
				inBody = true;
				continue;
			}
			
			if(line.toLowerCase().indexOf("body>") > -1){
				inBody = false;
			}

			if(inBody){
				if(line.toLowerCase().indexOf("arg" + iArgCounter + "_") > -1){
					String argName = this.getSoapArgumentName(line);
					String argXml = "";
					
					// empty argument including ending slash in tag
					if(argName.endsWith("/")){
						argName = argName.substring(0, argName.length() - 1).trim();
						smc.addParameterXml(argName, argXml);
						i++;
						continue;
					}
					
					// starting and ending tags both in same line
					if(line.substring(line.indexOf(argName) + argName.length()).indexOf(argName) > -1){
						argXml = this.getOneLineValue(line);
						smc.addParameterXml(argName, argXml);
						i++;
						continue;
					}
					
					i++;
					while(lines[i].indexOf(argName) == -1){
						argXml += lines[i].trim();
						i++;
					}
					
					smc.addParameterXml(argName, argXml);
					iArgCounter++;
				}
			}
			
			i++;
		}
		return smc;
	}

	/**
	 * Helper method for parsing information from "one line contains both
	 * starting and ending tags".
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private String getOneLineValue(String line) throws Exception {
		String strRet = "";
		if(line.indexOf("><") > -1){
			return strRet;
		}else{
			strRet = line.substring(line.indexOf(">"));
			strRet = line.substring(0, line.indexOf("<"));
		}
		return strRet;
	}

	/**
	 * Helper method for parsing argument name from received SOAP message.
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private String getSoapArgumentName(String line) throws Exception {
		String strRet = line;
		String parts[] = strRet.split(" ");
		strRet = parts[0];
		if(strRet.indexOf(":") > -1){
			strRet = strRet.substring(strRet.indexOf(":") + 1).trim();
		}else{
			strRet = strRet.substring(1).trim();
		}
		
		if(strRet.endsWith(">")){
			strRet = strRet.substring(0, strRet.length() - 1).trim();
		}
		
		
		return strRet;
	}

	/**
	 * Helper method for retrieving method name to be called from 
	 * SOAP message.
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private String getSoapMethodName(String line) throws Exception {
		String strRet = line;
		String parts[] = strRet.split(" ");
		strRet = parts[0];
		if(strRet.indexOf(":") > -1){
			strRet = strRet.substring(strRet.indexOf(":") + 1).trim();
		}else{
			strRet = strRet.substring(1).trim();
		}
		return strRet;
	}

	/**
	 * This will marshall SOAP body element to xma.
	 * 
	 * @param body
	 * @return
	 * @throws Exception
	 */
	private String bodyToXML(Body body) throws Exception {
		String strRet = null;
		Marshaller marshaller = jcBody.createMarshaller();
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(body, bOut);
		strRet = new String(bOut.toByteArray());
		return strRet;
	}

	/**
	 * This the first method called in execute. Received SOAP method
	 * is first unmarshalled as Envelope object.
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private Envelope xmlToEnvelope(String xml) throws Exception {
		Envelope env = null;
		Unmarshaller unmarshaller = jcEnvelope.createUnmarshaller();
		ByteArrayInputStream bIn = new ByteArrayInputStream(xml.getBytes());
		//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		unmarshaller.setSchema(null);
		env = (Envelope)((JAXBElement)unmarshaller.unmarshal(bIn)).getValue();
		return env;
	}
	
	
}
