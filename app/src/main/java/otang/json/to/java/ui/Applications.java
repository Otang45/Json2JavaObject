package otang.json.to.java.ui;

import android.app.Application;
import android.content.Context;
import org.xutils.x;
import otang.json.to.java.R;
import com.blankj.utilcode.util.ToastUtils;
import me.weishu.reflection.Reflection;
import androidx.appcompat.app.AppCompatDelegate;

public class Applications extends Application {

	private static Applications app;

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
		Reflection.unseal(context);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		initUtils();
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

	}

	private void initUtils() {
		com.blankj.utilcode.util.Utils.init(this);
		initToast();
		x.Ext.init(this);
		x.Ext.setDebug(false);
	}

	private void initToast() {
		ToastUtils maker = ToastUtils.getDefaultMaker();
		maker.setLeftIcon(R.mipmap.ic_launcher);
	}

	public static Applications getApp() {
		return app;
	}

}