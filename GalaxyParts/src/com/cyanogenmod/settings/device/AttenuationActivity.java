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

public class AttenuationActivity extends PreferenceActivity {

	public static boolean isAttenuationOn() {
		String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION).split(",");
		boolean bool = ATTENUATION_ARRAY[0].equals("0");
		return !bool;
	}

	public static void switchOnClick(boolean isChecked) {
		String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION).split(",");
		int enabled;

		if (isChecked) {
			enabled = 1;
		} else {
			enabled = 0;
		}

		SystemProperties.set(Constants.PROP_ATTENUATION,
			String.valueOf(enabled) + "," +
			String.valueOf(ATTENUATION_ARRAY[1]) + "," +
			String.valueOf(ATTENUATION_ARRAY[2]) + "," +
			String.valueOf(ATTENUATION_ARRAY[3]));
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
		if (DeviceSettings.mPrefAttenuation != null) {
			s.setChecked(DeviceSettings.mPrefAttenuation.isChecked());
		} else {
			s.setChecked(isAttenuationOn());
		}

		s.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				switchOnClick(arg1);
				if (DeviceSettings.mPrefAttenuation != null) {
					DeviceSettings.mPrefAttenuation.setChecked(arg1);
				}
				loadScreen(arg1);
			}

		});
	}

	private void loadScreen(boolean bool) {

		Resources resources = getResources();
		String[] ATTENUATION_ARRAY = SystemProperties.get(Constants.PROP_ATTENUATION,
			getResources().getString(R.string.attenuation_defaults)).split(",");
		String[] ATTENUATION_DEFAULT_ARRAY =
			getResources().getString(R.string.attenuation_defaults).split(",");

		PreferenceScreen prefScreen = getPreferenceManager().createPreferenceScreen(this);
		prefScreen.setEnabled(bool);

		PreferenceCategory globalCat = new PreferenceCategory(this);
		globalCat.setTitle(getText(R.string.global));
		prefScreen.addPreference(globalCat);

		// Attenuation: headset
		Preference attnHeadset = new Preference(this);
		attnHeadset.setTitle(getText(R.string.attn_headset));
		attnHeadset.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_ARRAY[1])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_DEFAULT_ARRAY[1])]);
		attnHeadset.setEnabled(true);
		attnHeadset.setOnPreferenceClickListener(new AttnHeadset(this, resources));
		globalCat.addPreference(attnHeadset);

		// Attenuation: speaker
		Preference attnSpeaker = new Preference(this);
		attnSpeaker.setTitle(getText(R.string.attn_speaker));
		attnSpeaker.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_ARRAY[2])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_DEFAULT_ARRAY[2])]);
		attnSpeaker.setEnabled(true);
		attnSpeaker.setOnPreferenceClickListener(new AttnSpeaker(this, resources));
		globalCat.addPreference(attnSpeaker);


		// Attenuation: FM
		PreferenceCategory fmCat = new PreferenceCategory(this);
		fmCat.setTitle(getText(R.string.fm_radio));
		prefScreen.addPreference(fmCat);
		Preference attnFM = new Preference(this);
		attnFM.setTitle(getText(R.string.attn_headset_speaker));
		attnFM.setSummary(getText(R.string.current_setting) + ": " + 
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_ARRAY[3])] +
			"\n" + getText(R.string.default_setting) + ": " +
			Constants.getAttn(resources)[Integer.parseInt(ATTENUATION_DEFAULT_ARRAY[3])]);
		attnFM.setEnabled(true);
		attnFM.setOnPreferenceClickListener(new AttnFM(this, resources));
		fmCat.addPreference(attnFM);
		
		setPreferenceScreen(prefScreen);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (DeviceSettings.mPrefAttenuation != null) {
			loadScreen(DeviceSettings.mPrefAttenuation.isChecked());
		} else {
			loadScreen(isAttenuationOn());
		}
	}

}
