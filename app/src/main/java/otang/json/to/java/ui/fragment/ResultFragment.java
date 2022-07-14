package otang.json.to.java.ui.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.blankj.utilcode.util.KeyboardUtils;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.lang.reflect.Modifier;
import org.json.JSONException;
import otang.json.to.java.databinding.ConfigViewBinding;
import otang.json.to.java.library.AnnotationStyle;
import otang.json.to.java.databinding.DialogViewBinding;
import otang.json.to.java.library.JavaBean;
import otang.json.to.java.library.Json2Bean;
import otang.json.to.java.library.OutputType;
import com.blankj.utilcode.util.FileIOUtils;
import otang.json.to.java.R;
import java.io.File;
import otang.json.to.java.databinding.ResultViewBinding;
import otang.json.to.java.util.AppUtils;
import otang.json.to.java.util.PrefUtils;

public class ResultFragment extends Fragment {

	private ConfigViewBinding configViewBinding;
	private GenerationResult generationResult;
	private Json2Bean.Builder builder;
	private PrefUtils prefUtils;
	private ResultViewBinding resultViewBinding;
	String pkgName, clsName, json;

	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
		pkgName = getArguments().getString("pkgName");
		clsName = getArguments().getString("clsName");
		json = getArguments().getString("json");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle saved) {
		configViewBinding = ConfigViewBinding.inflate(inflater);
		resultViewBinding = ResultViewBinding.inflate(inflater);
		return resultViewBinding.getRoot();
	}

	@Override
	public void onViewCreated(View view, Bundle saved) {
		super.onViewCreated(view, saved);
		prefUtils = new PrefUtils(getActivity());
		builder = new Json2Bean.Builder().setUseReturnThis(true);
		resultViewBinding.toolbar.mt.setTitle(getString(R.string.tab_output));
		generation();
		resultViewBinding.tietOutDir.setText(AppUtils.getJsonOutputDir().getAbsolutePath());
		setUpClick();
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
		if (StringUtils.isTrimEmpty(json)) {
			json = "{}";
		}
		JavaBean bean = json2Bean.toBean(json);
		String java = bean.toJava().trim();
		generationResult = new GenerationResult();
		generationResult.bean = bean;
		generationResult.java = java;
		if (java != null && bean != null) {
			resultViewBinding.tietResult.setText(java);
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
		builder.setPackageName(pkgName);
		builder.setClassName(clsName);
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

	private void setUpClick() {
		resultViewBinding.bWhole.setOnClickListener((v) -> onOutputWholeClick());
		resultViewBinding.bZip.setOnClickListener((v) -> onOutputZipClick());
		resultViewBinding.bSplist.setOnClickListener((v) -> onSplistOutputClick());
		resultViewBinding.bCopy.setOnClickListener((v) -> onResultCopyClick());
		resultViewBinding.bClear.setOnClickListener((v) -> onResultClearClick());
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
		showOutputDialog(getString(R.string.btn_out_whole), new File(outDir, String.format("%s.java", clsName)),
				(dia, with) -> {
					final TextInputEditText tiet = ((Dialog) dia).findViewById(R.id.tiet);
					File outFile = new File(tiet.getText().toString());
					if (outFile.isDirectory()) {
						tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						FileIOUtils.writeFileFromString(outFile, generationResult.java);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
					dia.dismiss();
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
		showOutputDialog(getString(R.string.btn_out_zip), new File(outDir, String.format("%s.zip", clsName)),
				(dia, with) -> {
					final TextInputEditText tiet = ((Dialog) dia).findViewById(R.id.tiet);
					File outFile = new File(tiet.getText().toString());
					if (outFile.isDirectory()) {
						tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						generationResult.bean.output(outFile, OutputType.ZIP);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
					dia.dismiss();
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
		showOutputDialog(getString(R.string.btn_out_splist), new File(outDir, String.format("%s", clsName)),
				(dia, with) -> {
					final TextInputEditText tiet = ((Dialog) dia).findViewById(R.id.tiet);
					File outFile = new File(tiet.getText().toString());
					if (outFile.isDirectory()) {
						tiet.setError(getString(R.string.message_same_foldername_exists));
						return;
					}
					try {
						generationResult.bean.output(outFile, OutputType.SPLIST);
						ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
					} catch (Throwable e) {
						ToastUtils.showShort(e.getMessage());
					}
					dia.dismiss();
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

	private void showOutputDialog(CharSequence title, final File out, final DialogInterface.OnClickListener l) {
		DialogViewBinding dialogViewBinding = DialogViewBinding.inflate(getLayoutInflater());
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(title)
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

	private class GenerationResult {
		public JavaBean bean;
		public String java;
	}
}