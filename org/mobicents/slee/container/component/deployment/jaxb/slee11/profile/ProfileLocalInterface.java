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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.03.17 at 04:43:58 PM WET 
//


package org.mobicents.slee.container.component.deployment.jaxb.slee11.profile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "profileLocalInterfaceName"
})
@XmlRootElement(name = "profile-local-interface")
public class ProfileLocalInterface {

    @XmlAttribute(name = "isolate-security-permissions")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isolateSecurityPermissions;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    protected Description description;
    @XmlElement(name = "profile-local-interface-name", required = true)
    protected ProfileLocalInterfaceName profileLocalInterfaceName;

    /**
     * Gets the value of the isolateSecurityPermissions property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsolateSecurityPermissions() {
        if (isolateSecurityPermissions == null) {
            return "False";
        } else {
            return isolateSecurityPermissions;
        }
    }

    /**
     * Sets the value of the isolateSecurityPermissions property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsolateSecurityPermissions(String value) {
        this.isolateSecurityPermissions = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the profileLocalInterfaceName property.
     * 
     * @return
     *     possible object is
     *     {@link ProfileLocalInterfaceName }
     *     
     */
    public ProfileLocalInterfaceName getProfileLocalInterfaceName() {
        return profileLocalInterfaceName;
    }

    /**
     * Sets the value of the profileLocalInterfaceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileLocalInterfaceName }
     *     
     */
    public void setProfileLocalInterfaceName(ProfileLocalInterfaceName value) {
        this.profileLocalInterfaceName = value;
    }

}
