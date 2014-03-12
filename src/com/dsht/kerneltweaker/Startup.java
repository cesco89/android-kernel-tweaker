package com.dsht.kerneltweaker;

import java.util.Iterator;
import java.util.List;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class Startup extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent bootintent) {
		// TODO Auto-generated method stub

		DatabaseHandler db = new DatabaseHandler(context);
		VddDatabaseHandler vddDb = new VddDatabaseHandler(context);
		applyValuesAsync(context, db, vddDb, false);
	}

	public static void applyValuesAsync(final Context mContext, final DatabaseHandler db, final VddDatabaseHandler vddDb, final boolean debug) {

		class LongOperation extends AsyncTask<String, Void, String> {

			ProgressDialog pd;

			@Override
			protected void onPreExecute() {
				if(debug) {
					pd = new ProgressDialog(mContext);
					pd.setIndeterminate(true);
					pd.setMessage("Applying values...Please wait");
					pd.setCancelable(false);
					pd.show();
				}
			}

			@Override
			protected String doInBackground(String... params) {
				List<DataItem> items = db.getAllItems();
				List<DataItem> vddItems = vddDb.getAllItems();

				if(items.size() != 0) {
					
					for(DataItem item : items) {
						if(item.getFileName().contains("TCP Congestion control")) {
							String cmd = item.getName().replaceAll("'", "");
							CMDProcessor.runSuCommand(cmd);
							Helpers.debugger(mContext, "---TCP---");
							Helpers.debugger(mContext,item.getName().replaceAll("'", "") );

						}else {
							String value = item.getValue();
							String fPath = item.getName().replaceAll("'", "");
							String cmd = "echo \""+value+"\" > "+fPath;
							CMDProcessor.runSuCommand(cmd);
							Helpers.debugger(mContext, item.getFileName());
							Helpers.debugger(mContext, "echo \""+value+"\" > "+fPath);
							Helpers.checkApply(mContext, item.getFileName(), value , fPath);
						}
						
					}
				}
				if(vddItems.size() != 0 ) {
					Helpers.debugger(mContext, "---VDD---");
					for(DataItem item : vddItems) {
						String path = item.getName().replaceAll("'", "");
						String value = item.getValue().replaceAll("'", "");
						CMDProcessor.runSuCommand("echo \""+value+"\" > "+path);
						Helpers.debugger(mContext, "echo \""+value+"\" > "+path);
						Helpers.checkApply(mContext, item.getFileName(), value , path);
					}
				}
				return "executed";
			}

			@Override
			protected void onPostExecute(String result) {
				if(debug) {
					pd.dismiss();
				}
			}
		}
		new LongOperation().execute();
	}

}
