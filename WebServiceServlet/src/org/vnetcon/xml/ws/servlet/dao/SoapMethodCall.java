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

import java.util.HashMap;

/**
 * This class is used as "information container" after
 * the actual SOAP message is parsed.
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public class SoapMethodCall {

	String methodName = null;
	String methodClass = null;
	HashMap<String, String> parameterXml = null;
	
	public SoapMethodCall(String methodName){
		this.methodName = methodName;
		this.parameterXml = new HashMap<String, String>();
	}
	
	public String getMethodName(){
		return this.methodName;
	}
	
	public HashMap<String, String> getParameterXml(){
		return this.parameterXml;
	}
	
	public void addParameterXml(String arg, String xml){
		this.parameterXml.put(arg, xml);
	}
	
	public String getParameterXml(String arg){
		return this.parameterXml.get(arg);
	}
	
}
