package com.wzx.library.scheme;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wzx.library.R;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivitySchemePull extends Activity implements View.OnClickListener {
    private Activity mActivity;
    private EditText mEtOriUrl;
    private EditText mEtEndoceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_skipapp);

        mEtOriUrl = (EditText) findViewById(R.id.et_ori_url);
        mEtEndoceUrl = (EditText) findViewById(R.id.et_encode_url);

        mEtOriUrl.setText("198");
        mEtEndoceUrl.setText("hkugc://group/?groupid=198");

        findViewById(R.id.encodeurl).setOnClickListener(this);
        findViewById(R.id.skipapp).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skipapp:
                String s = mEtEndoceUrl.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri content_url = Uri.parse(s);
                        intent.setData(content_url);
                        startActivity(intent);
                    }catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "未找到相关app", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.encodeurl:
                String toString = mEtOriUrl.getText().toString();
                if (!TextUtils.isEmpty(toString)) {
                    String url = Uri.encode(toString);
                    mEtEndoceUrl.setText("hkugc://group/?groupid=" + url);
                }
                break;
            default:
                break;
        }
    }
}
