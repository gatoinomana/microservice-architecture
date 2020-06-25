package rest;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import model.Survey;

public class JSONUtils {
	
	public static String jsonFromSurveys(List<Survey> surveys) {
		
	    JsonArrayBuilder array = Json.createArrayBuilder();
	    for (Survey survey : surveys) {
	        JsonObjectBuilder object = Json.createObjectBuilder();
	        array.add(object.add("title", survey.getTitle()).build());
	    }
		
		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		
		JsonObject value = factory.createObjectBuilder()
				.add("_links", factory.createObjectBuilder()
				         .add("self", factory.createObjectBuilder()
				        		 .add("href", "http://localhost:8081/api/surveys")))
				.add("total", surveys.size())
				.add("embedded", factory.createObjectBuilder()
				         .add("surveys", array))
				.build();
		
		return value.toString();
	}
}
