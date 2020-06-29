package persistence;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import model.StudentToDo;

public class StudentToDoRepository {
	
    private final MongoCollection<Document> todos;
    
    public StudentToDoRepository(MongoCollection<Document> todos) {
        this.todos = todos;
    }

	public StudentToDo save(StudentToDo newStudentToDo) {
		Document doc = new Document();
		doc.append("studentId", newStudentToDo.getStudentId());
		todos.insertOne(doc);
		return newStudentToDo;
	}
    
	public List<StudentToDo> getAllTodos() {
		List<StudentToDo> allTodos = new ArrayList<>();
        for (Document doc : todos.find()) {
        	allTodos.add(todo(doc));
        }
        return allTodos;
	}
	
	private StudentToDo todo(Document doc) {
		 return new StudentToDo(
	                doc.getString("studentId"));
	}
}
