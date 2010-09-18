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

import org.vnetcon.xml.ws.servlet.dao.UserObject;

/**
 * This is some kind of "base class" containing static information
 * like xmlns tags used in schema and wsdl creation.
 * 
 * This class contains also mapping information between xml and java.
 * 
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public abstract class XmlBase {

	public static final String WSDL_SOAP_XMLNS = ":soap";
	public static final String WS_XMLNS = ":tns";
	public static final String W3SCHEMA_XMLNS = ":xs";
	public static final String WSDL_XMLNS  = "";
	public static final String ORG_SCHEMA_XMLNS  = ":tnso";
	
	public static final String TAG_WSDL_SOAP_XMLNS = "soap:";
	public static final String TAG_WS_XMLNS = "tns:";
	public static final String TAG_W3SCHEMA_XMLNS = "xs:";
	public static final String TAG_WSDL_XMLNS  = "";
	public static final String TAG_ORG_SCHEMA_XMLNS  = "tnso:";
	
	protected String getSchemaType(String primitiveType) throws Exception {
		

		if(primitiveType.equals("void")){
			return null;
		}

		if(primitiveType.equals(String.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "string";
		}

		if(primitiveType.equals(java.math.BigInteger.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "integer";
		}
				
		if(primitiveType.equals("int")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "int";
		}

		if(primitiveType.equals("long")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "long";
		}

		if(primitiveType.equals("short")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "short";
		}
		
		if(primitiveType.equals(java.math.BigDecimal.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "decimal";
		}
		
		if(primitiveType.equals("float")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "float";
		}
		
		if(primitiveType.equals("double")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "double";
		}
		
		if(primitiveType.equals("boolean")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "boolean";
		}
		
		if(primitiveType.equals("byte")){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "byte";
		}

		if(primitiveType.equals(javax.xml.namespace.QName.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "QName";
		}

		if(primitiveType.equals(javax.xml.datatype.XMLGregorianCalendar.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "dateTime";
		}
		
		if(primitiveType.equals(Byte[].class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "base64Binary";
		}

		if(primitiveType.equals(javax.xml.datatype.Duration.class.getName())){
			return "" + XmlBase.TAG_W3SCHEMA_XMLNS + "duration";
		}


//		System.out.println("ObjectType: " + primitiveType);
		UserObject uo = new UserObject(primitiveType);
		String annotationName = uo.getAnnotationName();
		return annotationName;
		
		
//		if(primitiveType.equals(Object.class.getName())){
//			return "xsd:anySimpleType";
//		}
		
		
//		return null;
	}

	protected String getJavaType(String schemaType){
		return null;
	}
	
}
