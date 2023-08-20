package com.ting.attask;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;

import android.util.Pair;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class AccessibilityServiceApi  extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();


//        保存无障碍接口 到工具类
        PublicUtils.accessibilityServiceApi = AccessibilityServiceApi.this;
        // 服务开启

//        打印所有方法和属性
//        AccessibilityServiceApiName();
//        Toast.makeText(this, "Start!!!", Toast.LENGTH_SHORT).show();

        // 点击Home键
//        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    @Override
    public void onInterrupt() {

    }

    public boolean stringMatch(String s1,String s2,String matchType){

        switch (matchType){
            case "equals":
                return Objects.equals(s1,s2);
            case "startsWith":
                return s1.startsWith(s2);
            case "endsWith":
                return s1.endsWith(s2);
            case "contains":
                return s1.contains(s2);
        }
        PublicUtils.appendLog(PublicUtils.AppName+" : stringMatch异常 未知匹配类型！！");
        return false;
    }


//    设置节点文本
    public int setNodeText(String sParam) throws JSONException {
        JSONObject data = new JSONObject(sParam);
        AccessibilityNodeInfo nodeInfo = nodeFilter(sParam);
        if(nodeInfo==null){
            return -1;
        }

        String setText = (String) PublicUtils.getOrDefault(data, "STRING", "setText", "");

        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, setText);


        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)?1:0;
    }


//    筛选节点
    public  AccessibilityNodeInfo nodeFilter(String sParam) throws JSONException {

        JSONObject param = new JSONObject(sParam);

        //  根据id查找目标
        String viewIdResName = (String) PublicUtils.getOrDefault(param,"STRING","id",null);

        //  根据contentDescription内容描述 查找目标
        String contentDescription = (String) PublicUtils.getOrDefault(param,"STRING","desc",null);
        String descMatchType = (String) PublicUtils.getOrDefault(param,"STRING","descMatchType","equals");

        //  根据text查找目标
        String text = (String) PublicUtils.getOrDefault(param,"STRING","text","undefined");
        String textType = (String) PublicUtils.getOrDefault(param,"STRING","textMatchType","equals");


        //  根据className查找目标
        String className = (String) PublicUtils.getOrDefault(param,"STRING","className",null);
        String classNameType = (String) PublicUtils.getOrDefault(param,"STRING","classNameType","equals");


        //  根据packageName查找目标
        String packageName = (String) PublicUtils.getOrDefault(param,"STRING","packageName",null);
        String packageNameType = (String) PublicUtils.getOrDefault(param,"STRING","packageNameType","equals");


        // checked 是否选中
        int checked = (int) PublicUtils.getOrDefault(param,"INT","checked",-1);



        //  返回第几个
        int  nodeIndex = (int) PublicUtils.getOrDefault(param,"INT","nodeIndex",1);


        boolean allWindows = (boolean) PublicUtils.getOrDefault(param,"BOOLEAN","allWindows",false);



        //  获取每一个节点
        Pair<String,List<AccessibilityNodeInfo>> pair = getAllNodeInfo(allWindows);
        List<AccessibilityNodeInfo> nodeArray = pair.second;

        if(nodeArray==null||nodeArray.size()==0){
            PublicUtils.appendLog(PublicUtils.AppName+" : 没有获取到任何节点.");
            return null;
        }



        List<AccessibilityNodeInfo> result = new ArrayList<>();


        for ( AccessibilityNodeInfo nodeInfo : nodeArray) {

            if(nodeInfo==null){
                continue;
            }

            //  以id筛选
            if (viewIdResName!=null&&! stringMatch(nodeInfo.getViewIdResourceName(),viewIdResName,"equals")){
                continue;
            }

            //  以内容描述筛选节点
            if(!Objects.equals(contentDescription, "undefined")){
                String nodeDesc = nodeInfo.getContentDescription()==null?null:nodeInfo.getContentDescription().toString();
                if(nodeDesc==null&&!Objects.equals(contentDescription, "null")){
                    continue;
                }
                if(!stringMatch(nodeDesc,contentDescription,descMatchType)){
                    continue;
                }

            }


            //  以文本筛选节点

            if(!Objects.equals(text, "undefined")){
                String nodeText = nodeInfo.getText()==null?null:nodeInfo.getText().toString();
                if(nodeText==null&&!Objects.equals(text, "null")){
                    continue;
                }
                if(!stringMatch(nodeText,text,textType)){
                    continue;
                }

            }

            //  以类名筛选节点
            if(className!=null&&!stringMatch((String) nodeInfo.getClassName(),className,classNameType)){
                continue;
            }

            //  以包名筛选节点
            if(packageName!=null&&!stringMatch((String) nodeInfo.getPackageName(),packageName,packageNameType)){
                continue;
            }




            // 以是否选择筛选节点

            if (checked!=-1) {
                boolean isChecked =  nodeInfo.isChecked();
                if((checked!=1&&isChecked)||(checked==1&& !isChecked)){
                    continue;
                }
            }

            result.add(nodeInfo);
            if(result.size()>=nodeIndex){
                return result.get(nodeIndex-1);
            }

        }


        return null;
    }


    //    获取全部节点
