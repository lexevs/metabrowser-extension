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
package org.LexGrid.lexevs.metabrowser.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.impl.MetaBrowserServiceImpl;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode.ExpandedState;

/**
 * The Class ChildIterator.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class ChildIterator implements Iterator<MetaTreeNode>, Iterable<MetaTreeNode>, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -968792678746451575L;

	/** The page size. */
	private int pageSize = 10;
	
	/** The child list. */
	private List<MetaTreeNode> childList = new ArrayList<MetaTreeNode>();
	
	/** The children. */
	private int children;
	
	/** The position. */
	private int position = 0;
	
	/** The paged list position. */
	private int pagedListPosition = 0;
	
	/** The service. */
	private transient MetaBrowserService service;
	
	/** The cui. */
	private String cui;
	
	/** The source. */
	private String source;
	
	/** The relations. */
	private List<String> relations; 
	
	/** The direction. */
	private Direction direction;
	
	private MetaTreeNode parent;
	/**
	 * Instantiates a new child iterator.
	 */
	public ChildIterator(){
		super();
	}
	
	/**
	 * Instantiates a new child iterator.
	 * 
	 * @param cui the cui
	 * @param source the source
	 * @param relations the relations
	 * @param direction the direction
	 * @param service the service
	 * @param children the children
	 */
	public ChildIterator(
			String cui, 
			String source, 
			List<String> relations, 
			Direction direction,
			MetaBrowserService service,
			MetaTreeNode parent,
			int children){
		this.cui = cui;
		this.source = source;
		this.relations = relations;
		this.direction = direction;
		this.service = service;
		this.children = children;
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return position < children;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public MetaTreeNode next() {
		if(childList.size() == pagedListPosition){
			try {
				page();
			} catch (LBException e) {
				throw new RuntimeException(e);
			}
		} 
		MetaTreeNode node = childList.get(pagedListPosition);
		
		if(parent != null && node.getCui().equals(this.parent.getCui())){
			node = parent;
		}
		
		pagedListPosition++;
		this.position++;
		
		return node;
	}

	/**
	 * Page.
	 * 
	 * @throws LBException the LB exception
	 */
	protected void page() throws LBException{
		if(this.service == null){
			this.service = (MetaBrowserService) LexBIGServiceImpl.defaultInstance().getGenericExtension("metabrowser-extension");
		}
		Map<String, List<BySourceTabResults>> list = this.service.
		getBySourceTabDisplay(
				cui, 
				source, 
				relations, 
				direction, 
				true, 
				position, 
				pageSize);
		
		List<BySourceTabResults> bySourceChildResults = list.get(MetaBrowserServiceImpl.CHD_REL);
	
		this.childList = toMetaTreeNodeList(bySourceChildResults);
		this.pagedListPosition = 0;
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * To meta tree node list.
	 * 
	 * @param bySource the by source
	 * 
	 * @return the list< meta tree node>
	 */
	private List<MetaTreeNode> toMetaTreeNodeList(List<BySourceTabResults> bySource){
		List<MetaTreeNode> returnList = new ArrayList<MetaTreeNode>();
		for(BySourceTabResults tab : bySource){
			returnList.add(this.buildMetaTreeNode(tab));
		}
		return returnList;
	}
	
	/**
	 * Builds the meta tree node.
	 * 
	 * @param tab the tab
	 * 
	 * @return the meta tree node
	 */
	private MetaTreeNode buildMetaTreeNode(BySourceTabResults tab){
		MetaTreeNode node = new MetaTreeNode();
		node.setCui(tab.getCui());
		node.setName(tab.getTerm());
		try {
			int childCount = service.getCount(node.getCui(), source, relations, direction, true);
			
			node.setChildrenCount(childCount);
			
			if(childCount > 0){
				node.setExpandedState(ExpandedState.EXPANDABLE);
			} else {
				node.setExpandedState(ExpandedState.LEAF);
			}
	
		} catch (LBException e) {
			throw new RuntimeException(e);
		}
		return node;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<MetaTreeNode> iterator() {
		return this;
	}
}
