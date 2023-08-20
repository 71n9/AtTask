package com.ting.attask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Looper;

import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    String scriptContent;

    TaskScript taskScriptObject;
    final int REQUEST_CODE = 1;

    WindowManager mWindowManager;
    WindowManager.LayoutParams mLayoutParams;



    boolean floatingWindowIsOpen=false;
     FrameLayout mDecorView;


    @Override
    protected void onRestart() {
        //        开启无障碍
        super.onRestart();
        openAccessibilitySettingsOn(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.isFinishing()){

            if(mDecorView!=null){
                mDecorView.removeAllViews();
            }
            if(mWindowManager!=null){
                try {
                    mWindowManager.removeView(mDecorView);
                }catch (Exception e){

                }

            }
        }


    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
//        super.onSaveInstanceState(savedInstanceState);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PublicUtils.logTextView =  findViewById(R.id.logView);
        PublicUtils.mMainActivity  =  MainActivity.this;
        PublicUtils.AppName = getResources().getString(R.string.app_name);

//         预先加载JavaApi
        LoadJavaApiScript();

//        开启webview调试功能
        WebView.setWebContentsDebuggingEnabled(true);

        taskScriptObject   =new TaskScript(this);
//        testTaskScriptObject   =new TaskScript(this);
//        testTaskScriptObject.webView.evaluateJavascript("document.title = '"+PublicUtils.AppName+" test'",null);
//


//        权限申请
        askForPermissions();

//        开启无障碍
        openAccessibilitySettingsOn(PublicUtils.logTextView);
//        执行js代码
//        taskScript.execScript("alert(window.anime.callFromJS(\"来自js的123\"))");

    }




    public static boolean isServiceON(Context context,String className){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>
                runningServices = activityManager.getRunningServices(100);
        if (runningServices.size() == 0){
            return false;
        }
        for (int i = 0;i<runningServices.size();i++){
            ComponentName service = runningServices.get(i).service;
            if (service.getClassName().contains(className)){
                return true;
            }
        }
        return false;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void initFloatingWindow(){
        mWindowManager = mWindowManager==null?(WindowManager) getSystemService(WINDOW_SERVICE):mWindowManager;
        mLayoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//

        mDecorView = new FrameLayout(MainActivity.this);

        View m = LayoutInflater.from(this).inflate(R.layout.floating_window, mDecorView, false);
        PublicUtils.mFWindowTextView  = (TextView) m;

        PublicUtils.mFWindowTextView.setTextSize((float) (PublicUtils.getWindowSize(this).width()/(13*4.5)));
        mLayoutParams.width = PublicUtils.getWindowSize(this).width()/12;
        mLayoutParams.height = PublicUtils.getWindowSize(this).width()/12;
        mLayoutParams.x = 20;
        mLayoutParams.y = PublicUtils.getWindowSize(this).centerY()/2;


        System.out.println("canDrawOverlays:"+Settings.canDrawOverlays(this));

        mDecorView.addView(m);
//        mWindowManager.addView(mDecorView,mLayoutParams);
        mDecorView.setOnTouchListener(new View.OnTouchListener() {
            private int x;
            private int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;


                        x = nowX;
                        y = nowY;
                        int newX = mLayoutParams.x + movedX;
                        int newY =  mLayoutParams.y + movedY;
                        int width = PublicUtils.getWindowSize(PublicUtils.mMainActivity).width();
                        int height = PublicUtils.getWindowSize(PublicUtils.mMainActivity).height();

                        newX = Math.min(newX, width);
                        newY = Math.min(newY, height);

                        mLayoutParams.x = newX;
                        mLayoutParams.y =newY;

                        mWindowManager.updateViewLayout(v, mLayoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        int w = PublicUtils.getWindowSize(PublicUtils.mMainActivity).width();
                        x=  event.getRawX()>= w/2?w-mLayoutParams.width-10:10;
                        mLayoutParams.x = x;
                        mWindowManager.updateViewLayout(v, mLayoutParams);
                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        mDecorView.setOnClickListener(new View.OnClickListener() {
            private long  clickTime=0;
            @Override
            public void onClick(View v) {

                if(clickTime==0){
                     clickTime = System.currentTimeMillis();
                     return;
                }
                long currentTime = System.currentTimeMillis();
                if(currentTime-clickTime<500){
                    stopRun(v);
                    clickTime = 0;
                    return;
                }
                clickTime = currentTime;

            }
        });


//        mButton = new Button(getApplicationContext());
//        mButton.setText("开关");
//        mButton.setBackgroundColor(Color.BLUE);

//        mButton.setOnTouchListener(new View.OnTouchListener() {
//            private int x;
//            private int y;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        x = (int) event.getRawX();
//                        y = (int) event.getRawY();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        int nowX = (int) event.getRawX();
//                        int nowY = (int) event.getRawY();
//                        int movedX = nowX - x;
//                        int movedY = nowY - y;
//
//
//                        x = nowX;
//                        y = nowY;
//                        int newX = mLayoutParams.x + movedX;
//                        int newY =  mLayoutParams.y + movedY;
//                        int width = PublicUtils.getWindowSize(PublicUtils.mMainActivity).width();
//                        int height = PublicUtils.getWindowSize(PublicUtils.mMainActivity).height();
//
//                        newX = Math.min(newX, width);
//                        newY = Math.min(newY, height);
//
//                        mLayoutParams.x = newX;
//                        mLayoutParams.y =newY;
//
//                        mWindowManager.updateViewLayout(v, mLayoutParams);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        int w = PublicUtils.getWindowSize(PublicUtils.mMainActivity).width();
//                        x=  event.getRawX()>= w/2?w-mLayoutParams.width:0;
//                        mLayoutParams.x = x;
//                        mWindowManager.updateViewLayout(v, mLayoutParams);
//                        break;
//
//                    default:
//                        break;
//                }
//                return false;
//            }
//        });
//
//        mButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                stopRun(v);
//            }
//        });


    }


//    开启全局悬浮窗
    public boolean switchFloatingWindow(View view){

        Button btn =  findViewById(R.id.switchFloatingWindow);

        if( mDecorView==null){
            initFloatingWindow();
        }

        if(floatingWindowIsOpen){
            floatingWindowIsOpen = false;
            mWindowManager.removeView(mDecorView);
            btn.setText("开启悬浮窗");
            return true;
        }

        if (!Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(MainActivity.this).setTitle("需要开启悬浮权限")
                    .setMessage("请在随后的设置中选择\""+PublicUtils.AppName+"\"并开启权限。")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                        @Override
                        public void onClick(DialogInterface dialog, int which) {//
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            MainActivity.this.startActivity(intent);
                        }
                    }).setNegativeButton("取消",null).show();
            //    去设置悬浮窗权限

            return false;
        }

        floatingWindowIsOpen = true;
        mWindowManager.addView(mDecorView, mLayoutParams);
        btn.setText("关闭悬浮窗");
        return true;
    }

//    开启无障碍服务
    public void openAccessibilitySettingsOn(View view){
        Button btn =  findViewById(R.id.openAccessibilityService);


        if(!isServiceON(this,AccessibilityServiceApi.class.getName())) {
            if(view==null){
                return;
            }
            btn.setBackgroundColor(Color.RED);
            btn.setText("服务未开启");
            Dialog dialog  = new AlertDialog.Builder(MainActivity.this).setTitle("需要启动无障碍服务")
                    .setMessage("软件需要打开\"无障碍服务\"才能运行,请在随后的设置中选择\""+PublicUtils.AppName+"\"并开启服务。").setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                @Override
                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件，点击事件没写，自己添加

                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    MainActivity.this.startActivity(intent);
                }
            }).setNegativeButton("取消", null).create();

//            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            dialog.show();


            return;
        }

        btn.setBackgroundColor(Color.GREEN);
        btn.setText("服务已启动");
    }
    public boolean askForPermissions(){
        int permissionCheck;

        String QUERY_ALL_PACKAGES = null;

        // 申请储存权限
        permissionCheck = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 申请全局悬浮窗口
        permissionCheck += this.checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionCheck += this.checkSelfPermission(Manifest.permission.QUERY_ALL_PACKAGES);
            QUERY_ALL_PACKAGES = Manifest.permission.QUERY_ALL_PACKAGES;

        }

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {         //未获得权限
            String[] strArr =  new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    QUERY_ALL_PACKAGES
            };

            // 请求授权
            this.requestPermissions(strArr ,REQUEST_CODE);// 自定义常量,任意整型
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("权限开启成功.");
//                Toast.makeText(this, "权限开启成功", Toast.LENGTH_LONG).show();
            } else {
                System.out.println("权限异常！！！");
                Toast.makeText(this, "权限开启失败", Toast.LENGTH_LONG).show();

            }
        }
    }

