package graphql;

import java.util.ArrayList;
import java.util.List;

import com.coxautodev.graphql.tools.GraphQLRootResolver;

import model.Task;
import persistence.TaskRepository;

public class Query implements GraphQLRootResolver {

	private final TaskRepository taskRepository;

	public Query(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
	
	public List<Task> allTasksStudent(String studentId) {
		List<Task> theirTasks = new ArrayList<Task>();
		
		for (Task task : taskRepository.getAllTasks())
			if (task.getStudentId().equals(studentId))
				theirTasks.add(task);
		
		return theirTasks;
	}
}