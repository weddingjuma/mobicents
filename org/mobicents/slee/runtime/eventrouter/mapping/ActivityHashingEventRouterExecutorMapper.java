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
package org.mobicents.slee.runtime.eventrouter.mapping;

import java.util.concurrent.atomic.AtomicInteger;

import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.container.activity.ActivityContextHandle;
import org.mobicents.slee.container.eventrouter.EventRouterExecutor;
import org.mobicents.slee.runtime.eventrouter.EventRouterImpl;

/**
 * Simple {@link EventRouterExecutor} to {@link ActivityContextHandle} mapping
 * using the hashcode of the latter.
 * 
 * @author martins
 * 
 */
public class ActivityHashingEventRouterExecutorMapper extends
		AbstractEventRouterExecutorMapper {

	EventRouterImpl eventrouterimpl = null;

	
	public void setExecutors(EventRouterImpl eventrouterimpl,EventRouterExecutor[] executors) {
		super.setExecutors(eventrouterimpl,executors);
		this.eventrouterimpl = eventrouterimpl;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.mobicents.slee.runtime.eventrouter.mapping.
	 * AbstractEventRouterExecutorMapper
	 * #getExecutor(org.mobicents.slee.runtime.activity.ActivityContextHandle)
	 */


	public EventRouterExecutor getExecutor(
			ActivityContextHandle activityContextHandle) {
		synchronized (eventrouterimpl) {
			System.out.println("++"+eventrouterimpl.hashCode());
			int len = executors.length;
			System.out.println("num_of_executors :"+len);
			for(int i=0;i<len;i++){
				if((Integer.parseInt(executors[i].toString())) >= 4){
					eventrouterimpl.resize();
					break;
				}
			}
			System.out.println("finish");
		}
		return executors[(activityContextHandle.hashCode() & Integer.MAX_VALUE)
				% executors.length];
	}

}
