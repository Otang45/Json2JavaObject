package otang.json.to.java.ui.activity;

import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import android.provider.Settings;
import android.content.Intent;
import android.os.Environment;
import android.os.Build;
import android.os.Bundle;
import androidx.navigation.fragment.NavHostFragment;
import com.blankj.utilcode.util.ToastUtils;
import otang.json.to.java.R;
import androidx.navigation.NavController;
import otang.json.to.java.util.AppUtils;
import otang.json.to.java.databinding.ActivityMainBinding;
import otang.json.to.java.preference.WindowPreference;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	private static final int CODE_REQUEST_WRITE_STORAGE_PERMISSION = 100;
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setupWindow();
		checkPremission();
	}

	private boolean checkPremission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (Environment.isExternalStorageManager()) {
				return true;
			} else {
				try {
					Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
					intent.addCategory("android.intent.category.DEFAULT");
					intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
					startActivityForResult(intent, 2296);
				} catch (Exception e) {
					Intent intent = new Intent();
					intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
					startActivityForResult(intent, 2296);
				}
				return false;
			}
		} else {
			int result = ContextCompat.checkSelfPermission(MainActivity.this,
					Manifest.permission.READ_EXTERNAL_STORAGE);
			int result1 = ContextCompat.checkSelfPermission(MainActivity.this,
					Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(MainActivity.this,
						new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
						CODE_REQUEST_WRITE_STORAGE_PERMISSION);
				return false;
			}
		}
	}

	private void setupWindow() {
		new WindowPreference(this).applyEdgeToEdgePreference(getWindow(), getColor(R.color.colorSurface));
		getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorSurface));
		AppUtils.addSystemWindowInsetToPadding(binding.getRoot(), false, true, false, true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2296) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				if (Environment.isExternalStorageManager()) {
					// perform action when allow permission success
				} else {
					ToastUtils.showShort("Allow permission for storage access!");
				}
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
		case CODE_REQUEST_WRITE_STORAGE_PERMISSION:
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//gramdted
			} else {
				ToastUtils.showShort(R.string.message_not_write_storage_permission);
			}
		}
	}

}
