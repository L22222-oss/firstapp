Java.perform(function () {
    const Calculator = Java.use("com.fei.firstapp.Calculator");

    Calculator.calculate.overload("double", "double", "char").implementation = function (a, b, op) {
        const result = this.calculate(a, b, op);
        console.log("[??] " + a + " " + op + " " + b + " = " + result);
        return result;
    };
});
