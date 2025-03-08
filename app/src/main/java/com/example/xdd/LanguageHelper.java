package com.example.xdd;

import android.app.Application;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import java.util.Locale;

public class LanguageHelper {
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_NAME = "AppPrefs";

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_LANGUAGE, languageCode);
        editor.apply();
        Log.d("LanguageHelper", "Idioma guardado en SharedPreferences: " + languageCode);
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String language = preferences.getString(PREF_LANGUAGE, "es"); // Idioma por defecto
        Log.d("LanguageHelper", "Idioma recuperado de SharedPreferences: " + language);
        return language;
    }

    public static void applyLanguage(Application application) {
        String languageCode = getLanguage(application);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            application.getBaseContext().createConfigurationContext(config);
        }

        application.getResources().updateConfiguration(config, application.getResources().getDisplayMetrics());
        Log.d("LanguageHelper", "Aplicando idioma en Application: " + languageCode);
    }

    public static void applyLanguage(Activity activity) {
        String savedLanguage = getLanguage(activity);
        Locale currentLocale = activity.getResources().getConfiguration().getLocales().get(0);

        if (!currentLocale.getLanguage().equals(savedLanguage)) {
            Log.d("LanguageHelper", "Cambiando idioma a: " + savedLanguage);

            Locale locale = new Locale(savedLanguage);
            Locale.setDefault(locale);

            Configuration config = new Configuration();
            config.setLocale(locale);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                activity.getApplicationContext().createConfigurationContext(config);
            }

            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

            // Reiniciar la actividad para aplicar los cambios
            activity.recreate();
        } else {
            Log.d("LanguageHelper", "El idioma ya está aplicado: " + savedLanguage);
        }
    }
}