package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;

import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.EditTextPreference;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import net.typeblog.socks.util.Utility;

import java.util.Locale;

import static net.typeblog.socks.util.Constants.*;

public class ProfileFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        CompoundButton.OnCheckedChangeListener {
    private ProfileManager mManager;
    private Profile mProfile;

    private Switch mSwitch;
    private boolean mRunning = false, mStarting = false, mStopping = false;

    private IVpnService mBinder;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName p1, IBinder binder) {
            mBinder = IVpnService.Stub.asInterface(binder);

            try {
                mRunning = mBinder.isRunning();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mRunning) {
                updateState();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName p1) {
            mBinder = null;
        }
    };

    private final Runnable mStateRunnable = new Runnable() {
        @Override
        public void run() {
            updateState();
            mSwitch.postDelayed(this, 1000);
        }
    };

    private ListPreference mPrefProfile, mPrefRoutes;
    private EditTextPreference mPrefServer, mPrefPort, mPrefUsername, mPrefPassword,
            mPrefDns, mPrefDnsPort, mPrefAppList, mPrefUDPGW, mPrefSSH_Host, mPrefSSH_UserName, mPrefSSH_Pkey, mPrefSSH_Password, mprefSSH_Port;
    private CheckBoxPreference mPrefUserpw, mPrefPerApp, mPrefAppBypass, mPrefIPv6, mPrefUDP, mPrefAuto, mPrefSSH;
    private SwitchPreference mPrefSSH_Switch;


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
//        super.onCreate(savedInstanceState);
//        setPreferencesFromResource(R.xml.settings, rootKey);
        addPreferencesFromResource(R.xml.settings);
        setHasOptionsMenu(true);
        mManager = new ProfileManager(getActivity().getApplicationContext());
        initPreferences();
        reload();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);

        MenuItem s = menu.findItem(R.id.switch_main);
        mSwitch = s.getActionView().findViewById(R.id.switch_action_button);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.postDelayed(mStateRunnable, 1000);
        checkState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prof_add:
                addProfile();
                return true;
            case R.id.prof_del:
                removeProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        // TODO: Implement this method
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference p, Object newValue) {
        if (p == mPrefProfile) {
            String name = newValue.toString();
            mProfile = mManager.getProfile(name);
            mManager.switchDefault(name);
            reload();
            return true;
        } else if (p == mPrefServer) {
            mProfile.setServer(newValue.toString());
            resetTextN(mPrefServer, newValue);
            return true;
        } else if (p == mPrefPort) {
            if (TextUtils.isEmpty(newValue.toString()))
                return false;

            mProfile.setPort(Integer.parseInt(newValue.toString()));
            resetTextN(mPrefPort, newValue);
            return true;
        } else if (p == mPrefUserpw) {
            mProfile.setIsUserpw(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUsername) {
            mProfile.setUsername(newValue.toString());
            resetTextN(mPrefUsername, newValue);
            return true;
        } else if (p == mPrefPassword) {
            mProfile.setPassword(newValue.toString());
            resetTextN(mPrefPassword, newValue);
            return true;
        } else if (p == mPrefRoutes) {
            mProfile.setRoute(newValue.toString());
            resetListN(mPrefRoutes, newValue);
            return true;
        } else if (p == mPrefDns) {
            mProfile.setDns(newValue.toString());
            resetTextN(mPrefDns, newValue);
            return true;
        } else if (p == mPrefDnsPort) {
            if (TextUtils.isEmpty(newValue.toString()))
                return false;

            mProfile.setDnsPort(Integer.parseInt(newValue.toString()));
            resetTextN(mPrefDnsPort, newValue);
            return true;
        } else if (p == mPrefPerApp) {
            mProfile.setIsPerApp(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefAppBypass) {
            mProfile.setIsBypassApp(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefAppList) {
            mProfile.setAppList(newValue.toString());
            return true;
        } else if (p == mPrefIPv6) {
            mProfile.setHasIPv6(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUDP) {
            mProfile.setHasUDP(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUDPGW) {
            mProfile.setUDPGW(newValue.toString());
            resetTextN(mPrefUDPGW, newValue);
            return true;
        } else if (p == mPrefAuto) {
            mProfile.setAutoConnect(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefSSH) {
            mProfile.setIsSSH(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefSSH_Host) {
            mProfile.setSSH_Host(newValue.toString());
            return true;
        } else if (p == mPrefSSH_UserName) {
            mProfile.setSSH_UserName(newValue.toString());
            return true;
        } else if (p == mPrefSSH_Password) {
            mProfile.setSSH_Password(newValue.toString());
            return true;
        } else if (p == mPrefSSH_Pkey) {
            mProfile.setSSH_Pkey(newValue.toString());
            return true;
        } else if (p == mprefSSH_Port) {
            mProfile.setSSH_Port(Integer.parseInt(newValue.toString()));
            return true;
        } else if (p == mPrefSSH_Switch) {
            mProfile.setSSH_Switch(Boolean.parseBoolean(newValue.toString()));
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton p1, boolean checked) {
        if (checked) {
            startVpn();
        } else {
            stopVpn();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Utility.startVpn(getActivity(), mProfile);
            checkState();
        }
    }

    private void initPreferences() {
        mPrefProfile = (ListPreference) findPreference(PREF_PROFILE);
        mPrefServer = (EditTextPreference) findPreference(PREF_SERVER_IP);
        mPrefPort = (EditTextPreference) findPreference(PREF_SERVER_PORT);
        mPrefUserpw = (CheckBoxPreference) findPreference(PREF_AUTH_USERPW);
        mPrefUsername = (EditTextPreference) findPreference(PREF_AUTH_USERNAME);
        mPrefPassword = (EditTextPreference) findPreference(PREF_AUTH_PASSWORD);
        mPrefRoutes = (ListPreference) findPreference(PREF_ADV_ROUTE);
        mPrefDns = (EditTextPreference) findPreference(PREF_ADV_DNS);
        mPrefDnsPort = (EditTextPreference) findPreference(PREF_ADV_DNS_PORT);
        mPrefPerApp = (CheckBoxPreference) findPreference(PREF_ADV_PER_APP);
        mPrefAppBypass = (CheckBoxPreference) findPreference(PREF_ADV_APP_BYPASS);
        mPrefAppList = (EditTextPreference) findPreference(PREF_ADV_APP_LIST);
        mPrefIPv6 = (CheckBoxPreference) findPreference(PREF_IPV6_PROXY);
        mPrefUDP = (CheckBoxPreference) findPreference(PREF_UDP_PROXY);
        mPrefUDPGW = (EditTextPreference) findPreference(PREF_UDP_GW);
        mPrefAuto = (CheckBoxPreference) findPreference(PREF_ADV_AUTO_CONNECT);
        mPrefSSH = (CheckBoxPreference) findPreference(PREF_SELF_SSH);
        mPrefSSH_Host = (EditTextPreference) findPreference(PREF_SSH_HOST);
        mPrefSSH_UserName = (EditTextPreference) findPreference(PREF_SSH_USERNAME);
        mPrefSSH_Pkey = (EditTextPreference) findPreference(PREF_SSH_PKEY);
        mPrefSSH_Password = (EditTextPreference) findPreference(PREF_SSH_PASSWORD);
        mprefSSH_Port = (EditTextPreference) findPreference(PREF_SSH_PORT);
        mPrefSSH_Switch = (SwitchPreference) findPreference(PREF_SSH_SWITCH);

        mPrefProfile.setOnPreferenceChangeListener(this);
        mPrefServer.setOnPreferenceChangeListener(this);
        mPrefPort.setOnPreferenceChangeListener(this);
        mPrefUserpw.setOnPreferenceChangeListener(this);
        mPrefUsername.setOnPreferenceChangeListener(this);
        mPrefPassword.setOnPreferenceChangeListener(this);
        mPrefRoutes.setOnPreferenceChangeListener(this);
        mPrefDns.setOnPreferenceChangeListener(this);
        mPrefDnsPort.setOnPreferenceChangeListener(this);
        mPrefPerApp.setOnPreferenceChangeListener(this);
        mPrefAppBypass.setOnPreferenceChangeListener(this);
        mPrefAppList.setOnPreferenceChangeListener(this);
        mPrefIPv6.setOnPreferenceChangeListener(this);
        mPrefUDP.setOnPreferenceChangeListener(this);
        mPrefUDPGW.setOnPreferenceChangeListener(this);
        mPrefAuto.setOnPreferenceChangeListener(this);
        mPrefSSH.setOnPreferenceChangeListener(this);
        mPrefSSH_Host.setOnPreferenceChangeListener(this);
        mPrefSSH_UserName.setOnPreferenceChangeListener(this);
        mPrefSSH_Pkey.setOnPreferenceChangeListener(this);
        mPrefSSH_Password.setOnPreferenceChangeListener(this);
        mprefSSH_Port.setOnPreferenceChangeListener(this);
        mPrefSSH_Switch.setOnPreferenceChangeListener(this);
    }

    private void reload() {
        if (mProfile == null) {
            mProfile = mManager.getDefault();
        }

        mPrefProfile.setEntries(mManager.getProfiles());
        mPrefProfile.setEntryValues(mManager.getProfiles());
        mPrefProfile.setValue(mProfile.getName());
        mPrefRoutes.setValue(mProfile.getRoute());
        resetList(mPrefProfile, mPrefRoutes);

        mPrefUserpw.setChecked(mProfile.isUserPw());
        mPrefPerApp.setChecked(mProfile.isPerApp());
        mPrefAppBypass.setChecked(mProfile.isBypassApp());
        mPrefIPv6.setChecked(mProfile.hasIPv6());
        mPrefUDP.setChecked(mProfile.hasUDP());
        mPrefAuto.setChecked(mProfile.autoConnect());
        mPrefSSH.setChecked(mProfile.isSSH());

        mPrefServer.setText(mProfile.getServer());
        mPrefPort.setText(String.valueOf(mProfile.getPort()));
        mPrefUsername.setText(mProfile.getUsername());
        mPrefPassword.setText(mProfile.getPassword());
        mPrefDns.setText(mProfile.getDns());
        mPrefDnsPort.setText(String.valueOf(mProfile.getDnsPort()));
        mPrefUDPGW.setText(mProfile.getUDPGW());
        mPrefSSH_Host.setText(mProfile.getSSH_Host());
        mPrefSSH_UserName.setText(mProfile.getSSH_UserName());
        mPrefSSH_Pkey.setText(mProfile.getSSH_Pkey());
        mPrefSSH_Password.setText(mProfile.getSSH_Password());
        mprefSSH_Port.setText(String.valueOf(mProfile.getSSH_Port()));
        mPrefSSH_Switch.setChecked(mProfile.getSSH_switch());

        resetText(mPrefServer, mPrefPort, mPrefUsername, mPrefPassword, mPrefDns, mPrefDnsPort, mPrefUDPGW, mPrefSSH_Host, mPrefSSH_UserName, mPrefSSH_Password, mPrefSSH_Pkey, mprefSSH_Port);

        mPrefAppList.setText(mProfile.getAppList());
    }

    private void resetList(ListPreference... pref) {
        for (ListPreference p : pref)
            p.setSummary(p.getEntry());
    }

    private void resetListN(ListPreference pref, Object newValue) {
        pref.setSummary(newValue.toString());
    }

    private void resetText(EditTextPreference... pref) {
        for (EditTextPreference p : pref) {
            p.setSummary(p.getText());
        }
    }

    private void resetTextN(EditTextPreference pref, Object newValue) {
        pref.setSummary(newValue.toString());
    }

    private void addProfile() {
        final EditText e = new EditText(getActivity());
        e.setSingleLine(true);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.prof_add)
                .setView(e)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        String name = e.getText().toString().trim();

                        if (!TextUtils.isEmpty(name)) {
                            Profile p = mManager.addProfile(name);

                            if (p != null) {
                                mProfile = p;
                                reload();
                                return;
                            }
                        }

                        Toast.makeText(getActivity(),
                                String.format(getString(R.string.err_add_prof), name),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {

                    }
                })
                .create().show();
    }

    private void removeProfile() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.prof_del)
                .setMessage(String.format(getString(R.string.prof_del_confirm), mProfile.getName()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (!mManager.removeProfile(mProfile.getName())) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.err_del_prof, mProfile.getName()),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mProfile = mManager.getDefault();
                            reload();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {

                    }
                })
                .create().show();
    }

    private void checkState() {
        mRunning = false;
        mSwitch.setEnabled(false);
        mSwitch.setOnCheckedChangeListener(null);

        if (mBinder == null) {
            getActivity().bindService(new Intent(getActivity(), SocksVpnService.class), mConnection, 0);
        }
    }

    private void updateState() {
        if (mBinder == null) {
            mRunning = false;
        } else {
            try {
                mRunning = mBinder.isRunning();
            } catch (Exception e) {
                mRunning = false;
            }
        }

        mSwitch.setChecked(mRunning);

        if ((!mStarting && !mStopping) || (mStarting && mRunning) || (mStopping && !mRunning)) {
            mSwitch.setEnabled(true);
        }

        if (mStarting && mRunning) {
            mStarting = false;
        }

        if (mStopping && !mRunning) {
            mStopping = false;
        }

        mSwitch.setOnCheckedChangeListener(ProfileFragment.this);
    }

    private void startVpn() {
        mStarting = true;
        Intent i = VpnService.prepare(getActivity());

        if (i != null) {
            startActivityForResult(i, 0);
        } else {
            onActivityResult(0, Activity.RESULT_OK, null);
        }
    }

    private void stopVpn() {
        if (mBinder == null)
            return;

        mStopping = true;

        try {
            mBinder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBinder = null;

        getActivity().unbindService(mConnection);
        checkState();
    }
}
