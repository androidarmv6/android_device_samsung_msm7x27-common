package com.cyanogenmod.settings.device;

import android.app.ActionBar;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.cyanogenmod.settings.device.R;

public class DeviceSettings extends PreferenceActivity {

	public static PreferenceSwitch mPrefAttenuation;
	public static PreferenceSwitch mPrefDynLMK;
	public static PreferenceSwitch mPrefExtAmp;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			super.onBackPressed();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.mipmap.ic_launcher_settings);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Resources resources = getResources();
		boolean isRoot = SystemProperties.get(Constants.PROP_ROOTACCESS).equals("3");

		PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(this);

		PreferenceCategory audioCat = new PreferenceCategory(this);
		audioCat.setTitle(getText(R.string.audio_settings_cat));

		// Enable audio category (only if needed)
		if (getResources().getBoolean(R.bool.attenuation_feature)
			|| getResources().getBoolean(R.bool.extamp_feature)) {
			preferenceScreen.addPreference(audioCat);
		}

		// Attenuation
		if (getResources().getBoolean(R.bool.attenuation_feature)) {
			String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION,
				this.getString(R.string.attenuation_defaults)).split(",");

			mPrefAttenuation = new PreferenceSwitch(this);
			mPrefAttenuation.setTitle(getText(R.string.attenuation));
			mPrefAttenuation.setIntent(new Intent(this, AttenuationActivity.class));
			mPrefAttenuation.setChecked(AttenuationActivity.isAttenuationOn());
			mPrefAttenuation.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					AttenuationActivity.switchOnClick(arg1);
					mPrefAttenuation.setChecked(arg1);
				}
			});
		audioCat.addPreference(mPrefAttenuation);
		}

		// Extamp filter
		if (getResources().getBoolean(R.bool.extamp_feature)) {
			String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP,
				this.getString(R.string.extamp_defaults)).split(",");

			mPrefExtAmp = new PreferenceSwitch(this);
			mPrefExtAmp.setTitle(getText(R.string.extamp_filter));
			mPrefExtAmp.setIntent(new Intent(this, ExtAmpActivity.class));
			mPrefExtAmp.setChecked(ExtAmpActivity.isExtAmpOn());
			mPrefExtAmp.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					ExtAmpActivity.switchOnClick(arg1);
					mPrefExtAmp.setChecked(arg1);
				}
			});
			audioCat.addPreference(mPrefExtAmp);
		}

		// Audio subsystem restart
		if (getResources().getBoolean(R.bool.attenuation_feature)
			|| getResources().getBoolean(R.bool.extamp_feature)) {

			Preference killMedia = new Preference(this);
			killMedia.setTitle(getText(R.string.media_kill_gen));
			killMedia.setEnabled(isRoot);
			killMedia.setSummary(isRoot ? getText(R.string.media_kill_info)
				: getText(R.string.media_kill_root_req));
			killMedia.setOnPreferenceClickListener(new KillMediaTask(this));
			audioCat.addPreference(killMedia);
		}

		PreferenceCategory memory = new PreferenceCategory(this);
		memory.setTitle(getText(R.string.memory_man));
		preferenceScreen.addPreference(memory);

		// SD cache size
		Preference cacheSize = new Preference(this);
		cacheSize.setTitle(getText(R.string.cache_size));
		int currentCacheSize;

		try {
			currentCacheSize = Integer.parseInt(SystemProperties.get(Constants.PROP_CACHESIZE));
		} catch (NumberFormatException e) {
			currentCacheSize = 0;
		}

		cacheSize.setSummary(Constants.getCacheSize(resources)[currentCacheSize]);
		cacheSize.setEnabled(true);
		cacheSize.setOnPreferenceClickListener(new CacheSize(this, resources));
		memory.addPreference(cacheSize);

		// Swap
		final CheckBoxPreference swap = new CheckBoxPreference(this);
		swap.setTitle(getText(R.string.swap));
		swap.setChecked(SystemProperties.get(Constants.PROP_SWAP).equals("1"));

		swap.setEnabled(true);
		swap.setSummary(getText(R.string.swap_en));
		swap.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference arg0) {
				if (swap.isChecked()) {
					SystemProperties.set(Constants.PROP_SWAP, "1");
				} else {
					SystemProperties.set(Constants.PROP_SWAP, "0");
				}
				return false;
			}
		});
		memory.addPreference(swap);

		PreferenceCategory screenCat = new PreferenceCategory(this);
		screenCat.setTitle(getText(R.string.intrfc));
		preferenceScreen.addPreference(screenCat);

		// Highend Graphics
		final CheckBoxPreference highEndGfx = new CheckBoxPreference(this);
		highEndGfx.setTitle(getText(R.string.highend_gfx));
		highEndGfx.setSummary(getText(R.string.highend_gfx_sum));
		highEndGfx.setChecked(SystemProperties.get(Constants.PROP_HIGHEND_GFX).equals("1"));
		highEndGfx.setEnabled(true);
		highEndGfx.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference arg0) {
				if (highEndGfx.isChecked()) {
					SystemProperties.set(Constants.PROP_HIGHEND_GFX, "1");
				} else {
					SystemProperties.set(Constants.PROP_HIGHEND_GFX, "0");
				}
				return false;
			}
		});
		screenCat.addPreference(highEndGfx);


		// Fake dual-touch
		if (getResources().getBoolean(R.bool.dualtouch_feature)) {
			final CheckBoxPreference dualTouch = new CheckBoxPreference(this);
			dualTouch.setTitle(getText(R.string.fake_dt));
			dualTouch.setSummary(getText(R.string.fake_dt_sum));
			dualTouch.setChecked(SystemProperties.get(Constants.PROP_FAKE_DT).equals("1"));
			dualTouch.setEnabled(true);
			dualTouch.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				public boolean onPreferenceClick(Preference arg0) {
					if (dualTouch.isChecked()) {
						SystemProperties.set(Constants.PROP_FAKE_DT, "1");
					} else {
						SystemProperties.set(Constants.PROP_FAKE_DT, "0");
					}
					return false;
				}
			});
			screenCat.addPreference(dualTouch);
		}

		// Bug report
		PreferenceCategory reportBug = new PreferenceCategory(this);
		reportBug.setTitle(getText(R.string.report_bug));
		preferenceScreen.addPreference(reportBug);

		Preference generateReport = new Preference(this);
		generateReport.setTitle(getText(R.string.bug_report_gen));
		generateReport.setEnabled(isRoot);
		generateReport.setSummary(isRoot ? getText(R.string.bug_report_info)
			: getText(R.string.bug_report_root_req));
		generateReport.setOnPreferenceClickListener(new GetLogTask(this));
		reportBug.addPreference(generateReport);

		setPreferenceScreen(preferenceScreen);

	}
}
