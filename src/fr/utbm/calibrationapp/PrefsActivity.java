package fr.utbm.calibrationapp;

import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.layout.prefs);
		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		EditTextPreference editServerName = (EditTextPreference) findPreference("serverName");
		EditTextPreference editServerAddress = (EditTextPreference) findPreference("serverAddress");
		EditTextPreference editServerPort = (EditTextPreference) findPreference("serverPort");

		editServerName.setSummary(sp.getString("serverName", "Default server"));
		editServerAddress.setSummary(sp.getString("serverAddress", "0.0.0.0"));
		editServerPort.setSummary(sp.getString("serverPort", "80"));

		findPreference("serverAddress").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Pattern.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$", (String) newValue)) {
					displayModal("Invalid IP address", "Enter an IP address with the following format :"
							+ "\n[0-255].[0-255].[0-255].[0-255]");
					return false;
				}

				return true;
			}
		});
		
		findPreference("serverPort").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					int port = Integer.parseInt((String) newValue);
					if (port < 0 || port > 65536) {
						displayModal("Invalid port number", "Enter a port number between 0 and 65536 !");
						return false;
					}
				} catch (NumberFormatException e) {
					displayModal("Invalid port number", "Enter a port number between 0 and 65536 !");
					return false;
				}

				return true;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		if (pref instanceof EditTextPreference) {
			EditTextPreference etp = (EditTextPreference) pref;
			pref.setSummary(etp.getText());
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
			} else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void displayModal(String title, String error) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(PrefsActivity.this);
		builder.setTitle(title);
		builder.setMessage(error);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.show();
	}
}
