package com.cyanogenmod.settings.device;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.cyanogenmod.settings.device.R;

final class DynLMK implements Preference.OnPreferenceClickListener {

	Context mContext;
	Resources mResources;
	int mSelected;

	DynLMK(Context context, Resources resources) {
		mContext = context;
		mResources = resources;
	}

	public final boolean onPreferenceClick(final Preference preference) {
		
		mSelected = Integer.parseInt(SystemProperties.get(Constants.PROP_LMK, "0"));

		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(mContext.getText(R.string.lmk));
		dialog.setSingleChoiceItems(Constants.getDynLMK(mResources),
				(mSelected), new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						mSelected = arg1;
						if (mSelected != 0) {
							SystemProperties.set(Constants.PROP_LMK_ADJ, "" + Constants
								.getDynLMKAdj(mResources)[mSelected].toString());
							SystemProperties.set(Constants.PROP_LMK_MINFREE, "" + Constants
								.getDynLMKMin(mResources)[mSelected].toString());
							preference.setSummary(mContext.getText(R.string.current_setting) + ": "
								+ Constants.getDynLMK(mResources)[mSelected]
								+ "\nAdj: " + SystemProperties.get(Constants.PROP_LMK_ADJ)
								+ "\nMin: " + SystemProperties.get(Constants.PROP_LMK_MINFREE));
						} else {
							preference.setSummary(mContext.getText(R.string.current_setting) + ": "
								+ Constants.getDynLMK(mResources)[mSelected]);
						}
						SystemProperties.set(Constants.PROP_LMK, "" + mSelected);
						arg0.dismiss();
					}
				});
		dialog.setNegativeButton(mContext.getText(R.string.cancel), null);
		dialog.show();
		return false;

	}
}
