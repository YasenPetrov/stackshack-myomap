package com.example.android.myomap;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by yasen on 22/02/15.
 */
public class Utility {

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("Utility: ", "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }
}
