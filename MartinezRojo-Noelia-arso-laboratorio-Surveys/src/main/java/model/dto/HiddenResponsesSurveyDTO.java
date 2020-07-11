package model.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import model.Survey;
import model.Visibility;

public class HiddenResponsesSurveyDTO {
	
	private static final String MY_TIME_ZONE = "Europe/Madrid";
	
	private String id;
	private String creator;
	private String title;
	private String instructions;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm", timezone = MY_TIME_ZONE)
	private Date starts, ends;
	private int minOptions, maxOptions;
	private Visibility visibility;
	private List<String> options;
	
	public HiddenResponsesSurveyDTO() {}

	public HiddenResponsesSurveyDTO(Survey s) {
		this.id = s.getId();
		this.creator = s.getCreator();
		this.title = s.getTitle();
		this.instructions = s.getInstructions();
		this.starts = s.getStarts();
		this.ends = s.getEnds();
		this.minOptions = s.getMinOptions();
		this.maxOptions = s.getMaxOptions();
		this.visibility = s.getVisibility();
		this.options = s.getOptions();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public Date getStarts() {
		return starts;
	}

	public void setStarts(Date starts) {
		this.starts = starts;
	}

	public Date getEnds() {
		return ends;
	}

	public void setEnds(Date ends) {
		this.ends = ends;
	}

	public int getMinOptions() {
		return minOptions;
	}

	public void setMinOptions(int minOptions) {
		this.minOptions = minOptions;
	}

	public int getMaxOptions() {
		return maxOptions;
	}

	public void setMaxOptions(int maxOptions) {
		this.maxOptions = maxOptions;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}
}
