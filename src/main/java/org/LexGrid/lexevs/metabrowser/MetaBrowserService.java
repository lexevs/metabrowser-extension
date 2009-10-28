package org.LexGrid.lexevs.metabrowser;

import java.util.List;
import java.util.Map;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Extensions.Generic.GenericExtension;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.RelationshipTabResults;

public interface MetaBrowserService extends GenericExtension {
	
	public enum Direction {SOURCEOF, TARGETOF};

	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(String cui, String source, List<String> relations, Direction direction) throws LBException;
	
	public Map<String, List<BySourceTabResults>> getBySourceTabDisplay(String cui, String source, List<String> relations, Direction direction, boolean excludeSelfReferencing) throws LBException;
	
	public Map<String, List<RelationshipTabResults>> getRelationshipsDisplay(String cui, List<String> relations, Direction direction) throws LBException;
	
	public Map<String, List<RelationshipTabResults>> getRelationshipsDisplay(String cui, List<String> relations, Direction direction, boolean excludeSelfReferencing) throws LBException;
	
	public int getCount(String cui, List<String> relations, Direction direction) throws LBException;
	
	public int getCount(String cui, List<String> relations, Direction direction, boolean excludeSelfReferencing) throws LBException;
	
	public int getCount(String cui, String source, List<String> relations, Direction direction) throws LBException;

	public int getCount(String cui, String source, List<String> relations, Direction direction, boolean excludeSelfReferencing) throws LBException;
	
	public int getMaxToReturn() throws LBException;
}
