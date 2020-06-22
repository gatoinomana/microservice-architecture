package persistencia;

import java.util.List;

import controlador.UsuariosException;
import tipos.Rol;
import tipos.Usuario;

public interface InterfazPersistencia {
	
	/** 
	 * Hacer persistir un usuario
	 */
	void writeUsuario(Usuario usuario) throws UsuariosException;
	
	/** 
	 * Cargar de persistencia un usuario dado su id
	 */
	Usuario readUsuario(String id) throws UsuariosException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol profesor
	 */
	List<Usuario> readProfesores() throws UsuariosException;
	
	/** 
	 * Cargar de persistencia todos los usuarios con rol estudiante
	 */
	List<Usuario> readEstudiantes() throws UsuariosException;
	
	/** 
	 * Borra de persistencia un usuario dado su id
	 */
	void removeUsuario(String id) throws UsuariosException;
	
	/** 
	 * Borra de persistencia todos los usuarios
	 */
	void removeUsuarios() throws UsuariosException;
}
