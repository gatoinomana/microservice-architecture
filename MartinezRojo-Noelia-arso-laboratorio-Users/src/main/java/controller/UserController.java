package controller;

import java.util.List;

import model.Role;
import model.User;
import persistence.UserRepository;

public class UserController implements UserControllerInterface {
	
	private static UserController instance;
	private static UserRepository persistencia = UserRepository.getInstance();
	
	public static UserController getInstance() {
		if (instance == null)
			instance = new UserController();
		return instance;
	}

	public List<User> getAllTeachers() throws UserException {
		return persistencia.loadTeachers();
	}

	public List<User> getAllStudents() throws UserException {
		return persistencia.loadStudents();
	}
	
	public Role getRole(String id) throws UserException {
		User u = persistencia.loadUser(id);
		if (u != null)
			return u.getRole();
		else return null;
	}
}
