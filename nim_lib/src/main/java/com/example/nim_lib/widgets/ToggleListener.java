package com.example.nim_lib.widgets;

import android.view.View;

/**
 * Created by hzlichengda on 14-3-31.
 */
public interface ToggleListener {
    void toggleOn(View v);

    void toggleOff(View v);

    void toggleDisable(View v);
}