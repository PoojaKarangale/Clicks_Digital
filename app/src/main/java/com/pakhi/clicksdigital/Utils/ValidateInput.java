package com.pakhi.clicksdigital.Utils;

import android.text.TextUtils;
import android.widget.EditText;

import com.rengwuxian.materialedittext.MaterialEditText;

public class ValidateInput {

    public static boolean field(MaterialEditText editText) {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError("required");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean field(EditText editText) {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError("required");
            editText.requestFocus();
            return false;
        }
        return true;
    }
}

