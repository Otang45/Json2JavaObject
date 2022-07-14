package otang.json.to.java.ui.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.json.JSONException;
import otang.json.to.java.R;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import otang.json.to.java.databinding.ConfigViewBinding;
import otang.json.to.java.databinding.DialogViewBinding;
import otang.json.to.java.databinding.MainViewBinding;
import otang.json.to.java.networking.RequestNetwork;
import otang.json.to.java.networking.RequestNetworkController;
import otang.json.to.java.util.PrefUtils;
import otang.json.to.java.util.StringEscapeUtils;
import otang.json.to.java.util.GsonUtils;

public class HomeFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

	private MainViewBinding mainViewBinding;
	private NavController navController;
	private RequestNetwork requestNetwork;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle saved) {
		mainViewBinding = MainViewBinding.inflate(inflater);
		return mainViewBinding.getRoot();
	}

	@Override
	public void onViewCreated(View view, Bundle saved) {
		super.onViewCreated(view, saved);
		navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
		requestNetwork = new RequestNetwork(getActivity());
		mainViewBinding.toolbar.mt.setTitleCentered(false);
		mainViewBinding.toolbar.mt.setTitle(getString(R.string.app_name));
		mainViewBinding.toolbar.mt.inflateMenu(R.menu.main);
		mainViewBinding.toolbar.mt.setOnMenuItemClickListener(this);
		setUpClick();
	}

	private void setUpClick() {
		mainViewBinding.bCopy.setOnClickListener((v) -> onCopyClick());
		mainViewBinding.bPaste.setOnClickListener((v) -> onPasteClick());
		mainViewBinding.bClear.setOnClickListener((v) -> onClearClick());
		mainViewBinding.bCompress.setOnClickListener((v) -> onCompressClick());
		mainViewBinding.bFormat.setOnClickListener((v) -> onFormatClick());
		mainViewBinding.bFromUrl.setOnClickListener((v) -> onFromNetworkClick());
		mainViewBinding.bMore.setOnClickListener((v) -> onMoreClick());
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
		DialogViewBinding dialogViewBinding = DialogViewBinding.inflate(getLayoutInflater());
		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(mainViewBinding.bFromUrl.getText())
				.setView(dialogViewBinding.getRoot()).setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null).setNeutralButton(R.string.paste, null).create();
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener((v) -> {
			//l.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
			requestNetwork.startRequestNetwork(RequestNetworkController.GET,
					dialogViewBinding.tiet.getText().toString(), null, new RequestNetwork.RequestListener() {
						@Override
						public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
							mainViewBinding.tietJson.setText(response);
							dialog.dismiss();
						}

						@Override
						public void onErrorResponse(String tag, String message) {
							ToastUtils.showShort(message);
							mainViewBinding.tietJson.setText(message);
						}
					});
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
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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

	public boolean checkJsonIsEmpty() {
		if (TextUtils.isEmpty(mainViewBinding.tietJson.getText())) {
			ToastUtils.showShort(R.string.message_please_input_json);
			return true;
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_run:
			generate();
			break;
		case R.id.menu_settings:
			navController.navigate(R.id.configFragment);
			break;
		case R.id.menu_about:
			showAboutDialog();
			break;
		case R.id.menu_exit:
			getActivity().finish();
			break;
		}
		return false;
	}

	private void generate() {
		Bundle bundle = new Bundle();
		bundle.putString("pkgName", mainViewBinding.tietPkg.getText().toString());
		bundle.putString("clsName", mainViewBinding.tietCls.getText().toString());
		String json = mainViewBinding.tietJson.getText().toString();
		if (StringUtils.isTrimEmpty(json)) {
			json = "{}";
		}
		bundle.putString("json", json);
		navController.navigate(R.id.resultFragment, bundle);
	}

	private void showAboutDialog() {
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setIcon(com.blankj.utilcode.util.AppUtils.getAppIcon())
				.setTitle(com.blankj.utilcode.util.AppUtils.getAppName()).setMessage(R.string.app_introduction)
				.setPositiveButton(android.R.string.ok, null).create();
		dialog.show();
	}
}