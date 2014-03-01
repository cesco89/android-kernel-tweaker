package com.dsht.kerneltweaker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

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
						CommandCapture command = null;
						if(item.getFileName().contains("TCP Congestion control")) {
							command = new CommandCapture(0, item.getName().replaceAll("'", ""));
							Helpers.debugger(mContext, "---TCP---");
							Helpers.debugger(mContext,item.getName().replaceAll("'", "") );

						}else {
							String value = item.getValue();
							String fPath = item.getName().replaceAll("'", "");
							command = new CommandCapture(0, "echo \""+value+"\" > "+fPath);
							Helpers.debugger(mContext, item.getFileName());
							Helpers.debugger(mContext, "echo \""+value+"\" > "+fPath);
							Helpers.checkApply(mContext, item.getFileName(), value , fPath);
						}
						try {
							RootTools.getShell(true).add(command);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (RootDeniedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if(vddItems.size() != 0 ) {
					Helpers.debugger(mContext, "---VDD---");
					for(DataItem item : vddItems) {
						String path = item.getName().replaceAll("'", "");
						String value = item.getValue().replaceAll("'", "");
						CommandCapture command = new CommandCapture(0, "echo \""+value+"\" > "+path);
						try {
							RootTools.getShell(true).add(command);
							Helpers.debugger(mContext, "echo \""+value+"\" > "+path);
							Helpers.checkApply(mContext, item.getFileName(), value , path);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (RootDeniedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
