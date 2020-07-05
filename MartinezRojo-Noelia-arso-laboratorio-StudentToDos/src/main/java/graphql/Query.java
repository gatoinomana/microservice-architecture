package graphql;

import java.util.ArrayList;
import java.util.List;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.Task;
import persistence.TaskRepository;

public class Query implements GraphQLRootResolver {

	private final TaskRepository todoRepository;

	public Query(TaskRepository todoRepository) {
		this.todoRepository = todoRepository;
	}
	
	public List<Task> allTodosStudent(String studentId) {
		List<Task> theirTodos = new ArrayList<Task>();
		
		for (Task todo : todoRepository.getAllTasks())
			if (todo.getStudentId().equals(studentId))
				theirTodos.add(todo);
		
		return theirTodos;
	}
}