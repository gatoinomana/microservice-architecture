package persistence;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import model.Todo;

public class TodoRepository {
	
    private final MongoCollection<Document> todos;
    
    public TodoRepository(MongoCollection<Document> todos) {
        this.todos = todos;
    }

	public Todo save(Todo newTodo) {
		Document doc = new Document();
		doc.append("studentId", newTodo.getStudentId());
		todos.insertOne(doc);
		return newTodo;
	}
    
	public List<Todo> getAllTodos() {
		List<Todo> allTodos = new ArrayList<>();
        for (Document doc : todos.find()) {
        	allTodos.add(todo(doc));
        }
        return allTodos;
	}
	
	private Todo todo(Document doc) {
		 return new Todo(
	                doc.getString("studentId"));
	}
}
