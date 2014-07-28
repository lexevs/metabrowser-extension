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

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.LexGrid.LexBIG.DataModel.InterfaceElements.types.ProcessState;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Extensions.Load.MetaBatchLoader;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.impl.MetaBrowserServiceImpl;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.junit.Before;
import org.junit.Test;
import org.lexevs.locator.LexEvsServiceLocator;

/**
 * The Class MetaBrowserServiceImplTest.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class MetaBrowserServiceImplTest {

	@Before
	public void setUp() throws Exception{
		
//		System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
//		LexBIGService service = LexBIGServiceImpl.defaultInstance();
//        LexBIGServiceManager lbsm = service.getServiceManager(null);
//
//        MetaBatchLoader loader = (MetaBatchLoader) lbsm.getLoader("MetaBatchLoader");
//
//        loader.loadMeta(new File("resources/SAMPLEMETA").toURI());
//
//        while (loader.getStatus().getEndTime() == null) {
//            Thread.sleep(500);
//        }
//        assertTrue(loader.getStatus().getState().equals(ProcessState.COMPLETED));
//        assertFalse(loader.getStatus().getErrorsLogged().booleanValue());
//
//        lbsm.activateCodingSchemeVersion(loader.getCodingSchemeReferences()[0]);
		
		 SetUpHelper.init();
		 
	}
	
	
}
