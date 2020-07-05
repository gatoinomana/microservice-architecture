package persistence;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import model.Task;

public class TaskRepository {
	
    private final MongoCollection<Document> todos;
    
    public TaskRepository(MongoCollection<Document> tasks) {
        this.todos = tasks;
    }

	public Task save(Task newTask) {
		Document doc = new Document();
		doc.append("studentId", newTask.getStudentId());
		doc.append("description", newTask.getDescription());
		doc.append("deadline", newTask.getDeadline());
		doc.append("id", newTask.getId());
		doc.append("service", newTask.getService());
		todos.insertOne(doc);
		return newTask;
	}
    
	public List<Task> getAllTasks() {
		List<Task> allTodos = new ArrayList<>();
        for (Document doc : todos.find()) {
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
}
