package com.choosie.cameratry2;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.os.AsyncTask;

public class HttpHandler extends AsyncTask<String, Void, String> {

	private HttpClient m_httpClient;	
	private HttpPost m_httpPost;
	
	public HttpHandler(HttpClient httpClient, HttpPost httpPost)
	{
		m_httpClient = httpClient;
		m_httpPost = httpPost; 
	}
	
	@Override
	protected String doInBackground(String... params) {
		HttpResponse response = null;
		try {
			response = m_httpClient.execute(m_httpPost);
			response.getEntity().getContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "oren";       
	}
}


