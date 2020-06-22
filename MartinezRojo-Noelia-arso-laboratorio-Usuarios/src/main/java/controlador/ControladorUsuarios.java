package controlador;

import java.util.List;

import persistencia.Persistencia;
import tipos.Rol;
import tipos.Usuario;

public class ControladorUsuarios implements InterfazControladorUsuarios {
	
	private static ControladorUsuarios instance;
	private static Persistencia persistencia = Persistencia.getInstance();
	
	public static ControladorUsuarios getInstance() {
		if (instance == null)
			instance = new ControladorUsuarios();
		return instance;
	}

	public List<Usuario> getProfesores() throws UsuariosException {
		return persistencia.readProfesores();
	}

	public List<Usuario> getEstudiantes() throws UsuariosException {
		return persistencia.readEstudiantes();
	}
	
	public Rol getRol(String id) throws UsuariosException {
		Usuario u = persistencia.readUsuario(id);
		if (u != null)
			return u.getRol();
		else return null;
	}
	
	// TODO: quitar (prueba)
	public Usuario getUsuario(String id) throws UsuariosException {
		return persistencia.readUsuario(id);
	}
}
