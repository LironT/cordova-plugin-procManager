package org.apache.cordova.plugin;

import android.content.Context;

import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.os.Build;
import android.util.Log;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;

public class ProcManager extends CordovaPlugin {
    private ActivityManager activityManager;
    private Activity activity;
    private Context context;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        activity = cordova.getActivity();
        context = activity.getApplicationContext();
        activityManager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        int exitStatus = 100;

        if (action.equals("exec")) {
            return execCommand(args, callbackContext, exitStatus);
        } else if (action.equals("getProcessList")) {
            return getProcessList(callbackContext, exitStatus);
        } else if (action.equals("killProcessByName")) {
            return killProcessByName(args, callbackContext, exitStatus);
        } else if (action.equals("killAllNonSystemProcess")) {
            return killAllNonSystemProcess(callbackContext, exitStatus);
        } else {
            return false;
        }

//        switch (action){
//            case "exec":
//                return execCommand(args, callbackContext, exitStatus);
//            case "getProcessList":
//                return getProcessList(callbackContext, exitStatus);
//            case "killProcessByName":
//                return killProcessByName(args, callbackContext, exitStatus);
//
//            default:
//                return false;
//        }
    }

    private boolean printOutput(int exitStatus, CallbackContext callbackContext, StringBuffer output) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("exitStatus", exitStatus);
        json.put("output", output.toString());
        callbackContext.success(json);
        return true;
    }

    private boolean printMessage(CallbackContext callbackContext, String message) throws JSONException {
        callbackContext.success(message);
        return true;
    }

    private boolean execCommand(JSONArray args, CallbackContext callbackContext, int exitStatus) throws JSONException {
        Process p;
        StringBuffer output = new StringBuffer();
        try {
            p = Runtime.getRuntime().exec((String) args.get(0));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
                p.waitFor();
            }

            exitStatus = p.exitValue();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return printOutput(exitStatus, callbackContext, output);
    }

    private boolean getProcessList(CallbackContext callbackContext, int exitStatus) throws JSONException {
        StringBuffer output = new StringBuffer();
        List<ActivityManager.RunningAppProcessInfo> runningTasks = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo runningProInfo : runningTasks) {
            output.append("importance:" + runningProInfo.importance + ", ");
            output.append("pid:" + runningProInfo.pid + ", ");
            output.append("uid:" + runningProInfo.uid + ", ");

            output.append("pkgList: ");
            for (int i = 0; i < runningProInfo.pkgList.length; i++) {
                output.append(runningProInfo.pkgList[i]);
                if (i < runningProInfo.pkgList.length) {
                    output.append("**_**");
                }
            }

            output.append(", ");
            output.append("processName:" + runningProInfo.processName + "\r\n");
        }

        exitStatus = 0; //p.exitValue();
        return printOutput(exitStatus, callbackContext, output);
    }

    private boolean killProcessByName(JSONArray args, CallbackContext callbackContext, int exitStatus) throws JSONException {
        String packageName = (String) args.get(0);
        StringBuffer output = new StringBuffer();
        activityManager.killBackgroundProcesses(packageName);
        exitStatus = 0; //p.exitValue();

        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        Method forceStopPackage = activityManager.getClass().getDeclaredMethod("forceStopPackage", String.class).setAccessible(true);
//        forceStopPackage.setAccessible(true);
//        forceStopPackage.invoke(activityManager, packageName);
//        //activityManager.getClass().getDeclaredMethod("forceStopPackage", String.class).setAccessible(true).invoke(activityManager, packageName);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return printMessage(callbackContext, "killed " + packageName);
    }

    private boolean killAllNonSystemProcess(CallbackContext callbackContext, int exitStatus) throws JSONException {
        ActivityManager am = (ActivityManager) activity.getApplicationContext().getSystemService(activity.getApplicationContext().ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> procInfos = am.getRunningAppProcesses();

        for (int pnum = 0; pnum < procInfos.size(); pnum++) {
            if ((procInfos.get(pnum)).processName.contains("android") ||
                    (procInfos.get(pnum)).processName.contains("system") ||
                    (procInfos.get(pnum)).processName.contains("huawei") ||
                    (procInfos.get(pnum)).processName.contains("samsung") ||
                    (procInfos.get(pnum)).processName.contains("motorola") ||
                    (procInfos.get(pnum)).processName.contains("nexus") ||
                    (procInfos.get(pnum)).processName.contains("LG") ||
                    (procInfos.get(pnum)).processName.contains("SD")) {
            } else {
                am.killBackgroundProcesses(procInfos.get(pnum).processName);
            }
        }

        exitStatus = 0; //p.exitValue();
        return printMessage(callbackContext, "killed all");
    }
}

//    List<ApplicationInfo> packages;
//    PackageManager pm;
//    pm = getPackageManager();
//        //get a list of installed apps.
//        packages = pm.getInstalledApplications(0);
//
//        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//
//        for (ApplicationInfo packageInfo : packages) {
//        if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1)continue;
//        if(packageInfo.packageName.equals("mypackage")) continue;
//        mActivityManager.killBackgroundProcesses(packageInfo.packageName);
//        }


/*
-------------------------------------------------------------------------------
ActivityManager am = (ActivityManager) mContext
                .getSystemService(Activity.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity
                .getPackageName();
-------------------------------------------------------------------------------
ActivityManager actvityManager = (ActivityManager)
this.getSystemService( ACTIVITY_SERVICE );
List<RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
for(RunningAppProcessInfo runningProInfo:procInfos){
        Log.d("Running Processes", "()()"+runningProInfo.processName);
}
For more information you can visit this link.
To get the application name based on package name use PackageManager class.
final PackageManager pkgmgr = getApplicationContext().getPackageManager();
ApplicationInfo appinfo;
try {
    appinfo = pkgmgr.getApplicationInfo( this.getPackageName(), 0);
} catch (final NameNotFoundException e) {
    appinfo = null;
}
final String applicationName = (String) (appinfo != null ? pkgmgr.getApplicationLabel(appinfo) : "(unknown)");
To get the app name on the basis of PID use:-
public static String getAppNameByPID(Context context, int pid){
    ActivityManager manager
               = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for(RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
        if(processInfo.pid == pid){
            return processInfo.processName;
        }
    }
    return "";
}
and finally to check if an app is system app or not use:-
private boolean isSystemPackage(PackageInfo pkgInfo) {
        return (pkgInfo.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0;
    }
* */
