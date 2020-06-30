package graphql;

import java.util.ArrayList;
import java.util.List;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.StudentToDo;
import persistence.StudentToDoRepository;

public class Query implements GraphQLRootResolver {

	private final StudentToDoRepository todoRepository;

	public Query(StudentToDoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}
	
	public List<StudentToDo> allTodosStudent(String studentId) {
		List<StudentToDo> theirTodos = new ArrayList<StudentToDo>();
		
		for (StudentToDo todo : todoRepository.getAllTodos())
			if (todo.getStudentId().equals(studentId))
				theirTodos.add(todo);
		
		return theirTodos;
	}
}