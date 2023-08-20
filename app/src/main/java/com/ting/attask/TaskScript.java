package com.ting.attask;


import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Pair;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;


public class TaskScript {
    public  boolean isRUN;
    public  WebView webView=null;
    Activity mActivity;
    Context mContext;
//    Env
    JSONObject jsonEnvObject;
    PackageManager mPackageManager;

    //    每次执行任务的分割线
    static String divideLines = "==============TaskScript===============\n";




    //        初始化webview

    public void initWebView(){


        webView =new WebView(mActivity.getApplicationContext());

        //        清除之前的日志
        PublicUtils.logTextView = mActivity.findViewById(R.id.logView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // 设置支持执行js
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true); // 支持打开文件
        settings.setAllowFileAccessFromFileURLs(true);


//        设置js代码的客户端
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                int lineNumber = consoleMessage.lineNumber();
                String message = consoleMessage.message();
                String messageLevel = consoleMessage.messageLevel().name();


                String log = messageLevel+ " : " + message+" source:"+ lineNumber;

                PublicUtils.appendLog(log);

                return super.onConsoleMessage(consoleMessage);
            }
        });

//        设置提供js调用的代码
        webView.addJavascriptInterface(this, "AtTask");

//        加载js接口
        webView.evaluateJavascript(PublicUtils.JavaApiScript,null);

        webView.evaluateJavascript("document.title = '"+PublicUtils.AppName+" run'",null);

    }

    public TaskScript(Activity activity) {

        this.mActivity = activity;
        this.mContext = activity;

        //        初始化一些常用变量
        initEnv();

        //        初始化webview
        initWebView();
    }





    public void execScript(String script) {

        if(webView==null){
            initWebView();
        }

        PublicUtils.logTextView.append(divideLines);

        script = "(function(){try{" + script + "}catch(err){console.log(err);return 'error'}; return \"TaskScriptOK\" })()";



        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {

                if (Objects.equals(s, "\"TaskScriptOK\"")) {
                    //执行完成
//                    System.out.println("TaskScript:执行完成!");
//                    Toast.makeText(context, "TaskScript:执行完成!", Toast.LENGTH_LONG).show();
                    PublicUtils.appendLog(PublicUtils.AppName+" : TaskScript:执行完成!");
//                    TaskScript.webView = null;
                    TaskScript.this.isRUN = false;
                } else {
                    System.out.println("TaskScript执行异常:" + s);
//                Toast.makeText(context, "TaskScript执行异常:"+s, Toast.LENGTH_LONG).show();
                    PublicUtils.appendLog(PublicUtils.AppName+" : TaskScript执行异常:" + s);
//                    TaskScript.webView = null;
                    TaskScript.this.isRUN = false;
                }
//
            }
        });


    }


//    初始化一些常用变量
    public  void initEnv(){

        // TaskScript.js 长度
        try {
            jsonEnvObject =  new JSONObject();
            jsonEnvObject.put("JavaApiScriptLength", PublicUtils.JavaApiScriptLength);
            //    获取储存卡目录： /storage/emulated/0
            jsonEnvObject.put("SdcardPath",Environment.getExternalStorageDirectory().getAbsolutePath());
            //    获取app缓存目录：  data/user/0/com.ting.attask/files
            jsonEnvObject.put("AppFilesDir",mActivity.getApplicationContext().getFilesDir().getAbsolutePath());

        } catch (JSONException e) {
            e.printStackTrace();
            PublicUtils.appendLog(PublicUtils.AppName+" : jsonEnvObject初始化异常！！");
        }

        mPackageManager = mContext.getPackageManager();

//        获取设备宽和高
        Rect rect =  PublicUtils.getWindowSize(mContext);
        try {
            jsonEnvObject.put("width",rect.width());
            jsonEnvObject.put("height",rect.height());
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("获取设备宽高失败！");
        }



    }


//    全局按键
@JavascriptInterface
    public boolean performGlobalAction(String sParam) throws NoSuchFieldException, IllegalAccessException, JSONException {
        //        全局操作 无论那个位置都可以
        //        调用例子 funcName=callApi&data={"APINAME":"performGlobalActionApi","Field":"GLOBAL_ACTION_HOME","nClicks":2,"nTime":2000}
        //        "GLOBAL_ACTION_BACK"  返回/回退
        //        "GLOBAL_ACTION_HOME"  回到主屏幕
        //        "GLOBAL_ACTION_RECENTS"  打开最近任务
        //        "GLOBAL_ACTION_NOTIFICATIONS"  打开通知
        //        "GLOBAL_ACTION_QUICK_SETTINGS"  快速设置
        //        "GLOBAL_ACTION_POWER_DIALOG"  长打开按电源出现的对话框
        //        "GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN"  分屏当前应用
        //        "GLOBAL_ACTION_LOCK_SCREEN"  关闭屏幕
        //        "GLOBAL_ACTION_TAKE_SCREENSHOT"  屏幕截图

        JSONObject data = new JSONObject(sParam);
        String actionType = (String) PublicUtils.getOrDefault(data, "String", "actionType", "null");
        return PublicUtils.accessibilityServiceApi.performGlobalAction(AccessibilityService.class.getField(actionType).getInt(PublicUtils.accessibilityServiceApi));



    }

