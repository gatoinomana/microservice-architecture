package graphql;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.Todo;
import persistence.TodoRepository;

public class Mutation implements GraphQLRootResolver {
    
	private final TodoRepository todoRepository;

    public Mutation(TodoRepository todoRepository) {
      this.todoRepository = todoRepository;
    }
    
    public Todo createTodo(String studentId) {
    	Todo newTodo = new Todo(studentId);
    	return todoRepository.save(newTodo);
    }
}
