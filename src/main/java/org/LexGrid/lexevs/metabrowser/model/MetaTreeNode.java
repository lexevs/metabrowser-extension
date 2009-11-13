package org.LexGrid.lexevs.metabrowser.model;

import java.io.Serializable;
import java.util.List;

public class MetaTreeNode implements Serializable {

	private static final long serialVersionUID = 9090573514921671435L;
	public enum ExpandedState {EXPANDED, EXPANDABLE, LEAF, EXPANDABLE_PARENT, UNKNOWN}
	
	private String cui;
	private String name;
	private List<MetaTreeNode> parents;
	private List<MetaTreeNode> children;
	private ExpandedState expandedState = ExpandedState.UNKNOWN;
	
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<MetaTreeNode> getChildren() {
		return children;
	}
	public void setChildren(List<MetaTreeNode> children) {
		this.children = children;
	}
	public List<MetaTreeNode> getParents() {
		return parents;
	}
	public void setParents(List<MetaTreeNode> parents) {
		this.parents = parents;
	}
	public ExpandedState getExpandedState() {
		return expandedState;
	}
	public void setExpandedState(ExpandedState expandedState) {
		this.expandedState = expandedState;
	}
}
