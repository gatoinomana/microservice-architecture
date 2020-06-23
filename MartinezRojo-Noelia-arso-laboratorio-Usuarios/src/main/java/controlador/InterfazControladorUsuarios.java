package controlador;

import java.util.List;

import modelo.Rol;
import modelo.Usuario;

public interface InterfazControladorUsuarios {

	/**
	 * Método de consulta de todos los usuarios con rol profesor
	 */
	List<Usuario> getProfesores() throws UsuariosException;
	
	/**
	 * Método de consulta de todos los usuarios con rol estudiante
	 */
	List<Usuario> getEstudiantes() throws UsuariosException;

	/**
	 * Método de consulta del rol de un usuario
	 */
	Rol getRol(String id) throws UsuariosException;
	
	// TODO: quitar (prueba)
	Usuario getUsuario(String id) throws UsuariosException;

}
