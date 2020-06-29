package graphql;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.StudentToDo;
import persistence.StudentToDoRepository;

public class Mutation implements GraphQLRootResolver {
    
	private final StudentToDoRepository todoRepository;

    public Mutation(StudentToDoRepository todoRepository) {
      this.todoRepository = todoRepository;
    }
    
    public StudentToDo createStudentTodo(String studentId) {
    	StudentToDo newStudentToDo = new StudentToDo(studentId);
    	return todoRepository.save(newStudentToDo);
    }
}
