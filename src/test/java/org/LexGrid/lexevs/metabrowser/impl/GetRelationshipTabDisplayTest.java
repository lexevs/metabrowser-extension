package org.LexGrid.lexevs.metabrowser.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.RelationshipTabResults;
import org.junit.Test;

public class GetRelationshipTabDisplayTest {
	@Test
	public void testGetRelationshipsTabDisplaySourceOfExcludeSelfReferencingTrue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF, true);
		
		assertTrue("Count: " + count, count == 3);
	}
	
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
	
	@Test
	public void testGetRelationshipsTabDisplayValue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<RelationshipTabResults>> results = impl.getRelationshipsDisplay("C0000726", null, Direction.SOURCEOF);
		
		RelationshipTabResults result = results.get("CHD").get(0);
		
		assertTrue(result.getCui().equals("C0000737"));
		assertTrue(result.getRel().equals("CHD"));
		assertTrue(result.getRela() == null);
		assertTrue(result.getSource().equals("CST"));
		assertTrue(result.getName().equals("Abdominal Pain"));
	}
}
