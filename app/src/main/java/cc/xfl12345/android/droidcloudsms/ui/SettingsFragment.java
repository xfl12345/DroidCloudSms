package cc.xfl12345.android.droidcloudsms.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cc.xfl12345.android.droidcloudsms.MyApplication;
import cc.xfl12345.android.droidcloudsms.R;
import cc.xfl12345.android.droidcloudsms.model.MyISub;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private MyApplication context;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (MyApplication) requireContext().getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        ListPreference simCardListPreference = Objects.requireNonNull(getPreferenceManager().findPreference(MyApplication.SP_KEY_SMS_SIM_SUBSCRIPTION_ID));
        simCardListPreference.setDefaultValue(null);
        boolean isOk = false;
        if (context.getMyShizukuContext().refreshPermissionStatus()) {
            try {
                MyISub myISub = new MyISub();
                int defaultSmsSubId = myISub.getDefaultSmsSubId();
                List<SubscriptionInfo> availableSubscriptionInfoList = myISub.getAvailableSubscriptionInfoList();// 手机SIM卡信息
                String savedSubscriptionId = sharedPreferences.getString(MyApplication.SP_KEY_SMS_SIM_SUBSCRIPTION_ID, null);
                int saveSubscriptionIdIndex = -1;
                int defaultSmsSubIdIndex = -1;
                int listLength = availableSubscriptionInfoList.size();
                List<CharSequence> displayNames = new ArrayList<>(listLength);
                List<CharSequence> settingValuas = new ArrayList<>(listLength);

                for (int i = 0; i < listLength; i++) {
                    SubscriptionInfo info = availableSubscriptionInfoList.get(i);
                    int tmpSubscriptionId = info.getSubscriptionId();
                    int simCardSlotIndex = myISub.getSlotIndex(tmpSubscriptionId);
                    String tmpSubscriptionIdInText = String.valueOf(tmpSubscriptionId);
                    displayNames.add("卡 " + (simCardSlotIndex + 1)
                        + " - " + info.getDisplayName()
                        + String.format(" - [%s]", info.getNumber())
                    );
                    settingValuas.add(tmpSubscriptionIdInText);
                    if (tmpSubscriptionIdInText.equals(savedSubscriptionId)) {
                        saveSubscriptionIdIndex = i;
                    }
                    if (defaultSmsSubId == tmpSubscriptionId) {
                        defaultSmsSubIdIndex = i;
                    }
                }

                simCardListPreference.setEntries(displayNames.stream().toArray(CharSequence[]::new));
                simCardListPreference.setEntryValues(settingValuas.stream().toArray(CharSequence[]::new));
                if (saveSubscriptionIdIndex != -1) {
                    simCardListPreference.setValueIndex(saveSubscriptionIdIndex);
                } else {
                    if (defaultSmsSubIdIndex != -1) {
                        simCardListPreference.setValueIndex(defaultSmsSubIdIndex);
                    } else {
                        simCardListPreference.setValueIndex(0);
                    }
                }

                isOk = true;
            } catch (ReflectiveOperationException | RemoteException e) {
                // ignore
            }
        }

        if (!isOk) {
            simCardListPreference.setEntries(new CharSequence[]{"默认"});
            simCardListPreference.setEntryValues(new CharSequence[]{null});
            simCardListPreference.setValueIndex(0);
        }

        simCardListPreference.setSummary(simCardListPreference.getEntry());

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onResume() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MyApplication.SP_KEY_SMS_SIM_SUBSCRIPTION_ID.equals(key)) {
            ListPreference simCardListPreference = Objects.requireNonNull(getPreferenceManager().findPreference(MyApplication.SP_KEY_SMS_SIM_SUBSCRIPTION_ID));
            simCardListPreference.setSummary(simCardListPreference.getEntry());
        }
    }
}