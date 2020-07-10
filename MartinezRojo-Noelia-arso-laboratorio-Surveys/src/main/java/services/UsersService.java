package services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class UsersService {
	
	private static UsersService instance;

	public static UsersService getInstance() {
		if (instance == null)
			instance = new UsersService();
		return instance;
	}
	
	private UsersService() {}
	
	public boolean isTeacher(String userId) throws IOException {

		JsonObject object = sendGET(
				"http://localhost:8080/api/users/" + userId + "/role");
		
		return object != null ? object.getString("role").equals("TEACHER") : false;
	}
	
	public boolean isStudent(String userId) throws IOException {

		JsonObject object = sendGET(
				"http://localhost:8080/api/users/" + userId + "/role");
		
		return object != null ? object.getString("role").equals("STUDENT") : false;
	}
	
	private static JsonObject sendGET(String url) throws IOException {
		
		// Create GET connection object from URL
		URL urlObj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("GET");
		
		// Check request was successful
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
			return null;
		
		// Read response
		StringBuffer response = new StringBuffer();
		
		BufferedReader inputBuffer = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;

		while ((inputLine = inputBuffer.readLine()) != null) {
			response.append(inputLine);
		}
		
		inputBuffer.close();
		
		// Return response as JSON object
        JsonReader jsonReader = Json.createReader(
        		new StringReader(response.toString()));
        JsonObject object = jsonReader.readObject();
        
        return object;
	}
}
