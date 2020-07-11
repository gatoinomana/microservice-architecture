package controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import model.CreateTaskEvent;
import model.Survey;
import persistence.SurveyRepository;
import rest.ForbiddenException;
import services.MsgQueueService;
import services.UsersService;

public class SurveyController {
	
	private static SurveyController instance;
	private SurveyRepository surveyRepository;
	private MsgQueueService queueService;
	private static UsersService usersService;

	public static SurveyController getInstance() throws SurveyException {
		if (instance == null) {
			instance = new SurveyController();
		}
		return instance;
	}
	
	private SurveyController() throws SurveyException {
		
		// Get survey repository
		surveyRepository = SurveyRepository.getInstance();
		
		// Get message queue service 
		queueService = MsgQueueService.getInstance();
		
		// Get users service
    	usersService = UsersService.getInstance();
	}
	
	public String createSurvey(Survey survey, String userId) throws SurveyException, ForbiddenException, IllegalArgumentException, IOException {

		// Check authorization
		if (!usersService.isTeacher(userId)) {
			throw new ForbiddenException(
					"Only teachers can create surveys");
		}
		
		if (!survey.getCreator().equals(userId)) {
			throw new ForbiddenException(
					"The 'userId' parameter must match the survey's 'creator'");	
		}
		
		// Remove duplicate options
		List<String> options = survey.getOptions();
		survey.setOptions(new ArrayList<>(new HashSet<>(options)));
		
		// Check preconditions
		if (!isValid(survey)) {
			throw new IllegalArgumentException(
					"All the arguments must be valid");
		}
		
		// Check no other active or future survey by the same teacher has same title
		boolean duplicated = surveyRepository.getAllSurveys()
				.stream().filter(other -> other.getCreator().equals(survey.getCreator()) &&
						other.getEnds().after(new Date()))
				.anyMatch(other -> other.getTitle().equals(survey.getTitle()));
		
		if (duplicated) {
			throw new ForbiddenException(
					"The same teacher cannot have two surveys with the same title");
		}
			
		// Save in database
		String id = surveyRepository.save(survey).getId();
		
		// Publish serialized event
		CreateTaskEvent event = new CreateTaskEvent(
				"New survey", survey.getEnds(), id);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    mapper.setDateFormat(new StdDateFormat());
		String jsonString = "";
		
		try {
			jsonString = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new SurveyException("Couldn't serialize date");
		} 
		
		queueService.publishMessage(jsonString);
			
		return id;
	}
	
	public Survey getSurvey(String surveyId) {
		return surveyRepository.findById(surveyId);
	}

	public Survey getSurvey(String surveyId, String userId) throws IOException, ForbiddenException {
		
		Survey survey = surveyRepository.findById(surveyId);
		
		// Teachers can always see their own survey's results
		if (usersService.isTeacher(userId)) {
			
			if (!survey.getCreator().equals(userId)) {
				throw new ForbiddenException(
						"Teachers can only see their own surveys");
			}
			
			return survey;
			
		}
		
		// Students may or may not be able to see the survey's results
		// (check visibility)
		
		//TODO
		
		return surveyRepository.findById(surveyId);
	}

	public List<Survey> getAllSurveys(String userId) throws SurveyException, IOException, ForbiddenException {
		
		// Teachers can only see their own active or future surveys
		if (usersService.isTeacher(userId)) {
			return surveyRepository.getAllSurveys()
					.stream().filter(s -> s.getCreator().equals(userId))
					.collect(Collectors.toList());
		}
		
		// Students can see all active or future surveys
		else if (usersService.isStudent(userId)) {
			return surveyRepository.getAllSurveys();
		}
		
		// Non registered teachers or students cannot see surveys
		else {
			throw new ForbiddenException(
					"Only registered teachers or students can see surveys");
		}
	}