//    allWindows默认false 只获取交互活动窗口的节点信息
    public List<AccessibilityNodeInfo> getAllNode(boolean allWindows){

        List<AccessibilityNodeInfo> nodes = new ArrayList<>();

        if (allWindows){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                SparseArray<List<AccessibilityWindowInfo>> sparseArray = this.getWindowsOnAllDisplays();
                for (int i = 0; i < sparseArray.size(); i++) {
                    List<AccessibilityWindowInfo> li = sparseArray.valueAt(i);
                    li.forEach(windowInfo->{nodes.add(windowInfo.getRoot());});
                }
                return nodes;

            }else{
                this.getWindows().forEach(windowInfo->{nodes.add(windowInfo.getRoot());});
                return nodes;
            }
        }


        nodes.add(this.getRootInActiveWindow());
        return nodes;
    }



//    点击操作 xy坐标 count点击次数 duration每次点击花费时间  intervalTime 每次点击的间隔时间
    public  boolean gestureClick(JSONObject data) throws InterruptedException {
        long startTime = (long) PublicUtils.getOrDefault(data, "Long", "startTime", 0L);
        long duration = (long) PublicUtils.getOrDefault(data, "Long", "duration", 30L);// 每次点击花费时间
        int count = (int) PublicUtils.getOrDefault(data, "Int", "count", 1);


        long intervalTime = (long) PublicUtils.getOrDefault(data, "Long", "intervalTime", 30L);// 每次点击的间隔时间

        int X = (int) PublicUtils.getOrDefault(data, "Int", "X", -1);
        int Y = (int) PublicUtils.getOrDefault(data, "Int", "Y", -1);
        if (X < 0 || Y < 0) {
            String s= "点击的坐标不能为负数: X:"+X+" y:"+Y;
            System.out.println(s);
            PublicUtils.appendLog(s);
            return false;
        }

        Path path = new Path();
        path.moveTo(X, Y);


        boolean result = true;
        for (int i = 0; i < count; i++) {
            boolean ret = dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, startTime, duration)).build(), null, null);
            result = result && ret;//有一次执行异常 则返回 false

            if (count != 1) {
                Thread.sleep(intervalTime);
            }

        }

        return result;

    }



    public static JSONObject parseOneNodeInfo(AccessibilityNodeInfo nodeInfo) throws JSONException {
        if(nodeInfo==null){
            return null;
        }
        JSONObject nodeInfoJSONObject  = new JSONObject();
        String[] sArr = nodeInfo.toString().split("; ");


        for (int i = 0; i < sArr.length; i++) {
            String s = sArr[i];
            if (s.contains(":")) {
                int idx = s.indexOf(":");
                String name = s.substring(0, idx);

                nodeInfoJSONObject.put(name, s.substring(idx+2));

                continue;
            }

            nodeInfoJSONObject.put("Object", s); // android.view.accessibility.AccessibilityNodeInfo@800070a8

        }
        return nodeInfoJSONObject;
    }

