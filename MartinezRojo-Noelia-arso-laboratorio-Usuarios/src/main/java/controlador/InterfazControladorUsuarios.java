package controlador;

import java.util.LinkedList;

import usuario.Rol;
import usuario.Usuario;

public interface InterfazControladorUsuarios {
	
	/** 
	 * Método de creación de un usuario.
	 * Todos los parámetros son obligatorios (no aceptan valor nulo).
	 * El método retorna el id del nuevo usuario (su email)
	 */
	String createUsuario(String nombre, String email, Rol rol) throws UsuariosException;
	
	/**
	 * Método de actualización de un usuario.
	 * En relación al método de creación, añade un primer parámetro con el id del usuario.
	 */
	void updateUsuario(String id, String nombre, String email, Rol rol) throws UsuariosException;
	
	/**
	 * Recupera la información de un usuario utilizando el identificador. 	
	 */
	Usuario getUsuario(String id) throws UsuariosException;
	
	/**
	 * Método de consulta del rol de un usuario
	 */
	Rol getRol(String id) throws UsuariosException;
	
	/**
	 * Elimina un usuario utilizando el identificador
	 */
	boolean removeUsuario(String id) throws UsuariosException;
	
	/**
	 * Método de consulta de todos los usuarios con rol profesor
	 */
	LinkedList<Usuario> getProfesores() throws UsuariosException;
	
	/**
	 * Método de consulta de todos los usuarios con rol estudiante
	 */
	LinkedList<Usuario> getEstudiantes() throws UsuariosException;
}