	public void removeSurvey(String surveyId, String userId) throws SurveyException, ForbiddenException {
		
		Survey survey = surveyRepository.findById(surveyId);
		
		if (!survey.getCreator().equals(userId)) {
			throw new ForbiddenException(
					"Only teachers can remove surveys (their own)");
		}
		
		if (!survey.getEnds().after(new Date())) {
			throw new ForbiddenException(
					"Past surveys cannot be removed");
		}
		
		surveyRepository.remove(surveyId);
	}
	
	public Survey editSurvey(String surveyId, String userId, JsonPatch jsonPatch) throws SurveyException, ForbiddenException, IOException {
		
		Survey survey = surveyRepository.findById(surveyId);

		// Check it is a future survey 
		if (!survey.getStarts().after(new Date())) {
			throw new ForbiddenException(
					"Only future surveys can be edited");
		}
		
		// Check authorization
		if (!survey.getCreator().equals(userId)) {
			throw new ForbiddenException(
					"Only teachers can edit surveys (their own)");
		}
		
		// Convert Survey to JsonStructure, in order to apply JsonPatch
		
		// First convert to JSON string
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    mapper.setDateFormat(new StdDateFormat());
	    
		String surveyAsJsonString = "";
		
		try {
			surveyAsJsonString = mapper.writeValueAsString(survey);
		} catch (JsonProcessingException e) {
			throw new SurveyException("Couldn't serialize date");
		} 
		
		// Then from JSON string to JsonStructure
		JsonReader reader = Json.createReader(new StringReader(surveyAsJsonString));
		JsonStructure surveyAsJsonStructure = reader.read();
		
		// Apply patch
		JsonStructure patchedSurveyAsJson = jsonPatch.apply(surveyAsJsonStructure);
		
		// Convert patched JsonStructure back to Survey
		Survey patchedSurvey = null;
		
		try {
			patchedSurvey = mapper.readValue(patchedSurveyAsJson.toString(), Survey.class);
		} catch (Exception e) {
			throw new SurveyException("Couldn't deserialize patched survey");
		}

		// Check patched survey satisfies preconditions
		if (!isValid(patchedSurvey)) {
			throw new IllegalArgumentException(
					"All the arguments must be valid");
		}
		
		// Update survey in database
		surveyRepository.editSurvey(surveyId, patchedSurvey);
		
		return patchedSurvey;	
	}

	public boolean respondSurvey(String surveyId, 
			Map<String, Boolean> responses) throws SurveyException {
		return surveyRepository.updateResults(surveyId, responses);
	}
	
	// Data validation for surveys
	private boolean isValid(Survey survey) throws IllegalArgumentException {
		
		if (survey.getResults() != null && 
				survey.getResults().size() > 0) {
			throw new IllegalArgumentException(
					"'results' cannot be set by the user");
		}
		
		if (survey.getTitle() == null || 
				survey.getTitle().isEmpty()) {
			throw new IllegalArgumentException(
					"'title' can't be empty");
		}
		
		if (survey.getCreator() == null ||
				survey.getTitle().isEmpty()) {
			throw new IllegalArgumentException(
					"'creator' can't be empty");
		}
		
		if (survey.getStarts() == null) {
			throw new IllegalArgumentException(
					"'starts' can't be null");
		}
		
		if (survey.getEnds() == null) {
			throw new IllegalArgumentException(
					"'ends' can't be null");
		}
		
		if (survey.getVisibility() == null) {
			throw new IllegalArgumentException(
					"'visibility' can't be null");
		}
		
		if (survey.getEnds().before(survey.getStarts())) {
			throw new IllegalArgumentException(
					"End date must come after start date");
		}
		
		if (survey.getStarts().before(new Date())) {
			throw new IllegalArgumentException(
					"Survey must start in the future");
		}
		
		if (survey.getMinOptions() <= 0) {
			throw new IllegalArgumentException(
					"'minOptions' must be a positive number");
		}
		
		if (survey.getMaxOptions() < survey.getMinOptions()) {
			throw new IllegalArgumentException(
					"'maxOptions' cannot be less than 'minOptions'");
		}
		
		if (survey.getOptions().size() < 2) {
			throw new IllegalArgumentException(
					"Surveys must include at least 2 different options");
		}

		return true;
	}

}
