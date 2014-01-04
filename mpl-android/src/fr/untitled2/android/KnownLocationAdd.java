package fr.untitled2.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.google.common.base.Optional;
import fr.untitled2.android.i18n.I18nConstants;
import fr.untitled2.android.settings.Preferences;
import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.android.utils.PreferencesUtils;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.utils.GoogleMapsUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 02/01/14
 * Time: 22:31
 * To change this template use File | Settings | File Templates.
 */
public class KnownLocationAdd extends MenuActivity {

    private Preferences preferences;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferencesUtils.getPreferences(this);
        dbHelper = new DbHelper(this, preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.knownlocationadd);
        dbHelper = new DbHelper(this, preferences);
        Optional<LogRecording> currentLogRecording = dbHelper.getCurrentLog();
        preferences = PreferencesUtils.getPreferences(this);
        TextView nameLabel = (TextView) findViewById(R.id.KnownLocationNameLabel);
        nameLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labelname));

        TextView useLastKnownLocationLabel = (TextView) findViewById(R.id.NewKnownLocationUseLastKnownNameLabel);
        useLastKnownLocationLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labelusercurrentlocation));

        if (currentLogRecording.isPresent()) {
            CheckBox useLastKnownLocation = (CheckBox) findViewById(R.id.NewKnownLocationUseLastKnownPointCheckBox);
            useLastKnownLocation.setOnClickListener(OnSelectUseLastKnownLocation());
            useLastKnownLocation.setChecked(true);

            TextView addressLabel = (TextView) findViewById(R.id.KnownLocationNameAddressLabel);
            addressLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labeladdress));
            addressLabel.setVisibility(View.GONE);

            EditText address = (EditText) findViewById(R.id.NewKnownLocationAddress);
            address.setVisibility(View.GONE);
        } else {
            CheckBox useLastKnownLocation = (CheckBox) findViewById(R.id.NewKnownLocationUseLastKnownPointCheckBox);
            useLastKnownLocation.setOnClickListener(OnSelectUseLastKnownLocation());
            useLastKnownLocation.setChecked(false);
            useLastKnownLocation.setVisibility(View.GONE);

            TextView addressLabel = (TextView) findViewById(R.id.KnownLocationNameAddressLabel);
            addressLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labeladdress));
        }

        Button validation = (Button) findViewById(R.id.KnownLocationAddButton);
        validation.setText(preferences.getTranslation(I18nConstants.knownlocationadd_validate));
        validation.setOnClickListener(OnValidate());
    }

    View.OnClickListener OnValidate() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = (EditText) findViewById(R.id.NewKnownLocationName);
                EditText address = (EditText) findViewById(R.id.NewKnownLocationAddress);
                CheckBox useLastKnownLocation = (CheckBox) findViewById(R.id.NewKnownLocationUseLastKnownPointCheckBox);
                Optional<LogRecording> currentLogRecordingOptional = dbHelper.getCurrentLog();

                if (useLastKnownLocation.isChecked() && currentLogRecordingOptional.isPresent()) {
                    List<KnownLocation> knownLocations = preferences.getKnownLocations();
                    KnownLocation knownLocation = new KnownLocation();
                    knownLocation.setName(name.getText().toString());
                    LogRecording.LogRecord logRecord = currentLogRecordingOptional.get().getLastLogRecord();
                    knownLocation.setAltitude(logRecord.getAltitude());
                    knownLocation.setLatitude(logRecord.getLatitude());
                    knownLocation.setLongitude(logRecord.getLongitude());
                    knownLocations.add(knownLocation);
                    savePreferences(preferences);
                } else {
                    String addressString = address.getText().toString();
                    Pair<Double, Double> latitudeAndLongitude = GoogleMapsUtils.getGeocodes(addressString);

                    List<KnownLocation> knownLocations = preferences.getKnownLocations();
                    KnownLocation knownLocation = new KnownLocation();
                    knownLocation.setName(name.getText().toString());
                    knownLocation.setLatitude(latitudeAndLongitude.getValue0());
                    knownLocation.setLongitude(latitudeAndLongitude.getValue1());
                    knownLocations.add(knownLocation);
                    savePreferences(preferences);
                }
                GoToHomeView();
            }
        };
    }

    View.OnClickListener OnSelectUseLastKnownLocation() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                TextView addressLabel = (TextView) findViewById(R.id.KnownLocationNameAddressLabel);
                addressLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labeladdress));
                addressLabel.setVisibility(View.VISIBLE);
                EditText address = (EditText) findViewById(R.id.NewKnownLocationAddress);
                address.setVisibility(View.VISIBLE);
                CheckBox useLastKnownLocation = (CheckBox) findViewById(R.id.NewKnownLocationUseLastKnownPointCheckBox);
                useLastKnownLocation.setOnClickListener(OnUnSelectUseLastKnownLocation());
            }
        };
    }

    View.OnClickListener OnUnSelectUseLastKnownLocation() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                TextView addressLabel = (TextView) findViewById(R.id.KnownLocationNameAddressLabel);
                addressLabel.setText(preferences.getTranslation(I18nConstants.knownlocationadd_labeladdress));
                addressLabel.setVisibility(View.GONE);
                EditText address = (EditText) findViewById(R.id.NewKnownLocationAddress);
                address.setVisibility(View.GONE);
                CheckBox useLastKnownLocation = (CheckBox) findViewById(R.id.NewKnownLocationUseLastKnownPointCheckBox);
                useLastKnownLocation.setOnClickListener(OnSelectUseLastKnownLocation());
            }
        };
    }

    private void GoToHomeView() {
        Intent intent = new Intent(getApplicationContext(), Main.class);
        startActivity(intent);
    }

    private void savePreferences(Preferences preferences) {
        this.preferences = preferences;
        PreferencesUtils.setSharedPreferences(this, preferences);
    }

    @Override
    protected String getPageTitle(Preferences preferences) {
        return preferences.getTranslation(I18nConstants.knownlocationadd_title);

    }

    @Override
    protected boolean displayMenuBar() {
        return true;
    }
}
