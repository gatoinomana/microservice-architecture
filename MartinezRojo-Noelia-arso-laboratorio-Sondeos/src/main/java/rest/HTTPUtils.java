package rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtils {
	
	/** 
	 * Envía una petición GET a una URL y devuelve la respuesta
	 * @throws IOException 
	 */
	public static String sendGET(String url) throws IOException {
		
		// Create GET connection object from URL
		URL urlObj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("GET");
		
		// Get response code (if 200 OK, return response body)
		int responseCode = con.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader inputBuffer = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = inputBuffer.readLine()) != null) {
				response.append(inputLine);
			}
			inputBuffer.close();
			
			return response.toString();
		}  else return null;
	}
	
	/** 
	 * Envía una petición POST a una URL y devuelve la respuesta
	 * @throws IOException 
	 */
	public static String sendPOST(String url, String postParams) throws IOException {
		
		// Create POST connection object from URL
		URL urlObj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		con.setRequestMethod("POST");

		// Write request body
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(postParams.getBytes());
		os.flush();
		os.close();

		// Get response code (if 200 OK, return response body)
		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		} else return null;
	}

}
