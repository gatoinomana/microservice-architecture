package persistencia;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import controlador.UsuariosException;
import tipos.Rol;
import tipos.Usuario;

class PersistenciaTest {
	private static Persistencia persistencia = Persistencia.getInstance();
	private static Usuario estudiante1, estudiante2, profesor1, profesor2;
	
	@BeforeAll
	 private static void beforeEach() {
		estudiante1 = new Usuario();
		estudiante1.setNombre("pepe");
		estudiante1.setEmail("pepe@email.com");
		estudiante1.setRol(Rol.ESTUDIANTE);
		
		estudiante2 = new Usuario();
		estudiante2.setNombre("maria");
		estudiante2.setEmail("maria@email.com");
		estudiante2.setRol(Rol.ESTUDIANTE);
		
		profesor1 = new Usuario();
		profesor1.setNombre("marcos");
		profesor1.setEmail("marcos@email.com");
		profesor1.setRol(Rol.PROFESOR);
		
		profesor2 = new Usuario();
		profesor2.setNombre("antonia");
		profesor2.setEmail("antonia@email.com");
		profesor2.setRol(Rol.PROFESOR);
	}
	
	@AfterAll
	private static void afterAll() throws UsuariosException {
		persistencia.removeUsuario(estudiante1.getEmail());
		persistencia.removeUsuario(estudiante2.getEmail());
		persistencia.removeUsuario(profesor1.getEmail());
		persistencia.removeUsuario(profesor2.getEmail());
	}
	
	/** 
	 * Comprueba que writeUsuario() persiste un usuario
	 * @throws UsuariosException 
	 */
	@Test
	void writeUsuarioTest() throws UsuariosException {
		persistencia.writeUsuario(estudiante1);
		persistencia.writeUsuario(profesor1);
		assertTrue(new File("xml/", estudiante1.getEmail() + ".xml").exists());
		assertTrue(new File("xml/", profesor1.getEmail() + ".xml").exists());
		persistencia.removeUsuario(estudiante1.getEmail());
		persistencia.removeUsuario(profesor1.getEmail());
	}
	
	/** 
	 * Comprueba que readUsuario() carga un usuario de persistencia
	 * @throws UsuariosException, IOException
	 */
	@Test
	void readUsuarioTest() throws UsuariosException, IOException  {
		persistencia.writeUsuario(estudiante1);
		Usuario u = persistencia.readUsuario(estudiante1.getEmail());
		assertEquals(estudiante1.getNombre(), u.getNombre());
		assertEquals(estudiante1.getEmail(), u.getEmail());
		assertEquals(estudiante1.getRol(), u.getRol());
		persistencia.removeUsuario(estudiante1.getEmail());
	}
	
	/** 
	 * Comprueba que readProfesores() carga todos los usuarios 
	 * con rol profesor de persistencia 
	 * @throws UsuariosException 
	 */
	@Test
	void readProfesoresTest() throws UsuariosException {
		persistencia.writeUsuario(estudiante1);
		persistencia.writeUsuario(profesor1);
		persistencia.writeUsuario(profesor2);
		
		List<Usuario> profesores = persistencia.readProfesores();
		assertEquals(profesores.size(), 2);
		
		boolean leidoProfesor1 = false;
		boolean leidoProfesor2 = false;
		
		for(Usuario u : profesores) {
			assertTrue(u.getRol().equals(Rol.PROFESOR));
			if (u.equals(profesor1))
				leidoProfesor1 = true;
			else if (u.equals(profesor2))
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
	void readEstudiantesTest() throws UsuariosException {
		persistencia.writeUsuario(profesor1);
		persistencia.writeUsuario(estudiante1);
		persistencia.writeUsuario(estudiante2);
		
		List<Usuario> estudiantes = persistencia.readEstudiantes();
		assertEquals(estudiantes.size(), 2);
		
		boolean leidoEstudiante1 = false;
		boolean leidoEstudiante2 = false;
		
		for(Usuario u : estudiantes) {
			assertTrue(u.getRol().equals(Rol.ESTUDIANTE));
			if (u.equals(estudiante1))
				leidoEstudiante1 = true;
			else if (u.equals(estudiante2))
				leidoEstudiante2 = true;
				
		}
		assertTrue(leidoEstudiante1 && leidoEstudiante2);
	
	}

	/** 
	 * Comprueba que removeUsuario() borra un usuario de persistencia
	 * @throws UsuariosException 
	 */
	@Test
	void removeUsuarioTest() throws UsuariosException {
		persistencia.writeUsuario(estudiante1);
		persistencia.writeUsuario(profesor1);
		File f1 = new File("xml", estudiante1.getEmail() + ".xml");
		File f2 = new File("xml", profesor1.getEmail() + ".xml");
		assertTrue(f1.exists() && f2.exists());
		persistencia.removeUsuario(estudiante1.getEmail());
		assertTrue(!f1.exists() && f2.exists());
	}
	
	/** 
	 * Comprueba que removeUsuarios() borra todos los usuarios de persistencia
	 * @throws IOException, UsuariosException 
	 */
	@Test
	void removeUsuariosTest() throws IOException, UsuariosException {
		persistencia.writeUsuario(estudiante1);
		persistencia.writeUsuario(profesor1);
		File f1 = new File("xml", estudiante1.getEmail() + ".xml");
		File f2 = new File("xml", profesor1.getEmail() + ".xml");
		assertTrue(f1.exists() && f2.exists());
		persistencia.removeUsuarios();
		assertTrue(!f1.exists() && !f2.exists());
	}
	
}
