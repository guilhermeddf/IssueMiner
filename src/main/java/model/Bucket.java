package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bucket {
	private Date inicio;
	private Date fim;
	private List<Issue> issues = new ArrayList<>();
	
	public Date getInicio() {
		return inicio;
	}
	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}
	public Date getFim() {
		return fim;
	}
	public void setFim(Date fim) {
		this.fim = fim;
	}
	public List<Issue> getIssues() {
		return issues;
	}
	public void setIssues(List<Issue> issues) {
		this.issues = issues;
	}
	public void addIssue(Issue issue) {
		getIssues().add(issue);
	}	
}
