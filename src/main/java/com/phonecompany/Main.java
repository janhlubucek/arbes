package com.phonecompany;

import com.phonecompany.common.TelephoneBillCalculator;
import com.phonecompany.common.TelephoneBillCalculatorImpl;

public class Main {
    public static void main(String[] args) {

        String input = "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00\n";

        TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();
        System.out.println(calculator.calculate(input));
    }
}