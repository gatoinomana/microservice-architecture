package model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Survey {
	
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
	private Map<String, Integer> results;
	
	public Survey() {}
	
	public Survey(String id, String creator, String title, String instructions, Date starts, Date ends,
			int minOptions, int maxOptions, Visibility visibility, List<String> options, Map<String, Integer> results) {
		this.id = id;
		this.creator = creator;
		this.title = title;
		this.instructions = instructions;
		this.starts = starts;
		this.ends = ends;
		this.minOptions = minOptions;
		this.maxOptions = maxOptions;
		this.visibility = visibility;
		this.options = options;
		this.results = results;
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

	public Map<String, Integer> getResults() {
		return results;
	}

	public void setResults(Map<String, Integer> results) {
		this.results = results;
	}
	
	// TODO: Operacion modificar porcentaje de una opcion


	
}
