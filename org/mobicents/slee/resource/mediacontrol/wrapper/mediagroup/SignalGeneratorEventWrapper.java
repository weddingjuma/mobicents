/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.resource.mediacontrol.wrapper.mediagroup;

import javax.media.mscontrol.mediagroup.signals.SignalGenerator;
import javax.media.mscontrol.mediagroup.signals.SignalGeneratorEvent;
import javax.media.mscontrol.resource.ResourceEvent;

import org.mobicents.slee.resource.mediacontrol.wrapper.ResourceEventWrapper;

/**
 * @author baranowb
 *
 */
public class SignalGeneratorEventWrapper extends ResourceEventWrapper implements SignalGeneratorEvent {

	public static final String EMIT_SIGNALS_COMPLETED = "javax.media.mscontrol.mediagroup.signals.SignalGeneratorEvent.EMIT_SIGNALS_COMPLETED";
	
	protected final SignalGeneratorEvent wrappedEvent;
	protected final SignalGeneratorWrapper source;
	/**
	 * @param resourceEvent
	 */
	public SignalGeneratorEventWrapper(ResourceEvent resourceEvent,SignalGeneratorWrapper source) {
		super(resourceEvent);
		this.wrappedEvent = (SignalGeneratorEvent) resourceEvent;
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see javax.media.mscontrol.MediaEvent#getSource()
	 */
	
	public SignalGenerator getSource() {
		return this.source;
	}

}
