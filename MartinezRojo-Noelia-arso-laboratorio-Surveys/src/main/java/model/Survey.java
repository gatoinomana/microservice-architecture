package model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Survey {
	
	private String id;
	private String title;
	private String instructions;
	private Date starts, ends;
	private int minOptions, maxOptions;
	private Visibility visibility;
	private List<String> options;
	private Map<String, Integer> results;
	
	public Survey(String title, String instructions, Date starts, Date ends, 
			int minOptions, int maxOptions, Visibility visibility) {
		this.title = title;
		this.instructions = instructions;
		this.starts = starts;
		this.ends = ends;
		this.minOptions = minOptions;
		this.maxOptions = maxOptions;
		this.visibility = visibility;
		options = new LinkedList<String>();
		results = new HashMap<String, Integer>();
	}
	
	public Survey(String id, String title, String instructions, Date openingDateTime, 
			Date closingDateTime, int minOptions, int maxOptions, Visibility visibility,
			List<String> options, Map<String, Integer> results) {
		this(title, instructions, openingDateTime, closingDateTime, minOptions, maxOptions, visibility);
		this.id = id;
		this.options = options;
		this.results = results;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	
	public void addOption(String text) {
		options.add(text);
	}
	
	// TODO: da problemas?
	public void removeOption(String text) {
		for (String opt : options) {
			if (opt.equals(text)) {
				options.remove(opt);
			}
		}
	}

	// TODO: Operacion modificar porcentaje de una opcion
	
}
