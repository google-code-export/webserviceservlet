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


package org.vnetcon.xml.ws.servlet.schema;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vnetcon.xml.ws.servlet.dao.WSSchema;
import org.vnetcon.xml.ws.servlet.dao.WebMethod;

/**
 * Instances of this class are created in WebServiceServlet which
 * will get both wsdl and xsd "files" from this class.
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public class SchemaGenerator {

	String requestHostUrl = null;
//	String targetNamespace = "http://localhost:8888/webserviceservlet";
	String targetNamespace = "http://ws.vnetcon.org";
	
	Class<?> wsClass = null;
	Method publicMethods[] = null;
	ArrayList<WebMethod> webMethods = null;
	WSSchema schema = null;
	String schemaFile = null;
	String strClassName = null;
	
    HashMap<String, String> classSchema = new HashMap<String, String>();
    HashMap<String, String> classWsdl = new HashMap<String, String>();

    /**
     * 
     * @param requestHostUrl     this is the address where WebService locates
     * @param schemaFilePath     this the location of user generated JAXB schema file in jar
     * @param wsClass            actual user created WebService class
     * @param webMethods         list of WebServce's public methods encapsulated in WebMethod objects
     * @throws Exception
     */
	public SchemaGenerator(String requestHostUrl, String schemaFilePath, Class<?> wsClass, ArrayList<WebMethod> webMethods) throws Exception {
		this.requestHostUrl = requestHostUrl;
		this.schemaFile = schemaFilePath;
		this.wsClass = wsClass;
		this.webMethods = webMethods;
		webMethods = new ArrayList<WebMethod>();
		this.init();
	}

	/**
	 * This is the start point for loading schema information.
	 * 
	 * @throws Exception
	 */
	private void loadSchema() throws Exception {
		InputStream fIn = this.getClass().getResourceAsStream(this.schemaFile);
		BufferedReader bfIn = new BufferedReader(new InputStreamReader(fIn));
		int i = 0;
		this.schema = new WSSchema(this.strClassName, this.requestHostUrl);
		this.schema.loadSchema(bfIn);
		bfIn.close();
		fIn.close();
		
		while(i < this.webMethods.size()){
			this.schema.addMethodInfo(this.webMethods.get(i));
			i++;
		}
	}

	/**
	 * This is the starting point of all processing in SchemaGenerator.
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		this.strClassName = this.wsClass.getName();
		String parts[] = this.strClassName.split("\\.");
		int i = parts.length - 2;
		String delim = "";
		this.targetNamespace = "";
		while(i >= 0){
			this.targetNamespace += delim + parts[i];
			delim = ".";
			i--;
		}
		
		this.strClassName = parts[parts.length - 1];
		this.loadSchema();
		this.initMethods();
	}


	/**
	 * @deprecated
	 * @throws Exception
	 */
	private void initMethods() throws Exception {
		publicMethods = wsClass.getDeclaredMethods();
		int i = 0;
		

		while (i < publicMethods.length) {
			Method m = publicMethods[i];
			if (m.toString().startsWith("public ")) {
				WebMethod webMethod = new WebMethod(this.wsClass, m);
				webMethods.add(webMethod);
				
//				String retType = webMethod.getReturnType().getSchemaElement();
//				System.out.println("method: " + m.toString());
//				System.out.println("  return: " + retType);
/*
				int ii = 0;
				List<MethodParameter> args = webMethod.getParameters();
//				System.out.println("  args length: " + args.size());
				while(ii < args.size()){
					MethodParameter mp = args.get(ii);
//					System.out.println("  arg" + ii + ":   " + mp.getSchemaElement());
					ii++;
				}
*/
//				this.schema.addMethodInfo(webMethod);
				
			}
			i++;
		}
	}

	/**
	 * This will return the actual wsdl to be send to WebService client.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getWSDL() throws Exception {
		String s = "";
		List<String> wsdlMethods = this.schema.getWsdlMethods();
		List<String> portOperations = this.schema.getPortOperations();
		List<String> soapOperations = this.schema.getSoapOperations();
		int i = 0;
		
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

		s += "<!-- Powered by WSS (WebServiceServlet), www.vnetcon.org -->\n";
		
		s += "<definitions xmlns" + XmlBase.WSDL_SOAP_XMLNS + "=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns" + XmlBase.WS_XMLNS + "=\"" + this.targetNamespace + "\" xmlns" + XmlBase.W3SCHEMA_XMLNS + "=\"http://www.w3.org/2001/XMLSchema\" xmlns" + XmlBase.WSDL_XMLNS + "=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\"  targetNamespace=\"" + this.targetNamespace + "\" name=\"" + this.strClassName + "\">\n";
		s += "<types>\n";
		s += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "schema>\n";
		s += "<" + XmlBase.TAG_W3SCHEMA_XMLNS + "import namespace=\"" + this.targetNamespace + "\" schemaLocation=\"" + this.requestHostUrl + "?xsd=1\" />\n";
		s += "</" + XmlBase.TAG_W3SCHEMA_XMLNS + "schema>\n";
		s += "</types>\n";


		i = 0;
		while(i < wsdlMethods.size()){
			s += wsdlMethods.get(i);
			i++;
		}

		s += "<portType name=\"" + this.strClassName + "\">\n";
		i = 0;
		while(i < portOperations.size()){
			s += portOperations.get(i);
			i++;
		}
		s += "</portType>\n";
		
		
		s += "<binding name=\"" + this.strClassName + "PortBinding\" type=\"" + XmlBase.TAG_WS_XMLNS + "" + this.strClassName + "\">\n";
		s += "<" + XmlBase.TAG_WSDL_SOAP_XMLNS + "binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\">\n";
		s += "</" + XmlBase.TAG_WSDL_SOAP_XMLNS + "binding>\n";
		
		i = 0;
		while(i < soapOperations.size()){
			s += soapOperations.get(i);
			i++;
		}
		
		s += "</binding>\n";
		
		s += "<service name=\"" + this.strClassName + "Service\">\n";
		s += "<port name=\"" + this.strClassName + "Port\" binding=\"" + XmlBase.TAG_WS_XMLNS + "" + this.strClassName + "PortBinding\">\n";
		s += "<" + XmlBase.TAG_WSDL_SOAP_XMLNS + "address location=\"" + this.requestHostUrl + "\" />\n";   //</" + XmlBase.TAG_WSDL_SOAP_XMLNS + "address>\n";
		s += "</port>\n";
		s += "</service>\n";
		
		
		s += "</definitions>\n";		
		
		return s;
	}

	/**
	 * This will retrurn the wsdl related xsd to be send to WebService client.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getSchema() throws Exception {
		String s = "";
		List<String> schemaNameList = this.schema.getSchemaNameList();
		List<String> schemaTypeList = this.schema.getSchemaTypeList();
		int i = 0;
		
		s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

		s += "<!-- Powered by WSS (WebServiceServlet), www.vnetcon.org -->\n";
		
		s += "<xs:schema xmlns" + XmlBase.ORG_SCHEMA_XMLNS + "=\"" + this.targetNamespace + "\" version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"" + this.targetNamespace + "\" >\n\n";
//		s += "<xs:schema xmlns" + XmlBase.WS_XMLNS + "=\"" + this.targetNamespace + "\" version=\"1.0\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"" + this.targetNamespace + "\" >\n\n";

		i = 0;
		while(i < schemaNameList.size()){
//			s += schemaNameList.get(i) + "\n";
			s += schemaNameList.get(i).replaceAll("type=\"", "type=\"" + XmlBase.TAG_ORG_SCHEMA_XMLNS) + "\n";
			i++;
		}
		
		s += "\n";
		
		i = 0;
		while(i < schemaTypeList.size()){
//			s += schemaTypeList.get(i) + "\n";
			s += schemaTypeList.get(i).replaceAll("type=\"tns:", "type=\"" + XmlBase.TAG_ORG_SCHEMA_XMLNS) + "\n";
			i++;
		}
		
		
		s += "</xs:schema>\n";
		
		return s;
	}
		
}
