package com.ting.attask;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PublicUtils {

    public static TextView  mFWindowTextView;
    static String AppName;

    public static String JavaApiScript = null;
    public static TextView logTextView;
    public static int JavaApiScriptLength;
    public static AccessibilityServiceApi accessibilityServiceApi;
    public static MainActivity mMainActivity;

    public static Rect windowSize;


    //    添加日志
    public static void appendLog(String log) {

        mMainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    PublicUtils.logTextView.append(getDate("YYYYMMdd HH:mm:ss") + log + "\n");
                }
        });
    }


//    方便获取JSONObject的值
    public static Object getOrDefault(JSONObject data, String type, String name, Object Default) {


        try {

            switch (type.toUpperCase()) {
                case "STRING":
                    return data.getString(name);
                case "INT":
                    return data.getInt(name);
                case "BOOLEAN":
                    return data.getBoolean(name);
                case "ARRAY":
                    return data.getJSONArray(name);
                case "DOUBLE":
                    return data.getDouble(name);
                case "OBJECT":
                    return data.getJSONObject(name);
                case "LONG":
                    return data.getLong(name);
                default:
                    PublicUtils.appendLog(PublicUtils.AppName+" : 参数异常getOrDefault unknown type");
                    return Default;
            }

        } catch (JSONException ignored) {
//            PublicUtils.appendLog(PublicUtils.AppName+" : getOrDefault发生异常！");
        }
        return Default;
    }

//    方便根据时间格式获取时间
    public static String getDate(String format) {
//        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss"); //设置时间格式
        SimpleDateFormat formatter = new SimpleDateFormat(format); //设置时间格式

        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08")); //设置时区

        Date curDate = new Date(System.currentTimeMillis()); //获取当前时间

        return formatter.format(curDate);   //格式转换
    }


    public static Rect getWindowSize(Context context){


        if(PublicUtils.windowSize!=null){
            return PublicUtils.windowSize;
        }

        //获取系统window服务
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取屏幕参数
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);


        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

         windowSize = new Rect();
        System.out.println("windowSize:" + width + "," + height);

        windowSize.left = 0;
        windowSize.right = width;
        windowSize.top = 0;
        windowSize.bottom = height;

        return windowSize;
    }
}
