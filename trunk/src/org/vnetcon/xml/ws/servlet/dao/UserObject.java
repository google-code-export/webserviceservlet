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

import java.lang.annotation.Annotation;

import org.vnetcon.xml.ws.servlet.schema.XmlBase;

public class UserObject extends XmlBase {

	Class<?> userClass;
	
	public UserObject(String userClass) throws Exception {
		this.userClass = Class.forName(userClass);
	}
	
	public String getAnnotationName() throws Exception {
		Annotation annotation[] = userClass.getAnnotations();
		int i = 0;
		while(i < annotation.length){
			Annotation an = annotation[i];
//			System.out.println(an.annotationType().getName());
			if(an.annotationType().getName().equals("javax.xml.bind.annotation.XmlType")){
//				System.out.println(" annotationName: " + ((javax.xml.bind.annotation.XmlType)an).name());
				return XmlBase.TAG_WS_XMLNS + ((javax.xml.bind.annotation.XmlType)an).name();
			}
			i++;
		}

		throw new Exception("XmlType annotation name not set for class " + userClass.getName());
	}
	
}
