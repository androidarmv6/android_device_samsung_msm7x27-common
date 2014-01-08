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
	public static final String PROP_COMPCACHE = "persist.service.zram";
	public static final String PROP_COMPCACHE_RO = "ro.zram.default";
	public static final String PROP_FAKE_DT = "persist.sys.fakedt";
	public static final String PROP_ATTENUATION = "persist.sys.attenuation";
	public static final String PROP_LMK = "persist.sys.dynlmk";
	public static final String PROP_LMK_ADJ = "persist.sys.dynlmk-adj";
	public static final String PROP_LMK_MINFREE = "persist.sys.dynlmk-minfree";
	public static final String PROP_PROCESSLIMIT = "persist.sys.process_limit";
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

	public static CharSequence[] getDynLMK(Resources resources) {
		CharSequence[] dynLMK = resources.getStringArray(R.array.lmk_presets);
		return dynLMK;
	}

	public static CharSequence[] getDynLMKAdj(Resources resources) {
		CharSequence[] dynLMKAdj = resources.getStringArray(R.array.lmk_adj);
		return dynLMKAdj;
	}

	public static CharSequence[] getDynLMKMin(Resources resources) {
		CharSequence[] dynLMKMin = resources.getStringArray(R.array.lmk_minfree);
		return dynLMKMin;
	}

	public static CharSequence[] getProcessLimit(Resources resources) {
		CharSequence[] processLimit = resources.getStringArray(R.array.background_limit);
		return processLimit;
	}

}
