package com.fei.firstapp;

/** Java entry point for functions implemented in the native shared library. */
public class NativeBridge {
    static {
        System.loadLibrary("native-lib");
    }

    public native String getMessage();
}
