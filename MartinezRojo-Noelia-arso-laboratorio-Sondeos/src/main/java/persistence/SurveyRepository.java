package persistence;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import model.Survey;
import model.Visibility;

public class SurveyRepository {

    private final MongoCollection<Document> surveys;
    
    public SurveyRepository(MongoCollection<Document> surveys) {
        this.surveys = surveys;
    }

    public Survey saveSurvey(Survey survey) {
        Document doc = new Document();
        doc.append("title", survey.getTitle());
        doc.append("instructions", survey.getInstructions());
        doc.append("openingDateTime", survey.getOpeningDateTime());
        doc.append("closingDateTime", survey.getClosingDateTime());
        doc.append("minOptions", survey.getMinOptions());
        doc.append("maxOptions", survey.getMaxOptions());
        doc.append("visibility", survey.getVisibility().toString());
        doc.append("options", survey.getOptions());
        doc.append("results", survey.getResults());
        surveys.insertOne(doc);
        return survey(doc);
    }
    
    private Survey survey(Document doc) {
    	List<String> options = (LinkedList<String>) doc.get("options");
    	Map<String, Integer> results = (HashMap<String, Integer>) doc.get("results");
    	Visibility visibility = Visibility.valueOf(doc.getString("visibility"));
    	
        return new Survey(
            doc.get("_id").toString(),
            doc.getString("title"),
            doc.getString("instructions"),
            doc.getDate("openingDateTime"),
            doc.getDate("closingDateTime"),
            (int) doc.getInteger("minOptions"),
            (int) doc.getInteger("maxOptions"),
            visibility, options, results
        );
    }

//     public Survey findById(String id) {
//        Document doc = links.find(Filters.eq("_id", new ObjectId(id))).first();
//        return link(doc);
//    }
//    
//    public List<Survey> getAllSurveys() {
//        List<Link> allLinks = new ArrayList<>();
//        for (Document doc : links.find()) {
//            allLinks.add(link(doc));
//        }
//        return allLinks;
//    }
}
