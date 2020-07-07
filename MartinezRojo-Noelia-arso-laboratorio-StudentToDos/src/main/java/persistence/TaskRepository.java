package persistence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import model.Task;

public class TaskRepository {
	
    private final MongoCollection<Document> tasks;
    
    public TaskRepository(MongoCollection<Document> tasks) {
        this.tasks = tasks;
    }

	public Task save(Task newTask) {
		Document doc = new Document();
		doc.append("studentId", newTask.getStudentId());
		doc.append("description", newTask.getDescription());
		doc.append("deadline", newTask.getDeadline());
		doc.append("id", newTask.getId());
		doc.append("service", newTask.getService());
		tasks.insertOne(doc);
		return newTask;
	}
    
	public List<Task> getAllTasks() {
		List<Task> allTodos = new ArrayList<>();
        for (Document doc : tasks.find()) {
        	allTodos.add(task(doc));
        }
        return allTodos;
	}
	
	private Task task(Document doc) {
		 return new Task(
				 doc.getString("studentId"),
				 doc.getString("description"),
				 doc.getDate("deadline"),
				 doc.getString("id"),
				 doc.getString("service"));
	}
	
	public void remove(String studentId, String id, String service) {
		// Find task
		tasks.deleteMany(Filters.and(
				Filters.eq("studentId", studentId),
				Filters.eq("id", id), 
				Filters.eq("service", service)));
		
		
	}
}
