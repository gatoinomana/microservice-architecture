package persistence;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import controller.UserException;
import model.Role;
import model.User;

public class UserRepository implements UserRepositoryInterface {
	
	private static UserRepository instance;
	private static String JAXBContextPath = "model";
	
	public static UserRepository getInstance() {
		if (instance == null)
			instance = new UserRepository();
		return instance;
	}

	public String saveUser(User user) throws UserException {
		String id = user.getEmail();

		try {
			JAXBContext contexto = JAXBContext.newInstance(JAXBContextPath);
			Marshaller marshaller = contexto.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", true);
			marshaller.marshal(user, new File("xml/" + id + ".xml"));
			return id;
		} catch (JAXBException e) {
			throw new UserException("Error al persistir usuario", e);
		}
		
	}

	public User loadUser(String id) throws UserException {
		File f = new File("./xml/" + id + ".xml");
		if (!f.exists()) {
			return null;
		}
		try {
			JAXBContext contexto = JAXBContext.newInstance(JAXBContextPath);
			Unmarshaller unmarshaller = contexto.createUnmarshaller();
			User usuario = (User) unmarshaller.unmarshal(f);
			return usuario;
		} catch (JAXBException e) {
			throw new UserException("Error al cargar usuario", e);
		}
	}

	public List<User> loadTeachers() throws UserException {
		return loadUsers(Role.TEACHER);
	}

	public List<User> loadStudents() throws UserException {
		return loadUsers(Role.STUDENT);
	}

	public boolean removeUser(String id) throws UserException {
		File f = new File("xml/", id + ".xml");
		if (f.exists()) {
			f.delete();
			return true;
		}
		else return false;
			
	}

	public void removeUsers() throws UserException {
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
	
	private List<User> loadUsers(Role role) throws UserException {
		LinkedList<User> usuarios = new LinkedList<User>();
		File[] ficherosXML = new File("xml").listFiles(getXMLFilenameFilter());
		
		for (File f : ficherosXML) {
			try {
				JAXBContext contexto = JAXBContext.newInstance(JAXBContextPath);
				Unmarshaller unmarshaller = contexto.createUnmarshaller();
				User u = (User) unmarshaller.unmarshal(f);
				if (u.getRole().equals(role))
					usuarios.add(u);
			} catch (JAXBException e) {
				e.printStackTrace();
			}	
		}
		return usuarios;
	}
}
