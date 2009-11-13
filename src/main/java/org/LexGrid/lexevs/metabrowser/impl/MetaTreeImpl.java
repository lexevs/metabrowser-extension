package org.LexGrid.lexevs.metabrowser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaTree;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode;
import org.LexGrid.lexevs.metabrowser.model.MetaTreeNode.ExpandedState;

@org.LexGrid.annotations.LgHasRemoteDependencies
public class MetaTreeImpl implements MetaTree {

	private static final long serialVersionUID = -143953173771075028L;

	private Map<String,MetaTreeNode> treeNodes = new HashMap<String,MetaTreeNode>();
	
	private String CHD_REL = MetaBrowserServiceImpl.CHD_REL;
	private String PAR_REL = MetaBrowserServiceImpl.PAR_REL;
	private static String CODING_SCHEME_NAME = MetaBrowserServiceImpl.CODING_SCHEME_NAME;
	

	@org.LexGrid.annotations.LgProxyField
	private transient MetaBrowserService service;
	
	private String source;
	
	private MetaTreeNode currentFocus;
	
	public MetaTreeImpl();
	
	public MetaTreeImpl(MetaBrowserService service, String source) throws LBException {
		this.source = source;
		this.service = service;
		currentFocus = setInitialFocus();
	}
	
	public MetaTreeImpl(MetaBrowserService service, String focus, String source, int levels) throws LBException {
		this.source = source;
		this.service = service;
		currentFocus = setInitialFocus(focus, levels);
	}

	protected MetaTreeNode focusMetaTreeNode(MetaTreeNode treeNode) throws LBException{
		ExpandedState state = treeNode.getExpandedState();
		if(state.equals(ExpandedState.EXPANDED)){
			System.out.println("Already Expanded");
			return treeNode;
		} 
		
		if(state.equals(ExpandedState.EXPANDABLE)){
			treeNode.setChildren(
					getChildren(treeNode));
		} else if(state.equals(ExpandedState.EXPANDABLE_PARENT)){
			List<MetaTreeNode> foundChildren = getChildren(treeNode);
			List<MetaTreeNode> alreadyAddedChildren = treeNode.getChildren();
			
			List<String> cuis = getCuiList(alreadyAddedChildren);
			for(MetaTreeNode child : foundChildren){
				if(!cuis.contains(child.getCui())){
					treeNode.getChildren().add(child);
				}
			}
		} else {
			throw new LBException("Cannot focus on a node that is in state: " + state);
		}
		
		treeNode.setExpandedState(ExpandedState.EXPANDED);
		
		return treeNode;
	}
	
	private List<MetaTreeNode> getChildren(MetaTreeNode focus) throws LBException{
		List<MetaTreeNode> returnList = new ArrayList<MetaTreeNode>();
		
		List<String> rels = new ArrayList<String>();
		rels.add(CHD_REL);
		
		Map<String,List<BySourceTabResults>> results = service.getBySourceTabDisplay(focus.getCui(), source, rels, Direction.SOURCEOF);
		List<BySourceTabResults> relations = results.get(CHD_REL);

		for(BySourceTabResults result : relations){
			MetaTreeNode foundNode = buildMetaTreeNode(result);
			
			List<MetaTreeNode> parent = new ArrayList<MetaTreeNode>();
			parent.add(focus);
			foundNode.setParents(parent);
			int children = service.getCount(foundNode.getCui(), source, rels, Direction.SOURCEOF);
			
			if(children > 0){
				foundNode.setExpandedState(ExpandedState.EXPANDABLE);
			} else {
				foundNode.setExpandedState(ExpandedState.LEAF);
			}
			returnList.add(foundNode);
		}
		if(returnList.size() == 0){
			return null;
		}
		return returnList;
	}
	
	private List<MetaTreeNode> getParents(MetaTreeNode focus, int level) throws LBException{
		List<MetaTreeNode> returnList = new ArrayList<MetaTreeNode>();
		
		List<String> rels = new ArrayList<String>();
		rels.add(CHD_REL);
		
		Map<String,List<BySourceTabResults>> results = service.getBySourceTabDisplay(focus.getCui(), source, rels, Direction.TARGETOF);
		List<BySourceTabResults> relations = results.get(PAR_REL);
		
		for(BySourceTabResults result : relations){
			MetaTreeNode foundNode = buildMetaTreeNode(result);
			foundNode.setExpandedState(ExpandedState.EXPANDABLE_PARENT);
			if(level > 0 || level < 0){
				foundNode.setParents(
						getParents(foundNode, level - 1));
			}
			List<MetaTreeNode> childList = new ArrayList<MetaTreeNode>();
			childList.add(focus);
			
			List<MetaTreeNode> children = this.getChildren(foundNode);
			if(children != null){
				for(MetaTreeNode child : children){
					if(child.getCui().equals(focus.getCui())){
						childList.add(child);
					}
				}
			}
			foundNode.setChildren(childList);
			returnList.add(foundNode);
		}
		if(returnList.size() == 0){
			return null;
		}
		return returnList;
	}
	
