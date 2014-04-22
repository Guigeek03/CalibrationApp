package fr.utbm.calibrationapp.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class NetworkUtils extends AsyncTask<URL, Integer, Long> {	
	public String sendRequest(URL url) {
		String response = "";
		Log.d("REQUEST_SENT", "SENT REQUEST = " + url.getHost() + ":" + url.getPort() + url.getFile());
		try {
			// Initialize the URL and cast it in a HttpURLConnection
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			try {
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				response = getStringFromInputStream(in);
				Log.d("HTTP", "Sent request and received : " + response);
			} finally {
				urlConnection.disconnect();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();
	}

	
	
	@Override
	protected Long doInBackground(URL... urls) {
		for (int i = 0; i < urls.length; ++i) {
			sendRequest(urls[i]);
		}
		return null;
	}

}
