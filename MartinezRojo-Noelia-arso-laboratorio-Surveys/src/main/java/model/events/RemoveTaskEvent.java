package model.events;

public class RemoveTaskEvent {
	
	private String studentId;
	private String id;
	private String service;

	public RemoveTaskEvent(String studentId, String id) {
		this.studentId = studentId;
		this.id = id;
		this.service = "Surveys";
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
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
