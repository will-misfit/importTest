package com.misfit.ble.sample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.misfit.ble.sample.utils.Convertor;

/**
 * Created by Quoc-Hung Le on 9/15/15.
 */
public class BoltActivity extends BaseActivity {

	private EditText mGroupIdEditText;
	private CheckBox mGroupIdCheckbox;
	private Button mGroupIdButton;

	private EditText mPasscodeEditText;
	private CheckBox mPasscodeCheckbox;
	private Button mPasscodeButton;

	private Button mBoltToShineButton;
	private TextView mBoltNotificationTextview;

	private Button mInterruptBolt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bolt_layout);

		mGroupIdEditText = (EditText) findViewById(R.id.txtGroupId);

		mGroupIdCheckbox = (CheckBox) findViewById(R.id.cbGetSetGroupId);

		mGroupIdButton = (Button) findViewById(R.id.btnGroupId);
		mGroupIdButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGroupIdCheckbox.isChecked()) {
					setMessage("ADD GROUP ID");
					mService.addGroupId(Short.parseShort(mGroupIdEditText.getText().toString()));
				} else {
					setMessage("GET GROUP ID");
					mService.getGroupId();
				}
			}
		});

		mPasscodeEditText = (EditText) findViewById(R.id.txtPassCode);

		mPasscodeCheckbox = (CheckBox) findViewById(R.id.cbGetSetPassCode);

		mPasscodeButton = (Button) findViewById(R.id.btnPassCode);
		mPasscodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPasscodeCheckbox.isChecked()) {
					String tmp = mPasscodeEditText.getText().toString();
					if (TextUtils.isEmpty(tmp) || tmp.length() != 32) {
						Toast.makeText(BoltActivity.this, "Wrong format", Toast.LENGTH_SHORT).show();
						return;
					}

					setMessage("SET PASSCODE");
					mService.setPasscode(Convertor.hexStringToByteArray(tmp));
				} else {
					setMessage("GET PASSCODE");
					mService.getPasscode();
				}
			}
		});

		mBoltToShineButton = (Button) findViewById(R.id.btnBoltToShine);
		mBoltToShineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(BoltActivity.this, ShineActivity.class);
				startActivity(i);
				finish();
			}
		});

		mInterruptBolt = (Button) findViewById(R.id.btnBoltInterrupt);
		mInterruptBolt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.interrupt();
			}
		});

		mBoltNotificationTextview = (TextView) findViewById(R.id.tvBoltNotification);
	}

	@Override
	protected void setUiState() {
		super.setUiState();

		mGroupIdButton.setEnabled(isReady);
		mPasscodeButton.setEnabled(isReady);

		mInterruptBolt.setEnabled(isBusy);

		mBoltNotificationTextview.setText(mMessage);
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(BoltActivity.this, ShineActivity.class);
		startActivity(i);
		finish();
	}
}
