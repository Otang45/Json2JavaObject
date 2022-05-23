package otang.json.to.java.ui.activity;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.json.JSONException;
import otang.json.to.java.R;
import otang.json.to.java.databinding.ActivityMainBinding;
import otang.json.to.java.databinding.ConfigViewBinding;
import otang.json.to.java.databinding.DialogViewBinding;
import otang.json.to.java.databinding.MainViewBinding;
import otang.json.to.java.databinding.ResultViewBinding;
import otang.json.to.java.library.AnnotationStyle;
import otang.json.to.java.library.JavaBean;
import otang.json.to.java.library.Json2Bean;
import otang.json.to.java.library.OutputType;
import otang.json.to.java.networking.ApiClient;
import otang.json.to.java.networking.ApiInterface;
import otang.json.to.java.networking.RequestNetwork;
import otang.json.to.java.networking.RequestNetworkController;
import otang.json.to.java.preference.WindowPreference;
import otang.json.to.java.util.AppUtils;
import otang.json.to.java.util.GsonUtils;
import otang.json.to.java.util.PrefUtils;
import otang.json.to.java.util.StringEscapeUtils;

public class MainActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

	private static final int CODE_REQUEST_WRITE_STORAGE_PERMISSION = 100;
	private static final int GONE = android.view.View.GONE;
	private static final int VISIBLE = android.view.View.VISIBLE;
	private ActivityMainBinding binding;
	private ConfigViewBinding configViewBinding;
	private DialogViewBinding dialogViewBinding;
	private GenerationResult generationResult;
	private Json2Bean.Builder builder;
	private MainViewBinding mainViewBinding;
	private RequestNetwork requestNetwork;
	private ResultViewBinding resultViewBinding;
	private PrefUtils prefUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setupWindow();
		checkPremission();
		prefUtils = new PrefUtils(this);
		builder = new Json2Bean.Builder().setUseReturnThis(true);
		requestNetwork = new RequestNetwork(this);
		configViewBinding = binding.config;
		mainViewBinding = binding.main;
		resultViewBinding = binding.result;
		dialogViewBinding = DialogViewBinding.inflate(getLayoutInflater());
		setUpClick();
		setUpLogic();
	}

	private void setUpClick() {
		binding.mt.setNavigationOnClickListener((v) -> finish());
		binding.mt.setOnMenuItemClickListener(this);
		mainViewBinding.bCopy.setOnClickListener((v) -> onCopyClick());
		mainViewBinding.bPaste.setOnClickListener((v) -> onPasteClick());
		mainViewBinding.bClear.setOnClickListener((v) -> onClearClick());
		mainViewBinding.bCompress.setOnClickListener((v) -> onCompressClick());
		mainViewBinding.bFormat.setOnClickListener((v) -> onFormatClick());
		mainViewBinding.bFromUrl.setOnClickListener((v) -> onFromNetworkClick());
		mainViewBinding.bMore.setOnClickListener((v) -> onMoreClick());
		resultViewBinding.bWhole.setOnClickListener((v) -> onOutputWholeClick());
		resultViewBinding.bZip.setOnClickListener((v) -> onOutputZipClick());
		resultViewBinding.bSplist.setOnClickListener((v) -> onSplistOutputClick());
		resultViewBinding.bCopy.setOnClickListener((v) -> onResultCopyClick());
		resultViewBinding.bClear.setOnClickListener((v) -> onResultClearClick());
	}

	private void setUpLogic() {
		mainViewBinding.getRoot().setVisibility(VISIBLE);
		configViewBinding.getRoot().setVisibility(GONE);
		resultViewBinding.getRoot().setVisibility(GONE);
		// Modifier
		int modifier = prefUtils.getInteger("modifier");
		configViewBinding.rgModifier.setOnCheckedChangeListener((group, checkedId) -> {
			prefUtils.saveAs("modifier", group.getCheckedRadioButtonId());
		});
		if (modifier != 0 && modifier != 1) {
			if (modifier == configViewBinding.rbPrivate.getId()) {
				configViewBinding.rbPrivate.setChecked(true);
			} else {
				configViewBinding.rbPrivate.setChecked(false);
			}
			if (modifier == configViewBinding.rbPublic.getId()) {
				configViewBinding.rbPublic.setChecked(true);
			} else {
				configViewBinding.rbPublic.setChecked(false);
			}
			if (modifier == configViewBinding.rbProtected.getId()) {
				configViewBinding.rbProtected.setChecked(true);
			} else {
				configViewBinding.rbProtected.setChecked(false);
			}
		}
		// Method
		configViewBinding.cbSetter.setChecked(prefUtils.getBoolean("setter"));
		configViewBinding.cbSetter.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("setter", isChecked);
		});
		configViewBinding.cbGetter.setChecked(prefUtils.getBoolean("getter"));
		configViewBinding.cbGetter.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("getter", isChecked);
		});
		configViewBinding.cbReturn.setChecked(prefUtils.getBoolean("return"));
		configViewBinding.cbReturn.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("return", isChecked);
		});
		// Other
		configViewBinding.cbPrimitiveType.setChecked(prefUtils.getBoolean("primitive"));
		configViewBinding.cbPrimitiveType.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("primitive", isChecked);
		});
		configViewBinding.cbLongInteger.setChecked(prefUtils.getBoolean("longint"));
		configViewBinding.cbLongInteger.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("longint", isChecked);
		});
		configViewBinding.cbInitialize.setChecked(prefUtils.getBoolean("initialize"));
		configViewBinding.cbInitialize.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("initialize", isChecked);
		});
		configViewBinding.cbAcceptNull.setChecked(prefUtils.getBoolean("accept"));
		configViewBinding.cbAcceptNull.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("accept", isChecked);
		});
		// Implement
		configViewBinding.cbParcellable.setChecked(prefUtils.getBoolean("parcellable"));
		configViewBinding.cbParcellable.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("parcellable", isChecked);
		});
		configViewBinding.cbSerializable.setChecked(prefUtils.getBoolean("serializable"));
		configViewBinding.cbSerializable.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("serializable", isChecked);
		});
		// Override
		configViewBinding.cbEquals.setChecked(prefUtils.getBoolean("equals"));
		configViewBinding.cbEquals.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("equals", isChecked);
		});
		configViewBinding.cbHashCode.setChecked(prefUtils.getBoolean("hashcode"));
		configViewBinding.cbHashCode.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("hashcode", isChecked);
		});
		configViewBinding.cbToString.setChecked(prefUtils.getBoolean("tostring"));
		configViewBinding.cbToString.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("tostring", isChecked);
		});
		// Annotation
		configViewBinding.cbGson.setChecked(prefUtils.getBoolean("gson"));
		configViewBinding.cbGson.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("gson", isChecked);
		});
		configViewBinding.cbFastJson.setChecked(prefUtils.getBoolean("fastjson"));
		configViewBinding.cbFastJson.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefUtils.saveAs("fastjson", isChecked);
		});
		resultViewBinding.tietOutDir.setText(AppUtils.getJsonOutputDir().getAbsolutePath());
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_run:
			generation();
			break;
		case R.id.menu_settings:
			mainViewBinding.getRoot().setVisibility(GONE);
			configViewBinding.getRoot().setVisibility(VISIBLE);
			resultViewBinding.getRoot().setVisibility(GONE);
			break;
		case R.id.menu_about:
			showAboutDialog();
			break;
		case R.id.menu_exit:
			finish();
			break;
		}
		return false;
	}

	private void onCopyClick() {
		ClipboardUtils.copyText(mainViewBinding.tietJson.getText());
	}

	private void onPasteClick() {
		mainViewBinding.tietJson.setText(ClipboardUtils.getText());
	}

	private void onClearClick() {
		mainViewBinding.tietJson.setText(null);
		mainViewBinding.tietJson.setError(null);
	}

	private void onCompressClick() {
		if (checkJsonIsEmpty()) {
			return;
		}
		try {
			String result = GsonUtils.compressionJson(mainViewBinding.tietJson.getText().toString());
			if (!"null".equals(result)) {
				mainViewBinding.tietJson.setText(result);
			}
		} catch (Throwable e) {
			ToastUtils.showShort(e.getMessage());
		}
	}

	private void onFormatClick() {
		if (checkJsonIsEmpty()) {
			return;
		}
		try {
			String result = GsonUtils.formatJson(mainViewBinding.tietJson.getText().toString());
			if (!"null".equals(result)) {
				mainViewBinding.tietJson.setText(result);
			}
		} catch (Throwable e) {
			ToastUtils.showShort(e.getMessage());
		}
	}

	private void onFromNetworkClick() {
		final DialogPositiveButtonClickListener l = new DialogPositiveButtonClickListener();
		final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(mainViewBinding.bFromUrl.getText()).setView(dialogViewBinding.getRoot())
				.setPositiveButton(android.R.string.ok, null).setNegativeButton(android.R.string.cancel, null)
				.setNeutralButton(R.string.paste, null).create();
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((v) -> {
			l.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
		});
		dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener((view) -> {
			dialogViewBinding.tiet.setText(ClipboardUtils.getText());
		});
		dialogViewBinding.tiet.setHint(R.string.hint_input_url);
		dialogViewBinding.tiet.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() != 0);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		dialogViewBinding.tiet.setText(null);
		dialogViewBinding.tiet.post(new Runnable() {
			@Override
			public void run() {
				KeyboardUtils.showSoftInput(dialogViewBinding.tiet);
			}
		});
	}

	private void onMoreClick() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle(R.string.btn_more);
		dialog.setItems(R.array.more_tools, (dia, which) -> {
			String json = mainViewBinding.tietJson.getText().toString();
			try {
				switch (getResources().getStringArray(R.array.more_tools_key)[which]) {
				case "escape": {
					mainViewBinding.tietJson.setText(StringEscapeUtils.escapeJava(json));
				}
					break;
				case "unescape":
					mainViewBinding.tietJson.setText(StringEscapeUtils.unescapeJava(json));
					break;
				case "unicodeToStr": {
					mainViewBinding.tietJson.setText(StringEscapeUtils.unicode2String(json));
				}
					break;
				case "strTounicode":
					mainViewBinding.tietJson.setText(StringEscapeUtils.string2Unicode(json));
					break;
				}
			} catch (Throwable e) {
				ToastUtils.showShort(e.getMessage());
			}
		});
		dialog.setPositiveButton(R.string.close, null);
		dialog.create().show();
	}

	private void onOutputWholeClick() {
		if (checkGeneration()) {
			return;
		}
		final File outDir = new File(resultViewBinding.tietOutDir.getText().toString());
		if (outDir.isFile()) {
			resultViewBinding.tietOutDir.setError(getString(R.string.message_same_filename_exists));
			return;
		}
		showOutputDialog(getString(R.string.btn_out_whole),
				new File(outDir, String.format("%s.java", mainViewBinding.tietCls.getText().toString())),
				(dia, with) -> {
					File outFile = new File(dialogViewBinding.tiet.getText().toString());
					if (outFile.isDirectory()) {
						dialogViewBinding.tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						FileIOUtils.writeFileFromString(outFile, generationResult.java);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
						dia.dismiss();
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
				});
	}

	private void onOutputZipClick() {
		if (checkGeneration()) {
			return;
		}
		final File outDir = new File(resultViewBinding.tietOutDir.getText().toString());
		if (outDir.isFile()) {
			resultViewBinding.tietOutDir.setError(getString(R.string.message_same_filename_exists));
			return;
		}
		showOutputDialog(getString(R.string.btn_out_zip),
				new File(outDir, String.format("%s.zip", mainViewBinding.tietCls.getText().toString())),
				(dia, with) -> {
					File outFile = new File(dialogViewBinding.tiet.getText().toString());
					if (outFile.isDirectory()) {
						dialogViewBinding.tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						generationResult.bean.output(outFile, OutputType.ZIP);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
						dia.dismiss();
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
				});
	}

	private void onSplistOutputClick() {
		if (checkGeneration()) {
			return;
		}
		final File outDir = new File(resultViewBinding.tietOutDir.getText().toString());
		if (outDir.isFile()) {
			resultViewBinding.tietOutDir.setError(getString(R.string.message_same_filename_exists));
			return;
		}
		showOutputDialog(getString(R.string.btn_out_splist),
				new File(outDir, String.format("%s", mainViewBinding.tietCls.getText().toString())), (dia, with) -> {
					File outFile = new File(dialogViewBinding.tiet.getText().toString());
					if (outFile.isDirectory()) {
						dialogViewBinding.tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						generationResult.bean.output(outFile, OutputType.SPLIST);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
						dia.dismiss();
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
				});
	}

	private void onResultCopyClick() {
		ClipboardUtils.copyText(resultViewBinding.tietResult.getText());
	}

	private void onResultClearClick() {
		resultViewBinding.tietResult.setText(null);
	}

	private boolean checkGeneration() {
		if (generationResult == null) {
			ToastUtils.showShort(R.string.message_not_generate);
			return true;
		}
		return false;
	}

	private void generation() {
		updateBuilder(builder);
		try {
			generation(builder.create());
		} catch (Throwable e) {
			switch (e.getMessage()) {
			case "JSON parse failed":
				ToastUtils.showShort(R.string.message_json_parse_exception);
				break;
			case "Keyless array conversion is not supported":
				ToastUtils.showShort(R.string.message_keyless_array_exception);
				break;
			default:
				ToastUtils.showShort(e.getMessage());
				break;
			}
		}
	}

	private void generation(Json2Bean json2Bean) throws JSONException, IOException {
		String json = mainViewBinding.tietJson.getText().toString();
		if (StringUtils.isTrimEmpty(json)) {
			json = "{}";
		}
		JavaBean bean = json2Bean.toBean(json);
		String java = bean.toJava().trim();
		generationResult = new GenerationResult();
		generationResult.bean = bean;
		generationResult.java = java;
		if (java != null && bean != null) {
			mainViewBinding.getRoot().setVisibility(GONE);
			configViewBinding.getRoot().setVisibility(GONE);
			resultViewBinding.getRoot().setVisibility(VISIBLE);
			binding.result.tietResult.setText(java);
		}
	}

	private void updateBuilder(Json2Bean.Builder builder) {
		final int modifier = prefUtils.getInteger("modifier");
		final int priv = configViewBinding.rbPrivate.getId();
		final int publ = configViewBinding.rbPublic.getId();
		final int prot = configViewBinding.rbProtected.getId();
		if (modifier == priv) {
			builder.setFieldModifiers(Modifier.PRIVATE);
		} else if (modifier == publ) {
			builder.setFieldModifiers(Modifier.PUBLIC);
		} else if (modifier == prot) {
			builder.setFieldModifiers(Modifier.PROTECTED);
		} else {
			builder.setFieldModifiers(Modifier.PUBLIC);
		}
		builder.setPackageName(mainViewBinding.tietPkg.getText().toString());
		builder.setClassName(mainViewBinding.tietCls.getText().toString());
		builder.setUseSetter(prefUtils.getBoolean("setter"));
		builder.setUseGetter(prefUtils.getBoolean("getter"));
		builder.setUseReturnThis(prefUtils.getBoolean("return"));
		builder.setUsePrimitiveTypes(prefUtils.getBoolean("primitive"));
		builder.setUseLongIntegers(prefUtils.getBoolean("longint"));
		builder.setInitializeCollections(prefUtils.getBoolean("initialize"));
		builder.setAcceptNullValue(prefUtils.getBoolean("accept"));
		builder.setMakeClassesParcelable(prefUtils.getBoolean("parcellable"));
		builder.setMakeClassesSerializable(prefUtils.getBoolean("serializable"));
		builder.setOverrideEquals(prefUtils.getBoolean("equals"));
		builder.setOverrideHashCode(prefUtils.getBoolean("hashcode"));
		builder.setOverrideToString(prefUtils.getBoolean("tostring"));
		if (prefUtils.getBoolean("gson")) {
			builder.addAnnotationStyle(AnnotationStyle.GSON);
		} else {
			builder.removeAnnotationStyle(AnnotationStyle.GSON);
		}
		if (prefUtils.getBoolean("fastjson")) {
			builder.addAnnotationStyle(AnnotationStyle.FASTJSON);
		} else {
			builder.removeAnnotationStyle(AnnotationStyle.FASTJSON);
		}
	}

	public boolean checkJsonIsEmpty() {
		if (TextUtils.isEmpty(mainViewBinding.tietJson.getText())) {
			ToastUtils.showShort(R.string.message_please_input_json);
			return true;
		}
		return false;
	}

	private void showOutputDialog(CharSequence title, final File out, final DialogInterface.OnClickListener l) {
		final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle(title)
				.setView(dialogViewBinding.getRoot()).setPositiveButton(R.string.output, null)
				.setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
		dialogViewBinding.tiet.setText(out.getAbsolutePath());
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((v) -> {
			l.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
		});
		dialogViewBinding.tiet.setHint(R.string.hint_filename);
		dialogViewBinding.tiet.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() != 0);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		dialogViewBinding.tiet.post(new Runnable() {

			@Override
			public void run() {
				String name = out.getName();
				int start = out.getAbsolutePath().length() - name.length();
				int end = out.getAbsolutePath().lastIndexOf(".");
				if (end < 0) {
					end = out.getAbsolutePath().length();
				}
				dialogViewBinding.tiet.setSelection(start, end);
				KeyboardUtils.showSoftInput(dialogViewBinding.tiet);
			}
		});
	}

	private void showAboutDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).setIcon(com.blankj.utilcode.util.AppUtils.getAppIcon())
				.setTitle(com.blankj.utilcode.util.AppUtils.getAppName()).setMessage(R.string.app_introduction)
				.setPositiveButton(android.R.string.ok, null).create();
		dialog.show();
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

	private class GenerationResult {
		public JavaBean bean;
		public String java;
	}

	private class DialogPositiveButtonClickListener implements DialogInterface.OnClickListener {

		public DialogPositiveButtonClickListener() {
		}

		@Override
		public void onClick(final DialogInterface dia, int with) {
			final AlertDialog dialog = (AlertDialog) dia;
			String input = dialogViewBinding.tiet.getText().toString();
			requestNetwork.startRequestNetwork(RequestNetworkController.GET, input, null,
					new RequestNetwork.RequestListener() {
						@Override
						public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
							mainViewBinding.tietJson.setText(response);
							dia.dismiss();
						}

						@Override
						public void onErrorResponse(String tag, String message) {
							ToastUtils.showShort(message);
							mainViewBinding.tietJson.setText(message);
						}
					});
		}
	}

	private void setupWindow() {
		new WindowPreference(this).applyEdgeToEdgePreference(getWindow(), getColor(R.color.colorSurface));
		getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorSurface));
		AppUtils.addSystemWindowInsetToPadding(binding.abl, false, true, false, false);
		AppUtils.addSystemWindowInsetToPadding(binding.getRoot(), false, false, false, true);
	}

	@Override
	public void onBackPressed() {
		if (mainViewBinding.getRoot().getVisibility() == GONE) {
			mainViewBinding.getRoot().setVisibility(VISIBLE);
			configViewBinding.getRoot().setVisibility(GONE);
			resultViewBinding.getRoot().setVisibility(GONE);
		} else {
			super.onBackPressed();
		}
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
			if (grantResults.length > 0) {
				boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
				if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
					// perform action when allow permission success
				} else {
					ToastUtils.showShort(R.string.message_not_write_storage_permission);
				}
			}
			break;
		}
	}
}
