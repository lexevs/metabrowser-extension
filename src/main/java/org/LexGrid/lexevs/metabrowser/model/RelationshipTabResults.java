package org.LexGrid.lexevs.metabrowser.model;

import java.io.Serializable;

public class RelationshipTabResults implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String cui;
	private String rel;
	private String rela;
	private String name;
	private String source;
	
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}
	public String getRela() {
		return rela;
	}
	public void setRela(String rela) {
		this.rela = rela;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
