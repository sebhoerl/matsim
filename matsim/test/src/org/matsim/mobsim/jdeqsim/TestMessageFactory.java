package org.matsim.mobsim.jdeqsim;

import org.matsim.basic.v01.IdImpl;
import org.matsim.core.api.population.Person;
import org.matsim.mobsim.jdeqsim.DeadlockPreventionMessage;
import org.matsim.mobsim.jdeqsim.EndLegMessage;
import org.matsim.mobsim.jdeqsim.EndRoadMessage;
import org.matsim.mobsim.jdeqsim.EnterRoadMessage;
import org.matsim.mobsim.jdeqsim.LeaveRoadMessage;
import org.matsim.mobsim.jdeqsim.MessageFactory;
import org.matsim.mobsim.jdeqsim.Scheduler;
import org.matsim.mobsim.jdeqsim.SimulationParameters;
import org.matsim.mobsim.jdeqsim.StartingLegMessage;
import org.matsim.mobsim.jdeqsim.Vehicle;
import org.matsim.population.PersonImpl;
import org.matsim.testcases.MatsimTestCase;

public class TestMessageFactory extends MatsimTestCase{
	
	// check if gc turned on
	public void testMessageFactory1(){
		MessageFactory.GC_ALL_MESSAGES();
		SimulationParameters.setGC_MESSAGES(true);
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null,null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null,null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null,null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null,null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null,null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null,null));
		
		assertEquals(0, MessageFactory.getEndLegMessageQueue().size());
		assertEquals(0, MessageFactory.getEnterRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getStartingLegMessageQueue().size());
		assertEquals(0, MessageFactory.getLeaveRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getEndRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getEndLegMessageQueue().size());
	}
	
	// check when gc turned off
	public void testMessageFactory2(){
		MessageFactory.GC_ALL_MESSAGES();
		SimulationParameters.setGC_MESSAGES(false);
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null,null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null,null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null,null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null,null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null,null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null,null));
		
		assertEquals(1, MessageFactory.getEndLegMessageQueue().size());
		assertEquals(1, MessageFactory.getEnterRoadMessageQueue().size());
		assertEquals(1, MessageFactory.getStartingLegMessageQueue().size());
		assertEquals(1, MessageFactory.getLeaveRoadMessageQueue().size());
		assertEquals(1, MessageFactory.getEndRoadMessageQueue().size());
		assertEquals(1, MessageFactory.getEndLegMessageQueue().size());
	}
	
	// check check use of Message factory
	public void testMessageFactory3(){
		MessageFactory.GC_ALL_MESSAGES();
		SimulationParameters.setGC_MESSAGES(false);
		MessageFactory.disposeEndLegMessage(new EndLegMessage(null,null));
		MessageFactory.disposeEnterRoadMessage(new EnterRoadMessage(null,null));
		MessageFactory.disposeStartingLegMessage(new StartingLegMessage(null,null));
		MessageFactory.disposeLeaveRoadMessage(new LeaveRoadMessage(null,null));
		MessageFactory.disposeEndRoadMessage(new EndRoadMessage(null,null));
		MessageFactory.disposeDeadlockPreventionMessage(new DeadlockPreventionMessage(null,null));
		
		MessageFactory.getEndLegMessage(null, null);
		MessageFactory.getEnterRoadMessage(null, null);
		MessageFactory.getStartingLegMessage(null, null);
		MessageFactory.getLeaveRoadMessage(null, null);
		MessageFactory.getEndRoadMessage(null, null);
		MessageFactory.getDeadlockPreventionMessage(null, null);
		
		assertEquals(0, MessageFactory.getEndLegMessageQueue().size());
		assertEquals(0, MessageFactory.getEnterRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getStartingLegMessageQueue().size());
		assertEquals(0, MessageFactory.getLeaveRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getEndRoadMessageQueue().size());
		assertEquals(0, MessageFactory.getEndLegMessageQueue().size());
	}
	
	// check initialization using constructer
	public void testMessageFactory5(){
		MessageFactory.GC_ALL_MESSAGES();
		SimulationParameters.setGC_MESSAGES(true);
		Scheduler scheduler=new Scheduler();
		Person person= new PersonImpl(new IdImpl("abc"));
		Vehicle vehicle=new Vehicle(scheduler, person );
		
		assertEquals(true,MessageFactory.getEndLegMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getEnterRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getStartingLegMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getLeaveRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getEndRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).scheduler==scheduler);
		
		assertEquals(true,MessageFactory.getEndLegMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getEnterRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getStartingLegMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getLeaveRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getEndRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).vehicle==vehicle);
	}
	
	// check initialization using rest
	public void testMessageFactory6(){
		MessageFactory.GC_ALL_MESSAGES();
		SimulationParameters.setGC_MESSAGES(false);
		Scheduler scheduler=new Scheduler();
		Person person= new PersonImpl(new IdImpl("abc"));
		Vehicle vehicle=new Vehicle(scheduler, person );
		
		assertEquals(true,MessageFactory.getEndLegMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getEnterRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getStartingLegMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getLeaveRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getEndRoadMessage(scheduler, vehicle).scheduler==scheduler);
		assertEquals(true,MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).scheduler==scheduler);
		
		assertEquals(true,MessageFactory.getEndLegMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getEnterRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getStartingLegMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getLeaveRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getEndRoadMessage(scheduler, vehicle).vehicle==vehicle);
		assertEquals(true,MessageFactory.getDeadlockPreventionMessage(scheduler, vehicle).vehicle==vehicle);
	}
	
	
}
