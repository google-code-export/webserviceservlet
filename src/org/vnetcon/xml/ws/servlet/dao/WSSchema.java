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


package org.vnetcon.xml.ws.servlet.dao;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vnetcon.xml.ws.servlet.schema.XmlBase;

/**
 * This class is responsible of handling JAXB generated
 * schema file which will be read from web.xml configuratio location.
 * 
 * This class will convert this file to xsd file used in WebService
 * protocol. The actual xsd or wsdl returned to user will be build
 * in SchemaGenerator which uses this class as helper class.
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public class WSSchema {

	String strClassName = null;
	HashMap<String, String> schemaNameHash;
	HashMap<String, String> schemaTypeHash;
	ArrayList<String> schemaNameList;
	ArrayList<String> schemaTypeList;
	ArrayList<String> wsdlMethods;
	ArrayList<String> portOperations;
	ArrayList<String> soapOperations;
	ArrayList<WebMethod> webMethods = null;
	String callerHost = "http://fixme.com";
	
	public WSSchema(String className, String requestHostUrl){
		this.schemaNameHash = new HashMap<String, String>();
		this.schemaTypeHash = new HashMap<String, String>();
		this.schemaNameList = new ArrayList<String>();
		this.schemaTypeList = new ArrayList<String>();
		this.wsdlMethods = new ArrayList<String>();
		this.portOperations = new ArrayList<String>();
		this.soapOperations = new ArrayList<String>();
		this.strClassName = className.toLowerCase();
		this.callerHost = requestHostUrl;
	}
	
	public List<String> getSchemaNameList(){
		return this.schemaNameList;
	}

	/**
	 * In WebSerivce xsd file also the methods are declared. 
	 * These declarations are added to WebService schema by calling
	 * this method.
	 * 
	 * @param method
	 * @throws Exception
	 */
	public void addMethodInfo(WebMethod method) throws Exception {
		int i = 0;
		String methodName = method.getMethodName();
		String typeElement = "";
		List<MethodParameter> args = method.getParameters();
		
		typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType name=\"" + methodName + "\">\n";
		
		if (args.size() > 0)
			typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "sequence>\n";
		
		while(i < args.size()){
			MethodParameter arg = args.get(i);
			String elementTag = "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "element name=\"" + arg.getParameterName() + "\" type=\"" + arg.getSchemaType() + "\" minOccurs=\"0\" />\n";
			typeElement += elementTag;
			i++;
		}
		
		if (args.size() > 0)
			typeElement += "</" + XmlBase.TAG_W3SCHEMA_XMLNS + "sequence>\n";
		
		typeElement += "</" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType>\n";
		
		this.schemaTypeHash.put(methodName, typeElement);
		this.schemaTypeList.add(typeElement);
		
		typeElement = "  <" + XmlBase.TAG_W3SCHEMA_XMLNS + "element name=\"" + methodName + "\" type=\"" + methodName + "\"/>";
		this.schemaNameHash.put(methodName, typeElement);
		this.schemaNameList.add(typeElement);
		

		typeElement = "";
		typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType name=\"" + methodName + "Response\">\n";
		typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "sequence>\n";
//		typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "element name=\"return\" type=\"" + method.getReturnType().getSchemaElement() + "\" minOccurs=\"0\" />\n";
		typeElement += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "element name=\"return\" type=\"" + method.getReturnType().getSchemaElement() + "\" minOccurs=\"0\" />\n";
		typeElement += "</" + XmlBase.TAG_W3SCHEMA_XMLNS + "sequence>\n";
		typeElement += "</" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType>\n";
		
		this.schemaTypeHash.put(methodName + "Response", typeElement);
		this.schemaTypeList.add(typeElement);

		typeElement = "  <" + XmlBase.TAG_W3SCHEMA_XMLNS + "element name=\"" + methodName + "Response\" type=\"" + methodName + "Response\"/>";
		this.schemaNameHash.put(methodName + "Response", typeElement);
		this.schemaNameList.add(typeElement);

		typeElement = "";
		typeElement += "<message name=\"" + methodName + "\">\n";
		typeElement += "<part name=\"parameters\" element=\"" + XmlBase.TAG_WS_XMLNS + methodName + "\" />\n";
		typeElement += "</message>\n";
		typeElement += "<message name=\"" + methodName + "Response\">\n";
		typeElement += "<part name=\"parameters\" element=\"" + XmlBase.TAG_WS_XMLNS + methodName + "Response\" />\n";
		typeElement += "</message>\n";

		this.wsdlMethods.add(typeElement);
		
		typeElement = "";
		
		typeElement += "<operation name=\"" + methodName + "\">\n";
		typeElement += "<input wsam:Action=\"urn:" + methodName + "\"  message=\"" + XmlBase.TAG_WS_XMLNS + "" + methodName + "\" />\n";
		typeElement += "<output wsam:Action=\"" + "" + this.callerHost + "/" + methodName + "Response" + "\" message=\"" + XmlBase.TAG_WS_XMLNS + "" + methodName + "Response\" />\n";
		typeElement += "</operation>\n";
		
		this.portOperations.add(typeElement);

		typeElement = "";
		typeElement += "<operation name=\"" + methodName + "\">\n";
		typeElement += "<" + XmlBase.TAG_WSDL_SOAP_XMLNS + "operation soapAction=\"urn:" + methodName + "\"></" + XmlBase.TAG_WSDL_SOAP_XMLNS + "operation>\n";
		typeElement += "<input>\n";
		typeElement += "<" + XmlBase.TAG_WSDL_SOAP_XMLNS + "body use=\"literal\"></" + XmlBase.TAG_WSDL_SOAP_XMLNS + "body>\n";
		typeElement += "</input>\n";
		typeElement += "<output>\n";
		typeElement += "<" + XmlBase.TAG_WSDL_SOAP_XMLNS + "body use=\"literal\"></" + XmlBase.TAG_WSDL_SOAP_XMLNS + "body>\n";
		typeElement += "</output>\n";
		typeElement += "</operation>\n";

		this.soapOperations.add(typeElement);
		
	}

	/**
	 * This will return generated soap operation tags.
	 * 
	 * @return
	 */
	public List<String> getSoapOperations(){
		return this.soapOperations;
	}
	
	/**
	 * This will return generated port operation tags.
	 * 
	 * @return
	 */
	public List<String> getPortOperations(){
		return this.portOperations;
	}
	
	/**
	 * This method will return wsdl method tags
	 * 
	 * @return
	 */
	public List<String> getWsdlMethods(){
		return this.wsdlMethods;
	}
	
	/**
	 * This method will return schema type tags.
	 * 
	 * @return
	 */
	public List<String> getSchemaTypeList(){
		return this.schemaTypeList;
	}
	
	/**
	 * This will load the users WebService's JAXB generated
	 * schema file.
	 * 
	 * @param bfIn
	 * @throws Exception
	 */
	public void loadSchema(BufferedReader bfIn) throws Exception {
		String s = null;
		boolean inComplexTypes = false;
		int iComplexTypeCount = 0;
		
		while((s = bfIn.readLine()) != null){
//			s = s.trim();
			
			if(s.indexOf("<" + XmlBase.TAG_W3SCHEMA_XMLNS + "element ") > -1 && !inComplexTypes){
				String name = this.getElementName(s);
//				System.out.println("nameput: " + name + " = " + s);
				if(!name.toLowerCase().equals(this.strClassName)){
					this.schemaNameHash.put(name, s);
					this.schemaNameList.add(s);
				}
			}
			
			if(s.indexOf("<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType ") > -1){
				inComplexTypes = true;
			}
			
			if(inComplexTypes){
				String complexType = "";
				iComplexTypeCount++;
				
				if(s.indexOf("<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType ") > -1){
					complexType += s + "\n";
										
					
					while((s = bfIn.readLine()) != null){
						s = s.replaceAll("ref=\"", "ref=\"" + XmlBase.TAG_ORG_SCHEMA_XMLNS) + "\n";
						
						if(s.indexOf("<xs:element ") > -1 && s.indexOf(" type=\"") > -1 && s.indexOf(" type=\"xs:") == -1){
							s = s.replaceAll("type=\"", "type=\"" + XmlBase.TAG_ORG_SCHEMA_XMLNS) + "\n";
						}
					
						if(s.indexOf("<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType ") > -1 || s.indexOf("<" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType>") > -1){
							iComplexTypeCount++;
						}

						if(s.indexOf("</" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType>") > -1){
							iComplexTypeCount--;
						}
						
						complexType += s + "\n";
						if(s.indexOf("</" + XmlBase.TAG_W3SCHEMA_XMLNS + "complexType>") > -1 && iComplexTypeCount == 0){
							String name = this.getComplexTypeName(complexType);
							
							if(!name.toLowerCase().equals(this.strClassName)){
								this.schemaTypeHash.put(name, complexType);
								this.schemaTypeList.add(complexType);
								complexType = "";
							}
							//break;
						}
						
						
					}
				}
			}
			
		}
	}

	/**
	 * Helper method for retrieving name value from complex type
	 * element from schema.
	 * 
	 * @param complexType
	 * @return
	 * @throws Exception
	 */
	private String getComplexTypeName(String complexType) throws Exception {
		String s = complexType;
		s = s.substring(s.indexOf("name=\"") + 6);
		s = s.substring(0, s.indexOf("\""));
		return s;
	}
	
	/**
	 * Helper method for get element name from schema.
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private String getElementName(String line) throws Exception {
		String s = line;
		s = s.substring(s.indexOf("name=\"") + 6);
		s = s.substring(0, s.indexOf("\""));
		return s;
	}
	
/*	
	public String getSchemaElementByName(String name) throws Exception {
		return this.schemaNameHash.get(name);
	}

	public String getSchemaComplexType(String type) throws Exception {
		return this.schemaTypeHash.get(type);
	}
*/	
}
