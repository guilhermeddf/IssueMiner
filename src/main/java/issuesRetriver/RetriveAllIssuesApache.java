package issuesRetriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.ClientProtocolException;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import model.Bucket;
import model.Issue;

public class RetriveAllIssuesApache {

	static final int PAGE_SIZE = 500;
	
	//https://builds.apache.org/analysis/api/
	//https://sonarcloud.io/api/
	
	static final String RESPOSITORY = "https://builds.apache.org/analysis/api/";
	
	private static DateTime referencia;
	private static DateTime fimPosterior;
	private static DateTime fimAnterior;
	private static DateTime inicioAnterior;
	
	private static List<Bucket> buckets = new ArrayList<>();
	private static DateTime inicioPosterior;
	
	public static void retriveIssuesProject(URL url, String projectKey, String directory, int pageIndex) throws IOException {
		StringBuilder result = new StringBuilder();
		FileWriter writeFile = null;
		FileWriter writeFileCSV = null;
		int iterator=1;
		int limite;
		int contSave=0;
		
		String directoryJson = directory+projectKey+"/JSON/";
		String directoryCSV = directory+projectKey+"/CSV/";
		
		File f = new File(directoryJson);
		File f2 = new File(directoryCSV);
		
		BufferedReader reader;
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		JsonObject json = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject();
		int resultado = json.get("total").getAsInt();
		
		if((resultado % PAGE_SIZE) == 0) {
			limite = (resultado/PAGE_SIZE);
		}
		else {
			limite = (resultado/PAGE_SIZE)+1;
		}
		
		System.out.println("Foram encontradas "+ resultado + " issues em " + limite + " paginas.");
		
			while(iterator <= limite) {
		
				URL url2 = new URL(url+"&p="+pageIndex);
				conn = (HttpsURLConnection) url2.openConnection();
		
				System.out.println("Recuperando as issues da pagina "+pageIndex+".");
				conn.setRequestMethod("GET");
				
				reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String line;
				while((line = reader.readLine()) != null) {
					result.append(line);
				}
				
				String stringResult = result.toString();
				JSONObject temp = new JSONObject(stringResult);
				
				JSONArray issues = temp.getJSONArray("issues");
				
				List<Issue> issuesArray = new ArrayList<>();
				
				for(int i = 0; i < issues.length(); i++) {
					JSONObject issue = (JSONObject) issues.get(i);
					issuesArray.add(new Issue(issue));
				}
				
				for (Issue issue : issuesArray) {
					/**
					String startDate =  issue.getDataInicio().toString();
					DateTimeFormatter VDC = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z uuuu").withLocale(Locale.US);
					ZonedDateTime zdt = ZonedDateTime.parse(startDate, VDC);
					
					java.time.LocalDate ld = zdt.toLocalDate();
					DateTimeFormatter fLocalDate = DateTimeFormatter.ofPattern("uuuu-MM-dd");
					String output =  ld.format(fLocalDate) ;
					Date formatedDate = (Date) fLocalDate.parse(output);
					issue.setDataInicio(formatedDate);
					**/
					DateTime dtInicio = new DateTime(issue.getDataInicio());
					DateTime dtFim = issue.getDataFim() == null ? null : new DateTime(issue.getDataFim());
					
					if (dtInicio.isAfter(fimPosterior) || (dtFim != null && dtFim.isBefore(inicioAnterior))
							|| (new Interval(fimAnterior, inicioPosterior).contains(dtInicio))) {
						continue;
					}
					
					for (Bucket bucket : buckets) {
						
						DateTime inicioBucket = new DateTime(bucket.getInicio());
						DateTime fimBucket = new DateTime(bucket.getFim());
						Interval intervalo = new Interval(inicioBucket, fimBucket);
						
						if ((intervalo.contains(dtInicio) || 
							(dtFim != null && (intervalo.contains(dtFim))) || 
							(dtInicio.isBefore(inicioBucket) && (dtFim == null || dtFim.isAfter(fimBucket))))) {
							
							bucket.addIssue(issue);
						}
					}
				}
				
				iterator++;
				pageIndex++;
				result = new StringBuilder();
				conn.disconnect();
			}
			
			System.out.println("Salvando as issues em arquivos.");
			
			f.mkdirs();
			f2.mkdirs();
			
			for (contSave = 0; contSave < buckets.size();contSave++) {
				System.out.println("O bucket "+contSave+ " inicia em " + buckets.get(contSave).getInicio()+" e termina em "+ buckets.get(contSave).getFim());
				writeFile = new FileWriter(new File(directoryJson+contSave+".json"));
				writeFileCSV = new FileWriter(new File(directoryCSV+contSave+".csv"));
				
				JSONObject JSONObjectWriter = new JSONObject(buckets.get(contSave));
				JSONArray JSONArrayWriter = JSONObjectWriter.getJSONArray("issues");
				
				String csvParser = CDL.toString(JSONArrayWriter);
				try {
					writeFileCSV.write(csvParser);
				} catch (Exception e) {
					writeFileCSV.write("Nao tem nada nessa bagaca");
				}
				
				writeFile.write(JSONArrayWriter.toString());
				writeFile.close();
				writeFileCSV.close(); 
			}
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {	
		
		String method = "issues/search";
		String directory = "resources/apache/";
		//SEMPRE ALTERAR A CHAVE DO PROJETO E A SUA DATA DE ADOCAO DE CI
		String projectKey = "org.apache.edgent:edgent-parent";
		referencia = new DateTime(2016, 3, 10, 0, 0, 0);
		
		int pageIndex = 1;
		URL urlApache = new URL(RESPOSITORY + method + "?" + "componentRoots=" + projectKey + "&pageSize=" + PAGE_SIZE);
		URL urlSonarCloud = new URL(RESPOSITORY + method + "?" + "componentKeys=" + projectKey + "&ps=" + PAGE_SIZE);
		
		fimAnterior = referencia.minusDays(15);
		inicioAnterior = fimAnterior.minusDays(360);
		
		inicioPosterior = referencia.plusDays(15);
		fimPosterior = inicioPosterior.plusDays(360);
				
		DateTime inicioAnteriorRef = inicioAnterior;
		while (inicioAnteriorRef.isBefore(fimAnterior)) {
			Bucket bucket = new Bucket();
			bucket.setInicio(inicioAnteriorRef.toDate());
			
			inicioAnteriorRef = inicioAnteriorRef.plusDays(30);
			
			bucket.setFim(inicioAnteriorRef.toDate());
			buckets.add(bucket);
		}
		
		DateTime inicioPosteriorRef = inicioPosterior;
		while(inicioPosteriorRef.isBefore(fimPosterior)) {
			Bucket bucket = new Bucket();
			bucket.setInicio(inicioPosteriorRef.toDate());
			
			inicioPosteriorRef = inicioPosteriorRef.plusDays(30);
			
			bucket.setFim(inicioPosteriorRef.toDate());
			buckets.add(bucket);
		}
		
		System.out.println("Analizando o projeto: " + projectKey);
		retriveIssuesProject(urlApache, projectKey, directory, pageIndex);
		System.out.println("O projeto "+projectKey+" foi analizado.");
	}
}