//    多级节点解析到JSON对象
    public static Pair<JSONObject,List<AccessibilityNodeInfo>> parseNodeInfo(AccessibilityNodeInfo node) throws JSONException {

        JSONObject childAllNodeInfoJSONObject = new JSONObject();

        List<AccessibilityNodeInfo> eachNodeInfoJSONArray = new ArrayList<>();
        if (node==null){
            return null;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo nodeChildOjb = node.getChild(i);
            eachNodeInfoJSONArray.add(nodeChildOjb);

            Pair<JSONObject,List<AccessibilityNodeInfo>> pair  = parseNodeInfo(nodeChildOjb);
            if(pair==null){
                continue;
            }
            JSONObject child = pair.first;
            List<AccessibilityNodeInfo> eachChild = pair.second;
            eachNodeInfoJSONArray.addAll(eachChild);

            String key = child.getString("packageName")+":"+child.getString("className")+"@" +child.getString("Object").split("@")[1];
            childAllNodeInfoJSONObject.put(key ,child);



        }

        String[] sArr = node.toString().split("; ");


        JSONObject allNodeInfoJSONObject = parseOneNodeInfo(node);



        allNodeInfoJSONObject.put("CHILDREN", childAllNodeInfoJSONObject);


        return new Pair<>(allNodeInfoJSONObject,eachNodeInfoJSONArray);
    }


//    获取节点信息文本
//    allWindows默认false 只获取交互活动窗口的节点信息
    public Pair<String, List<AccessibilityNodeInfo>> getAllNodeInfo(boolean allWindows) throws JSONException {
        List<AccessibilityNodeInfo> li = getAllNode(allWindows);
        JSONObject jsonObject = new JSONObject();
        List<AccessibilityNodeInfo> eachNodeList = new ArrayList<>();


        for (AccessibilityNodeInfo n : li) {


            Pair<JSONObject,List<AccessibilityNodeInfo>> pair  = parseNodeInfo(n);
            if(pair==null){
                continue;
            }
            JSONObject obj = pair.first;
            List<AccessibilityNodeInfo> eachChild = pair.second;

            eachNodeList.addAll(eachChild);

            String key = obj.getString("packageName")+":"+obj.getString("className")+"@" +obj.getString("Object").split("@")[1];
            jsonObject.put(key ,obj);

            eachNodeList.add(n);
        }

        return  new Pair<>(jsonObject.toString(),eachNodeList);
    }



    public boolean gestures(String sParam) throws JSONException {
        JSONArray gesturesArray = new JSONArray(sParam);
        //    gesturesArray =  [
        //        {
        //        "delay":0
        //        "duration":500,
        //        "gesturesLi":[ [AppEvn.width*0.5, AppEvn.height*0.35],[AppEvn.width*0.5, AppEvn.height*0.1]]
        //        }
        //    ]

        boolean result = true;
        for (int i = 0; i < gesturesArray.length(); i++) {
           JSONObject gesture =  gesturesArray.getJSONObject(i);

            int delay = (int) PublicUtils.getOrDefault(gesture, "Int", "delay", 0);
            int duration = (int) PublicUtils.getOrDefault(gesture, "Int", "duration", 500);
            JSONArray gesturesLi = (JSONArray) PublicUtils.getOrDefault(gesture, "Array", "gesturesLi", null);
            if (gesturesLi==null||gesturesLi.length()==0){
                System.out.println("执行异常:手势列表为空..");
                result = false;
            }

            Path path = new Path();

            int lastX =0;
            int lastY =0;

            for (int j = 0; j < gesturesLi.length(); j++) {
                JSONArray li = (JSONArray) gesturesLi.get(j);
                int x = li.getInt(0);
                int y = li.getInt(1);

                if (j ==0){
                    lastX = x;
                    lastY = y;
                    path.moveTo(x,y );
                    continue;
                }

                path.quadTo(lastX,lastY,x,y);
                lastX = x;
                lastY = y;


            }

            result = result &&dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, delay, duration)).build(),null , null);

        }

        return result;
    }
}

