package eventsRetriver;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.ClientProtocolException;

public class RetriveEventsApache {
	
	private static void retriveEventsFromApache(String repository, String projectKey, String method, String directory) throws IOException {
		StringBuilder result = new StringBuilder();
		FileWriter writeFile = null;
		
		URL url = new URL(repository + method + "?" + "resource=" + projectKey);
		
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		
		if(conn.getResponseCode()!= 200) {
			System.out.println("Deu merda!");
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while((line = reader.readLine()) != null) {
			result.append(line);
		}
		reader.close();

		try {
			writeFile = new FileWriter(projectKey+".json");
			writeFile.write(result.toString());
			writeFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		String repository = "https://builds.apache.org/analysis/api/";
		String projectKey = "org.apache.kylin:kylin";
		String method = "events";
		String directory = "resources/apache/";
		retriveEventsFromApache(repository, projectKey, method, directory);
		System.out.println("Era pra ter dado certo!");
	}
	
}
