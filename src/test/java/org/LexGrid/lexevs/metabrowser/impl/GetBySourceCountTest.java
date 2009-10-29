package org.LexGrid.lexevs.metabrowser.impl;

import static org.junit.Assert.assertTrue;

import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.junit.Test;

public class GetBySourceCountTest {
	
	@Test
	public void testGetCountBSouceTabSourceOf() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.SOURCEOF);
		
		assertTrue("Count: " + count, count == 20);
	}
	
	@Test
	public void testGetCountBySouceTabSourceOfExcludeSelfReferencingFalse() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.SOURCEOF, true);
		
		assertTrue("Count: " + count, count == 20);
	}
	
	@Test
	public void testGetCountBySouceTabSourceOfExcludeSelfReferencingTrue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.SOURCEOF, false);
		
		assertTrue("Count: " + count, count == 22);
	}
	
	@Test
	public void testGetCountBySouceTabTargetOf() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF);
		
		assertTrue("Count: " + count, count == 3);
	}
	
	@Test
	public void testGetCountBySouceTabTargetOfExcludeSelfReferencingTrue() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF, true);
		
		assertTrue("Count: " + count, count == 3);
	}
	
	@Test
	public void testGetCountBySouceTabTargetOfExcludeSelfReferencingFalse() throws Exception {
		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		int count = impl.getCount("C0000726", null, null, Direction.TARGETOF, false);
		
		assertTrue("Count: " + count, count == 5);
	}
}
