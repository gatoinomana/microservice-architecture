package controlador;

import java.util.LinkedList;

import tipos.Rol;
import tipos.Usuario;

public class ControladorUsuarios implements InterfazControladorUsuarios {
	
	private static ControladorUsuarios instance;
	
	public static ControladorUsuarios getInstance() {
		if (instance == null)
			instance = new ControladorUsuarios();
		return instance;
	}

	public String createUsuario(String nombre, String email, Rol rol) throws UsuariosException {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateUsuario(String id, String nombre, String email, Rol rol) throws UsuariosException {
		// TODO Auto-generated method stub
		
	}

	public Usuario getUsuario(String id) throws UsuariosException {
		// TODO Auto-generated method stub
		return null;
	}

	public Rol getRol(String id) throws UsuariosException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeUsuario(String id) throws UsuariosException {
		// TODO Auto-generated method stub
		return false;
	}

	public LinkedList<Usuario> getProfesores() throws UsuariosException {
		// TODO Auto-generated method stub
		return null;
	}

	public LinkedList<Usuario> getEstudiantes() throws UsuariosException {
		// TODO Auto-generated method stub
		return null;
	}

}
