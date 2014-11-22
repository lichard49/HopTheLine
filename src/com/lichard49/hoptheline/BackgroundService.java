package com.lichard49.hoptheline;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class BackgroundService extends Service
{
	private Firebase firebase;
	private String ID;
	private NotificationCompat.Builder notifBuilder;
	private Intent resultIntent;
	private PendingIntent resultPendingIntent;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	public void onCreate()
	{
		super.onCreate();
		
		TelephonyManager tMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		ID = tMgr.getLine1Number();
		
		Firebase.setAndroidContext(this);
		firebase = new Firebase("https://hoptheline2.firebaseio.com/");
		firebase.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onCancelled(FirebaseError e) { }

			@Override
			public void onDataChange(DataSnapshot datas)
			{
				boolean notify = false;
				for(DataSnapshot data: datas.getChildren())
				{
					if(!data.getName().equals(ID))
					{
						notify = true;
						break;
					}
				}
				if(notify)
				{
					postNotification();
				}
			}
		});
	}
	
	private void postNotification()
	{
		notifBuilder = new NotificationCompat.Builder(this);
		resultIntent = new Intent(this, HopLineActivity.class);
		resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notifBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifBuilder.setContentTitle("Hop the Line");
		notifBuilder.setContentText("People are in line with you. Check them out.");
		notifBuilder.setContentIntent(resultPendingIntent);
		System.out.println("notify");
		NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.notify(0, notifBuilder.build());
	}
}
