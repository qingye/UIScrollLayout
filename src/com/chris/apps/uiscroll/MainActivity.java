package com.chris.apps.uiscroll;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;

public class MainActivity extends Activity {

	private ListView mListView = null;
	private ListAdapter mAdapter = null;
	private List<String> mList = new ArrayList<String>();
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		
		addData();
		mAdapter = new ListAdapter();
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mAdapter);
	}
	
	private void addData(){
		for(int i = 0; i < 100; i ++){
			mList.add("data: " + i);
		}
	}
	
	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null);
			}
			
			TextView tv = (TextView) convertView.findViewById(R.id.text);
			tv.setText(mList.get(position));
			return convertView;
		}
		
	}
}
