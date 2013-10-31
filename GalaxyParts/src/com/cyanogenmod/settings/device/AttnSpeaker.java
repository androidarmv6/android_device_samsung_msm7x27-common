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

final class AttnSpeaker implements Preference.OnPreferenceClickListener {

	int device_index = 2;
	Context mContext;
	Resources mResources;
	int mSelected;

	AttnSpeaker(Context context, Resources resources) {
		mContext = context;
		mResources = resources;
	}

	public final boolean onPreferenceClick(final Preference preference) {
		final String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION,
			mContext.getString(R.string.attenuation_defaults)).split(",");
		final String[] ATTENUATION_DEFAULT_ARRAY =
			mContext.getString(R.string.attenuation_defaults).split(",");

		try {
			mSelected = Integer.parseInt(ATTENUATION_ARRAY[device_index]);
		} catch (NumberFormatException e) {
			mSelected = 0;
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(mContext.getText(R.string.attn_speaker));
		dialog.setSingleChoiceItems(Constants.getAttn(mResources), mSelected,
				new OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						ATTENUATION_ARRAY[device_index] = Integer.toString(arg1);
						SystemProperties.set(Constants.PROP_ATTENUATION, 
							ATTENUATION_ARRAY[0] + "," +
							ATTENUATION_ARRAY[1] + "," +
							ATTENUATION_ARRAY[2] + "," +
							ATTENUATION_ARRAY[3]);
						preference.setSummary(mContext
							.getText(R.string.current_setting)
							+ ": "
							+ Constants.getAttn(mResources)[arg1]
							.toString() + "\n"
							+ mContext.getText(R.string.default_setting)
							+ ": "
							+ Constants.getAttn(mResources)[Integer
							.parseInt(ATTENUATION_DEFAULT_ARRAY[device_index])]
							.toString());
						arg0.dismiss();
					}

				});
		dialog.setNegativeButton(mContext.getText(R.string.cancel), null);
		dialog.show();
		return false;

	}
}
