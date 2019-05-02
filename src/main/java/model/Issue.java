package model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class Issue {

	private Date dataInicio;
	private Date dataFim;
	private JSONObject source;
	
	public Issue(JSONObject source) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
		Date parsedCreationDate = null;
		Date parsedEndDate = null;
		try {
			String creationDate = source.getString("creationDate");
			parsedCreationDate = formatter.parse(creationDate);
			
			String endDate = source.getString("closeDate");
			parsedEndDate = formatter.parse(endDate);
		} catch(Exception e) {
			
		}
		setDataInicio(parsedCreationDate);
		setDataFim(parsedEndDate);
		
		setSource(source);
		
	}
	
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public JSONObject getSource() {
		return source;
	}

	public void setSource(JSONObject source) {
		this.source = source;
	}
	
	public String getKey() {
		return (String) getValue("key");
	}
	
	public String getSeverity() {
		return (String) getValue("severity");
	}
	
	public String getComponent() {
		return (String) getValue("component");
	}
	
	public String getSubProject() {
		return (String) getValue("subProject");
	}
	
	public String getStatus() {
		return (String) getValue("status");
	}
	
	public String getMessage() {
		return (String) getValue("message");
	}
	
	public String getEffort() {
		return (String) getValue("effort");
	}
	
	public String getDebt() {
		return (String) getValue("debt");
	}
	
	public String getAuthor() {
		return (String) getValue("author");
	}
	
	public String getType() {
		return (String) getValue("type");
	}
	
	private Object getValue(String key) {
		try {
			return getSource().get(key);
		} catch (Exception e) {
			return null;
		}
	}
}
