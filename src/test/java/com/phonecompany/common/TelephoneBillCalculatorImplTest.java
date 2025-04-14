package com.phonecompany.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneBillCalculatorImplTest {

    @Test
    void testPrivateIsPeakTime() throws Exception {
        Method method = TelephoneBillCalculatorImpl.class.getDeclaredMethod("isPeakTime", LocalDateTime.class);
        method.setAccessible(true);

        // test borders
        assertTrue( (boolean) method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 8, 0, 0)));
        assertFalse( (boolean) method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 7, 59, 59)));
        assertTrue( (boolean) method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 15, 59, 59)));
        assertFalse( (boolean) method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 16, 0, 0)));
    }

    @Test
    void testPrivateIntervalToMinutes() throws Exception {
        Method method = TelephoneBillCalculatorImpl.class.getDeclaredMethod("intervalToMinutes", LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);

        assertEquals(1, method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 8, 0, 0), LocalDateTime.of(2023, 4, 14, 8, 0, 1)));
        assertEquals(1, method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 8, 0, 1), LocalDateTime.of(2023, 4, 14, 8, 1, 1)));
        assertEquals(2, method.invoke(new TelephoneBillCalculatorImpl(), LocalDateTime.of(2023, 4, 14, 8, 0, 0), LocalDateTime.of(2023, 4, 14, 8, 1, 59)));
    }

    @Test
    void testPrivateParseCallLog() throws Exception {
        String input = "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00\n" +
                "420776562353,18-01-2020 09:09:59,18-01-2020 09:10:00";

        Method method = TelephoneBillCalculatorImpl.class.getDeclaredMethod("parseCallLog", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, List<CallRecord>> result = (Map<String, List<CallRecord>>) method.invoke(null, input);

        assertEquals(2, result.size());

        // assert keys
        assertTrue(result.containsKey("420774577453"));
        assertTrue(result.containsKey("420776562353"));
        assertEquals(2, result.size());

        // assert CallRecord
        List<CallRecord> firstNumberCalls = result.get("420774577453");
        assertEquals(1, firstNumberCalls.size());
        assertEquals(new CallRecord("13-01-2020 18:10:15", "13-01-2020 18:12:57"), firstNumberCalls.get(0));

        // assert CallRecord
        List<CallRecord> secondNumberCalls = result.get("420776562353");
        assertEquals(2, secondNumberCalls.size());
        assertEquals(new CallRecord("18-01-2020 08:59:20", "18-01-2020 09:10:00"), secondNumberCalls.get(0));
        assertEquals(new CallRecord("18-01-2020 09:09:59", "18-01-2020 09:10:00"), secondNumberCalls.get(1));

    }

    @Test
    void testPrivateGetCallPrice() throws Exception {
        TelephoneBillCalculatorImpl calculator = new TelephoneBillCalculatorImpl();

        // Access private getCallPrice method
        Method getCallPrice = TelephoneBillCalculatorImpl.class.getDeclaredMethod("getCallPrice", CallRecord.class);
        getCallPrice.setAccessible(true);

        // 3-minute call during peak hours (08:00–16:00)
        CallRecord peakCall = new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:02:59");
        BigDecimal peakPrice = (BigDecimal) getCallPrice.invoke(calculator, peakCall);
        assertEquals(new BigDecimal("3.0"), peakPrice); // 3 minutes × 1.00

        // 7-minute call: 5 mins in peak, 2 extra discounted
        CallRecord longPeakCall = new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:06:59");
        BigDecimal longPeakPrice = (BigDecimal) getCallPrice.invoke(calculator, longPeakCall);
        assertEquals(new BigDecimal("5.2"), longPeakPrice); // 5×1.00 + 0.20

        // 0-second call
        CallRecord zeroDuration = new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00");
        BigDecimal zeroPrice = (BigDecimal) getCallPrice.invoke(calculator, zeroDuration);
        assertEquals(BigDecimal.ZERO, zeroPrice);
    }

    @Test
    void testFindDiscountedNumber() throws Exception {
        TelephoneBillCalculatorImpl calculator = new TelephoneBillCalculatorImpl();

        // Access private getCallPrice method
        Method findDiscountedNumber = TelephoneBillCalculatorImpl.class.getDeclaredMethod("findDiscountedNumber", Map.class);
        findDiscountedNumber.setAccessible(true);

        // more calls
        Map<String, List<CallRecord>> callHistoryMap1 = new HashMap<>();
        callHistoryMap1.put("420774577453", new ArrayList<>(List.of(new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"), new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"))));
        callHistoryMap1.put("420774577454", new ArrayList<>(List.of(new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"))));

        // same number of calls
        Map<String, List<CallRecord>> callHistoryMap2 = new HashMap<>();
        callHistoryMap1.put("420774577453", new ArrayList<>(List.of(new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"), new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"))));
        callHistoryMap1.put("420774577454", new ArrayList<>(List.of(new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"), new CallRecord("18-01-2020 08:00:00", "18-01-2020 08:00:00"))));

        assertEquals("420774577454", findDiscountedNumber.invoke(calculator, callHistoryMap1));
    }

    @Test
    void testCalculate() throws Exception {
        TelephoneBillCalculatorImpl calculator = new TelephoneBillCalculatorImpl();

        String input = "420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57\n" +
                "420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00\n";

        assertEquals(new BigDecimal("1.5"), calculator.calculate(input));

    }

}