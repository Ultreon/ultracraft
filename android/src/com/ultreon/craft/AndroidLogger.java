package com.ultreon.craft;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class AndroidLogger implements Logger {
    private final String name;

    public AndroidLogger(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        
    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(String format, Object[] argArray) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray) {

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String msg) {
        Log.d(this.name, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        Log.d(this.name, String.format(format, arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.d(this.name, String.format(format, arg1), t);
        } else {
            Log.d(this.name, String.format(format, arg1, arg2));
        }
    }

    @Override
    public void debug(String format, Object[] argArray) {
        Log.d(this.name, String.format(format, argArray));
    }

    @Override
    public void debug(String msg, Throwable t) {
        Log.d(this.name, msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String msg) {
        Log.d(this.name + "/" + marker.getName(), msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        Log.d(this.name + "/" + marker.getName(), String.format(format, arg));
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.d(this.name + "/" + marker.getName(), String.format(format, arg1), t);
        } else {
            Log.d(this.name + "/" + marker.getName(), String.format(format, arg1, arg2));
        }
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        Log.d(this.name + "/" + marker.getName(), String.format(format, argArray));
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        Log.d(this.name + "/" + marker.getName(), msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        Log.i(this.name, msg);
    }

    @Override
    public void info(String format, Object arg) {
        Log.i(this.name, String.format(format, arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.i(this.name, String.format(format, arg1), t);
        } else {
            Log.i(this.name, String.format(format, arg1, arg2));
        }
    }

    @Override
    public void info(String format, Object[] argArray) {
        Log.i(this.name, String.format(format, argArray));
    }

    @Override
    public void info(String msg, Throwable t) {
        Log.i(this.name, msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String msg) {
        Log.i(this.name + "/" + marker.getName(), msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        Log.i(this.name + "/" + marker.getName(), String.format(format, arg));
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.i(this.name + "/" + marker.getName(), String.format(format, arg1), t);
        } else {
            Log.i(this.name + "/" + marker.getName(), String.format(format, arg1, arg2));
        }
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        Log.i(this.name + "/" + marker.getName(), String.format(format, argArray));
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        Log.i(this.name + "/" + marker.getName(), msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        Log.w(this.name, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        Log.w(this.name, String.format(format, arg));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.w(this.name, String.format(format, arg1), t);
        } else {
            Log.w(this.name, String.format(format, arg1, arg2));
        }
    }

    @Override
    public void warn(String format, Object[] argArray) {
        Log.w(this.name, String.format(format, argArray));
    }

    @Override
    public void warn(String msg, Throwable t) {
        Log.w(this.name, msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String msg) {
        Log.w(this.name + "/" + marker.getName(), msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        Log.w(this.name + "/" + marker.getName(), String.format(format, arg));
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.w(this.name + "/" + marker.getName(), String.format(format, arg1), t);
        } else {
            Log.w(this.name + "/" + marker.getName(), String.format(format, arg1, arg2));
        }
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        Log.w(this.name + "/" + marker.getName(), String.format(format, argArray));
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        Log.w(this.name + "/" + marker.getName(), msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        Log.e(this.name, msg);
    }

    @Override
    public void error(String format, Object arg) {
        Log.e(this.name, String.format(format, arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.e(this.name, String.format(format, arg1), t);
        } else {
            Log.e(this.name, String.format(format, arg1, arg2));
        }
    }

    @Override
    public void error(String format, Object[] argArray) {
        Log.e(this.name, String.format(format, argArray));
    }

    @Override
    public void error(String msg, Throwable t) {
        Log.e(this.name, msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String msg) {
        Log.e(this.name + "/" + marker.getName(), msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        Log.e(this.name + "/" + marker.getName(), String.format(format, arg));
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (arg2 instanceof Throwable t) {
            Log.e(this.name + "/" + marker.getName(), String.format(format, arg1), t);
        } else {
            Log.e(this.name + "/" + marker.getName(), String.format(format, arg1, arg2));
        }
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        Log.e(this.name + "/" + marker.getName(), String.format(format, argArray));
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        Log.e(this.name + "/" + marker.getName(), msg, t);
    }
}
