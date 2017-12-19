package com.wzx.library;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivitySchemePull extends Activity implements View.OnClickListener {
    private Activity mActivity;
    private EditText mEtUrl;
    private EditText mEtUrl1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_skipapp);

        mEtUrl = (EditText) findViewById(R.id.et_url);
        mEtUrl1 = (EditText) findViewById(R.id.et_url1);

        mEtUrl.setText("http://www.baidu.com");
//        mEtUrl1.setText("hkmrkd://webview/?eid=miui&url=http://www.baidu.com");

        Button button = (Button) findViewById(R.id.btn);
        Button button1 = (Button) findViewById(R.id.btn1);

        button.setOnClickListener(this);
        button1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                String toString = mEtUrl.getText().toString();
                if (!TextUtils.isEmpty(toString)) {
                    String url = Uri.encode(toString);
                    mEtUrl1.setText("hkmrkd://webview/?eid=miui&url="+url);
                }
                break;
            case R.id.btn:
                String s = mEtUrl1.getText().toString();
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
            default:
                break;
        }
    }
}
