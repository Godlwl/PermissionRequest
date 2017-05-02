package com.jl.permeissionactivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final int REQUESTCODE=1;
    private PermissionCallBack mPermCallBack;
    private String mPermSettingMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
    }

    /**
     * 权限请求结果回调
     */
    interface PermissionCallBack{
        /**
         * 允许权限
         */
        void permGrant(String[] permsGranted);

        /**
         * 拒绝权限
         */
        void permDeny(String[] permsDenied);
    }
    public void baseRequestPermission(String[] perms,PermissionCallBack callBack,String settingMsg){
        mPermCallBack=callBack;
        mPermSettingMsg=settingMsg;
        if (!checkPerm(perms)){
            requestPermissions(perms,REQUESTCODE);
        }else {
            if (callBack!=null){
                callBack.permGrant(perms);
            }
        }

    }

    /**
     * 权限检查
     */
    public boolean checkPerm(String[] perms){
        for (String perm:perms){
            if (ContextCompat.checkSelfPermission(this,perm)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * 允许的权限
         */
        List<String> permsGranted=new ArrayList<>();
        /**
         * 拒绝的权限
         */
        List<String> permsDenied=new ArrayList<>();
        if (requestCode==REQUESTCODE){
            /**
             * 添加允许权限和拒绝权限到相应的集合中
             */
            for (int i=0;i<permissions.length;i++){
                if (grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    permsGranted.add(permissions[i]);
                }else {
                    permsDenied.add(permissions[i]);
                }

            }
            /**
             *进行权限回调
             */
            if (permsGranted.size()!=0&&permsDenied.size()==0){
                if (mPermCallBack!=null){
                    mPermCallBack.permGrant((String[]) permsGranted.toArray(new String[permsGranted.size()]));
                }
            }else {
                if (mPermCallBack!=null){
                    mPermCallBack.permDeny((String[]) permsDenied.toArray(new String[permsDenied.size()]));
                }
                /**
                 * 点击不再提醒拒绝权限
                 * 提示是否进入设置界面设置权限
                 */
                for (String permDenied:permsDenied){
                    if (!shouldShowRequestPermissionRationale(permDenied)){
                        gotoSetting(mPermSettingMsg);
                        return;
                    }
                }

            }
        }
    }

    /**
     * 进入应用设置界面设置权限
     * @param msg
     */
    private void gotoSetting(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(localIntent);
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
