package org.LexGrid.lexevs.metabrowser.impl;

import static org.junit.Assert.assertTrue;

import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaTree;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode.ExpandedState;
import org.junit.Test;

public class GetMetaTreeTest {

	private MetaBrowserService impl = new MetaBrowserServiceImpl();
	
	@Test
	public void testGetBySouceTabDisplaySourceOfExcludeSelfReferencingTrue() throws Exception {
		//System.setProperty("LG_CONFIG_FILE", "src/test/resources/config/lbconfig.props");
		MetaBrowserService impl = new MetaBrowserServiceImpl();

		MetaTree tree = impl.getMetaNeighborhood("AIR");
		printMetaTreeNode(tree.getCurrentFocus(), 0);
		
		tree = tree.focusMetaTreeNode("C0221566");
		
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		
		//printMetaTreeNode(tree.getCurrentFocus(), 0);
		
		tree = tree.focusMetaTreeNode("C0027853");
		
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
		
		//printMetaTreeNode(tree.getCurrentFocus(), 0);
		tree = impl.getMetaNeighborhood("AIR");
		
	}
	
	private void printMetaTreeNode(MetaTreeNode node, int level) throws Exception {
		System.out.println(getLevelIndent(level) + "CUI: " + node.getCui());
		System.out.println(getLevelIndent(level) + "Name: " + node.getName());
		System.out.println(getLevelIndent(level) + "Exapandable: " + node.getExpandedState());
		
		System.out.println(getLevelIndent(level) + "Children: ");
		if(node.getChildren() != null){
			for(MetaTreeNode child : node.getChildren()){
				/*
				if(child.getExpandedState().equals(EXPANDED_STATE.EXPANDABLE)){
					child = impl.focusMetaTreeNode(child);
				}
				*/
				printMetaTreeNode(child, level + 1);
			}
		}
	/*
		System.out.println(getLevelIndent(level) + "Parents: ");
		if(node.getParents() != null){
			for(MetaTreeNode parent : node.getParents()){
				printMetaTreeNode(parent, level + 1);
			}
		}
	*/
	}
	
	private String getLevelIndent(int level){
		String indent = "";
		for(int i=0;i<level;i++){
			indent = indent + " - ";
		}
		return indent;
	}
}
