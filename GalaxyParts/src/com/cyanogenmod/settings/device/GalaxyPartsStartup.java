package com.cyanogenmod.settings.device;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.cyanogenmod.settings.device.R;

public class GalaxyPartsStartup extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent bootintent) {

		// Initialize attenuation values
		if (context.getResources().getBoolean(R.bool.attenuation_feature)) {
			String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION,
				context.getString(R.string.attenuation_defaults)).split(",");
			SystemProperties.set(Constants.PROP_ATTENUATION,
				String.valueOf(ATTENUATION_ARRAY[0]) + "," +
				String.valueOf(ATTENUATION_ARRAY[1]) + "," +
				String.valueOf(ATTENUATION_ARRAY[2]) + "," +
				String.valueOf(ATTENUATION_ARRAY[3]));
		}

		// Initialize extamp values
		if (context.getResources().getBoolean(R.bool.extamp_feature)) {
			String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP, context.getString(R.string.extamp_defaults)).split(",");
			SystemProperties.set(Constants.PROP_EXTAMP,
				String.valueOf(EXTAMP_ARRAY[0]) + "," +
				String.valueOf(EXTAMP_ARRAY[1]) + "," +
				String.valueOf(EXTAMP_ARRAY[2]) + "," +
				String.valueOf(EXTAMP_ARRAY[3]) + "," +
				String.valueOf(EXTAMP_ARRAY[4]));
		}

		// Initialize fake dual-touch value
		if (context.getResources().getBoolean(R.bool.dualtouch_feature)) {
			String currentFakeDt = SystemProperties.get(Constants.PROP_FAKE_DT, "0");
			SystemProperties.set(Constants.PROP_FAKE_DT, currentFakeDt);
		}

		System.exit(0);
	}
}
