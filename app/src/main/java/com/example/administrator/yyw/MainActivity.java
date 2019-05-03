package com.example.administrator.yyw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private Button mbtn;
    private EditText medit;
    private Button mbtn2;
    private View view;
    private TextView mtxt;
    private TextView mtxt2;
    private Button mbtn3;
    public final static int REQUEST_READ_PHONE_STATE = 1;

    private static final String TAG = "TestFloatWinActivity";
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    //开启悬浮窗的Service
     Intent floatWinIntent;



    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //无title
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        mbtn = (Button) findViewById(R.id.btn);
        mbtn2 = (Button) findViewById(R.id.btn2);
        medit = (EditText) findViewById(R.id.edit);
        mtxt = (TextView) findViewById(R.id.text);
        mtxt2 = (TextView) findViewById(R.id.text2);
        mbtn3 = (Button) findViewById(R.id.btn3);


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }

        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW);
        if (permissionCheck2 != PackageManager.PERMISSION_GRANTED){
           // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},OVERLAY_PERMISSION_REQ_CODE);
        askForPermission();
        }



//        //权限开关
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (!Settings.canDrawOverlays(MainActivity.this)) {
//               // Toast.makeText(MainActivity.this, "是否允许打开悬浮窗权限？", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, 10);
//            }
//        }
        mbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();//杀死所有activity
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);

            }
        });

        mbtn.setBackgroundColor(Color.argb(0, 0, 0, 0));

        mbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int len = medit.length();
                if (medit.length() == 0) {
                    Toast.makeText(MainActivity.this, "请先输入时间！", Toast.LENGTH_SHORT).show();

                } else {



                    ((ViewGroup) view.getParent()).removeView(view);//每一次回收一个子的view（因为倒计时的时候回没秒调用一次）

                    //产生对话，挡住屏幕让用户点击不了屏幕
                    final Dialog dialog = new AlertDialog.Builder(getApplicationContext(), R.style.TransparentWindowBg).setView(view).create();
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams params = window.getAttributes();
                    params.gravity = Gravity.BOTTOM;
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setAttributes(params);
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                    dialog.setCancelable(false);
                    dialog.show();

                    //startLockTask();//开始锁屏
                    medit.setInputType(InputType.TYPE_NULL);//不弹出键盘
                    medit.clearFocus();//让EditText失去焦点
                    mtxt.setText("屏幕已锁定，好好做作业");
                    medit.setVisibility(View.INVISIBLE);
                    mbtn2.setVisibility(View.INVISIBLE);
                    int i = Integer.parseInt(medit.getText().toString());//把字符型转换为整型
                    CountDownTimer countDownTimer = new CountDownTimer(i*60*1000+1050, 1000) {
                        @Override
                        public void onTick(long l) {

                            //监听来电，如果来电则取消解锁屏幕（也就是取消对话）
                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            tm.getCallState();
                            if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {

                                dialog.dismiss();
                                finishAffinity();//杀死所有activity
                                //回到桌面
                                Intent home = new Intent(Intent.ACTION_MAIN);
                                home.addCategory(Intent.CATEGORY_HOME);
                                startActivity(home);

                            } else {
                                medit.setText("");
                                mbtn2.setVisibility(View.INVISIBLE);
                                mbtn.setText("倒计时");
                                mtxt2.setVisibility(View.VISIBLE);
                                mtxt2.setText((l / 1000 -1) + "S");
                            }

                        }

                        @Override
                        public void onFinish() {
                            mtxt.setText("屏幕已解锁，可以玩手机了");
                            mbtn2.setText("再玩一次");
                            mbtn.setText("争分夺秒！");
                            mtxt2.setVisibility(View.INVISIBLE);
                            mbtn2.setVisibility(View.VISIBLE);
                            dialog.dismiss();//结束对话框（对话框消失）
                            medit.setVisibility(View.VISIBLE);
                            mbtn3.setVisibility(View.VISIBLE);
                            //medit.setFocusable(true);//让EditText获得焦点
                            //  stopLockTask(); //结束锁屏

                        }
                    }.start();
                }
            }
        });

    }


    /**
     * 请求用户给予悬浮窗的权限
     */
    public void askForPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(MainActivity.this, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            } else {
               // startService(floatWinIntent);
            }
        }
    }
    /**
     * 用户返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= 23) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(MainActivity.this, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "权限授予成功！", Toast.LENGTH_SHORT).show();
                //启动FxService
              //  startService(floatWinIntent);
            }
        }
        }
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                finishAffinity();//杀死进程
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 判断是否有某项权限
     * * @param string_permission 权限
     * * @param request_code 请求码
     * * @return
     */
    public boolean checkReadPermission(String string_permission, int request_code) {
        boolean flag = false;
        if (ContextCompat.checkSelfPermission(this, string_permission) == PackageManager.PERMISSION_GRANTED) {
            //已有权限
            flag = true;
        } else {//申请权限
            ActivityCompat.requestPermissions(this, new String[]{string_permission}, request_code);
        }
        return flag;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }



}




