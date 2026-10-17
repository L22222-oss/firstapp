#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_fei_firstapp_NativeBridge_getMessage(JNIEnv *env, jobject /* this */) {
    return env->NewStringUTF("Hello from C++ through JNI");
}
