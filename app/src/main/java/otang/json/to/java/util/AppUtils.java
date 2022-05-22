package otang.json.to.java.util;

import android.os.Build;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.graphics.Insets;
import android.view.View;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import android.net.ConnectivityManager;
import android.content.Context;
import java.util.Calendar;
import android.content.res.Resources;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import otang.json.to.java.R;
import java.io.PrintWriter;
import java.io.StringWriter;
import otang.json.to.java.ui.Applications;
import android.os.Environment;
import java.io.File;

public class AppUtils {

	public static File getJsonOutputDir() {
		return new File(Environment.getExternalStorageDirectory(), "Json2Java");
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
	}

	public static boolean isInternetAvailable() {
		try {
			InetAddress ipAddr = InetAddress.getByName("google.com");
			return !ipAddr.equals("");
		} catch (Exception e) {
			return false;
		}
	}

	public static float dipToPixels(Context context, float dipValue) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}

	public static void showToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void addSystemWindowInsetToPadding(View view, boolean left, boolean top, boolean right,
			boolean bottom) {
		ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat windowInsets) {
				Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
				int paddingLeft, paddingTop, paddingRight, paddingBottom;
				if (left) {
					paddingLeft = insets.left;
				} else {
					paddingLeft = 0;
				}
				if (top) {
					paddingTop = insets.top;
				} else {
					paddingTop = 0;
				}
				if (right) {
					paddingRight = insets.right;
				} else {
					paddingRight = 0;
				}
				if (bottom) {
					paddingBottom = insets.bottom;
				} else {
					paddingBottom = 0;
				}
				v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				return windowInsets;
			}
		});
	}

	public static void addSystemWindowInsetToMargin(View view, boolean left, boolean top, boolean right,
			boolean bottom) {
		ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat windowInsets) {
				Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
				int marginLeft, marginTop, marginRight, marginBottom;
				if (left) {
					marginLeft = insets.left;
				} else {
					marginLeft = 0;
				}
				if (top) {
					marginTop = insets.top;
				} else {
					marginTop = 0;
				}
				if (right) {
					marginRight = insets.right;
				} else {
					marginRight = 0;
				}
				if (bottom) {
					marginBottom = insets.bottom;
				} else {
					marginBottom = 0;
				}
				if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
					ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
					p.setMargins(marginLeft, marginTop, marginRight, marginBottom);
					v.requestLayout();
				}
				return windowInsets;
			}
		});
	}

	public static int getNavigationBarHeight(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DisplayMetrics metrics = new DisplayMetrics();
			context.getDisplay().getMetrics(metrics);
			int usableHeight = metrics.heightPixels;
			context.getDisplay().getRealMetrics(metrics);
			int realHeight = metrics.heightPixels;
			if (realHeight > usableHeight)
				return realHeight - usableHeight;
			else
				return 0;
		}
		return 0;
	}

	public static String greetingText() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour >= 10 && hour < 15) {
			return "Hallo, Selamat Siang!";
		} else if (hour >= 15 && hour < 18) {
			return "Hallo, Selamat Sore!";
		} else if (hour >= 18 && hour < 19) {
			return "Hallo, Selamat Petang!";
		} else if (hour >= 19 && hour < 24) {
			return "Hallo, Selamat Malam!";
		} else {
			return "Hallo, Selamat Pagi!";
		}
	}

	public static boolean isDarkTheme(Context context) {
		int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
			return true;
		} else {
			return false;
		}
	}

	public static Applications getApp() {
		return Applications.getApp();
	}

	public static String escapeJava(String data) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			switch (c) {
			case '"':
				result.append("\\\"");
				break;
			case '\\':
				result.append("\\\\");
				break;
			case '\b':
				result.append("\\b");
				break;
			case '\t':
				result.append("\\t");
				break;
			case '\n':
				result.append("\\n");
				break;
			case '\f':
				result.append("\\f");
				break;
			case '\r':
				result.append("\\r");
				break;
			default:
				if (Character.isISOControl(c)) {
					result.append(String.format("\\u%04x", (int) c));
				} else {
					result.append(c);
				}
			}
		}
		return result.toString();
	}

	public static String millis2FitTimeSpan(long millis, int precision) {
		if (precision <= 0)
			return null;
		precision = Math.min(precision, 5);
		Resources res = getApp().getResources();
		String[] units = new String[] { res.getString(R.string.time_unit_d), res.getString(R.string.time_unit_h),
				res.getString(R.string.time_unit_m), res.getString(R.string.time_unit_s),
				res.getString(R.string.time_unit_ms) };
		if (millis == 0)
			return 0 + units[precision - 1];
		StringBuilder sb = new StringBuilder();
		if (millis < 0) {
			sb.append("-");
			millis = -millis;
		}
		int[] unitLen = new int[] { 86400000, 3600000, 60000, 1000, 1 };
		for (int i = 0; i < precision; i++) {
			if (millis >= unitLen[i]) {
				long mode = millis / unitLen[i];
				millis -= mode * unitLen[i];
				sb.append(mode).append(units[i]);
			}
		}
		return sb.toString();
	}

	public static void copyToClipboard(CharSequence text, boolean showToast) {
		ClipboardUtils.copyText(text);
		if (showToast)
			ToastUtils.showShort(R.string.message_copied_to_clipboard);
	}

	public static void copyToClipboard(CharSequence text) {
		copyToClipboard(text, true);
	}

	public static String getFullStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}