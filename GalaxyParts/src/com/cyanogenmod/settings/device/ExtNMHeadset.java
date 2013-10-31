package com.cyanogenmod.settings.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.preference.Preference;
import com.cyanogenmod.settings.device.R;

final class ExtNMHeadset implements Preference.OnPreferenceClickListener {

	int device_index = 2;
	Context mContext;
	Resources mResources;
	int mSelected;

	ExtNMHeadset(Context context, Resources resources) {
		mContext = context;
		mResources = resources;
	}

	public final boolean onPreferenceClick(final Preference preference) {
		final String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP,
			mContext.getString(R.string.extamp_defaults)).split(",");
		final String[] EXTAMP_DEFAULT_ARRAY =
			mContext.getString(R.string.extamp_defaults).split(",");

		try {
			mSelected = Integer.parseInt(EXTAMP_ARRAY[device_index]);
		} catch (NumberFormatException e) {
			mSelected = 0;
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(mContext.getText(R.string.attn_nmheadset));
		dialog.setSingleChoiceItems(Constants.getExt(mResources), mSelected,
				new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

						EXTAMP_ARRAY[device_index] = Integer.toString(arg1);
						SystemProperties.set(Constants.PROP_EXTAMP, 
							EXTAMP_ARRAY[0] + "," +
							EXTAMP_ARRAY[1] + "," +
							EXTAMP_ARRAY[2] + "," +
							EXTAMP_ARRAY[3] + "," +
							EXTAMP_ARRAY[4]);
						preference.setSummary(mContext
							.getText(R.string.current_setting)
							+ ": "
							+ Constants.getExt(mResources)[arg1]
							.toString() + "\n"
							+ mContext.getText(R.string.default_setting)
							+ ": "
							+ Constants.getExt(mResources)[Integer
							.parseInt(EXTAMP_DEFAULT_ARRAY[device_index])]
							.toString());
						arg0.dismiss();
					}

				});
		dialog.setNegativeButton(mContext.getText(R.string.cancel), null);
		dialog.show();
		return false;

	}
}
