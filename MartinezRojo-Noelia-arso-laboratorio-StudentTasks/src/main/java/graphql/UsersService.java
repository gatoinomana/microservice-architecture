package graphql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class UsersService {
	
	public static List<String> getAllStudents() throws IOException {
		
		List<String> studentIds = new LinkedList<String>();
		
		JsonObject object = sendGET("http://localhost:8080/api/users/students");
		
		JsonArray studentsArray = object.getJsonArray("students");
		studentsArray.forEach(e -> studentIds.add(
				e.asJsonObject().get("email").toString().replace("\"", "")));
		
		return studentIds;
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
