package org.LexGrid.lexevs.metabrowser;

import java.io.Serializable;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode.ExpandedState;

public interface MetaTree extends Serializable {
		
	public MetaTree focusMetaTreeNode(String cui) throws LBException;
	
	public MetaTreeNode getCurrentFocus();
	
	public ExpandedState getExpandedState(String cui);
}
