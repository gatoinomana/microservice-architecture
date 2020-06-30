package graphql;

import java.util.ArrayList;
import java.util.List;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.Todo;
import persistence.TodoRepository;

public class Query implements GraphQLRootResolver {

	private final TodoRepository todoRepository;

	public Query(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}
	
	public List<Todo> allTodosStudent(String studentId) {
		List<Todo> theirTodos = new ArrayList<Todo>();
		
		for (Todo todo : todoRepository.getAllTodos())
			if (todo.getStudentId().equals(studentId))
				theirTodos.add(todo);
		
		return theirTodos;
	}
}