	private MetaTreeNode buildMetaTreeNode(String cui, String term){
		MetaTreeNode node = new MetaTreeNode();
		node.setCui(cui);
		node.setName(term);
		registerMetaTreeNode(node);
		return node;
	}
	
	private MetaTreeNode buildMetaTreeNode(BySourceTabResults tab){
		return 
			this.buildMetaTreeNode(
				tab.getCui(), tab.getTerm());
	}
	
	private void registerMetaTreeNode(MetaTreeNode node){
		this.treeNodes.put(node.getCui(), node);
	}
	
	private MetaTreeNode getRegisteredMetaTreeNode(String cui){
		return this.treeNodes.get(cui);
	}
	
	/**
	 * Gets the UMLS root node of a given SAB.
	 *
	 * @param sab
	 * @return
	 * @throws LBException
	 */
	protected ResolvedConceptReference getCodingSchemeRoot(String sab) throws LBException {
		CodedNodeSet cns = service.getLexBIGService().getNodeSet(CODING_SCHEME_NAME, null, null);

		cns = cns.restrictToProperties(null, new PropertyType[] {PropertyType.PRESENTATION}, Constructors.createLocalNameList("SRC"), null, Constructors.createNameAndValueList("source-code", "V-"+sab));
		ResolvedConceptReference[] refs = cns.resolveToList(null, null, null, null, false, -1).getResolvedConceptReference();

		if(refs.length > 1){
			throw new LBException("Found more than one Root for SAB: " + sab);
		}
		if(refs.length == 0){
			throw new LBException("Didn't find a Root for SAB: " + sab);
		}
		return refs[0];
	}
	
	
	private List<String> getCuiList(List<MetaTreeNode> nodes){
		List<String> returnList = new ArrayList<String>();
		for(MetaTreeNode node : nodes){
			returnList.add(node.getCui());
		}
		return returnList;
	}
	
	private MetaTreeNode setInitialFocus(String cui, int levelsParents) throws LBException {	
		MetaTreeNode focusNode = this.getFocusDetails(cui);

		focusNode.setParents(getParents(focusNode, levelsParents));
		List<MetaTreeNode> children = getChildren(focusNode);

		if(children != null){
			focusNode.setExpandedState(ExpandedState.EXPANDED);
		} else {
			focusNode.setExpandedState(ExpandedState.LEAF);
		}
		focusNode.setChildren(children);

		List<MetaTreeNode> parents = focusNode.getParents();
		if(parents != null){
			for(MetaTreeNode node : parents){
				node = this.focusMetaTreeNode(node);
			}
		}
		return focusNode;
	}
	private MetaTreeNode setInitialFocus() throws LBException {	
		ResolvedConceptReference ref = getCodingSchemeRoot(source);
		MetaTreeNode focusNode = 
			this.buildMetaTreeNode(
					ref.getCode(), 
					ref.getEntityDescription().getContent());	

		focusNode.setParents(getParents(focusNode, 0));
		List<MetaTreeNode> children = getChildren(focusNode);

		if(children != null){
			focusNode.setExpandedState(ExpandedState.EXPANDED);
		} else {
			focusNode.setExpandedState(ExpandedState.LEAF);
		}
		focusNode.setChildren(children);

		return focusNode;	
	}

	public MetaTree focusMetaTreeNode(String cui) throws LBException {
		MetaTreeNode node = this.getRegisteredMetaTreeNode(cui);
		this.currentFocus = this.focusMetaTreeNode(node);
		return this;
	}

	public MetaTreeNode getCurrentFocus() {
		return this.currentFocus;
	}

	public ExpandedState getExpandedState(String cui) {
		MetaTreeNode node = this.getRegisteredMetaTreeNode(cui);
		if(node == null){
			return ExpandedState.UNKNOWN;
		} else {
			return node.getExpandedState();
		}
	}
	
	private MetaTreeNode getFocusDetails(String cui) throws LBException {
		CodedNodeSet cns = LexBIGServiceImpl.defaultInstance().getCodingSchemeConcepts(CODING_SCHEME_NAME, null);
		cns = cns.restrictToCodes(Constructors.createConceptReferenceList(cui));
		ResolvedConceptReference ref = cns.resolveToList(null, null, new PropertyType[]{PropertyType.PRESENTATION}, 1).getResolvedConceptReference(0);
		return buildMetaTreeNode(cui, 
				getBestSabTermString(
						ref.getReferencedEntry()));	
	}
	
	private String getBestSabTermString(Entity entity){
		Presentation bestPres = null;
		for(Presentation pres : entity.getPresentation()){
			for(Source source : pres.getSource()){
				if(source.equals(source)){
					if(bestPres == null){
						bestPres = pres;
					} else {
						if(pres.getRepresentationalForm().equals("PT")){
							bestPres = pres;
						}
					}
				}
			}
		}
		if(bestPres == null){
			return entity.getEntityDescription().getContent();
		}
		return bestPres.getValue().getContent();
	}
	
	public void setService(MetaBrowserService service) {
		this.service = service;
	}
}
