package controller;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import model.events.*;
import model.Survey;
import model.SurveyResponse;
import model.dto.HiddenResponsesSurveyDTO;
import persistence.SurveyRepository;
import rest.exceptions.*;
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
		
		if (!isValid(survey)) {
			throw new IllegalArgumentException(
					"All the arguments must be valid");
		}
		
		if (survey.getResponses() != null) {
			throw new IllegalArgumentException(
					"'responses' cannot be set on creation");
		}
		
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
		
		// Publish event (new survey)
		CreateTaskEvent event = new CreateTaskEvent(
				"New survey", survey.getEnds(), id);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    mapper.setDateFormat(new StdDateFormat());
		String jsonString = "";
		
		try {
			jsonString = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			throw new SurveyException("Couldn't serialize survey");
		} 
		
		queueService.publishMessage(jsonString);
			
		return id;
	}
	
	public Survey editSurvey(String surveyId, String userId, JsonPatch jsonPatch) throws SurveyException, ForbiddenException, IOException, ResourceNotFoundException {
		
		Survey survey = surveyRepository.getSurvey(surveyId);
		
		if (survey == null) {
			
			throw new ResourceNotFoundException(
					"The id does not belong to any existing survey");
		}

		if (!survey.getStarts().after(new Date())) {
			throw new ForbiddenException(
					"Only future surveys can be edited");
		}
		
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
			throw new SurveyException("Couldn't serialize survey");
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
			throw new IllegalArgumentException("JSON Patch was not valid. "
					+ "Couldn't deserialize patched survey");
		}
		
		// Check patched survey didn't change responses
		if (!patchedSurvey.getResponses().equals(survey.getResponses())) {
			throw new ForbiddenException(
					"Responses cannot be edited by the teacher");
		}

		// Check patched survey satisfies preconditions
		if (!isValid(patchedSurvey)) {
			throw new IllegalArgumentException(
					"All the arguments must be valid");
		}
		
		// Update survey in database
		surveyRepository.edit(surveyId, patchedSurvey);
		
		return patchedSurvey;	
	}
	
	public void removeSurvey(String surveyId, String userId) throws SurveyException, ForbiddenException, ResourceNotFoundException {
		
		Survey survey = surveyRepository.getSurvey(surveyId);
		
		if (survey == null) {
			
			throw new ResourceNotFoundException(
					"The id does not belong to any existing survey");
		}
		
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

	public List<HiddenResponsesSurveyDTO> getAllSurveys(String userId) throws IOException, ForbiddenException {
		
		List<HiddenResponsesSurveyDTO> surveys = 
				new ArrayList<HiddenResponsesSurveyDTO>();
		
		// Teachers can only see their own active or future surveys
		if (usersService.isTeacher(userId)) {
			
			surveyRepository.getAllSurveys().stream()
				.filter(s -> s.getCreator().equals(userId))
				.forEach(s -> surveys.add(new HiddenResponsesSurveyDTO(s)));
		}
		
		// Students can see all active or future surveys
		else if (usersService.isStudent(userId)) {
			
			surveyRepository.getAllSurveys().stream().forEach(
					s -> surveys.add(new HiddenResponsesSurveyDTO(s)));
		}
		
		// Non registered teachers or students cannot see surveys
		else {
			throw new ForbiddenException(
					"Only registered teachers or students can see surveys");
		}
		
		return surveys;
	}
	

	public HiddenResponsesSurveyDTO getSurvey(String surveyId, String userId) throws ResourceNotFoundException, ForbiddenException, IOException {
		
		Survey survey = surveyRepository.getSurvey(surveyId);
		
		if (survey == null) {
			
			throw new ResourceNotFoundException(
					"The id does not belong to any existing survey");
		}
		
		// Check authorization
		if (usersService.isTeacher(userId)) {
			
			if (!survey.getCreator().equals(userId)) {
				throw new ForbiddenException(
					"Teachers can only see their own surveys");
			}
		}
		else if (usersService.isStudent(userId)) {
			
			if (survey.getStarts().after(new Date())) {
				throw new ForbiddenException(
					"Students cannot see future surveys");
			}
		} 
		else {
			
			throw new ForbiddenException(
					"Only registered teachers or students can see surveys");
		}
		
		// Hide responses
		return new HiddenResponsesSurveyDTO(survey);
	}
	
	
	public void fillSurvey(String surveyId, String userId, SurveyResponse response) 
			throws ForbiddenException, IOException, ResourceNotFoundException {
		
		Survey survey = surveyRepository.getSurvey(surveyId);
		
		if (survey == null) {
			
			throw new ResourceNotFoundException(
					"The id does not belong to any existing survey");
		}
		
		// Check user can vote
		if (!usersService.isStudent(userId)) {
			
			throw new ForbiddenException(
					"Only students can fill surveys");
		}
		
		if (!userId.equals(response.getStudent())) {
			
			throw new ForbiddenException(
					"Trying to fill survey on behalf of another user");	
		}

		// Check survey is active
		if (survey.getEnds().before(new Date()) || 
				survey.getStarts().after(new Date())) {
			
			throw new ForbiddenException(
					"The survey is not active");
		}
		
		// Check student hasn't voted already
		boolean hasVoted = survey.getResponses().stream().anyMatch(
				r -> r.getStudent().equals(userId));
		if (hasVoted) {
			throw new ForbiddenException(
					"You can only fill the survey once");		
		}
	
		// Check options are valid
		if (!survey.getOptions().containsAll(response.getSelectedOptions())) {
			throw new IllegalArgumentException(
					"All options must exist");
		}
		
		if (survey.getMinOptions() > response.getSelectedOptions().size() ||
				survey.getMaxOptions() < response.getSelectedOptions().size()) {
			throw new IllegalArgumentException(
					"The number of selected options is invalid");
		}

		// Save to database
		surveyRepository.saveResponse(surveyId, response);
		
		// Publish event (filled survey)
		

	}
	
	public Map<String, Double> getResults(String surveyId, String userId) throws IOException, ForbiddenException, ResourceNotFoundException {
		
		Survey survey = surveyRepository.getSurvey(surveyId);
		
		if (survey == null) {
			
			throw new ResourceNotFoundException(
					"The id does not belong to any existing survey");
		}
		
		// Check user can see results
		
		// Teachers can only see their own surveys
		if (usersService.isTeacher(userId)) {
			
			if (!survey.getCreator().equals(userId)) {
				throw new ForbiddenException(
						"Teachers can only see their own surveys");
			}
		}	
		// Students can see results depending on survey's visibility
		else if (usersService.isStudent(userId)) {
			
			switch(survey.getVisibility()) {
				
				case NEVER:	
					if (!survey.getCreator().equals(userId)) {
						throw new ForbiddenException(
							"This survey's results are not visible for students");
					}
				
				case ONCE_CLOSED:
					if (survey.getEnds().after(new Date())) {
						throw new ForbiddenException(
							"This survey's results are not visible until it is closed");
					}
				
				case PARTICIPANTS_ONLY: {
					boolean hasParticipated = survey.getResponses().stream().anyMatch(
							response -> response.getStudent().equals(userId));
					
					if (!hasParticipated) {
						throw new ForbiddenException(
							"This survey's results are only visible for participants");
					}
				}
				
				default: break;
			}
		}
		else {
			throw new ForbiddenException(
				"Only registered teachers or students can see surveys");
		}
		
		// Get all responses
		List<SurveyResponse> responses = survey.getResponses();
		
		// Calculate % of votes per option
		
		Map<String, Integer> voteCountPerOption = new HashMap<String, Integer>();
		Map<String, Double> votePercentagePerOption = new HashMap<String, Double>();
		
		for(String option : survey.getOptions()) {
			voteCountPerOption.put(option, 0);
			votePercentagePerOption.put(option, 0.0);
		}
		
		double totalVoteCount = 0;
		
		for(SurveyResponse r : responses) {
			for(String selectedOption : r.getSelectedOptions()) {
				totalVoteCount++;
				int voteCount = voteCountPerOption.get(selectedOption);
				votePercentagePerOption.put(selectedOption, ++voteCount/totalVoteCount*100);
			}
		}
		
		return votePercentagePerOption;
	}
	
	// Data validation for surveys
	private boolean isValid(Survey survey) throws IllegalArgumentException {
		
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
