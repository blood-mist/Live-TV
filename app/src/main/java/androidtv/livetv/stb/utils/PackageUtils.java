package androidtv.livetv.stb.utils;

import android.content.Context;
import android.content.Intent;

public class PackageUtils {
	public static boolean isPackageInstalled(Context context, String packageName) {
		Intent intent=context.getPackageManager().getLaunchIntentForPackage(packageName);
		if(intent!=null)
			return true;
		else
			return false;
	}
}
