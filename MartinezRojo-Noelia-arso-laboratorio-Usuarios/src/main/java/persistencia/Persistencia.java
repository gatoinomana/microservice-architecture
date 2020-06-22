package persistencia;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import controlador.UsuariosException;
import tipos.Rol;
import tipos.Usuario;

public class Persistencia implements InterfazPersistencia {
	
	private static Persistencia instance;
	
	public static Persistencia getInstance() {
		if (instance == null)
			instance = new Persistencia();
		return instance;
	}

	public void writeUsuario(Usuario usuario) throws UsuariosException {
		String id = usuario.getEmail();

		try {
			JAXBContext contexto = JAXBContext.newInstance("tipos");
			Marshaller marshaller = contexto.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", true);
			marshaller.setProperty("jaxb.schemaLocation", "http://www.um.es/autor autor.xsd");
			marshaller.marshal(usuario, new File("xml/" + id + ".xml"));
		} catch (JAXBException e) {
			throw new UsuariosException("Error al persistir usuario", e);
		}
	}

	public Usuario readUsuario(String id) throws UsuariosException {
		File f = new File("xml/", id + ".xml");
		if (!f.exists())
			return null;
		try {
			JAXBContext contexto = JAXBContext.newInstance("tipos");
			Unmarshaller unmarshaller = contexto.createUnmarshaller();
			Usuario usuario = (Usuario) unmarshaller.unmarshal(f);
			return usuario;
		} catch (JAXBException e) {
			throw new UsuariosException("Error al cargar usuario", e);
		}
	}

	public List<Usuario> readProfesores() throws UsuariosException {
		return readUsuarios(Rol.PROFESOR);
	}

	public List<Usuario> readEstudiantes() throws UsuariosException {
		return readUsuarios(Rol.ESTUDIANTE);
	}

	public void removeUsuario(String id) throws UsuariosException {
		File f = new File("xml/", id + ".xml");
		if (f.exists())
			f.delete();
	}

	public void removeUsuarios() throws UsuariosException {
		File[] ficherosXML = new File("xml").listFiles(getXMLFilenameFilter());
		
		for (File f : ficherosXML) {
			f.delete();
		}
	}
	
	private FilenameFilter getXMLFilenameFilter() {
		return new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
	}
	
	private List<Usuario> readUsuarios(Rol rol) throws UsuariosException {
		LinkedList<Usuario> usuarios = new LinkedList<Usuario>();
		File[] ficherosXML = new File("xml").listFiles(getXMLFilenameFilter());
		
		for (File f : ficherosXML) {
			try {
				JAXBContext contexto = JAXBContext.newInstance("tipos");
				Unmarshaller unmarshaller = contexto.createUnmarshaller();
				Usuario u = (Usuario) unmarshaller.unmarshal(f);
				if (u.getRol().equals(rol))
					usuarios.add(u);
			} catch (JAXBException e) {
				e.printStackTrace();
			}	
		}
		return usuarios;
	}
}
