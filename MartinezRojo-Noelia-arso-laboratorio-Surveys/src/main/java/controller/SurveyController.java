package controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Survey;
import model.Visibility;
import persistence.SurveyRepository;

public class SurveyController implements SurveyControllerInterface {
	
	private static SurveyController instance;
	private SurveyRepository surveyRepository;
	
	public static SurveyController getInstance(SurveyRepository surveyRepository) {
		if (instance == null) {
			instance = new SurveyController(surveyRepository);
		}
		return instance;
	}
	
	private SurveyController(SurveyRepository surveyRepository) {
		this.surveyRepository = surveyRepository;
	}
	
	@Override
	public String createSurvey(String title, String instructions, 
			Date starts, Date ends, int minOptions, int maxOptions, 
			Visibility visibility) throws SurveyException {
		
		/* Comprobar que tiene título y las fechas 
		 * y los límites de opciones son válidos */
		Date now = new Date();
		if (title.isEmpty() || starts.before(now) || starts.after(ends) || 
				starts.equals(ends) || minOptions > maxOptions ||
				minOptions == 0 || maxOptions == 0)
			return null;
		
		Survey survey = new Survey(title, instructions, starts, ends, 
				minOptions, maxOptions, visibility);
		
		return surveyRepository.saveSurvey(survey).getId();
	}

	@Override
	public void addOption(String surveyId, String text) throws SurveyException {
		surveyRepository.addOption(surveyId, text);
		
	}

	@Override
	public void removeOption(String surveyId, String text) throws SurveyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Survey> getAllSurveys() throws SurveyException {
		return surveyRepository.getAllSurveys();
	}

	@Override
	public void editSurvey(String title, String instructions, 
			Date openingDateTime, Date closingDateTime,
			int minOptions, int maxOptions, 
			Visibility visibility) throws SurveyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Integer> getResults(String surveyId) throws SurveyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> respondSurvey(
			String surveyId, boolean[] responses) throws SurveyException {
		// TODO Auto-generated method stub
		return null;
	}

}
