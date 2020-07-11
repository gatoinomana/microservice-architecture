package model.events;

import java.util.Date;

public class CreateTaskEvent {
	
	private String description;
	private Date deadline;
	private String id;
	private String service;

	public CreateTaskEvent(String description, Date deadline, String id) {
		this.description = description;
		this.deadline = deadline;
		this.id = id;
		this.service = "Surveys";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
}
