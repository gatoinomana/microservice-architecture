package model;

import java.util.List;

public class SurveyResponse {
	
	private String id;
	private String student;
	private List<String> selectedOptions;
	
	public SurveyResponse() {}
	
	public SurveyResponse(String id, String userId, List<String> selectedOptions) {
		this.id = id;
		this.student = userId;
		this.selectedOptions = selectedOptions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStudent() {
		return student;
	}
	
	public void setStudent(String student) {
		this.student = student;
	}
	
	public List<String> getSelectedOptions() {
		return selectedOptions;
	}

	public void setSelectedOptions(List<String> selectedOptions) {
		this.selectedOptions = selectedOptions;
	}
}