//    判断某个类是否存在
    @JavascriptInterface
    public boolean classExists(String className){

        try {
            Class.forName(className);


            return true;
        } catch (ClassNotFoundException e) {


            return false;
        }


    }

//    获取节点信息
    @JavascriptInterface

    public String getAllNodeInfo(boolean allWindows) throws JSONException {


        Pair<String, List<AccessibilityNodeInfo>> pair  = PublicUtils.accessibilityServiceApi.getAllNodeInfo( allWindows);


        return  pair.first ;
    }

//    判断节点是否存在
    @JavascriptInterface
    public String nodeExists(String sParam) throws JSONException {

        JSONObject node =  AccessibilityServiceApi.parseOneNodeInfo(PublicUtils.accessibilityServiceApi.nodeFilter(sParam));
        if(node!=null){
            System.out.println("节点存在:"+sParam);

            return node.toString();
        }
        System.out.println("节点不存在:"+sParam);


        return null;
    }


// 设置节点text文本
    @JavascriptInterface
    public static int nodeSetText(String sParam) throws JSONException {

        return PublicUtils.accessibilityServiceApi.setNodeText(sParam);
    }

//    点击节点
    @JavascriptInterface
    public  int nodeClick(String sParam) throws JSONException, InterruptedException {
        JSONObject data = new JSONObject(sParam);

        int offsetDx = (int) PublicUtils.getOrDefault(data, "Int", "offsetDx", 0);
        int offsetDy = (int) PublicUtils.getOrDefault(data, "Int", "offsetDy", 0);
        boolean clickUI = (boolean) PublicUtils.getOrDefault(data, "boolean", "clickUI", true);
        boolean parent = (boolean) PublicUtils.getOrDefault(data, "boolean", "parent", false);

        String position = (String) PublicUtils.getOrDefault(data, "String", "position", "center");


        AccessibilityNodeInfo nodeInfo = PublicUtils.accessibilityServiceApi.nodeFilter(sParam);


        if(nodeInfo==null){
            return -1;
        }

        if(parent){
            nodeInfo = nodeInfo.getParent();
        }


        try {
            nodeInfo.setClickable(true);
        }catch (Exception e){
//            e.printStackTrace();
        }




        if(clickUI&&nodeInfo.isClickable()){
//            nodeInfo.setClickable(true);
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)?1:0;
        }



        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        int X = rect.centerX()+offsetDx;
        int Y = rect.centerY()+offsetDy;

        // 左上角
        if(Objects.equals(position, "topLeft")){
            X = rect.left+offsetDx;
            Y = rect.top+offsetDy;
        }

        // 左下角
        if(Objects.equals(position, "bottomLeft")){
            X = rect.left+offsetDx;
            Y = rect.bottom+offsetDy;
        }

        // 右上角
        if(Objects.equals(position, "topRight")){
            X = rect.right+offsetDx;
            Y = rect.top+offsetDy;
        }

        // 右下角
        if(Objects.equals(position, "bottomRight")){
            X = rect.right+offsetDx;
            Y = rect.bottom+offsetDy;
        }


        data.put("X",X);
        data.put("Y",Y);

        return PublicUtils.accessibilityServiceApi.gestureClick(data)?1:0;
    }


//   执行手势
    @JavascriptInterface
    public boolean gestures(String sParam) throws JSONException {
        //    gesturesArray =  [
        //        {
        //        "delay":0
        //        "duration":500,
        //        "gesturesLi":[ [AppEvn.width*0.5, AppEvn.height*0.35],[AppEvn.width*0.5, AppEvn.height*0.1]]
        //        }
        //    ]

        return PublicUtils.accessibilityServiceApi.gestures(sParam);
    }




    @JavascriptInterface
//    一些常用变量
    public String getAppEnv() {
            return jsonEnvObject.toString();
    }



    @JavascriptInterface
//    根据格式获取时间
    public static String getDate(String format) {
//        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss"); //设置时间格式
        return PublicUtils.getDate(format);

    }



    @JavascriptInterface
    public void setFloatingWindowText(String str){
        if(PublicUtils.mFWindowTextView!=null){
            PublicUtils.mFWindowTextView.setText(str);
        }
    }

    @JavascriptInterface
