package com.example.smart_attend.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("smart_attend_prefs", Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String role) {
        prefs.edit()
            .putString("jwt_token", token)
            .putString("role", role)
            .apply();
    }

    public String getToken() {
        return prefs.getString("jwt_token", null);
    }

    public String getRole() {
        return prefs.getString("role", null);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
