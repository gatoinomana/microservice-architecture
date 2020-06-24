package persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import controller.UserException;
import model.Role;
import model.User;
import persistence.UserRepository;

class UserRepositoryTest {
	private static UserRepository userPersistence = UserRepository.getInstance();
	private static User student1, student2, teacher1, teacher2;
	
	@BeforeAll
	 private static void beforeEach() {
		student1 = new User();
		student1.setName("pepe");
		student1.setEmail("pepe@email.com");
		student1.setRole(Role.STUDENT);
		
		student2 = new User();
		student2.setName("maria");
		student2.setEmail("maria@email.com");
		student2.setRole(Role.STUDENT);
		
		teacher1 = new User();
		teacher1.setName("marcos");
		teacher1.setEmail("marcos@email.com");
		teacher1.setRole(Role.TEACHER);
		
		teacher2 = new User();
		teacher2.setName("antonia");
		teacher2.setEmail("antonia@email.com");
		teacher2.setRole(Role.TEACHER);
	}
	
	@AfterAll
	private static void afterAll() throws UserException {
		userPersistence.removeUser(student1.getEmail());
		userPersistence.removeUser(student2.getEmail());
		userPersistence.removeUser(teacher1.getEmail());
		userPersistence.removeUser(teacher2.getEmail());
	}
	
	/** 
	 * Comprueba que writeUsuario() persiste un usuario
	 * @throws UsuariosException 
	 */
	@Test
	void writeUsuarioTest() throws UserException {
		userPersistence.saveUser(student1);
		userPersistence.saveUser(teacher1);
		assertTrue(new File("xml/", student1.getEmail() + ".xml").exists());
		assertTrue(new File("xml/", teacher1.getEmail() + ".xml").exists());
		userPersistence.removeUser(student1.getEmail());
		userPersistence.removeUser(teacher1.getEmail());
	}
	
	/** 
	 * Comprueba que readUsuario() carga un usuario de persistencia
	 * @throws UsuariosException, IOException
	 */
	@Test
	void readUsuarioTest() throws UserException, IOException  {
		userPersistence.saveUser(student1);
		User u = userPersistence.loadUser(student1.getEmail());
		assertEquals(student1.getName(), u.getName());
		assertEquals(student1.getEmail(), u.getEmail());
		assertEquals(student1.getRole(), u.getRole());
		userPersistence.removeUser(student1.getEmail());
	}
	
	/** 
	 * Comprueba que readProfesores() carga todos los usuarios 
	 * con rol profesor de persistencia 
	 * @throws UsuariosException 
	 */
	@Test
	void readProfesoresTest() throws UserException {
		userPersistence.saveUser(student1);
		userPersistence.saveUser(teacher1);
		userPersistence.saveUser(teacher2);
		
		List<User> profesores = userPersistence.loadTeachers();
		assertEquals(profesores.size(), 2);
		
		boolean leidoProfesor1 = false;
		boolean leidoProfesor2 = false;
		
		for(User u : profesores) {
			assertTrue(u.getRole().equals(Role.TEACHER));
			if (u.equals(teacher1))
				leidoProfesor1 = true;
			else if (u.equals(teacher2))
				leidoProfesor2 = true;
				
		}
		assertTrue(leidoProfesor1 && leidoProfesor2);
	}
	
	/** 
	 * Comprueba que readEstudiantes() carga todos los usuarios 
	 * con rol estudiante de persistencia
	 * @throws UsuariosException 
	 */
	@Test
	void readEstudiantesTest() throws UserException {
		userPersistence.saveUser(teacher1);
		userPersistence.saveUser(student1);
		userPersistence.saveUser(student2);
		
		List<User> estudiantes = userPersistence.loadStudents();
		assertEquals(estudiantes.size(), 2);
		
		boolean leidoEstudiante1 = false;
		boolean leidoEstudiante2 = false;
		
		for(User u : estudiantes) {
			assertTrue(u.getRole().equals(Role.STUDENT));
			if (u.equals(student1))
				leidoEstudiante1 = true;
			else if (u.equals(student2))
				leidoEstudiante2 = true;
				
		}
		assertTrue(leidoEstudiante1 && leidoEstudiante2);
	
	}

	/** 
	 * Comprueba que removeUsuario() borra un usuario de persistencia
	 * @throws UsuariosException 
	 */
	@Test
	void removeUsuarioTest() throws UserException {
		userPersistence.saveUser(student1);
		userPersistence.saveUser(teacher1);
		File f1 = new File("xml", student1.getEmail() + ".xml");
		File f2 = new File("xml", teacher1.getEmail() + ".xml");
		assertTrue(f1.exists() && f2.exists());
		userPersistence.removeUser(student1.getEmail());
		assertTrue(!f1.exists() && f2.exists());
	}
	
	/** 
	 * Comprueba que removeUsuarios() borra todos los usuarios de persistencia
	 * @throws IOException, UsuariosException 
	 */
	@Test
	void removeUsuariosTest() throws IOException, UserException {
		userPersistence.saveUser(student1);
		userPersistence.saveUser(teacher1);
		File f1 = new File("xml", student1.getEmail() + ".xml");
		File f2 = new File("xml", teacher1.getEmail() + ".xml");
		assertTrue(f1.exists() && f2.exists());
		userPersistence.removeUsers();
		assertTrue(!f1.exists() && !f2.exists());
	}
}