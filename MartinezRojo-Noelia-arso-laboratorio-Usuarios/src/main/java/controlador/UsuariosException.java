package controlador;

@SuppressWarnings("serial")
public class UsuariosException extends Exception {
	
	public UsuariosException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public UsuariosException(String msg) {
		super(msg);
	}
}
