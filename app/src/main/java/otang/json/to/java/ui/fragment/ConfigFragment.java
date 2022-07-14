package otang.json.to.java.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.fragment.app.Fragment;
import otang.json.to.java.R;
import otang.json.to.java.databinding.ConfigViewBinding;
import otang.json.to.java.util.PrefUtils;

public class ConfigFragment extends Fragment {

	private ConfigViewBinding configViewBinding;
	private PrefUtils prefUtils;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle saved) {
		configViewBinding = ConfigViewBinding.inflate(inflater);
		return configViewBinding.getRoot();
	}

	@Override
	public void onViewCreated(View view, Bundle saved) {
		super.onViewCreated(view, saved);
		configViewBinding.toolbar.mt.setTitle(getString(R.string.tab_config));
		prefUtils = new PrefUtils(getActivity());
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
	}
}