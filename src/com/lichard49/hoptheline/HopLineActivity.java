package com.lichard49.hoptheline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class HopLineActivity extends Activity
{
	private Dialog dialog;
	
	private EditText requestField;
	private EditText requestValueField;
	private ListView requestList;
	private ArrayList<RequestItem> requestItems;
	private RequestListAdapter requestListAdapter;
	
	private Firebase firebase;
	private String ID; 

	private GPSTracker gps;
	private final String baseURL= "https://maps.googleapis.com/maps/api/place/textsearch/json";
	private final String apiKey = "AIzaSyACATQdsDQLuB54k8aJ8PoFsuQFroRbHaU";
	private final int REQUEST_CODE_VENMO_APP_SWITCH = 134;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hop_line);
		
		dialog = new Dialog(HopLineActivity.this);
		dialog.setTitle("Thanks for hopping!");
		dialog.setContentView(R.layout.request_dialog);
		
		Firebase.setAndroidContext(this);
		firebase = new Firebase("https://hoptheline2.firebaseio.com/");
		firebase.addValueEventListener(handleData);
		
		//ID = Secure.getString(getApplicationContext().getContentResolver(),
	    //        Secure.ANDROID_ID);
		TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		ID = tMgr.getLine1Number();
		
		requestField = (EditText) findViewById(R.id.request_field);
		requestValueField = (EditText) findViewById(R.id.request_value_field);
		Button requestButton = (Button) findViewById(R.id.request_button);
		requestButton.setOnClickListener(addData);
		
		requestList = (ListView) findViewById(R.id.request_list);
		requestList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				RequestItem me = requestItems.get(0);
				
				dialogHandler.sendEmptyMessage(position);
			}
			
		});
		requestItems = new ArrayList<RequestItem>();
		gps = new GPSTracker(this);
		new LocationRequestTask().execute(baseURL + "?query=coffee&location=" + gps.getLatitude() + "," + gps.getLongitude() + "&radius=1&key=" + apiKey);
	}
	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first
	    Log.d("onpause", "pause");
	    
	    startService(new Intent(HopLineActivity.this,FirebaseBackgroundService.class));
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
	    Log.d("onResume", "resume");
	    
	    stopService(new Intent(HopLineActivity.this,FirebaseBackgroundService.class));
	}

	private int pressedPosition = -1;
	private Handler dialogHandler = new Handler()
	{
		public void handleMessage(Message m)
		{
			if(dialog.isShowing())
			{
				dialog.hide();
			}
			else
			{
				pressedPosition = m.what;
				dialog.show();
				Button dialogButton = (Button) dialog.findViewById(R.id.dialog_button);
				dialogButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						RequestItem mine = requestItems.get(pressedPosition);
						Firebase me = firebase.child(""+mine.ID);
						me.child("status").setValue("requestmoney");
						dialogHandler.sendEmptyMessage(0);
					}
				});
			}
		}
	};
	
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
		}
	};
	
	private ValueEventListener handleData = new ValueEventListener()
	{
		@Override
		public void onCancelled(FirebaseError e) { }

		@Override
		public void onDataChange(DataSnapshot e)
		{
			try
			{
				requestItems.clear();
				for(DataSnapshot data: e.getChildren())
				{
					if(!data.getName().equals(ID))
					{
						RequestItem item = new RequestItem();
						item.description = (String) data.child("description").getValue();
						item.value = Double.parseDouble(data.child("price").getValue().toString());
						item.ID = data.getName();
						requestItems.add(item);
					}
					else
					{
						if(((String)data.child("status").getValue()).equals("requestmoney"))
						{	
							data.child("status").getRef().setValue("moneysent");
							Toast.makeText(getApplicationContext(), "Sending money now", Toast.LENGTH_SHORT).show();
							
							//wrong phone number to charge
							//Intent venmoIntent = VenmoLibrary.openVenmoPayment("2133", "HopTheLine", data.getName(), data.child("price").getValue().toString(), data.child("description").getValue().toString(), "pay");
					        //startActivityForResult(venmoIntent, REQUEST_CODE_VENMO_APP_SWITCH);
						}
					}
				}
				requestListAdapter = new RequestListAdapter(getApplicationContext(), requestItems);
				requestList.setAdapter(requestListAdapter);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	};
	
	public class RequestListAdapter extends ArrayAdapter<RequestItem>
	{
		private Context context;
		private final List<RequestItem> items;
		private DecimalFormat moneyFormat = new DecimalFormat("#.##");
		
		public RequestListAdapter(Context context, List<RequestItem> items)
		{
			super(context, R.layout.request_item, items);
			this.context = context;
			this.items = items;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater =
					(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = inflater.inflate(R.layout.request_item, parent, false);
			TextView itemPrice = (TextView) row.findViewById(R.id.item_price);
			TextView itemDescription = (TextView) row.findViewById(R.id.item_description);
			
			RequestItem item = items.get(position);
			itemPrice.setText(moneyFormat.format(item.value));
			itemDescription.setText(item.description);
			
			return row;
		}
		
		public int getCount()
		{
			return items.size();
		}
	}
	private class LocationRequestTask extends AsyncTask<String, String, String>{

	    @Override
	    protected String doInBackground(String... uri) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
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
		                Log.d("location", s);
		            } catch (JSONException e) {
		                // Oops
		            }
		        }
	        } catch(Exception e) {
	        	e.printStackTrace();
	        }
	        
	        return responseString;
	    }
	}
}
