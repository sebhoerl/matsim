/* *********************************************************************** *
 * project: org.matsim.*
 * PlanAnalyzeTourModeChoiceSetTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.meisterk.org.matsim.population.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.population.BasicLeg;
import org.matsim.basic.v01.IdImpl;
import org.matsim.config.groups.PlanomatConfigGroup;
import org.matsim.core.api.facilities.Facility;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.facilities.FacilitiesImpl;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.gbl.Gbl;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.PersonImpl;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.world.Layer;
import org.matsim.world.Location;

import playground.meisterk.org.matsim.config.groups.MeisterkConfigGroup;

/**
 * Test class for {@link PlanAnalyzeTourModeChoiceSet}.
 * 
 * Contains illustrative examples for analysis of feasible mode chains. See documentation <a href=http://matsim.org/node/267">here</a>.
 * @author meisterk
 *
 */
public class PlanAnalyzeTourModeChoiceSetTest extends MatsimTestCase {

	private static final String CONFIGFILE = "test/scenarios/equil/config.xml";
	private static Logger log = Logger.getLogger(PlanAnalyzeTourModeChoiceSetTest.class);

	protected void setUp() throws Exception {
		super.setUp();
		super.loadConfig(PlanAnalyzeTourModeChoiceSetTest.CONFIGFILE);
	}

	public void testFacilitiesBased() {

		// load data
		log.info("Reading facilities xml file...");
		FacilitiesImpl facilities = new FacilitiesImpl();
		new MatsimFacilitiesReader(facilities).readFile(Gbl.getConfig().facilities().getInputFile());
		log.info("Reading facilities xml file...done.");

		// config
		MeisterkConfigGroup meisterk = new MeisterkConfigGroup();

		// run
		this.runDemo((Layer) facilities, meisterk);

	}

	public void testNetworkBased() {

		// load data
		log.info("Reading network xml file...");
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
		log.info("Reading network xml file...done.");

		// config
		MeisterkConfigGroup meisterk = new MeisterkConfigGroup();
		Gbl.getConfig().planomat().setTripStructureAnalysisLayer("link");

		// run
		this.runDemo((Layer) network, meisterk);

	}

