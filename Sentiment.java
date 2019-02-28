import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;

public class Sentiment {
	public static void main(String[] args) throws Exception {
		List<String> tweets = getTweetsFromSearch("dinosaurs");
		double finalScore = 0;
		for (String tweet: tweets) {
			double score = getSentiment(tweet);
			finalScore += score;
		}
		finalScore = finalScore/tweets.size();
		System.out.println(finalScore);
	}
	
	public static List<String> getTweetsFromSearch(String searchTerm) {
		//Object to give framework Twitter4J
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("<ConsumerKey>");
		cb.setOAuthConsumerSecret("<ConsumerSecret>");
		cb.setOAuthAccessToken("<AccessToken>");
		cb.setOAuthAccessTokenSecret("<AccessTokenSecret>");
		//build Twitter Factory using configuration builder.
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		Query query = new Query(searchTerm);
		List<String> list = new ArrayList<>();
		try {
			QueryResult result = twitter.search(query);
			List<Status> tweets = result.getTweets();
			for (Status tweet : tweets) {
				list.add(tweet.getText());
			}
		} catch(TwitterException exception) {
			System.out.println(exception.getMessage());
		}
		return list;
	}
			
	public static double getSentiment(String tweet) throws Exception {
		URL url = new URL("https://language.googleapis.com/v1/documents:analyzeSentiment?key=<key>");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setDoOutput(true);
		con.setDoInput(true);
		
		JSONObject body = new JSONObject();
		body.put("encodingType", "UTF8");
		JSONObject doc = new JSONObject();
		doc.put("type", "PLAIN_TEXT");
		doc.put("content", tweet);
		doc.put("language", "en-us");
		body.put("document", doc);
		
		OutputStream output = con.getOutputStream();
		output.write(body.toString().getBytes("UTF-8"));
		output.close();
		
		//int responseCode = con.getResponseCode();
		
		InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream(), "utf-8");
		BufferedReader reader = new BufferedReader(inputStreamReader);
		String line = null;
		StringBuffer buffy = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			buffy.append(line);
		}
		String outputString = buffy.toString();
		
		JSONObject response = new JSONObject(outputString);
		JSONObject docSent = (JSONObject) response.get("documentSentiment");
		double score;
		try {
			score = (double) docSent.get("score");
		} catch(Exception e) {
			score = ((Integer) docSent.get("score")).doubleValue();
		}
		return score;
	}
}
