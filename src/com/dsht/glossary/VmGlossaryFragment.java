package com.dsht.glossary;

import com.dsht.kerneltweaker.GlossaryArrayAdapter;
import com.dsht.kerneltweaker.R;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class VmGlossaryFragment extends Fragment {

	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.menu_list, container,false);
		
		ListView list = (ListView) v.findViewById(R.id.navbarlist);
		GlossaryArrayAdapter mAdapter = new GlossaryArrayAdapter(
				mContext,
				R.layout.list_item,
				getResources().getStringArray(R.array.glo_vm_titles), 
				getResources().getStringArray(R.array.glo_vm_descs),
				getResources().getStringArray(R.array.menu_colors)[0]);
		list.setAdapter(mAdapter);
		
		return v;
	}

}
