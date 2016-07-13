/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * 
 */
package org.mobicents.slee.container.component.ratype;

import javax.slee.resource.ResourceAdaptorTypeID;

import org.mobicents.slee.container.component.SleeComponent;

/**
 * 
 * @author martins
 * 
 */
public interface ResourceAdaptorTypeComponent extends SleeComponent {

	/**
	 * Retrieves the component's descriptor.
	 * @return
	 */
	public ResourceAdaptorTypeDescriptor getDescriptor();
	
	/**
	 * Retrieves the instance of the aci factory for this ra type
	 * 
	 * @return
	 */
	public Object getActivityContextInterfaceFactory();

	/**
	 * Retrieves the aci factory concrete class, generated by SLEE
	 * 
	 * @return
	 */
	public Class<?> getActivityContextInterfaceFactoryConcreteClass();

	/**
	 * Retrieves the aci factory interface
	 * 
	 * @return
	 */
	public Class<?> getActivityContextInterfaceFactoryInterface();

	/**
	 * 
	 * @return
	 */
	public Class<?> getResourceAdaptorSBBInterface();

	/**
	 * Retrieves the ratype id
	 * 
	 * @return
	 */
	public ResourceAdaptorTypeID getResourceAdaptorTypeID();

	/**
	 * Retrieves the JAIN SLEE specs descriptor
	 * 
	 * @return
	 */
	public javax.slee.resource.ResourceAdaptorTypeDescriptor getSpecsDescriptor();

	/**
	 * Sets the instance of the aci factory for this ra type
	 * 
	 * @param o
	 */
	public void setActivityContextInterfaceFactory(Object o);

	/**
	 * Sets the aci factory concrete class, generated by SLEE
	 * 
	 * @param c
	 */
	public void setActivityContextInterfaceFactoryConcreteClass(Class<?> c);

	/**
	 * Sets the aci factory interface
	 * 
	 * @param c
	 */
	public void setActivityContextInterfaceFactoryInterface(Class<?> c);

	/**
	 * 
	 * @param c
	 */
	public void setResourceAdaptorSBBInterface(Class<?> c);

}
