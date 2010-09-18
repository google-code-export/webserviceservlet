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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.vnetcon.xml.ws.servlet.schema.XmlBase;

/**
 * This class contains information of WebServices public method.
 * From WebService each public methods information is encapsulated
 * into this class.
 * 
 * @author Michael Kankkonen 17.9.2010 
 *         http://www.vnetcon.org
 *
 */
public class WebMethod extends XmlBase {

	Class<?> wsClass = null;
	Method nativeMethod = null;
	ReturnType retType = null;
	String methodName = null;
	
	public WebMethod(Class<?> wsClass, Method method) throws Exception {
		this.wsClass = wsClass;
		this.nativeMethod = method;
		methodName = method.getName();
		this.init();
	}
	
	public String getMethodName(){
		return this.methodName;
	}
	
	private void init() throws Exception {
//		this.retType = this.getReturnType();
//		this.methodParamters = this.getParameters();
	}

	public ReturnType getReturnType() throws Exception {
		ReturnType s = null;
		Class<?> retType = this.nativeMethod.getReturnType();
		s = new ReturnType(retType);
		return s;
	}
	
	public List<MethodParameter> getParameters() throws Exception {
		ArrayList<MethodParameter> a = new ArrayList<MethodParameter>();
//		Class<?> paramClasses[]  = this.nativeMethod.getParameterTypes();
		Type paramClasses[]  = this.nativeMethod.getGenericParameterTypes();
		int i = 0;

//		System.out.println("PARAM CLASSES LENGTH: " + paramClasses.length);
		while(i < paramClasses.length){
			Type c = paramClasses[i];
			String className = c.toString();
			if(className.startsWith("class ")){
				className = className.substring(6).trim();
			}
			MethodParameter mp = new MethodParameter("arg" + i + "_" + className.replaceAll("\\.", "_"), c);
			a.add(mp);
			i++;
		}
		
		return a;
	}

	
}
