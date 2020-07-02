package controller;

import java.util.List;

import model.Role;
import model.User;
import persistence.UserRepository;

public class UsersController {
	
	private static UsersController instance;
	private static UserRepository userRepository = UserRepository.getInstance();
	
	public static UsersController getInstance() {
		if (instance == null)
			instance = new UsersController();
		
		return instance;
	}

	/**
	 * Returns all users with role "TEACHER"
	 */
	public List<User> getAllTeachers() throws UsersException {
		return userRepository.loadTeachers();
	}

	/**
	 * Returns all users with role "STUDENT"
	 */
	public List<User> getAllStudents() throws UsersException {
		return userRepository.loadStudents();
	}
	
	/**
	 * Returns the role of a user, given their id
	 */
	public Role getRole(String id) throws UsersException {
		User u = userRepository.loadUser(id);
		
		if (u != null)
			return u.getRole();
		
		return null;
	}
}
