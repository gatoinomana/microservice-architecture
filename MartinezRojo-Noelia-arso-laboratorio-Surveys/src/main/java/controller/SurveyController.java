package controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import model.Survey;
import model.Visibility;

public class SurveyController implements SurveyControllerInterface {

	@Override
	public String createSurvey(String title, String instructions, Date openingDateTime, Date closingDateTime,
			int minOptions, int maxOptions, Visibility visibility) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addOption(String surveyId, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOption(String surveyId, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Survey> getAllSurveys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void editSurvey(String title, String instructions, Date openingDateTime, Date closingDateTime,
			int minOptions, int maxOptions, Visibility visibility) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Integer> getResults(String surveyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> respondSurvey(String surveyId, boolean[] responses) {
		// TODO Auto-generated method stub
		return null;
	}

}
