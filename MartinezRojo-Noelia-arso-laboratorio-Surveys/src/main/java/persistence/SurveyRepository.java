package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;

import model.Survey;
import model.Visibility;

public class SurveyRepository {

    private final MongoCollection<Document> surveys;
	private static SurveyRepository instance;
	
	public static SurveyRepository getInstance() {
		if (instance == null)
			instance = new SurveyRepository();
		return instance;
	}

    private SurveyRepository() {
    	
		// Connect to database
		MongoClientURI uri = new MongoClientURI(
			    "mongodb://arso:arso-20@cluster0-shard-00-00-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-01-xi0ku.azure.mongodb.net:27017,cluster0-shard-00-02-xi0ku.azure.mongodb.net:27017/arso?ssl=true&replicaSet=Cluster0-shard-0&authSource=admin&retryWrites=true&w=majority");
		@SuppressWarnings("resource")
		MongoDatabase mongo = new MongoClient(uri).getDatabase("arso");
		
		// Get survey collection
        this.surveys = mongo.getCollection("surveys");
    }

    public Survey save(Survey survey) {
  
        Document doc = new Document();
        
        doc.append("creator", survey.getCreator());
        doc.append("title", survey.getTitle());
        doc.append("instructions", survey.getInstructions());
        doc.append("starts", survey.getStarts());
        doc.append("ends", survey.getEnds());
        doc.append("minOptions", survey.getMinOptions());
        doc.append("maxOptions", survey.getMaxOptions());
        doc.append("visibility", survey.getVisibility().toString());
        doc.append("options", survey.getOptions());
        doc.append("results", new HashMap<String, Boolean>());
        
        surveys.insertOne(doc);
        
        return survey(doc);
    }
    
    @SuppressWarnings("unchecked")
	private Survey survey(Document doc) {
    	
    	return new Survey(      
            doc.get("_id").toString(),
            doc.getString("creator"),
            doc.getString("title"),
            doc.getString("instructions"),
            doc.getDate("starts"),
            doc.getDate("ends"),
            (int) doc.getInteger("minOptions"),
            (int) doc.getInteger("maxOptions"),
            Visibility.valueOf(doc.getString("visibility")),
            (List<String>) doc.get("options"),
            (Map<String, Integer>) doc.get("results")
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
	
	@SuppressWarnings("unchecked")
	private Map<String, Integer> findResultsById(String surveyId) {
        Document doc = surveys.find(Filters.eq("_id", new ObjectId(surveyId))).first();
        return (Map<String, Integer>) doc.get("results");
	}
	
    public void editSurvey(String surveyId, Survey patchedSurvey) {
    	
        Document doc = new Document();
        
        doc.append("creator", patchedSurvey.getCreator());
        doc.append("title", patchedSurvey.getTitle());
        doc.append("instructions", patchedSurvey.getInstructions());
        doc.append("starts", patchedSurvey.getStarts());
        doc.append("ends", patchedSurvey.getEnds());
        doc.append("minOptions", patchedSurvey.getMinOptions());
        doc.append("maxOptions", patchedSurvey.getMaxOptions());
        doc.append("visibility", patchedSurvey.getVisibility().toString());
        doc.append("options", patchedSurvey.getOptions());
        doc.append("options", patchedSurvey.getResults());
        
        surveys.replaceOne(Filters.eq("_id", new ObjectId(surveyId)), doc);
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
		surveys.findOneAndDelete(
				Filters.eq("_id", new ObjectId(surveyId)));
	}
}
