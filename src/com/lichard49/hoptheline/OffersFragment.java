package com.lichard49.hoptheline;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class OffersFragment extends Fragment
{
	private Dialog dialog;
	private Firebase firebase;
	
	private ArrayList<RequestItem> requestItems;
	private RequestListAdapter requestListAdapter;
	private ListView requestList;
	
	private String ID; 

	private Context context;
	
	private final int REQUEST_CODE_VENMO_APP_SWITCH = 134;
	
	private OffersFragment(Context context)
	{
		this.context = context;
		
		Firebase.setAndroidContext(context);
		firebase = new Firebase("https://hoptheline2.firebaseio.com/");
		firebase.addValueEventListener(handleData);
		
		//ID = Secure.getString(getApplicationContext().getContentResolver(),
	    //        Secure.ANDROID_ID);
		TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		ID = tMgr.getLine1Number();

		requestItems = new ArrayList<RequestItem>();
	}
	
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
						if(((String)data.child("status").getValue()).contains("requestmoney"))
						{
							String theirID = ((String)data.child("status").getValue()).replace("requestmoney", "");
							data.child("status").getRef().setValue("moneysent");
							Toast.makeText(context, "Sending money now", Toast.LENGTH_SHORT).show();
							
							//wrong phone number to charge
							Intent venmoIntent = VenmoLibrary.openVenmoPayment("2133", "HopTheLine", theirID, data.child("price").getValue().toString(), data.child("description").getValue().toString(), "pay");
					        startActivityForResult(venmoIntent, REQUEST_CODE_VENMO_APP_SWITCH);
						}
					}
				}
				requestListAdapter = new RequestListAdapter(context, requestItems);
				requestList.setAdapter(requestListAdapter);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	};

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
						me.child("status").setValue("requestmoney"+ID);
						dialogHandler.sendEmptyMessage(0);
					}
				});
			}
		}
	};
	
	
	public static Fragment newInstance(Context context)
	{
        OffersFragment f = new OffersFragment(context);
        return f;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.offers, container, false);
        
        dialog = new Dialog(getActivity());
		dialog.setTitle("Thanks for hopping!");
		dialog.setContentView(R.layout.request_dialog);
		System.out.println("Offers oncreateview");
        
        requestList = (ListView) root.findViewById(R.id.request_list);
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
        
        return root;
    }
}
