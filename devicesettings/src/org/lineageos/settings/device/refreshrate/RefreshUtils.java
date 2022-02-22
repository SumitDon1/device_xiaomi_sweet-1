/*
 * Copyright (C) 2020 The LineageOS Project
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

package org.lineageos.settings.device.refreshrate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.view.Display;

import android.provider.Settings;
import androidx.preference.PreferenceManager;

public final class RefreshUtils {

    private static final String REFRESH_CONTROL = "refresh_control";

    private static float defaultMaxRate;
    private static float defaultMinRate;
    private static final String KEY_PEAK_REFRESH_RATE = "peak_refresh_rate";
    private static final String KEY_MIN_REFRESH_RATE = "min_refresh_rate";
    private Context mContext;
    protected static boolean isAppInList = false;

    protected static final int STATE_DEFAULT = 0;
    protected static final int STATE_LOW = 1;
    protected static final int STATE_MEDIUM = 2;
    protected static final int STATE_HIGH = 3;
    protected static final int STATE_EXTREME = 4;

    private static final float REFRESH_STATE_DEFAULT = 60f;
    private static final float REFRESH_STATE_LOW = 30f;
    private static final float REFRESH_STATE_MEDIUM = 60f;
    private static final float REFRESH_STATE_HIGH = 90f;
    private static final float REFRESH_STATE_EXTREME = 120f;

    private static final String REFRESH_LOW = "refresh.low=";
    private static final String REFRESH_MEDIUM = "refresh.medium=";
    private static final String REFRESH_HIGH = "refresh.high=";
    private static final String REFRESH_EXTREME = "refresh.extreme=";

    private SharedPreferences mSharedPrefs;

    protected RefreshUtils(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public static void startService(Context context) {
        context.startServiceAsUser(new Intent(context, RefreshService.class),
                UserHandle.CURRENT);
    }

    private void writeValue(String profiles) {
        mSharedPrefs.edit().putString(REFRESH_CONTROL, profiles).apply();
    }

   protected void getOldRate(){
        defaultMaxRate = Settings.System.getFloat(mContext.getContentResolver(), KEY_PEAK_REFRESH_RATE, 60);
        defaultMinRate = Settings.System.getFloat(mContext.getContentResolver(), KEY_MIN_REFRESH_RATE, 60);
    }


    private String getValue() {
        String value = mSharedPrefs.getString(REFRESH_CONTROL, null);

        if (value == null || value.isEmpty()) {
            value = REFRESH_LOW + ":" + REFRESH_MEDIUM + ":" +
                    REFRESH_HIGH + ":" + REFRESH_EXTREME;
            writeValue(value);
        }
        return value;
    }

    protected void writePackage(String packageName, int mode) {
        String value = getValue();
        value = value.replace(packageName + ",", "");
        String[] modes = value.split(":");
        String finalString;

        switch (mode) {
            case STATE_LOW:
                modes[0] = modes[0] + packageName + ",";
                break;
            case STATE_MEDIUM:
                modes[1] = modes[1] + packageName + ",";
                break;
            case STATE_HIGH:
                modes[2] = modes[2] + packageName + ",";
                break;
            case STATE_EXTREME:
                modes[3] = modes[3] + packageName + ",";
                break;
        }

        finalString = modes[0] + ":" + modes[1] + ":" + modes[2] + ":" + modes[3] + ":" +
                modes[4];

        writeValue(finalString);
    }

    protected int getStateForPackage(String packageName) {
        String value = getValue();
        String[] modes = value.split(":");
        int state = STATE_DEFAULT;
        if (modes[0].contains(packageName + ",")) {
            state = STATE_LOW;
        } else if (modes[1].contains(packageName + ",")) {
            state = STATE_MEDIUM;
        } else if (modes[2].contains(packageName + ",")) {
            state = STATE_HIGH;
        } else if (modes[3].contains(packageName + ",")) {
            state = STATE_EXTREME;
        }
        return state;
    }

    protected void setRefreshRate(String packageName) {
        String value = getValue();
        String modes[];
        float maxrate = defaultMaxRate;
        float minrate = defaultMinRate;
        isAppInList = false;

            if (value != null) {
            modes = value.split(":");

            if (modes[0].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_LOW;
                if ( minrate > maxrate){
                minrate = maxrate;
                }
                isAppInList = true;
            } else if (modes[1].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_MEDIUM;
                if ( minrate > maxrate){
                minrate = maxrate;
                }
		isAppInList = true;
            } else if (modes[2].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_HIGH;
                if ( minrate > maxrate){
                minrate = maxrate;
                }
		isAppInList = true;
           } else if (modes[3].contains(packageName + ",")) {
                maxrate = REFRESH_STATE_EXTREME;
                if ( minrate > maxrate){
                minrate = maxrate;
                }
		isAppInList = true;
           }
          }
	Settings.System.putFloat(mContext.getContentResolver(), KEY_MIN_REFRESH_RATE, minrate);
        Settings.System.putFloat(mContext.getContentResolver(), KEY_PEAK_REFRESH_RATE, maxrate);
    }
}