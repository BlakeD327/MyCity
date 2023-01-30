import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class CitySniffer {

	private static HttpURLConnection connection = null;

	// returns the HTTP response code
	private static int getResponse(String urlStr) {
		int statusCode = 0;
		try {
			URL url = new URL(urlStr); //create a url from the string
			connection = (HttpURLConnection)url.openConnection();
			statusCode = connection.getResponseCode();
			if(connection != null){
				connection.disconnect();
			}
		} catch(MalformedURLException e)
			{ e.printStackTrace();
		} catch(IOException e) 
			{ e.printStackTrace();
		} finally {
			if( connection != null) {
				connection.disconnect();
			}
		}
		return statusCode;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Here we go!");
		//parse cli inputs into url string and number of hops(int)
		System.out.println(args[0]);
		String root = args[1]; //starting URL
		int hops = Integer.parseInt(args[1]); //number of URL hops		

		Stack<String> urlStack = new Stack<>(); //urlStack of URLs to visit
		HashSet<String> visited = new HashSet<>(); //previously visited URLs
		urlStack.push(root);
		visited.add(root);

		String rawHtml, newUrl;
		int status;
		

		while (!urlStack.empty()) {
			//if (urlStack == null || urlStack.size() == 0) {
			if (hops == 0) {
				System.out.println("stack empty");
				break;
			}
			String urlStr = urlStack.pop();
			rawHtml = ""; //reset
			status = getResponse(urlStr);

			if (status == 500) {
				//?
				//break;
				System.out.println("500");
			}
			if (status == 404) {
				System.out.println("404");
			}
			if (status == 301) {
				URL url = new URL(urlStr);
				connection = (HttpURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("accept", "*/*");
				connection.setRequestProperty("connection", "Keep-Alive");
				// set false，otherwise redirect to Location address
				connection.setInstanceFollowRedirects(false);
				// get Location address
				String newUrlStr = connection.getHeaderField("Location");
				status = getResponse(newUrlStr);
				urlStack.add(newUrlStr);
				//urlStack.add(new)
			}
			else if (status == 200) {
				hops--; //decrement count
				System.out.print(urlStr);
				try {
					URL url = new URL(urlStr); //create a url from the string
					connection = (HttpURLConnection)url.openConnection();
					BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
						rawHtml += line;
					}
				} catch(MalformedURLException e) {
					e.printStackTrace();
				} catch(IOException e){
					e.printStackTrace();
				} finally { 
					if (connection != null) {
						connection.disconnect();
					}
				}
				//search html line and add new urls
				Pattern pattern = Pattern.compile("<a\\s(.*?)href=\"http(.*?)\"");
				Matcher matcher = pattern.matcher(rawHtml);
				while (matcher.find()) {
					newUrl = matcher.group(1);
					if(newUrl.startsWith("http")){
						urlStack.add(newUrl);
					}					
				}
			}
		}
		System.out.print("end");
	}
}
/*
public class CitySnifer
{
	public static void main(String[] args) throws IOException {
		//parse cli inputs into url string and number of hops(int)
		//String stringUrl = args[0];
		//int hops = Integer.parseInt(args[1]);		

		//make a URL object from the input String
		//URL url = new URL(stringUrl);

		//make the connection to fetch resources
		//HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		int statusCode = connection.getResponseCode();
		System.out.println(statusCode);

		BufferedReader reader = new BufferedReader(
			new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}

		int count = 0;
		while (count < hops) {

		}
	}
}*/
