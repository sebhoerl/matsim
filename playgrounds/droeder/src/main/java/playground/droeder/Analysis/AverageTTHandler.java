/* *********************************************************************** *
 * project: org.matsim.*
 * Plansgenerator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.droeder.Analysis;

import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.AgentWait2LinkEvent;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentWait2LinkEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;

/**
 * @author droeder
 *
 */
public class AverageTTHandler implements LinkEnterEventHandler, LinkLeaveEventHandler, 
		AgentArrivalEventHandler, AgentDepartureEventHandler, AgentWait2LinkEventHandler{
	
	private double travelTime = 0.0;
	private int popSize;

	public AverageTTHandler(int popSize) {
		this.popSize = popSize;
	}
	
	@Override
	public void reset(int iteration) {
		this.travelTime = 0.0;
	}
	
	@Override
	public void handleEvent(LinkEnterEvent event) {
		this.travelTime -= event.getTime();
	}
	
	@Override
	public void handleEvent(LinkLeaveEvent event) {
		this.travelTime += event.getTime();
	}
	
	@Override
	public void handleEvent(AgentArrivalEvent event) {
		this.travelTime += event.getTime();
	}
	
	@Override
	public void handleEvent(AgentDepartureEvent event) {
		this.travelTime -= event.getTime();		
	}

	@Override
	public void handleEvent(AgentWait2LinkEvent event) {
//		this.travelTime -= event.getTime();
	}
	
	public double getAverageTravelTime() {
		return this.travelTime / this.popSize;
	}
	
	public double getAbsoluteTT() {
		return this.travelTime;
	}



	

	
	
}

