package com.lichard49.hoptheline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class RequestsFragment extends Fragment
{
	private Firebase firebase;
	private String ID;
	private Context context;
	
	private EditText requestField;
	private EditText requestValueField;
	
	private GPSTracker gps;
	private final String baseURL= "https://maps.googleapis.com/maps/api/place/textsearch/json";
	private final String apiKey = "AIzaSyACATQdsDQLuB54k8aJ8PoFsuQFroRbHaU";
	private Spinner spinner;
	
	private RequestsFragment(Context context)
	{
		this.context = context;
		
		Firebase.setAndroidContext(context);
		firebase = new Firebase("https://hoptheline2.firebaseio.com/");
		
		TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		ID = tMgr.getLine1Number();
	}
	
	private OnClickListener addData = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Firebase me = firebase.child(""+ID);
			Map<String, String> values = new HashMap<String, String>();
			values.put("description", requestField.getText().toString());
			values.put("price", requestValueField.getText().toString());
			values.put("status", "posted");
			me.setValue(values);
			
			Toast.makeText(context, "Request posted", Toast.LENGTH_SHORT).show();
		}
	};

	private class LocationRequestTask extends AsyncTask<String, String, String[]>{
		
	    @Override
	    protected String[] doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        String[] locationsList = new String[5];
	        try {
	            response = httpclient.execute(new HttpGet(uri[0]));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	            //TODO Handle problems..
	        } catch (IOException e) {
	            //TODO Handle problems..
	        }
	        try {
	        	JSONObject jObject = new JSONObject(responseString);
		        JSONArray jArray = jObject.getJSONArray("results");	
		        for (int i=0; i < 5; i++)
		        {
		            try {
		                JSONObject jObj = jArray.getJSONObject(i);
		                // Pulling items from the array
		                String s = jObj.getString("name") + ";" + jObj.getString("formatted_address");
		                locationsList[i] = s;
		                Log.d("location", s);
		            } catch (JSONException e) {
		                // Oops
		            }
		        }
	        } catch(Exception e) {
	        	e.printStackTrace();
	        }
	        
	        return locationsList;
	    }
	    
	    /*@Override
	    protected void onPostExecute(Void result) {
	        mimagesPagerFragment.updateAdapter(mImages);
	    }
	    @Override
	    protected void onPostExecute(Void fuck) {
	    	ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(HopLineActivity.this,   android.R.layout.simple_spinner_item, locationsList);
	        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	        spinner.setAdapter(spinnerArrayAdapter);
	    }*/
	}
	
	public static Fragment newInstance(Context context)
	{
        RequestsFragment f = new RequestsFragment(context);
        return f;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.requests, container, false);
        
        requestField = (EditText) root.findViewById(R.id.request_field);
		requestValueField = (EditText) root.findViewById(R.id.request_value_field);
		Button requestButton = (Button) root.findViewById(R.id.request_button);
		requestButton.setOnClickListener(addData);
		
		gps = new GPSTracker(context);
		try {
			String[] locationsList = new LocationRequestTask().execute(baseURL + "?query=coffee&location=" + gps.getLatitude() + "," + gps.getLongitude() + "&radius=1&key=" + apiKey).get();
			spinner = (Spinner) root.findViewById(R.id.location_spinner);
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, locationsList);
	        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
	        spinner.setAdapter(spinnerArrayAdapter);
		} catch (Exception e) {
			//well fuck
		}
        
        return root;
    }
}
