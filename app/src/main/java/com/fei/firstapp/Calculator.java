package com.fei.firstapp;

/** Basic arithmetic operations used by the calculator screen. */
public class Calculator {
    public double calculate(double first, double second, char operator) {
        switch (operator) {
            case '+':
                return first + second;
            case '-':
                return first - second;
            case '\u00d7':
            case '*':
                return first * second;
            case '\u00f7':
            case '/':
                if (second == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                return first / second;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}
