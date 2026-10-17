Java.perform(function () {
    const NativeBridge = Java.use("com.fei.firstapp.NativeBridge");
    const getMessage = NativeBridge.getMessage.overload();

    getMessage.implementation = function () {
        console.log("[Hook] 已替换 native-lib 返回的字符串");
        return "这是 Hook 后的字符";
    };
});