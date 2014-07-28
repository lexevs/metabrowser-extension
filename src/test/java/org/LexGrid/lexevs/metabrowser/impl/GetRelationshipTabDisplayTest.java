/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.LexGrid.lexevs.metabrowser.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.RelationshipTabResults;
import org.junit.Test;

/**
 * The Class GetRelationshipTabDisplayTest.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class GetRelationshipTabDisplayTest extends MetaBrowserServiceImplTest{
	
	/**
	 * Test get relationships tab display source of exclude self referencing true.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRelationshipsTabDisplaySourceOfExcludeSelfReferencingTrue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF, true);
		
		assertTrue("Count: " + count, count == 20);
	}
	
	/**
	 * Test get relationships tab display source of map values.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRelationshipsTabDisplaySourceOfMapValues() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<RelationshipTabResults>> results = impl.getRelationshipsDisplay("C0000726", null, Direction.SOURCEOF);
		
		assertTrue(results.keySet().size() > 3);
		
		assertTrue(results.keySet().contains("AQ"));
		assertTrue(results.keySet().contains("CHD"));
		assertTrue(results.keySet().contains("RO"));	
	}
	
	/**
	 * Test get relationships tab display size.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRelationshipsTabDisplaySize() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<RelationshipTabResults>> results = impl.getRelationshipsDisplay("C0000726", null, Direction.SOURCEOF);
		
		int size = 0;
		for(String key : results.keySet()){
			size += results.get(key).size();
		}
		assertTrue("Size: " + size, size == 20);
	}
	
	/**
	 * Test get relationships tab display value.
	 * 
	 * @throws Exception the exception
	 */
	@Test
	public void testGetRelationshipsTabDisplayValue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<RelationshipTabResults>> results = impl.getRelationshipsDisplay("C0000726", null, Direction.SOURCEOF);
		
		RelationshipTabResults result = results.get("PAR").get(0);
		
		assertTrue(result.getCui().equals("C0000737"));
		assertTrue(result.getRel().equals("PAR"));
		assertTrue(result.getRela() == null);
		assertTrue(result.getSource().equals("CST"));
		assertTrue(result.getName().equals("Abdominal Pain"));
	}
}
