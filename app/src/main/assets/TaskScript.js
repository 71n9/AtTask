var AppEvn = JSON.parse(window.AtTask.getAppEnv());




// node节点 相关接口
if(true){


        //    获取节点信息 默认当前交互节点-
        function getAllNodeInfo(allWindows){
            allWindows = allWindows?allWindows:false
            return window.AtTask.getAllNodeInfo(allWindows);
        }



//      增加节点信息 默认筛选值
        function nodeInfoParamFilter(param){
            __filter__ = {
                "id":undefined,
                "desc":undefined, "descMatchType":"equals",
                "text":undefined, "textMatchType":"equals",
                "className":undefined,"classNameType":"equals",
                "packageName":undefined,"packageNameType":"equals",
                "checked":undefined,"clickUI":true,"parent":false,
                "nodeIndex":1,"allWindows":undefined,
                "position":"center",//点击位置
            }




            param["text"] = param["text"] +"";
            param["desc"] = param["desc"] +"";

            // 转换checked为数字
            if(param["checked"]!=undefined){
                param["checked"] = param["checked"]==true?1:0;
            }

            //给默认类加上 android.widget前缀
            if(param["className"]!=undefined&&param["className"].indexOf(".")==-1){
                if(classExists("android.widget."+param["className"])){
                    param["className"] = "android.widget."+param["className"];
                }else if(classExists("androidx.widget."+param["className"])){
                    param["className"] = "androidx.widget."+param["className"];
                }else{
                    throw new Error("错误的className");
                }

            }


            Object.keys(param).forEach(x=>{__filter__[x]=param[x]})
            return JSON.stringify(__filter__)
        }


//      判断节点是否存在 存在则返回信息
        function nodeExists(param){
            return window.AtTask.nodeExists(nodeInfoParamFilter(param));
        }

//      点击节点
        function nodeClick(param){

            return window.AtTask.nodeClick(nodeInfoParamFilter(param));
        }

//      设置节点TEXT文本
        function nodeSetText(param){

            return window.AtTask.nodeSetText(nodeInfoParamFilter(param));
        }
}

// 文件/目录 相关接口
if(true){
 //获取sdcard路径
        function getSdcardPath(){
            return AppEvn.SdcardPath;
        }

        //获取程序缓存目录
        function getFilesDir(){
            return AppEvn.AppFilesDir;
        }


        // 列出目录
        function listFiles(str,i){
            // 0=读取所有  1=文件夹  2=文件
            i = i?i:0
            return JSON.parse(window.AtTask.listFiles(str,i))
        }

        // 删除目录或者文件
        function deleteFile(path){
            return window.AtTask.deleteFile(path)
        }

        //创建目录
        function mkdirs(path){
            return window.AtTask.mkdirs(path)
        }

        // 写文件
        function fileWrite(filePath,content,append){
            append= append?append:false;
            return window.AtTask.fileWrite(filePath,content,append)
        }

        // 文件是否存在
        function fileExists(filePath){

            return window.AtTask.fileExists(filePath)
        }

        // 读文件
        function fileRead(filePath){
            var ret = window.AtTask.fileRead(filePath)
            if(ret=="AttaskFileReadError"){

                let stack = (new Error()).stack


                stack = stack.split("\n").map((x)=>{
                    var li = x.match(/(.*>):(\d+):(\d+)/)
                    if(li==null){return x}
        //            console.log(li)
                    var lineNumber = li[2] -1
                    position = " --TaskScript.js:" + lineNumber;

                    if (lineNumber >= AppEvn.JavaApiScriptLength) {
                        position = " --source.js:" + (lineNumber - AppEvn.JavaApiScriptLength);

                    }
                    var log =li[1]+" "+position;
        //            console.log(JSON.stringify(log))
        //            console.log(log)
                    return log.replace(" (<anonymous>","")
                })

                console.log(stack.join("\n"))
                return null;
            }
            return ret
        }
}

// gestures手势 相关接口
if(true){

//    var gesturesArray =  [
//        {
//        "delay":0,
//        "duration":500,
//        "gesturesLi":[ [AppEvn.width*0.5, AppEvn.height*0.35],[AppEvn.width*0.5, AppEvn.height*0.1]]
//        }
//    ]

    function gestures(gesturesArray){
        gesturesArray.forEach(x=>{
//            console.log(x);
            if(x.duration==null){
                throw new Error("手势缺少duration 执行时间！")
            };

            x.delay = x.delay==null?x.delay:0;

            if(!x.gesturesLi || !x.gesturesLi.length){
                throw new Error("手势列表gesturesLi为空！")
            };

            x.gesturesLi.forEach(g=>{
//                console.log(g);
                if(!g||g.length!=2){
                    throw new Error("手势参数异常！必须为[x,y]")
                }
            });
        });
        return window.AtTask.gestures(JSON.stringify(gesturesArray));


    }

}

// 全局操作 performGlobalAction
if(true){

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
        function globalAction(param){
            var __li = [
                "GLOBAL_ACTION_BACK",//  返回/回退
                "GLOBAL_ACTION_HOME",//   回到主屏幕
                "GLOBAL_ACTION_RECENTS",//   打开最近任务
                "GLOBAL_ACTION_NOTIFICATIONS",//   打开通知
                "GLOBAL_ACTION_QUICK_SETTINGS",//   快速设置
                "GLOBAL_ACTION_POWER_DIALOG",//   长打开按电源出现的对话框
                "GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN",//   分屏当前应用
                "GLOBAL_ACTION_LOCK_SCREEN",//   关闭屏幕
                "GLOBAL_ACTION_TAKE_SCREENSHOT",//  屏幕截图
            ]
            if(!param["actionType"]|| !__li.includes(param["actionType"])){
                throw new Error("未知actionType:"+param["actionType"]);
            }
            return window.AtTask.performGlobalAction(JSON.stringify(param));
        }
}


// 判断某个Java类是否存在
function classExists(className){
    return window.AtTask.classExists(className);
}

// 根据格式 获取时间
// "YYYY-MM-dd HH:mm:ss"
function getDate(format){
    format = format?format:"YYYY-MM-dd HH:mm:ss"
    return window.AtTask.getDate(format);
}

// 根据名称 启动app
function launchApp(appName){
    return window.AtTask.launchApp(appName);
}

// 根据包名 启动app
function launchPackage(packageName){
    return window.AtTask.launchPackage(packageName);
}

// 设置悬浮窗text setFloatingWindowText
function setFloatingWindowText(str){
    if(str>999){
        str="999"
    }
    return window.AtTask.setFloatingWindowText(str);
}

//执行shell
function shellExec(cmd){
    return window.AtTask.shellExec(cmd);
}


function toast(msg,i){
    window.AtTask.TOAST(msg+"",i?1:0)
}

// 睡眠 毫秒
function sleep(num){
    window.AtTask.sleep(num)
}

;