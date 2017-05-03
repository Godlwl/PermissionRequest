
关于适配方案网上有许许多多的文章，有使用框架的，有自己写的，我看了很多方法都觉得过于繁琐，所以想自己通过原生方法简单的封装一下，所以接下来我直接讲解我的封装方案。

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
我的做法是把权限申请封装在activity的基类中，我想大都数项目都会建这个基类。首先定义一个回调接口PermissionCallBack用来回调权限申请接口，主要有两个方法一个允许一个拒绝。baseRequestPermission方法是用来检查权限和申请权限，其中checkPerm是用来检查权限是否通过申请的，因为可以同时申请多个权限，所以需要遍历检查。最后需要重写FragmentActivity中的onRequestPermissionsResult方法进行权限结果处理，注意这其中的逻辑才是重点。
因为可能会申请多个权限，所以我定义了两个集合，一个申请通过的权限集合，另一个是拒绝的权限集合。而通过权限的判断条件是申请通过的权限集合不为空，且拒绝的权限集合为空，这样就证明了所有权限已经通过，可以调用某些功能了。这里面还有一个很重要的方法就是shouldShowRequestPermissionRationale(permDenied)，这个方法是用来判断用户是否点击了不再提醒按钮后拒绝的权限，一旦用户点击了不再提醒则会返回false，这时候可以提示用户进入设置界面进行权限设置。这就是在基类的封装，然后在你需要申请权限的地方直接调用baseRequestPermission就行，代码如下：

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
以后申请权限只要像上面这样调用就行，看起来就简洁很多了。

参考

https://github.com/googlesamples/easypermissions
这是google官方出的一个框架，用起来也挺简单的，大家可以看看。
https://github.com/tsy12321/easypermissions
这个是对上面那个框架做了一些封装，我觉得官方封装好了自己再封装一遍有点过度封装的感觉，所以参考了他的封装方式自己简化了一下。