//  毫秒单位睡眠
    public void sleep(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @JavascriptInterface
//    土司提示
    public void TOAST(String text, int i) {
//        Toast.LENGTH_SHORT = 0
//        Toast.LENGTH_LONG = 1
        Toast.makeText(mContext,  text, i).show();
    }

//    根据app名称获取包名
    @JavascriptInterface
    public String getPackageName(String appName){
        List<ApplicationInfo> installedApplications = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo:installedApplications) {
            System.out.println(mPackageManager.getApplicationLabel(applicationInfo)+":"+applicationInfo.packageName);
            if (appName.contentEquals(mPackageManager.getApplicationLabel(applicationInfo))) {

                return applicationInfo.packageName;
            }
        }
        return null;
    }

// 提供应用名称 启动一个ap
    @JavascriptInterface
    public boolean launchApp(String appName){
        String packageName = getPackageName(appName);
        if(packageName==null){
            return false;
        };

        return launchPackage(packageName);
    }

//    提供包名 启动一个app
//  通过应用名称启动应用. 如果该名称对应的应用不存在, 则返回false; 否则返回true. 如果该名称对应多个应用, 则只启动其中某一个.
    @JavascriptInterface
    public boolean launchPackage(String packageName){
        PackageManager mPackageManager= mContext.getPackageManager();
        Intent intent =  mPackageManager.getLaunchIntentForPackage(packageName);
        System.out.println(packageName);
        if (intent!=null){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            mContext.startActivity(intent);
            return true;
        }

        return false;
    }

    @JavascriptInterface
    //执行shell 命令后可以打印执行后返回的结果
    public String shellExec(String cmd) {
        Runtime mRuntime = Runtime.getRuntime();

        try {
            //Process中封装了返回的结果和执行错误的结果

            String[] shellCmd = {"/bin/sh", "-c", cmd};
            Process mProcess = mRuntime.exec(shellCmd);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            System.out.println("shell执行成功:" + cmd);
            System.out.println(mRespBuff.toString());

            return mRespBuff.toString();
//            Log.i("nioTag2", "执行shell2脚本成功 " + mRespBuff.toString());//结果
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("shell执行失败:" + cmd);
        }
        return "ShellError";
    }


    //    获取目录内容
    @JavascriptInterface
    public String listFiles(String path, int type) {
//        System.out.println(path);
        File[] files = new File(path).listFiles();
        JSONArray result = new JSONArray();
        if (files != null) {

            for (int i = 0; i < files.length; i++) {
                if (type == 0) {
                    result.put(files[i]);
                    continue;
                }
                if (type == 1 && files[i].isDirectory()) {
                    result.put(files[i]);
                    continue;
                }
                if (type == 2 && files[i].isFile()) {
                    result.put(files[i]);

                }

            }
        }
        return result.toString();
    }


    //    删除目录或文件
    @JavascriptInterface
    public boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (!file.isFile()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }

            for (File f : childFiles) {
                deleteFile(f.getPath());
            }

        }

        return file.delete();

    }

    //    创建多级目录
    @JavascriptInterface
    public boolean mkdirs(String path) {
        return new File(path).mkdirs();
    }

    @JavascriptInterface
//    文件是否存在
    public boolean fileExists(String path){
        return new File(path).exists();
    }

    @JavascriptInterface
    public boolean fileWrite(String filePath, String content,boolean append) {

        File f = new File(filePath);
        File path = new File(f.getParentFile().getAbsolutePath());

        if(!path.exists()&&!path.mkdirs()){
            PublicUtils.appendLog("创建目录失败:"+path.getAbsolutePath());
        }
        if(f.exists()&&f.isDirectory()){
            PublicUtils.appendLog("创建目录失败:已经存在相同名称的文件夹！");
            return false;
        }
        File file = new File(f.getParentFile().toString(),f.getName()) ;

        // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file,append);
            outStream.write(content.getBytes());
            outStream.close();
            return true;
        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            return false;
        }
    }

    @JavascriptInterface
    public String fileRead(String filePath )  {
        File file = new File(filePath);

        if(!file.exists()||file.isDirectory()){
            PublicUtils.appendLog(file.toString());
            PublicUtils.appendLog("文件读取失败,文件不存在或不是一个文件！");
            return "AtTaskFileReadError";
        }

        String res="";
        try{
            FileInputStream fin = new FileInputStream(filePath);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer);
            fin.close();
            return res;
        }

        catch(Exception e){
            e.printStackTrace();
            PublicUtils.appendLog("文件读取失败,读取发生异常！");
            return "AtTaskFileReadError";
        }



    }


    public void stopRun() {
        webView.getSettings().setJavaScriptEnabled(false);
        webView.removeAllViews();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.destroy();
        isRUN = false;
        webView = null;
    }
}
