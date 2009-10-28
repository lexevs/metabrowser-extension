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
import org.LexGrid.LexBIG.Impl.Extensions.AbstractExtendable;
import org.LexGrid.LexBIG.Impl.dataAccess.ResourceManager;
import org.LexGrid.LexBIG.Impl.dataAccess.SQLImplementedMethods;
import org.LexGrid.LexBIG.Impl.dataAccess.SQLInterface;
import org.LexGrid.LexBIG.Impl.internalExceptions.MissingResourceException;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.helper.MrDocLoader;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.RelationshipTabResults;
import org.LexGrid.naming.SupportedAssociation;
import org.LexGrid.relations.Relations;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.lexgrid.loader.meta.constants.MetaLoaderConstants;
import org.lexgrid.loader.rrf.constants.RrfLoaderConstants;

public class MetaBrowserServiceImpl extends AbstractExtendable implements MetaBrowserService {

	/**
	 * 
	 */
	
	private static String CODING_SCHEME_NAME = "NCI MetaThesaurus";
	
	private static final long serialVersionUID = 1L;
	private static String SOURCE_QUAL_COL = "sourceQualifier";
	private static String RELA_QUAL_COL = "relaQualifier";
	private static String REL_COL = "rel";
	private static String SOURCE_CODE_QUAL_COL = "sourceCodeQualifier";
	private static String SOURCE_QUAL_VALUE = MetaLoaderConstants.SOURCE_QUALIFIER;
	private static String SOURCE_CODE_QUAL_VALUE = MetaLoaderConstants.SOURCE_CODE_QUALIFIER;
	private static String RELA_QUAL_VALUE = RrfLoaderConstants.RELA_QUALIFIER;
	private static String AUI_TARGET_QUAL_VALUE = MetaLoaderConstants.TARGET_AUI_QUALIFIER;
	private static String AUI_SOURCE_QUAL_VALUE = MetaLoaderConstants.SOURCE_AUI_QUALIFIER;
	private static String AUI_QUAL_VALUE = RrfLoaderConstants.AUI_QUALIFIER;
	private static String ROOT = "@";
	private static String TAIL = "@@";
	
	private String internalName;
	private String internalVersion;
	
	private int maxToReturn;
	
	private List<String> associations = new ArrayList<String>();
	
	private Map<String,String> associationReverseNames = new HashMap<String,String>();
	private Map<String,String> relaReverseNames = new HashMap<String,String>();
	
	private SQLInterface sqlInterface;

	public static void main(String[] args) throws Exception {
		/*
		MetaBrowserService impl = 
			(MetaBrowserService)LexBIGServiceImpl.defaultInstance().getGenericExtension("metabrowser-extension");
		*/
		
		MetaBrowserService impl = new MetaBrowserServiceImpl();
		//impl.initExtension();
	/*
		Map<String, List<RelationshipTabResults>> cl = impl.getRelationshipsDisplay("C0012634", "NDRFT", Direction.TARGETOF);
		
		for(String key : cl.keySet()){
			System.out.println(key);
			for(RelationshipTabResults ac : cl.get(key)){
				System.out.println("--" + ac.getCui());
				System.out.println("  --Name: " + ac.getName());
				System.out.println("  --Rel: " + ac.getRel());
				System.out.println("  --Rela: " + ac.getRela());
				System.out.println("  --Source: " + ac.getSource());
			}
		}
		
		
	*/
		//System.out.println(impl.getMaxToReturn());
		System.out.println("COUNT: " + impl.getCount("C0000726", "MSH", null, Direction.TARGETOF));
		
		Map<String, List<BySourceTabResults>> bysource = impl.getBySourceTabDisplay("C0000726", "MSH", null, Direction.TARGETOF);
		
		int count = 0;
		for(String key : bysource.keySet()){
			//System.out.println(key);
			for(BySourceTabResults ac : bysource.get(key)){
				count++;
				System.out.println("--" + ac.getCui());
				System.out.println("  --Name: " + ac.getTerm());
				System.out.println("  --Rel: " + ac.getRel());
				System.out.println("  --Rela: " + ac.getRela());
				System.out.println("  --Source: " + ac.getSource());
				System.out.println("  --Type: " + ac.getType());
				System.out.println("  --Code: " + ac.getCode());
			}
		}
		System.out.println("Actual Count: " + count);
		//System.out.println(impl.getCount("C0012634", null, Direction.TARGETOF));
	}

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
		 
