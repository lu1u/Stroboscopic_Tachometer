package pilloni.lucien.comptetoursstroboscopique.Graphiques;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lucien on 22/11/2016.
 */

public class PreferencesUtils {
	static final String PREFS_NAME = "CompteToursStroboscopique";
	static final String EXPOSURE = "lpi.exposure";

	private static void savePreference(Context c, String nom, int valeur)
	{
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(nom, valeur);
		editor.apply();
	}

	private static int getIntPreference(Context c, String nom, int defaut)
	{
		SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
		return settings.getInt(nom, defaut);
	}
	public static void saveExposure(Context c, int exposure)
	{
		savePreference(c, EXPOSURE, exposure);
	}

	public static int getExposure(Context c)
	{
		return getIntPreference(c, EXPOSURE, 0);
	}

}
