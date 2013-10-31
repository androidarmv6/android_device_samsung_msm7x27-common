package com.cyanogenmod.settings.device;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class ExtAmpActivity extends PreferenceActivity {

	public static boolean isExtAmpOn() {
		String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP).split(",");
		boolean bool = EXTAMP_ARRAY[0].equals("0");
		return !bool;
	}

	public static void switchOnClick(boolean isChecked) {
		String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP).split(",");
		int enabled;

		if (isChecked) {
			enabled = 1;
		} else {
			enabled = 0;
		}

		SystemProperties.set(Constants.PROP_EXTAMP,
			String.valueOf(enabled) + "," +
			String.valueOf(EXTAMP_ARRAY[1]) + "," +
			String.valueOf(EXTAMP_ARRAY[2]) + "," +
			String.valueOf(EXTAMP_ARRAY[3]) + "," +
			String.valueOf(EXTAMP_ARRAY[4]));
	}

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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add(getText(R.string.attenuation));
		menuItem.setActionView(R.layout.header_switch).setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
		headerSwitch(menuItem.getActionView());
		return super.onCreateOptionsMenu(menu);
	}

	private void headerSwitch(final View view) {

		Switch s = (Switch) view.findViewById(R.id.header_switch);
		if (DeviceSettings.mPrefExtAmp != null) {
			s.setChecked(DeviceSettings.mPrefExtAmp.isChecked());
		} else {
			s.setChecked(isExtAmpOn());
		}

		s.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				switchOnClick(arg1);
				if (DeviceSettings.mPrefExtAmp != null) {
					DeviceSettings.mPrefExtAmp.setChecked(arg1);
				}
				loadScreen(arg1);
			}

		});
	}

	private void loadScreen(boolean bool) {

		Resources resources = getResources();
		String[] EXTAMP_ARRAY = SystemProperties.get(Constants.PROP_EXTAMP,
			getResources().getString(R.string.extamp_defaults)).split(",");
		String[] EXTAMP_DEFAULT_ARRAY =
			getResources().getString(R.string.extamp_defaults).split(",");

		PreferenceScreen prefScreen = getPreferenceManager().createPreferenceScreen(this);
		prefScreen.setEnabled(bool);

		PreferenceCategory globalCat = new PreferenceCategory(this);
		globalCat.setTitle(getText(R.string.global));
		prefScreen.addPreference(globalCat);

		// Extamp: headset
		Preference extHeadset = new Preference(this);
		extHeadset.setTitle(getText(R.string.attn_headset));
		extHeadset.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_ARRAY[1])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_DEFAULT_ARRAY[1])]);
		extHeadset.setEnabled(true);
		extHeadset.setOnPreferenceClickListener(new ExtHeadset(this, resources));
		globalCat.addPreference(extHeadset);

		// Extamp: no mic headset
		Preference extNMHeadset = new Preference(this);
		extNMHeadset.setTitle(getText(R.string.attn_nmheadset));
		extNMHeadset.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_ARRAY[2])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_DEFAULT_ARRAY[2])]);
		extNMHeadset.setEnabled(true);
		extNMHeadset.setOnPreferenceClickListener(new ExtNMHeadset(this, resources));
		globalCat.addPreference(extNMHeadset);

		// Extamp: speaker
		Preference extSpeaker = new Preference(this);
		extSpeaker.setTitle(getText(R.string.attn_speaker));
		extSpeaker.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_ARRAY[3])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_DEFAULT_ARRAY[3])]);
		extSpeaker.setEnabled(true);
		extSpeaker.setOnPreferenceClickListener(new ExtSpeaker(this, resources));
		globalCat.addPreference(extSpeaker);


		// Extamp: FM
		PreferenceCategory fmCat = new PreferenceCategory(this);
		fmCat.setTitle(getText(R.string.fm_radio));
		prefScreen.addPreference(fmCat);
		Preference extFM = new Preference(this);
		extFM.setTitle(getText(R.string.attn_headset_speaker));
		extFM.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_ARRAY[4])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getExt(resources)[Integer.parseInt(EXTAMP_DEFAULT_ARRAY[4])]);
		extFM.setEnabled(true);
		extFM.setOnPreferenceClickListener(new ExtFM(this, resources));
		fmCat.addPreference(extFM);
		
		setPreferenceScreen(prefScreen);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (DeviceSettings.mPrefExtAmp != null) {
			loadScreen(DeviceSettings.mPrefExtAmp.isChecked());
		} else {
			loadScreen(isExtAmpOn());
		}
	}

}
