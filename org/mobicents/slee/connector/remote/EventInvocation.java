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

/*
 * ***************************************************
 *                                                 *
 *  Mobicents: The Open Source JSLEE Platform      *
 *                                                 *
 *  Distributable under LGPL license.              *
 *  See terms of license at gnu.org.               *
 *                                                 *
 ***************************************************
 *
 * Created on Dec 12, 2004 EventInvocation.java
 */
package org.mobicents.slee.connector.remote;

import java.io.Serializable;
import javax.slee.Address;
import javax.slee.EventTypeID;
import javax.slee.connection.ExternalActivityHandle;

/**
 * "struct" class for holding a SLEE event invocation. We pass an ArrayList of
 * these from the RA to the SLEE in one remote call to avoid a lot of network
 * traffic.
 * 
 * @author Tim
 */
public class EventInvocation implements Serializable {
   public EventInvocation(Object event, EventTypeID eventTypeId,
         ExternalActivityHandle externalActivityHandle, Address address) {
      this.event = event;
      this.eventTypeId = eventTypeId;
      this.externalActivityHandle = externalActivityHandle;
      this.address = address;
   }
   public Object event;
   public EventTypeID eventTypeId;
   public ExternalActivityHandle externalActivityHandle;
   public Address address;
}