		 MrDocLoader mrdocLoader = new MrDocLoader();
		 relaReverseNames = mrdocLoader.getRelasAndReverseRelas();
		 
		 maxToReturn = rm.getSystemVariables().getMaxResultSize();
		 
		 try {
			this.associations = buildAssociationList();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		sqlInterface = this.getSqlInterface();
		
		} catch (Throwable e1) {
			throw new LBException(e1.toString());
		}
	}
	
	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(String cui,
			String source, List<String> relationships, Direction direction) throws LBException {	
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
					buildGetBySourceDisplaySql(source, direction, relationships));
			
			getRelations.setString(1, cui);
			
			if(source != null){
				getRelations.setString(2, source);
			}

			System.out.println(getRelations);
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

	public Map<String, List<RelationshipTabResults>> getRelationshipsDisplay(String cui, 
			List<String> relationships, 
			Direction direction) throws LBException {
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
					buildGetRelationshipsDisplaySql(direction, relationships));
			
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
	
	private String reverseRel(String rel){
		return associationReverseNames.get(rel);
	}
	
	private String reverseRela(String rela){
		return relaReverseNames.get(rela);
	}
	
	private SQLInterface getSqlInterface() throws RuntimeException{
		try {
			return ResourceManager.instance().getSQLInterface(internalName,
			        internalVersion);
		} catch (MissingResourceException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String buildGetRelationshipsCountSql(Direction direction, List<String> relations) {
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
		
		return sb.toString();
	}
	
	private String buildBySourceCountSql(Direction direction, List<String> relations) {
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
	             " sourceQual." + SQLTableConstants.TBLCOL_QUALIFIERVALUE + " = ? " +
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
		
		return sb.toString();
	}
	
	private String buildGetRelationshipsDisplaySql(Direction direction, List<String> relations) {
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
				
		return sb.toString();
	}
	
	private String buildGetBySourceDisplaySql(String source, Direction direction, List<String> relations) {
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
                 " = entityPropertyMultiAttrib. " + SQLTableConstants.TBLCOL_ENTITYCODENAMESPACE +
                 " AND " + 
                 " entityProperty." + si.getSQLTableConstants().entityCodeOrId + 
                 " = entityPropertyMultiAttrib. " + si.getSQLTableConstants().entityCodeOrId +
                 " AND " + 
                 " entityProperty." + SQLTableConstants.TBLCOL_CODINGSCHEMENAME + 
                 " = entityPropertyMultiAttrib. " + SQLTableConstants.TBLCOL_CODINGSCHEMENAME +
                 " AND " + 
                 " entityProperty." + SQLTableConstants.TBLCOL_PROPERTYID +
                 " = entityPropertyMultiAttrib. " + SQLTableConstants.TBLCOL_PROPERTYID + " )" +
                  
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
						"assocSourceQual." + SQLTableConstants.TBLCOL_QUALIFIERVALUE + " = ? ");
				}

		return sb.toString();
	}
	
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

	public int getCount(String cui,
			List<String> relationships, Direction direction) throws LBException {
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}

		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildGetRelationshipsCountSql(direction, relationships));
			
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
	
	public int getCount(String cui, String source,
			List<String> relationships, Direction direction) throws LBException {
		initExtension();
		
		PreparedStatement getRelations = null;
		ResultSet rs = null;

		if(relationships == null){
			relationships = this.associations;
		}

		try {
			getRelations = sqlInterface.modifyAndCheckOutPreparedStatement(
					buildBySourceCountSql(direction, relationships));
			
			getRelations.setString(1, cui);
			getRelations.setString(2, source);
			
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

	public int getMaxToReturn() throws LBException {
		initExtension();
		return this.maxToReturn;
	}
	
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
}
