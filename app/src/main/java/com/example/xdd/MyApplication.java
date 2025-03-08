package com.example.xdd;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LanguageHelper.applyLanguage(this);
        Log.d("MyApplication", "Idioma aplicado al iniciar la app");
    }
}

