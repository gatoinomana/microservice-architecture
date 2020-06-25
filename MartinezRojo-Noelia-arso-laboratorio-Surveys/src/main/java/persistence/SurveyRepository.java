package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import model.Survey;
import model.Visibility;

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

    public Survey saveSurvey(Survey survey) {
        Document doc = new Document();
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

     public Survey findById(String id) {
        Document doc = surveys.find(Filters.eq("_id", new ObjectId(id))).first();
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
}
