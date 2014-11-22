package com.lichard49.hoptheline;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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