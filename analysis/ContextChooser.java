import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import org.json.JSONObject;
import org.json.JSONArray;

public class ContextChooser	{
	
	public ContextChooser()	{

	}

	public static void main(String[] args) throws Exception {
 
		ContextChooser http = new ContextChooser();
 
		// System.out.println("Testing 1 - Send Http GET request");
		// http.sendGet();
 
		System.out.println("\nTesting 2 - Send Http POST request");
		http.sendPost();
 
	}
 
	// HTTP GET request
	private void sendGet() throws Exception {
 
		String url = "http://www.google.com/search?q=mkyong";
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		//con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
 
	}
 
	// HTTP POST request
	private void sendPost() throws Exception {
 
		String url = "http://access.alchemyapi.com/calls/text/TextGetRankedKeywords";
		URL obj = new URL(url);
		String urlParameters = "apikey=7b20d889727147640e3e3ca71ed8220d80ec16aa&text=" + URLEncoder.encode("There are so many chocolates and sweets", "ISO-8859-1") + "&outputMode=json";
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
 //String url = "http://access.alchemyapi.com/calls/text/TextGetRankedKeywords?apikey=7b20d889727147640e3e3ca71ed8220d80ec16aa&text=" + URLEncoder.encode("I love chocolate and sweets", "ISO-8859-1") + "&outputMode=json"; 

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
 
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String input;
		StringBuffer returned = new StringBuffer();
 
		while ((input = in.readLine()) != null) {
			returned.append(input);
		}
		in.close();

		float cumulativeRelevance = 0;

		try {

			JSONObject jsonObj = new JSONObject(returned.toString());
			JSONArray array = new JSONArray();
			array = jsonObj.getJSONArray("keywords");
			System.out.println(jsonObj.toString());
			for (int i = 0; i<array.length(); i++)	{
				//System.out.println(((JSONObject)array.get(i)).get("relevance"));
				cumulativeRelevance = cumulativeRelevance + Float.valueOf(((JSONObject)array.get(i)).get("relevance").toString());
			}

			System.out.println("relevance: " + Float.toString(cumulativeRelevance));
			//System.out.println(array.toString());


			//System.out.println(jsonObj.toString());
			System.out.println();
		//	System.out.println(keywords);
		}catch(Exception e)	{
	         e.printStackTrace();
	      }
		 // try{
	  //        Object o = parser.parse(returned.toString());
	  //        JSONArray array = (JSONArray)o;
	  //        System.out.println("The 2nd element of array");
	  //        System.out.println(array.get(1));
	  //        System.out.println();

	  //        JSONObject obj2 = (JSONObject)array.get(1);
	  //        System.out.println("Field \"1\"");
	  //        System.out.println(obj2.get("1"));    

	  //        s = "{}";
	  //        o = parser.parse(s);
	  //        System.out.println(o);

	  //        s = "[5,]";
	  //        o = parser.parse(s);
	  //        System.out.println(o);

	  //        s = "[5,,2]";
	  //        o = parser.parse(s);
	  //        System.out.println(o);
	  //     }catch(ParseException pe)	{
	  //        System.out.println("position: " + pe.getPosition());
	  //        System.out.println(pe);
	  //     }
		
		//System.out.println(returned);
  
	}
}


//7b20d889727147640e3e3ca71ed8220d80ec16aa
//String url = "http://access.alchemyapi.com/calls/text/TextGetRankedKeywords?apikey=7b20d889727147640e3e3ca71ed8220d80ec16aa&text=" + URLEncoder.encode("I love chocolate and sweets", "ISO-8859-1") + "&outputMode=json"; 


// Or "UTF-8".