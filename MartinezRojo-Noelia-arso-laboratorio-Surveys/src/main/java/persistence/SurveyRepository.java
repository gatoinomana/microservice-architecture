package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import model.Survey;
import model.Visibility;

// TODO: las fechas se están guardando mal
public class SurveyRepository {

    private final MongoCollection<Document> surveys;
	private static SurveyRepository instance;
	
	public static SurveyRepository getInstance(MongoCollection<Document> surveys) {
		if (instance == null)
			instance = new SurveyRepository(surveys);
		return instance;
	}

    private SurveyRepository(MongoCollection<Document> surveys) {
        this.surveys = surveys;
    }

    public Survey saveSurvey(Survey survey, String creatorId) {
        Document doc = new Document();
        doc.append("creator", creatorId);
        doc.append("title", survey.getTitle());
        doc.append("instructions", survey.getInstructions());
        doc.append("starts", survey.getStarts());
        doc.append("ends", survey.getEnds());
        doc.append("minOptions", survey.getMinOptions());
        doc.append("maxOptions", survey.getMaxOptions());
        doc.append("visibility", survey.getVisibility().toString());
        doc.append("options", survey.getOptions());
        doc.append("results", survey.getResults());
        surveys.insertOne(doc);
        return survey(doc);
    }
    
    @SuppressWarnings("unchecked")
	private Survey survey(Document doc) {
    	List<String> options = (List<String>) doc.get("options");
    	Map<String, Integer> results = (Map<String, Integer>) doc.get("results");
    	Visibility visibility = Visibility.valueOf(doc.getString("visibility"));
    	
        return new Survey(      
            doc.get("_id").toString(),
            doc.getString("title"),
            doc.getString("instructions"),
            doc.getDate("starts"),
            doc.getDate("ends"),
            (int) doc.getInteger("minOptions"),
            (int) doc.getInteger("maxOptions"),
            visibility, options, results
        );
    }

     public Survey findById(String surveyId) {
        Document doc = surveys.find(Filters.eq("_id", new ObjectId(surveyId))).first();
        return survey(doc);
    }
    
    public List<Survey> getAllSurveys() {
        List<Survey> allSurveys = new ArrayList<>();
        for (Document doc : surveys.find()) {
        	allSurveys.add(survey(doc));
        }
        return allSurveys;
    }
    
    public void addOption(String surveyId, String text) {
    	Survey old = findById(surveyId);
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("options", old.getOptions());
    	
    	List<String> newOptions = new LinkedList<String>(old.getOptions());
    	newOptions.add(text);

    	BasicDBObject newDocument = new BasicDBObject();
    	newDocument.put("options", newOptions);

    	BasicDBObject updateObject = new BasicDBObject();
    	updateObject.put("$set", newDocument); //

    	surveys.updateOne(query, updateObject);
    }

	public void removeOption(String surveyId, String text) {
    	Survey old = findById(surveyId);
    	
    	BasicDBObject query = new BasicDBObject();
    	query.put("options", old.getOptions());
    	
    	List<String> newOptions = new LinkedList<String>(old.getOptions());
    	newOptions.remove(text);

    	BasicDBObject newDocument = new BasicDBObject();
    	newDocument.put("options", newOptions);

    	BasicDBObject updateObject = new BasicDBObject();
    	updateObject.put("$set", newDocument);

    	surveys.updateOne(query, updateObject);
	}
	
	public String findCreatorById(String surveyId) {
        Document doc = surveys.find(Filters.eq("_id", new ObjectId(surveyId))).first();
        return doc.getString("creator");
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Integer> findResultsById(String surveyId) {
        Document doc = surveys.find(Filters.eq("_id", new ObjectId(surveyId))).first();
        return (Map<String, Integer>) doc.get("results");
	}
	
    public boolean editBasicInformation(String surveyId, Survey newSurvey) {
    	
		Bson surveyToModify = Filters.eq("_id", new ObjectId(surveyId));
		Bson updateOperation = combine(
				set("title", newSurvey.getTitle()),
				set("instructions", newSurvey.getInstructions()),
				set("starts", newSurvey.getStarts()),
				set("ends", newSurvey.getEnds()),
				set("minOptions", newSurvey.getMinOptions()),
				set("maxOptions", newSurvey.getMaxOptions()),
				set("visibility", newSurvey.getVisibility().toString())
		);
		
		UpdateResult updateResult = surveys.updateOne(surveyToModify, updateOperation);
		return updateResult.getModifiedCount() == 1;
    }
    
	public boolean updateResults(String surveyId, Map<String, Boolean> responses) {
    	
    	Map<String, Integer> oldResults = findResultsById(surveyId);
    	Map<String, Integer> newResults = new HashMap<String, Integer>();
    	
    	/* Por cada opción asociada a "true" en "respones"
    	 * incrementar en 1 el número de votos asociado en "results"
    	 * */
    	for(String option : oldResults.keySet()) {
    		int oldVotes = oldResults.get(option);
    		if (responses.get(option))
    			newResults.put(option, oldVotes + 1);
    		else
    			newResults.put(option, oldVotes);
    	}
    	
		Bson surveyToModify = Filters.eq("_id", new ObjectId(surveyId));
		Bson updateOperation = set("results", newResults);
		
		UpdateResult updateResult = surveys.updateOne(surveyToModify, updateOperation);
		return updateResult.getModifiedCount() == 1;
    }
    
	public void remove(String surveyId) {
		// TODO Auto-generated method stub
	}
}
