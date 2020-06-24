package controller;

import java.util.List;

import model.Role;
import model.User;

public interface UserControllerInterface {

	/**
	 * Método de consulta de todos los usuarios con rol profesor
	 */
	List<User> getAllTeachers() throws UserException;
	
	/**
	 * Método de consulta de todos los usuarios con rol estudiante
	 */
	List<User> getAllStudents() throws UserException;

	/**
	 * Método de consulta del rol de un usuario
	 */
	Role getRole(String id) throws UserException;

}
