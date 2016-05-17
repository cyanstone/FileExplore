package com.my.cyanstone.fileexplore;

import android.util.Log;

/**
 * Created by bjshipeiqing on 2016/5/17.
 */
public class Loger {

    private static boolean debugSwitchOn = true;
    private static String message;

    public static void debug(String tag, String msg, Object... args) {
        if (debugSwitchOn) {
            message = String.format(msg, args);
            message += "     ";
            message += callMethodAndLine();
            Log.d(tag, message);
        }
    }

    private static String callMethodAndLine() {
        String rst = "at ";
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[2];
        rst += thisMethodStack.getClassName() + ",";
        rst += thisMethodStack.getMethodName();
        rst += "(" + thisMethodStack.getFileName();
        rst += ":" + thisMethodStack.getLineNumber() + ") ";
        return rst;
    }

    public static void setDebugSwitchOn(boolean debuggable) {
        debugSwitchOn = debuggable;
    }
}
