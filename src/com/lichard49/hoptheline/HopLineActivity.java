package com.lichard49.hoptheline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


public class HopLineActivity extends Activity
{
	private EditText requestField;
	private EditText requestValueField;
	private ListView requestList;
	private ArrayList<RequestItem> requestItems;
	private RequestListAdapter requestListAdapter;
	
	private Firebase firebase;
	private static long ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hop_line);

		Firebase.setAndroidContext(this);
		firebase = new Firebase("https://hoptheline2.firebaseio.com/");
		firebase.addValueEventListener(handleData);
		
		ID = (long)(Math.random()*Long.MAX_VALUE);
		
		requestField = (EditText) findViewById(R.id.request_field);
		requestValueField = (EditText) findViewById(R.id.request_value_field);
		Button requestButton = (Button) findViewById(R.id.request_button);
		requestButton.setOnClickListener(addData);
		
		requestList = (ListView) findViewById(R.id.request_list);
		requestItems = new ArrayList<RequestItem>();
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
					RequestItem item = new RequestItem();
					item.description = (String) data.child("description").getValue();
					item.value = Double.parseDouble(data.child("price").getValue().toString());
					requestItems.add(item);
				}
				requestListAdapter = new RequestListAdapter(getApplicationContext(), requestItems);
				requestList.setAdapter(requestListAdapter);
			}
			catch (Exception e1)
			{
				
			}
		}
	};
	
	private class RequestListAdapter extends ArrayAdapter<RequestItem>
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
}
