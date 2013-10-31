package com.cyanogenmod.settings.device;

import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.Preference;
import android.widget.Toast;
import com.cyanogenmod.settings.device.R;

final class KillMediaTask implements Preference.OnPreferenceClickListener {

	Context mContext;

	KillMediaTask(Context context) {
		mContext = context;
	}

	public final boolean onPreferenceClick(final Preference preference) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(mContext.getText(R.string.media_kill_gen));
		dialog.setMessage(mContext.getText(R.string.media_kill_dialog));
		dialog.setPositiveButton(mContext.getText(R.string.continew), new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				killMedia.execute();
			}

		});
		dialog.setNegativeButton(mContext.getText(R.string.cancel), null);
		dialog.show();

		return false;

	}

	private AsyncTask<String, Integer, String> killMedia = new AsyncTask<String, Integer, String>() {

		ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage(mContext.getText(R.string.media_kill_process));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {

			try {
				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec("su");
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						process.getOutputStream());
				outputStreamWriter.write("killall -9 mediaserver");
				outputStreamWriter.flush();
				outputStreamWriter.close();
				process.waitFor();
				process.destroy();

				return mContext.getText(R.string.media_kill_success).toString();
			} catch (Exception e) {
				return mContext.getText(R.string.media_kill_failed).toString();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.cancel();
			Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
		}

	};

}
