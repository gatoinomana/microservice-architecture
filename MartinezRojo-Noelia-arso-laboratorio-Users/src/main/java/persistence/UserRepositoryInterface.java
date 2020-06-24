package persistence;

import java.util.List;

import controller.UserException;
import model.User;

public interface UserRepositoryInterface {
	
	/** 
	 * Hacer persistir un usuario (devuelve id)
	 */
	String saveUser(User usuario) throws UserException;
	
	/** 
	 * Cargar de persistencia un usuario dado su id
	 */
	User loadUser(String id) throws UserException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol profesor
	 */
	List<User> loadTeachers() throws UserException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol estudiante
	 */
	List<User> loadStudents() throws UserException;
	
	/** 
	 * Borra de persistencia un usuario dado su id
	 */
	boolean removeUser(String id) throws UserException;
	
	/** 
	 * Borra de persistencia todos los usuarios
	 */
	void removeUsers() throws UserException;
}
