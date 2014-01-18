package com.cyanogenmod.settings.device;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.preference.Preference;

import android.content.res.Resources;
import android.os.SystemProperties;

public class Constants {

	public static final String PROP_CACHESIZE = "persist.sys.read_ahead_kb";
	public static final String PROP_FAKE_DT = "persist.sys.fakedt";
	public static final String PROP_ATTENUATION = "persist.sys.attenuation";
	public static final String PROP_ROOTACCESS = "persist.sys.root_access";
	public static final String PROP_EXTAMP = "persist.sys.extamp";
	public static final String PROP_SWAP = "persist.sys.swap";
	public static final String PROP_HIGHEND_GFX = "persist.sys.force_highendgfx";

	public static CharSequence[] getAttn(Resources resources) {
		CharSequence[] attn = resources.getStringArray(R.array.attenuation);
		return attn;
	}

	public static CharSequence[] getExt(Resources resources) {
		CharSequence[] ext = resources.getStringArray(R.array.extamp);
		return ext;
	}

	public static CharSequence[] getCacheSize(Resources resources) {
		CharSequence[] cacheSize = resources.getStringArray(R.array.cache_size);
		return cacheSize;
	}

}