	protected void runDemo(Layer layer, MeisterkConfigGroup meisterk) {

		HashMap<String, ArrayList<BasicLeg.Mode[]>> testCases = new HashMap<String, ArrayList<BasicLeg.Mode[]>>();

		////////////////////////////////////////////////////////////////
		//
		// 1 2 1
		//
		////////////////////////////////////////////////////////////////
		String testedActChainLocations = "1 2 1";
		ArrayList<BasicLeg.Mode[]> expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 20 1
		//
		////////////////////////////////////////////////////////////////
		testedActChainLocations = "1 2 20 1";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 1 2 1
		//
		////////////////////////////////////////////////////////////////

		testedActChainLocations = "1 2 1 2 1";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.car});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.bike});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 1 3 1
		//
		////////////////////////////////////////////////////////////////
		testedActChainLocations = "1 2 1 3 1";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 3 4
		//
		////////////////////////////////////////////////////////////////
		testedActChainLocations = "1 2 3 4";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});
		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 2 1
		//
		////////////////////////////////////////////////////////////////
		testedActChainLocations = "1 2 2 1";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.pt, BasicLeg.Mode.car});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.walk, BasicLeg.Mode.car});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.bike});

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.walk});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.pt});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.walk});

		testCases.put(testedActChainLocations, expectedTourModeOptions);

		////////////////////////////////////////////////////////////////
		//
		// 1 2 3 4 3 2 1
		//
		////////////////////////////////////////////////////////////////
		testedActChainLocations = "1 2 3 4 3 2 1";
		expectedTourModeOptions = new ArrayList<BasicLeg.Mode[]>();

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car, BasicLeg.Mode.car});

		int variableLegs = 2;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = combination[1] = combination[4] = combination[5] = BasicLeg.Mode.car;
			for (int jj = 0; jj < variableLegs ; jj++) {
				combination[jj + 2] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}		

		variableLegs = 4;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = combination[5] = BasicLeg.Mode.car;
			for (int jj = 0; jj < variableLegs ; jj++) {
				combination[jj + 1] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}

		variableLegs = 5;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = BasicLeg.Mode.pt;
			for (int jj = 0; jj < variableLegs; jj++) {
				combination[jj + 1] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}

		variableLegs = 3;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = combination[5] = BasicLeg.Mode.bike;
			combination[1] = BasicLeg.Mode.pt;
			for (int jj = 0; jj < variableLegs; jj++) {
				combination[jj + 2] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}

		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.pt, BasicLeg.Mode.bike, BasicLeg.Mode.bike});
		expectedTourModeOptions.add(new BasicLeg.Mode[]{BasicLeg.Mode.bike, BasicLeg.Mode.bike, BasicLeg.Mode.walk, BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.bike});

		variableLegs = 3;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = combination[5] = BasicLeg.Mode.bike;
			combination[1] = BasicLeg.Mode.walk;
			for (int jj = 0; jj < variableLegs; jj++) {
				combination[jj + 2] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}

		variableLegs = 5;
		for (int ii = 0; ii < (int) Math.pow(2, variableLegs); ii++) {
			BasicLeg.Mode[] combination = new BasicLeg.Mode[testedActChainLocations.split(" ").length - 1];
			combination[0] = BasicLeg.Mode.walk;
			for (int jj = 0; jj < variableLegs; jj++) {
				combination[jj + 1] = (((ii & ((int) Math.pow(2, variableLegs - (jj + 1)))) == 0)) ? BasicLeg.Mode.pt : BasicLeg.Mode.walk; 
			}
			expectedTourModeOptions.add(combination);
		}

		testCases.put(testedActChainLocations, expectedTourModeOptions);

		PlanAnalyzeTourModeChoiceSet testee = new PlanAnalyzeTourModeChoiceSet(meisterk);
		EnumSet<BasicLeg.Mode> possibleModes = EnumSet.of(BasicLeg.Mode.walk, BasicLeg.Mode.bike, BasicLeg.Mode.pt, BasicLeg.Mode.car);
		testee.setModeSet(possibleModes);

		Person person = new PersonImpl(new IdImpl("1000"));
		PlanomatConfigGroup.TripStructureAnalysisLayerOption subtourAnalysisLocationType = Gbl.getConfig().planomat().getTripStructureAnalysisLayer();
		Location location = null;
		Activity act = null;
		for (Entry<String, ArrayList<BasicLeg.Mode[]>> entry : testCases.entrySet()) {

			String facString  = entry.getKey();
			log.info("Testing location sequence: " + facString);

			Plan plan = new org.matsim.population.PlanImpl(person);

			String[] locationIdSequence = facString.split(" ");
			for (int aa=0; aa < locationIdSequence.length; aa++) {
				location = layer.getLocation(new IdImpl(locationIdSequence[aa]));
				if (PlanomatConfigGroup.TripStructureAnalysisLayerOption.facility.equals(subtourAnalysisLocationType)) {
					act = plan.createAct("actAtFacility" + locationIdSequence[aa], (Facility) location);
				} else if (PlanomatConfigGroup.TripStructureAnalysisLayerOption.link.equals(subtourAnalysisLocationType)) {
					act = plan.createAct("actOnLink" + locationIdSequence[aa], (Link) location);
				}
				act.setEndTime(10*3600);
				if (aa != (locationIdSequence.length - 1)) {
					plan.createLeg(BasicLeg.Mode.undefined);
				}
			}
			testee.run(plan);

			ArrayList<BasicLeg.Mode[]> actual = testee.getResult();
			assertEquals(entry.getValue().size(), actual.size());
			assertTrue(Arrays.deepEquals(
					(BasicLeg.Mode[][]) entry.getValue().toArray(new BasicLeg.Mode[0][0]), 
					(BasicLeg.Mode[][]) actual.toArray(new BasicLeg.Mode[0][0])));

		}

	}

}
