package persistence;

import java.util.List;

import controller.UsersException;
import model.User;

public interface UserRepositoryInterface {
	
	/** 
	 * Hacer persistir un usuario (devuelve id)
	 */
	String saveUser(User usuario) throws UsersException;
	
	/** 
	 * Cargar de persistencia un usuario dado su id
	 */
	User loadUser(String id) throws UsersException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol profesor
	 */
	List<User> loadTeachers() throws UsersException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol estudiante
	 */
	List<User> loadStudents() throws UsersException;
	
	/** 
	 * Borra de persistencia un usuario dado su id
	 */
	boolean removeUser(String id) throws UsersException;
	
	/** 
	 * Borra de persistencia todos los usuarios
	 */
	void removeUsers() throws UsersException;
}
