package org.LexGrid.lexevs.metabrowser.model;

import java.io.Serializable;

public class BySourceTabResults implements Serializable {

	private static final long serialVersionUID = 1L;
	private String cui;
	private String rel;
	private String rela;
	private String term;
	private String source;
	private String type;
	private String code;
	
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
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
