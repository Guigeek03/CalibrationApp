package fr.utbm.calibrationapp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class NetworkUtils {
	private static final String DEBUG_TAG = "HTTP_REQUEST";
	public static final String UNABLE_TO_CONTACT_SERVER = "{\"success\":false,\"exception\":\"Unable to contact the server !\"}";

	public static String sendRequest(String urls) throws IOException {
		InputStream is = null;
		Log.d("REQUEST_SENT", "SENT REQUEST = " + urls);
		try {
			// Initialize the URL and cast it in a HttpURLConnection
			URL url = new URL(urls);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(10000 /* milliseconds */);
			urlConnection.setConnectTimeout(15000 /* milliseconds */);
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoInput(true);

			// Starts the query
			urlConnection.connect();
			int response = urlConnection.getResponseCode();
			Log.d(DEBUG_TAG, "The response is: " + response);
			is = urlConnection.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = null;
			if (is.available() != 0) {
				contentAsString = readIt(is, is.available());
			} else {
				contentAsString = streamToString(is);
			}
			return contentAsString;

		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	public static String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

	public static String streamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
}