//    从网络上下载代码
    public void doGetAsync(URL url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient = okHttpClient.newBuilder().readTimeout(5,TimeUnit.SECONDS).build();

        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println(PublicUtils.AppName+" : 连接失败:"+url);
                Looper.prepare();
//                Toast.makeText(MainActivity.this, "连接失败:"+url, Toast.LENGTH_SHORT).show();
                PublicUtils.appendLog( PublicUtils.AppName+" : 连接失败:"+url);
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("onResponse");
                Looper.prepare();
                if (response.isSuccessful()) {
                    scriptContent = response.body().string();
//                    Toast.makeText(MainActivity.this,"加载成功!", Toast.LENGTH_SHORT).show();
                    PublicUtils.appendLog( PublicUtils.AppName+" : 加载成功!");
                    return;
                }

//                Toast.makeText(MainActivity.this,"请求错误:"+ response.toString(), Toast.LENGTH_SHORT).show();
                PublicUtils.appendLog( PublicUtils.AppName+" : 请求错误!"+response.toString());
                Looper.loop();
            }
        });
    }


//    停止执行
    public  void stopRun(View view){

        if(taskScriptObject.isRUN){
            taskScriptObject.stopRun();
            taskScriptObject.initWebView();

            PublicUtils.appendLog(PublicUtils.AppName+" : 任务已停止.");
            Toast.makeText(this, "任务已停止.", Toast.LENGTH_SHORT).show();

//            final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);

        }else{
            PublicUtils.appendLog(PublicUtils.AppName+" : 无运行中的任务.");
        }


    }

