package com.yanlong.im.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import net.cb.cb.library.utils.ToastUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordTextWather implements TextWatcher {
    EditText editText;
    Context context;

    public PasswordTextWather(EditText editText, Context context) {
        this.editText = editText;
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String editable = editText.getText().toString();
        String regEx = "[^a-zA-Z0-9_]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(editable);
        String str = m.replaceAll("").trim();
        if (!editable.equals(str)) {
            editText.setText(str);
            editText.setSelection(str.length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {


    }
}
