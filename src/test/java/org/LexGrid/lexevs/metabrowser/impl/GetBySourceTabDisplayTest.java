package org.LexGrid.lexevs.metabrowser.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.junit.Test;

public class GetBySourceTabDisplayTest {
	@Test
	public void testGetBySouceTabDisplaySourceOfExcludeSelfReferencingTrue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF, true);
		
		assertTrue("Count: " + count, count == 3);
	}
	
	@Test
	public void testGetBySouceTabDisplaySourceOfMapValues() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<BySourceTabResults>> results = impl.getBySourceTabDisplay("C0000726", null, null, Direction.SOURCEOF);
		
		assertTrue(results.keySet().size() > 3);
		
		assertTrue(results.keySet().contains("AQ"));
		assertTrue(results.keySet().contains("CHD"));
		assertTrue(results.keySet().contains("RO"));	
	}
	
	@Test
	public void testGetBySouceTabDisplaySourceOfSize() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<BySourceTabResults>> results = impl.getBySourceTabDisplay("C0000726", null, null, Direction.SOURCEOF);
		
		int size = 0;
		for(String key : results.keySet()){
			size += results.get(key).size();
		}
		assertTrue("Size: " + size, size == 20);
	}
	
	@Test
	public void testGetBySouceTabDisplaySourceOfWithSourceSize() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<BySourceTabResults>> results = impl.getBySourceTabDisplay("C0000726", "CST", null, Direction.SOURCEOF);
		
		int size = 0;
		for(String key : results.keySet()){
			size += results.get(key).size();
		}
		assertTrue("Size: " + size, size == 1);
	}
	
	@Test
	public void testGetBySouceTabDisplaySourceOfWithSourceValue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		Map<String,List<BySourceTabResults>> results = impl.getBySourceTabDisplay("C0000726", "CST", null, Direction.SOURCEOF);
		
		BySourceTabResults result = results.get("CHD").get(0);
		
		assertTrue(result.getCui().equals("C0000737"));
		assertTrue(result.getCode().equals("PAIN ABDO"));
		assertTrue(result.getRel().equals("CHD"));
		assertTrue(result.getRela() == null);
		assertTrue(result.getSource().equals("CST"));
		assertTrue(result.getTerm().equals("ABDOMINAL PAIN"));
		assertTrue(result.getType().equals("PT"));
	}
}
