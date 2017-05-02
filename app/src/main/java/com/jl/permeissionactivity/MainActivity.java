package com.jl.permeissionactivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic();
            }
        });
    }

    private void takePic() {
        String[] perms={Manifest.permission.CAMERA,Manifest.permission.READ_SMS};
        baseRequestPermission(perms, new PermissionCallBack() {

            @Override
            public void permGrant(String[] permsGranted) {
                Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void permDeny(String[] permsDenied) {
                Toast.makeText(MainActivity.this, "未授权", Toast.LENGTH_SHORT).show();
            }
        },"调用拍照功能需要获取权限，是否跳转权限设置？");

    }
}
