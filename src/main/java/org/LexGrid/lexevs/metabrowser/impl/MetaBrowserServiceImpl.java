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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.ExtensionDescription;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.Impl.Extensions.AbstractExtendable;
import org.LexGrid.LexBIG.Impl.dataAccess.ResourceManager;
import org.LexGrid.LexBIG.Impl.dataAccess.SQLImplementedMethods;
import org.LexGrid.LexBIG.Impl.dataAccess.SQLInterface;
import org.LexGrid.LexBIG.Impl.internalExceptions.MissingResourceException;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.annotations.LgClientSideSafe;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaTree;
import org.LexGrid.lexevs.metabrowser.helper.MrDocLoader;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.RelationshipTabResults;
import org.LexGrid.naming.SupportedAssociation;
import org.LexGrid.relations.Relations;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.lexgrid.loader.meta.constants.MetaLoaderConstants;
import org.lexgrid.loader.rrf.constants.RrfLoaderConstants;

/**
 * The Class MetaBrowserServiceImpl.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class MetaBrowserServiceImpl extends AbstractExtendable implements MetaBrowserService {

	/** The CODIN g_ schem e_ name. */
	public static String CODING_SCHEME_NAME = "NCI MetaThesaurus";
	
	/** The NC i_ source. */
	private static String NCI_SOURCE = "NCI";
	
	/** The NC i_ root. */
	private static String NCI_ROOT = "C1140168";
	
	/** The CH d_ rel. */
	public static String CHD_REL = "CHD";
	
	/** The PA r_ rel. */
	public static String PAR_REL = "PAR";
	
	/** The lbs. */
	private transient LexBIGService lbs;
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The SOURC e_ qua l_ col. */
	private static String SOURCE_QUAL_COL = "sourceQualifier";
	
	/** The REL a_ qua l_ col. */
	private static String RELA_QUAL_COL = "relaQualifier";
	
	/** The RE l_ col. */
	private static String REL_COL = "rel";
	
	/** The SOURC e_ cod e_ qua l_ col. */
	private static String SOURCE_CODE_QUAL_COL = "sourceCodeQualifier";
	
	/** The SOURC e_ qua l_ value. */
	private static String SOURCE_QUAL_VALUE = MetaLoaderConstants.SOURCE_QUALIFIER;
	
	/** The SOURC e_ cod e_ qua l_ value. */
	private static String SOURCE_CODE_QUAL_VALUE = MetaLoaderConstants.SOURCE_CODE_QUALIFIER;
	
	/** The REL a_ qua l_ value. */
	private static String RELA_QUAL_VALUE = RrfLoaderConstants.RELA_QUALIFIER;
	
	/** The AU i_ targe t_ qua l_ value. */
	private static String AUI_TARGET_QUAL_VALUE = MetaLoaderConstants.TARGET_AUI_QUALIFIER;
	
	/** The AU i_ sourc e_ qua l_ value. */
	private static String AUI_SOURCE_QUAL_VALUE = MetaLoaderConstants.SOURCE_AUI_QUALIFIER;
	
	/** The AU i_ qua l_ value. */
	private static String AUI_QUAL_VALUE = RrfLoaderConstants.AUI_QUALIFIER;
	
	/** The ROOT. */
	private static String ROOT = "@";
	
	/** The TAIL. */
	private static String TAIL = "@@";
	
	/** The sab root node cache. */
	private Map<String,String> sabRootNodeCache = new HashMap<String,String>();
	
	/** The internal name. */
	private String internalName;
	
	/** The internal version. */
	private String internalVersion;
	
	/** The max to return. */
	private int maxToReturn;
	
	/** The associations. */
	private List<String> associations;
	
	/** The association reverse names. */
	private Map<String,String> associationReverseNames = new HashMap<String,String>();
	
	/** The rela reverse names. */
	private Map<String,String> relaReverseNames;
	
	/** The sql interface. */
	private transient SQLInterface sqlInterface;

	/**
	 * Inits the extension.
	 * 
	 * @throws LBException the LB exception
	 */
	public void initExtension() throws LBException{
		CodingSchemeVersionOrTag tagOrVersion = null;
		String codingSchemeName = CODING_SCHEME_NAME;
		
		ResourceManager rm = null;
		try {
			rm = ResourceManager.instance();
		
		
		String version; 
		if (tagOrVersion == null || tagOrVersion.getVersion() == null || tagOrVersion.getVersion().length() == 0) {
	            version = rm.getInternalVersionStringFor(codingSchemeName,
	                    (tagOrVersion == null ? null : tagOrVersion.getTag()));
	        } else {
	            version = tagOrVersion.getVersion();
	        }
		 
		 this.internalName = rm
         .getInternalCodingSchemeNameForUserCodingSchemeName(codingSchemeName, version);
		 
		 this.internalVersion = version;
		 
		 if(relaReverseNames == null){
			 MrDocLoader mrdocLoader = new MrDocLoader();
			 relaReverseNames = mrdocLoader.getRelasAndReverseRelas();
		 }
		 
		 maxToReturn = rm.getSystemVariables().getMaxResultSize();
		 
		 try {
			 if(associations == null){
				 associations = buildAssociationList();
			 }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		sqlInterface = this.getSqlInterface();
		
		} catch (Throwable e1) {
			throw new LBException(e1.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getBySourceTabDisplay(java.lang.String, java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction)
	 */
	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(String cui,
			String source, List<String> relationships, Direction direction) throws LBException {	
		return getBySourceTabDisplay(cui,
				source, relationships, direction, true);
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getBySourceTabDisplay(java.lang.String, java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction, boolean)
	 */
	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(
			String cui,
			String source, 
			List<String> relationships, 
			Direction direction, 
			boolean excludeSelfReferencing) throws LBException{
		return this.getBySourceTabDisplay(
				cui, 
				source, 
				relationships, 
				direction, 
				excludeSelfReferencing, 
				0, 
				-1);
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getBySourceTabDisplay(java.lang.String, java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction, boolean, int, int)
	 */
	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(
			String cui,
			String source, 
			List<String> relationships, 
			Direction direction, 
			boolean excludeSelfReferencing, 
			int start,
			int pageSize) throws LBException {	
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}
		
		Map<String, List<BySourceTabResults>> map =
			buildRelationshipMap(relationships, direction, BySourceTabResults.class);
		
		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildGetBySourceDisplaySql(
							source, 
							direction, 
							relationships, 
							excludeSelfReferencing, 
							start, 
							pageSize));
			
			getRelations.setString(1, cui);

			rs = getRelations.executeQuery();
			return buildBySourceTabResults(rs, map, direction);	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(getRelations != null){
					sqlInterface.checkInPreparedStatement(getRelations);
					getRelations.close();
				}
				if(rs != null){
					rs.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}	
	
	}
	
	/**
	 * Gets the association reverse name.
	 * 
	 * @param cs the cs
	 * @param associationName the association name
	 * 
	 * @return the association reverse name
	 */
	private String getAssociationReverseName(CodingScheme cs, String associationName){
		Relations[] relations = cs.getRelations();
		for (int i = 0; i < relations.length; i++) {
			org.LexGrid.relations.Association[] associations = relations[i].getAssociation();
			for (int j = 0; j < associations.length; j++) {
				if (associations[j].getEntityCode() != null
						&& associations[j].getEntityCode().equalsIgnoreCase(associationName))
					return associations[j].getReverseName();
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getRelationshipsDisplay(java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction)
	 */
	public Map<String, List<RelationshipTabResults>> getRelationshipsDisplay(String cui, 
			List<String> relationships, 
			Direction direction) throws LBException {
		return getRelationshipsDisplay(cui, 
				relationships, 
				direction, true);
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getRelationshipsDisplay(java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction, boolean)
	 */
	public Map<String, List<RelationshipTabResults>> getRelationshipsDisplay(String cui, 
			List<String> relationships, 
			Direction direction, boolean excludeSelfReferencing) throws LBException {
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}
		
		Map<String, List<RelationshipTabResults>> map =
			buildRelationshipMap(relationships, direction, RelationshipTabResults.class);
		
		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildGetRelationshipsDisplaySql(direction, relationships, excludeSelfReferencing));
			
			getRelations.setString(1, cui);

			rs = getRelations.executeQuery();
			return buildRelationshipTabResults(rs, map, direction);	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(getRelations != null){
					sqlInterface.checkInPreparedStatement(getRelations);
					getRelations.close();
				}
				if(rs != null){
					rs.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}	
	}
	
	/**
	 * Builds the relationship map.
	 * 
	 * @param relationships the relationships
	 * @param direction the direction
	 * @param result the result
	 * 
	 * @return the map< string, list< t>>
	 */
	private <T> Map<String, List<T>> buildRelationshipMap(
			List<String> relationships, Direction direction,
			Class<T> result) {
		Map<String, List<T>> map = 
			new HashMap<String, List<T>>();
		for(String rel : relationships){
			String relationDirectionName = rel;
			if(direction.equals(Direction.TARGETOF)){
				relationDirectionName = reverseRel(rel);
			}
			map.put(relationDirectionName, new ArrayList<T>());
		}
		return map;
	}

	/**
	 * Builds the relationship tab results.
	 * 
	 * @param rs the rs
	 * @param map the map
	 * @param direction the direction
	 * 
	 * @return the map< string, list< relationship tab results>>
	 * 
	 * @throws SQLException the SQL exception
	 */
	private Map<String, List<RelationshipTabResults>> buildRelationshipTabResults(
			ResultSet rs, 
			Map<String, List<RelationshipTabResults>> map,
			Direction direction) throws SQLException{
		while(rs.next()){
			RelationshipTabResults result = new RelationshipTabResults();
			
			String rel = rs.getString(REL_COL);
			if(direction.equals(Direction.TARGETOF)){
				rel = reverseRel(rel);
			}
			
			String targetConceptCode = null;
			if(direction.equals(Direction.TARGETOF)){
				targetConceptCode = rs.getString(sqlInterface.getSQLTableConstants().sourceEntityCodeOrId);
			} else {
				targetConceptCode = rs.getString(sqlInterface.getSQLTableConstants().targetEntityCodeOrId);
			}
			
			String entityDescription = rs.getString(SQLTableConstants.TBLCOL_ENTITYDESCRIPTION);
			String sourceQualValue = rs.getString(SOURCE_QUAL_COL);
			
			String relaQualValue = rs.getString(RELA_QUAL_COL);
			if(direction.equals(Direction.TARGETOF)){
				relaQualValue = reverseRela(relaQualValue);
			}
			
			result.setCui(targetConceptCode);
			result.setName(entityDescription);
			result.setRel(rel);
			result.setSource(sourceQualValue);
			result.setRela(relaQualValue);
			
			map.get(rel).add(result);
		}
		return map;
	}
	
	/**
	 * Builds the by source tab results.
	 * 
	 * @param rs the rs
	 * @param map the map
	 * @param direction the direction
	 * 
	 * @return the map< string, list< by source tab results>>
	 * 
	 * @throws SQLException the SQL exception
	 */
	private Map<String, List<BySourceTabResults>> buildBySourceTabResults(
			ResultSet rs, 
			Map<String, List<BySourceTabResults>> map,
			Direction direction) throws SQLException{
		while(rs.next()){
			BySourceTabResults result = new BySourceTabResults();
			
			String rel = rs.getString(REL_COL);
			if(direction.equals(Direction.TARGETOF)){
				rel = reverseRel(rel);
			}
			
			String targetConceptCode = null;
			if(direction.equals(Direction.TARGETOF)){
				targetConceptCode = rs.getString(sqlInterface.getSQLTableConstants().sourceEntityCodeOrId);
			} else {
				targetConceptCode = rs.getString(sqlInterface.getSQLTableConstants().targetEntityCodeOrId);
			}
			
			String termText = rs.getString(SQLTableConstants.TBLCOL_PROPERTYVALUE);
			String sourceQualValue = rs.getString(SOURCE_QUAL_COL);
			String repForm = rs.getString(SQLTableConstants.TBLCOL_REPRESENTATIONALFORM);
			String sourceCode = rs.getString(SOURCE_CODE_QUAL_COL);
			
			String relaQualValue = rs.getString(RELA_QUAL_COL);
			if(direction.equals(Direction.TARGETOF)){
				relaQualValue = reverseRela(relaQualValue);
			}
			
			result.setCui(targetConceptCode);
			result.setTerm(termText);
			result.setRel(rel);
			result.setSource(sourceQualValue);
			result.setRela(relaQualValue);
			result.setType(repForm);
			result.setCode(sourceCode);
			
			map.get(rel).add(result);
		}
		return map;
	}
	
	/**
	 * Reverse rel.
	 * 
	 * @param rel the rel
	 * 
	 * @return the string
	 */
	private String reverseRel(String rel){
		return associationReverseNames.get(rel);
	}
	
	/**
	 * Reverse rela.
	 * 
	 * @param rela the rela
	 * 
	 * @return the string
	 */
	private String reverseRela(String rela){
		return relaReverseNames.get(rela);
	}
	
	/**
	 * Gets the sql interface.
	 * 
	 * @return the sql interface
	 * 
	 * @throws RuntimeException the runtime exception
	 */
	private SQLInterface getSqlInterface() throws RuntimeException{
		try {
			return ResourceManager.instance().getSQLInterface(internalName,
			        internalVersion);
		} catch (MissingResourceException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Builds the get relationships count sql.
	 * 
	 * @param direction the direction
	 * @param relations the relations
	 * @param excludeSelfReferencing the exclude self referencing
	 * 
	 * @return the string
	 */
	private String buildGetRelationshipsCountSql(Direction direction, List<String> relations, boolean excludeSelfReferencing) {
		SQLInterface si = this.getSqlInterface();
		String targetCol = null;
		if(direction.equals(Direction.SOURCEOF)){
			targetCol = si.getSQLTableConstants().sourceEntityCodeOrId;
		} else {
			targetCol = si.getSQLTableConstants().targetEntityCodeOrId;
		}
		
		StringBuffer sb = new StringBuffer();
		String sql = 
	    		 "SELECT count(*) " + 
	    		 " FROM " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) +
	             " {AS} eate " +
	
	             " WHERE " +
	             targetCol  + " = ? " +
	             " AND " +
	             targetCol + " != '" + ROOT + "' " +
	             " AND " +
	             targetCol + " != '" + TAIL + "' " +
	             " AND ( ";
		
				sb.append(sql);
		
				for(int i=0;i<relations.size();i++){
					sb.append("eate." + si.getSQLTableConstants().entityCodeOrAssociationId + " = '" + relations.get(i) + "'");
					if(i == relations.size() -1 ){
						sb.append(" )");
					} else {
						sb.append(" OR ");
					}
				}
				
				if(excludeSelfReferencing){
					sb.append(getExcludeSelfReferencingSql());
				}
		return sb.toString();
	}
	
	/**
	 * Builds the by source count sql.
	 * 
	 * @param source the source
	 * @param direction the direction
	 * @param relations the relations
	 * @param excludeSelfReferencing the exclude self referencing
	 * 
	 * @return the string
	 */
	private String buildBySourceCountSql(String source, Direction direction, List<String> relations,  boolean excludeSelfReferencing) {
		SQLInterface si = this.getSqlInterface();
		String targetCol = null;
		if(direction.equals(Direction.SOURCEOF)){
			targetCol = si.getSQLTableConstants().sourceEntityCodeOrId;
		} else {
			targetCol = si.getSQLTableConstants().targetEntityCodeOrId;
		}
		
		StringBuffer sb = new StringBuffer();
		String sql = 
	    		 "SELECT count(*) " + 
	    		 " FROM " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) +
	             " {AS} eate " +
	             
	             " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} sourceQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = sourceQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " AND " +
                 " sourceQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '"+ SOURCE_QUAL_VALUE + "' )" +
	
	             " WHERE " +
	             targetCol  + " = ? " +	           
	             " AND " +
	             targetCol + " != '" + ROOT + "' " +
	             " AND " +
	             targetCol + " != '" + TAIL + "' " +
	             " AND ( ";
		
				sb.append(sql);
		
				for(int i=0;i<relations.size();i++){
					sb.append("eate." + si.getSQLTableConstants().entityCodeOrAssociationId + " = '" + relations.get(i) + "'");
					if(i == relations.size() -1 ){
						sb.append(" )");
					} else {
						sb.append(" OR ");
					}
				}
				
				if(source != null){
					sb.append(" AND " +
						"sourceQual." + SQLTableConstants.TBLCOL_QUALIFIERVALUE + " =  '" + source + "'");
				}
		
				if(excludeSelfReferencing){
					sb.append(getExcludeSelfReferencingSql());
				}
		return sb.toString();
	}
	
	/**
	 * Builds the get relationships display sql.
	 * 
	 * @param direction the direction
	 * @param relations the relations
	 * @param excludeSelfReferencing the exclude self referencing
	 * 
	 * @return the string
	 */
	private String buildGetRelationshipsDisplaySql(Direction direction, List<String> relations, boolean excludeSelfReferencing) {
		SQLInterface si = this.getSqlInterface();
		String sourceCol = null;
		String targetCol = null;
		if(direction.equals(Direction.SOURCEOF)){
			sourceCol = si.getSQLTableConstants().targetEntityCodeOrId;
			targetCol = si.getSQLTableConstants().sourceEntityCodeOrId;
		} else {
			targetCol = si.getSQLTableConstants().targetEntityCodeOrId;
			sourceCol = si.getSQLTableConstants().sourceEntityCodeOrId;
		}
		
		StringBuffer sb = new StringBuffer();
		String sql = 
	    		 "SELECT " + 
	    		 "eate." + si.getSQLTableConstants().entityCodeOrAssociationId  + " {AS} " + REL_COL + ", " +
	    		 sourceCol + ", " +
	    		 " entity."+ SQLTableConstants.TBLCOL_ENTITYDESCRIPTION + ", " +
	    		 " sourceQual."+ SQLTableConstants.TBLCOL_QUALIFIERVALUE + " {AS} " + SOURCE_QUAL_COL + ", " +
	    		 " relaQual."+ SQLTableConstants.TBLCOL_QUALIFIERVALUE + " {AS} " + RELA_QUAL_COL +
	    		 " FROM " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) +
	             " {AS} eate " +
	             
	             " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY) +
                 " {AS} entity ON " +
                 " eate." + si.getSQLTableConstants().codingSchemeNameOrId +
                 " = entity." + si.getSQLTableConstants().codingSchemeNameOrId +
                 " AND " +
                 sourceCol +
                 " = entity." + si.getSQLTableConstants().entityCodeOrId +
                 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} sourceQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = sourceQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " AND " +
                 " sourceQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '"+ SOURCE_QUAL_VALUE + "' )" +
                 
                 " LEFT JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} relaQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = relaQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY  +
                 " AND " +
                 " relaQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '" + RELA_QUAL_VALUE + "' )" +
                 
	             " WHERE " +
	             targetCol  + " = ? " +
	             " AND " +
	             targetCol + " != '" + ROOT + "' " +
	             " AND " +
	             targetCol + " != '" + TAIL + "' " +
	             " AND ( ";
		
				sb.append(sql);
		
				for(int i=0;i<relations.size();i++){
					sb.append("eate." + si.getSQLTableConstants().entityCodeOrAssociationId + " = '" + relations.get(i) + "'");
					if(i == relations.size() -1 ){
						sb.append(" )");
					} else {
						sb.append(" OR ");
					}
				}
				
				if(excludeSelfReferencing){
					sb.append(getExcludeSelfReferencingSql());
				}
				
				
		return sb.toString();
	}
	
	/**
	 * Builds the get by source display sql.
	 * 
	 * @param source the source
	 * @param direction the direction
	 * @param relations the relations
	 * @param excludeSelfReferencing the exclude self referencing
	 * @param start the start
	 * @param pageSize the page size
	 * 
	 * @return the string
	 */
	private String buildGetBySourceDisplaySql(
			String source, Direction direction, List<String> relations,  
			boolean excludeSelfReferencing, int start, int pageSize) {
		SQLInterface si = this.getSqlInterface();
		String sourceCol = null;
		String targetCol = null;
		String auiCol = null;
		if(direction.equals(Direction.SOURCEOF)){
			sourceCol = si.getSQLTableConstants().targetEntityCodeOrId;
			targetCol = si.getSQLTableConstants().sourceEntityCodeOrId;
			auiCol = "auiTargetQual";
		} else {
			targetCol = si.getSQLTableConstants().targetEntityCodeOrId;
			sourceCol = si.getSQLTableConstants().sourceEntityCodeOrId;
			auiCol = "auiSourceQual";
		}
		
		StringBuffer sb = new StringBuffer();
		String sql = 
	    		 "SELECT " + 
	    		 "eate." + si.getSQLTableConstants().entityCodeOrAssociationId  + " {AS} " + REL_COL + ", " +
	    		 sourceCol + ", " +
	    		 " entityProperty." + SQLTableConstants.TBLCOL_PROPERTYVALUE + ", " +
	    		 " entityProperty." + SQLTableConstants.TBLCOL_REPRESENTATIONALFORM + ", " +
	    		 " propSourceQual." + SQLTableConstants.TBLCOL_ATTRIBUTEVALUE + " {AS} " + SOURCE_QUAL_COL + ", " +
	    		 " sourceCodeQual." + SQLTableConstants.TBLCOL_VAL1 + " {AS} " + SOURCE_CODE_QUAL_COL + ", " +
	    		 " relaQual."+ SQLTableConstants.TBLCOL_QUALIFIERVALUE + " {AS} " + RELA_QUAL_COL +
	    		 " FROM " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_ENTITY) +
	             " {AS} eate " +
	     
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} assocSourceQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = assocSourceQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " AND " +
                 " assocSourceQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '"+ SOURCE_QUAL_VALUE + "' )" +
                 
                 " LEFT JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} relaQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = relaQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY  +
                 " AND " +
                 " relaQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '" + RELA_QUAL_VALUE + "' )" +
                 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} auiSourceQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = auiSourceQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY  +
                 " AND " +
                 " auiSourceQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '" + AUI_SOURCE_QUAL_VALUE + "' )" +
                 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_ASSOCIATION_TO_E_QUALS) +
                 " {AS} auiTargetQual ON (" +
                 " eate." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY +
                 " = auiTargetQual." + SQLTableConstants.TBLCOL_MULTIATTRIBUTESKEY  + 
                 " AND " +
                 " auiTargetQual." + SQLTableConstants.TBLCOL_QUALIFIERNAME +
                 " = '" + AUI_TARGET_QUAL_VALUE + "' )" +
 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_PROPERTY_MULTI_ATTRIBUTES) +
                 " {AS} entityPropertyMultiAttrib ON ( " +
                 " entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_PROPERTYID + 
                 " = " + auiCol + ".qualifierValue )" +
                 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_PROPERTY_MULTI_ATTRIBUTES) +
                 " {AS} sourceCodeQual ON ( " +
                 " sourceCodeQual." + SQLTableConstants.TBLCOL_PROPERTYID + 
                 " = " + auiCol + ".qualifierValue " +
                 " AND " + 
                 " sourceCodeQual." + SQLTableConstants.TBLCOL_ATTRIBUTEVALUE +
                 " = '" + SOURCE_CODE_QUAL_VALUE + "' )" +
              
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_PROPERTY_MULTI_ATTRIBUTES) +
                 " {AS} propSourceQual ON ( " +
                 " propSourceQual." + SQLTableConstants.TBLCOL_PROPERTYID + 
                 " = " + auiCol + ".qualifierValue " +
                 " AND " + 
                 " propSourceQual." + SQLTableConstants.TBLCOL_TYPENAME +
                 " = '" + SQLTableConstants.TBLCOLVAL_SOURCE + "' )" +
                 
                 " INNER JOIN " + si.getTableName(SQLTableConstants.ENTITY_PROPERTY) +
                 " {AS} entityProperty ON (" +
                 " entityProperty." + SQLTableConstants.TBLCOL_ENTITYCODENAMESPACE + 
                 " = entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_ENTITYCODENAMESPACE +
                 " AND " + 
                 " entityProperty." + si.getSQLTableConstants().entityCodeOrId + 
                 " = entityPropertyMultiAttrib." + si.getSQLTableConstants().entityCodeOrId +
                 " AND " + 
                 " entityProperty." + SQLTableConstants.TBLCOL_CODINGSCHEMENAME + 
                 " = entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_CODINGSCHEMENAME +
                 " AND " + 
                 " entityProperty." + SQLTableConstants.TBLCOL_PROPERTYID +
                 " = entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_PROPERTYID + " )" +
                  
	             " WHERE " +
	             targetCol  + " = ? " +   
	             " AND " +
                 " entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_PROPERTYID + 
                 " = " + auiCol + ".qualifierValue " +
                 " AND " +
                 " entityPropertyMultiAttrib." + SQLTableConstants.TBLCOL_ATTRIBUTEVALUE +
                 " = '" + AUI_QUAL_VALUE + "' " +
	             " AND " +
	             targetCol + " != '" + ROOT + "' " +
	             " AND " +
	             targetCol + " != '" + TAIL + "' " +
	             " AND ( ";
		
				sb.append(sql);
		
				for(int i=0;i<relations.size();i++){
					sb.append("eate." + si.getSQLTableConstants().entityCodeOrAssociationId + " = '" + relations.get(i) + "'");
					if(i == relations.size() -1 ){
						sb.append(" )");
					} else {
						sb.append(" OR ");
					}
				}
				
				if(source != null){
					sb.append(" AND " +
						"assocSourceQual." + SQLTableConstants.TBLCOL_QUALIFIERVALUE + " = '" + source + "'");
				}
				
				if(excludeSelfReferencing){
					sb.append(getExcludeSelfReferencingSql());
				}
				
				if(pageSize > 0){
					sb.append(" LIMIT " + pageSize);
					sb.append(" OFFSET " + start);
				}	
		return sb.toString();
	}
	
	/**
	 * Builds the association list.
	 * 
	 * @return the list< string>
	 * 
	 * @throws Exception the exception
	 */
	private List<String> buildAssociationList() throws Exception {
		List<String> returnList = new ArrayList<String>();
		CodingScheme cs = SQLImplementedMethods.buildCodingScheme(internalName, internalVersion);
		for(SupportedAssociation assoc : cs.getMappings().getSupportedAssociation()){
			String assocName = assoc.getLocalId();
			returnList.add(assocName);
			associationReverseNames.put(assocName, 
					this.getAssociationReverseName(cs, assocName));
		}
		return returnList;
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getCount(java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction)
	 */
	public int getCount(String cui,
			List<String> relationships, Direction direction) throws LBException {
		return getCount(cui,
				relationships, direction,  true);
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getCount(java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction, boolean)
	 */
	public int getCount(String cui,
			List<String> relationships, Direction direction,  boolean excludeSelfReferencing) throws LBException {
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}

		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildGetRelationshipsCountSql(direction, relationships, excludeSelfReferencing));
			
			getRelations.setString(1, cui);
			
			rs = getRelations.executeQuery();
			if(!rs.next()){
				return 0;
			}
			return rs.getInt(1);	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(getRelations != null){
					sqlInterface.checkInPreparedStatement(getRelations);
					getRelations.close();
				}
				if(rs != null){
					rs.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}	
	}
	
	/**
	 * Gets the exclude self referencing sql.
	 * 
	 * @return the exclude self referencing sql
	 */
	private String getExcludeSelfReferencingSql(){
		StringBuffer sb = new StringBuffer();
		/*
		sb.append(
				" AND " +
				sqlInterface.getSQLTableConstants().targetEntityCodeOrId +
				" != " +
				sqlInterface.getSQLTableConstants().sourceEntityCodeOrId);
				*/
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getCount(java.lang.String, java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction)
	 */
	public int getCount(String cui, String source,
			List<String> relationships, Direction direction) throws LBException {
		return getCount(cui, source,
				relationships, direction, true);
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getCount(java.lang.String, java.lang.String, java.util.List, org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction, boolean)
	 */
	public int getCount(String cui, String source,
			List<String> relationships, Direction direction, boolean excludeSelfReferencing) throws LBException {
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}

		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildBySourceCountSql(source, direction, relationships, excludeSelfReferencing));
			
			getRelations.setString(1, cui);
			
			rs = getRelations.executeQuery();
			if(!rs.next()){
				return 0;
			}
			return rs.getInt(1);	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(getRelations != null){
					sqlInterface.checkInPreparedStatement(getRelations);
					getRelations.close();
				}
				if(rs != null){
					rs.close();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
		}	
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getMaxToReturn()
	 */
	public int getMaxToReturn() throws LBException {
		initExtension();
		return this.maxToReturn;
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.LexBIG.Impl.Extensions.AbstractExtendable#buildExtensionDescription()
	 */
	@Override
	protected ExtensionDescription buildExtensionDescription() {
		ExtensionDescription ed = new ExtensionDescription();
		ed.setDescription("MetaBrowser Relationship Utility Extension");
		ed.setExtensionBaseClass(MetaBrowserService.class.getName());
		ed.setExtensionClass(MetaBrowserServiceImpl.class.getName());
		ed.setExtensionProvider(new Source("Mayo Clinic"));
		ed.setName("metabrowser-extension");
		ed.setVersion("1.0");
		return ed;
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getMetaNCINeighborhood(java.lang.String)
	 */
	@LgClientSideSafe 
	public MetaTree getMetaNCINeighborhood(String focus) throws LBException {
		return getMetaNeighborhood(focus, NCI_SOURCE);	
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getMetaNCINeighborhood()
	 */
	public MetaTree getMetaNCINeighborhood() throws LBException {
		sabRootNodeCache.put(NCI_SOURCE, NCI_ROOT);
		return getMetaNeighborhood(NCI_SOURCE);	
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getMetaNeighborhood(java.lang.String, java.lang.String)
	 */
	public MetaTree getMetaNeighborhood(String focus, String source) throws LBException {
		MetaBrowserService svc = this;
		return new  MetaTreeImpl(svc, focus, source);	
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getMetaNeighborhood(java.lang.String)
	 */
	public MetaTree getMetaNeighborhood(String source) throws LBException {
		MetaBrowserService svc = this;
		if(sabRootNodeCache.containsKey(source)){
			return new MetaTreeImpl(svc, sabRootNodeCache.get(source), source);
		} else {
			MetaTree tree = new MetaTreeImpl(svc, source);	
			sabRootNodeCache.put(source, tree.getCurrentFocus().getCui());
			return tree;
		}
	}

	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#setLexBIGService(org.LexGrid.LexBIG.LexBIGService.LexBIGService)
	 */
	@LgClientSideSafe 
	public void setLexBIGService(LexBIGService lbs) throws LBException {
		this.lbs = lbs;	
	}
	
	/* (non-Javadoc)
	 * @see org.LexGrid.lexevs.metabrowser.MetaBrowserService#getLexBIGService()
	 */
	@LgClientSideSafe 
	public LexBIGService getLexBIGService() throws LBException {
		if(lbs == null){
			lbs = LexBIGServiceImpl.defaultInstance();
		} 
		return lbs;
	}
}
