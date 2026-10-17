package com.fei.firstapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

public class CalculatorActivity extends Activity implements View.OnClickListener {
    private TextView display;
    private final Calculator calculator = new Calculator();
    private String currentInput = "0";
    private double firstOperand;
    private char pendingOperator;
    private boolean startNewNumber = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        display = findViewById(R.id.calculator_display);

        int[] buttonIds = {
                R.id.button_clear, R.id.button_sign, R.id.button_percent, R.id.button_divide,
                R.id.button_7, R.id.button_8, R.id.button_9, R.id.button_multiply,
                R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_subtract,
                R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_add,
                R.id.button_zero, R.id.button_decimal, R.id.button_equals
        };
        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String value = button.getText().toString();
        switch (value) {
            case "C":
                clear();
                break;
            case "+/-":
                toggleSign();
                break;
            case "%":
                currentInput = format(Double.parseDouble(currentInput) / 100.0);
                updateDisplay();
                break;
            case "+":
            case "−":
            case "×":
            case "÷":
                chooseOperator(value.charAt(0));
                break;
            case "=":
                calculateResult();
                break;
            case ".":
                addDecimalPoint();
                break;
            default:
                addDigit(value);
                break;
        }
    }

    private void addDigit(String digit) {
        if (startNewNumber || "0".equals(currentInput)) {
            currentInput = digit;
            startNewNumber = false;
        } else if (currentInput.length() < 15) {
            currentInput += digit;
        }
        updateDisplay();
    }

    private void addDecimalPoint() {
        if (startNewNumber) {
            currentInput = "0.";
            startNewNumber = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        updateDisplay();
    }

    private void chooseOperator(char operator) {
        firstOperand = Double.parseDouble(currentInput);
        // The button uses a typographic minus (−), while Java arithmetic uses '-'.
        pendingOperator = operator == '\u2212' ? '-' : operator;
        startNewNumber = true;
    }

    private void calculateResult() {
        if (pendingOperator == '\0' || startNewNumber) return;
        double secondOperand = Double.parseDouble(currentInput);
        try {
            currentInput = format(calculator.calculate(firstOperand, secondOperand, pendingOperator));
            pendingOperator = '\0';
            startNewNumber = true;
            updateDisplay();
        } catch (ArithmeticException exception) {
            currentInput = "Error";
            pendingOperator = '\0';
            startNewNumber = true;
            updateDisplay();
        }
    }

    private void toggleSign() {
        if (!"0".equals(currentInput) && !"Error".equals(currentInput)) {
            currentInput = currentInput.startsWith("-")
                    ? currentInput.substring(1) : "-" + currentInput;
            updateDisplay();
        }
    }

    private void clear() {
        currentInput = "0";
        firstOperand = 0;
        pendingOperator = '\0';
        startNewNumber = true;
        updateDisplay();
    }

    private String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "Error";
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private void updateDisplay() {
        display.setText(currentInput);
    }
}
