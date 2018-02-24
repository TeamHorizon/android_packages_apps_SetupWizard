/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.setupwizard;

import static org.lineageos.setupwizard.SetupWizardApp.DISABLE_NAV_KEYS;
import static org.lineageos.setupwizard.SetupWizardApp.KEY_PRIVACY_GUARD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.setupwizardlib.util.WizardManagerHelper;

import org.lineageos.setupwizard.R;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

public class LineageSettingsActivity extends BaseSetupWizardActivity {

    public static final String TAG = LineageSettingsActivity.class.getSimpleName();

    private SetupWizardApp mSetupWizardApp;

    private CheckBox mNavKeys;
    private CheckBox mPrivacyGuard;

    private boolean mHideNavKeysRow = false;

    private View.OnClickListener mNavKeysClickListener = view -> {
        boolean checked = !mNavKeys.isChecked();
        mNavKeys.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(DISABLE_NAV_KEYS, checked);
    };

    private View.OnClickListener mPrivacyGuardClickListener = view -> {
        boolean checked = !mPrivacyGuard.isChecked();
        mPrivacyGuard.setChecked(checked);
        mSetupWizardApp.getSettingsBundle().putBoolean(KEY_PRIVACY_GUARD, checked);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetupWizardApp = (SetupWizardApp) getApplication();
        setNextText(R.string.next);

        View navKeysRow = findViewById(R.id.nav_keys);
        navKeysRow.setOnClickListener(mNavKeysClickListener);
        mNavKeys = (CheckBox) findViewById(R.id.nav_keys_checkbox);
        boolean needsNavBar = true;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            needsNavBar = windowManager.needsNavigationBar();
        } catch (RemoteException e) {
        }
        mHideNavKeysRow = hideKeyDisabler(this);
        if (mHideNavKeysRow || needsNavBar) {
            navKeysRow.setVisibility(View.GONE);
        } else {
            boolean navKeysDisabled = isKeyDisablerActive(this);
            mNavKeys.setChecked(navKeysDisabled);
        }

        View privacyGuardRow = findViewById(R.id.privacy_guard);
        privacyGuardRow.setOnClickListener(mPrivacyGuardClickListener);
        mPrivacyGuard = (CheckBox) findViewById(R.id.privacy_guard_checkbox);
        mPrivacyGuard.setChecked(LineageSettings.Secure.getInt(getContentResolver(),
                LineageSettings.Secure.PRIVACY_GUARD_DEFAULT, 0) == 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDisableNavkeysOption();
        updatePrivacyGuardOption();
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        Intent intent = WizardManagerHelper.getNextIntent(getIntent(), Activity.RESULT_OK);
        startActivityForResult(intent, 1);
    }

    @Override
    protected int getTransition() {
        return TRANSITION_ID_SLIDE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setup_lineage_settings;
    }

    @Override
    protected int getTitleResId() {
        return R.string.setup_xenonhd;
    }

    @Override
    protected int getIconResId() {
        return R.drawable.ic_features;
    }

    private void updateDisableNavkeysOption() {
        if (!mHideNavKeysRow) {
            final Bundle myPageBundle = mSetupWizardApp.getSettingsBundle();
            boolean enabled = LineageSettings.Global.getInt(getContentResolver(),
                    LineageSettings.Global.DEV_FORCE_SHOW_NAVBAR, 0) != 0;
            boolean checked = myPageBundle.containsKey(DISABLE_NAV_KEYS) ?
                    myPageBundle.getBoolean(DISABLE_NAV_KEYS) :
                    enabled;
            mNavKeys.setChecked(checked);
            myPageBundle.putBoolean(DISABLE_NAV_KEYS, checked);
        }
    }

    private void updatePrivacyGuardOption() {
        final Bundle bundle = mSetupWizardApp.getSettingsBundle();
        boolean enabled = LineageSettings.Secure.getInt(getContentResolver(),
                LineageSettings.Secure.PRIVACY_GUARD_DEFAULT, 0) != 0;
        boolean checked = bundle.containsKey(KEY_PRIVACY_GUARD) ?
                bundle.getBoolean(KEY_PRIVACY_GUARD) :
                enabled;
        mPrivacyGuard.setChecked(checked);
        bundle.putBoolean(KEY_PRIVACY_GUARD, checked);
    }

    private static boolean hideKeyDisabler(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return !hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    private static boolean isKeyDisablerActive(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.get(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }
}