//    执行代码
    public void execScript(View view){
//        System.out.println(scriptContent);


        if(taskScriptObject==null){
            taskScriptObject = new TaskScript(this);
        }

        if(taskScriptObject.isRUN){
            Toast.makeText(this,"有任务正在执行！！", Toast.LENGTH_SHORT).show();
            PublicUtils.appendLog(PublicUtils.AppName+" : 有任务正在执行！！");
            return;
        }

        taskScriptObject.isRUN=true;
        if(PublicUtils.JavaApiScript==null){
            Toast.makeText(this,"JavaApiScript.js 未加载成功！", Toast.LENGTH_SHORT).show();
            PublicUtils.appendLog(PublicUtils.AppName+" : JavaApiScript.js未加载成功!!！");
            return;
        }

        taskScriptObject.execScript(scriptContent);

    }


//    添加日志

//  清除日志
    public void clearLogView(View view){
        PublicUtils.logTextView.setText("");
    }

// 显示代码
    public void showScript(View view){
//        Toast.makeText(this, scriptContent==null?"代码为空":scriptContent, Toast.LENGTH_LONG).show();


        PublicUtils.appendLog( scriptContent==null?PublicUtils.AppName+" : 代码为空":"\n"+scriptContent);
    }





// 加载代码
    public void loadScript(View view)  {
        EditText scriptInput =  findViewById(R.id.scriptInput);
        String script = scriptInput.getText().toString();
        if(script.equals("")){
            script = "file:/Download/task/task.js";
        }

        try {
            URL url = new URL(script);
            doGetAsync(url);
            return;
        } catch (Exception e) {
//            e.printStackTrace();
        }

        if (script.startsWith("file:")){
            try {
                String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String result = taskScriptObject.fileRead(sdcardPath+script.replace("file:",""));

                if(Objects.equals(result, "AtTaskFileReadError")){
                    PublicUtils.appendLog( PublicUtils.AppName+" : "+script+"文件读取失败！");
                    return;
                }
                script = result;
            } catch (Exception e) {
                e.printStackTrace();
                PublicUtils.appendLog( PublicUtils.AppName+" : "+script+"文件读取失败！");
                return ;
            }
        }





        scriptContent = script;

//        Toast.makeText(this, "加载成功!", Toast.LENGTH_SHORT).show();
        PublicUtils.appendLog( PublicUtils.AppName+" : 加载成功!");

    }



    //    加载js接口文件
    public void LoadJavaApiScript(){

        String fileName = "TaskScript.js"; //文件名字
        try{
            //得到资源中的asset数据流
            InputStream in = getResources().getAssets().open(fileName);


            int length = in.available();
            byte [] buffer = new byte[length];

            in.read(buffer);
            in.close();
            PublicUtils.JavaApiScript =  new String(buffer);
            PublicUtils.JavaApiScriptLength = PublicUtils.JavaApiScript.split("\n").length;

        }catch(Exception e){

            e.printStackTrace();

        }
    }



}