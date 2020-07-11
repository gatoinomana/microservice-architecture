package persistence;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import model.Survey;
import model.SurveyResponse;
import model.Visibility;

public class SurveyRepository {

    private final MongoCollection<Document> surveys;
    private final MongoCollection<Document> responses;
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
        this.responses = mongo.getCollection("responses");
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
        doc.append("responses", new ArrayList<SurveyResponse>());
        
        surveys.insertOne(doc);
        
        return survey(doc);
    }
    
	public void saveResponse(String surveyId, SurveyResponse response) {
        
		// Save response
		Document doc = new Document();
        
        doc.append("student", response.getStudent());
        doc.append("selectedOptions", response.getSelectedOptions());
        
        responses.insertOne(doc);
        
        // Save reference in survey
        surveys.updateOne(
        		Filters.eq("_id", new ObjectId(surveyId)), 
        		new Document("$push",  new BasicDBObject(
        				"responses", response(doc).getId())));
	}
    
    @SuppressWarnings("unchecked")
	private Survey survey(Document doc) {
    	
    	// Get List<SurveyResult> from document references
    	List<SurveyResponse> responses = new ArrayList<SurveyResponse>();
    	List<String> responsesIds = (List<String>) doc.get("responses");
    	
    	for(String id : responsesIds) {
    		SurveyResponse response = getResponse(id);
    		if (response != null)
    			responses.add(response);
    	}

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
            responses
        );
    }

	@SuppressWarnings("unchecked")
	private SurveyResponse response(Document doc) {
    	
    	return new SurveyResponse(      
            doc.get("_id").toString(),
            doc.getString("student"),
            (List<String>) doc.get("selectedOptions")
        );
    }

    public Survey getSurvey(String surveyId) {
    	
        Document doc = surveys.find(Filters.eq("_id", 
        		new ObjectId(surveyId))).first();
        
        if (doc != null) {
        	return survey(doc);
        }
        	
        return null;
    }
    
    
    private SurveyResponse getResponse(String responseId) {
    	
        Document doc = responses.find(Filters.eq("_id", 
        		new ObjectId(responseId))).first();
        
        if (doc != null) {
        	return response(doc);
        }
        	
        return null;
	}

    public List<Survey> getAllSurveys() {
        List<Survey> allSurveys = new ArrayList<>();
        for (Document doc : surveys.find()) {
        	allSurveys.add(survey(doc));
        }
        return allSurveys;
    }

    public void edit(String surveyId, Survey patchedSurvey) {
    	
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
        doc.append("responses", patchedSurvey.getResponses());
        
        surveys.replaceOne(Filters.eq("_id", new ObjectId(surveyId)), doc);
    }
    
	public void remove(String surveyId) {
		surveys.findOneAndDelete(
				Filters.eq("_id", new ObjectId(surveyId)));
	}
   
}
