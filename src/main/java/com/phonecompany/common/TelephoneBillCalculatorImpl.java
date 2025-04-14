package com.phonecompany.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final int FIRST_MINUTES_LIMIT = 5;
    private static final double PEAK_RATE = 1.00;
    private static final double RATE = 0.50;
    private static final double DISCOUNTED_RATE = 0.20;

    @Override
    public BigDecimal calculate(String phoneLog) {
        Map<String, List<CallRecord>> callHistoryMap = parseCallLog(phoneLog);
        String discountedNumber = findDiscountedNumber(callHistoryMap);

        return callHistoryMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(discountedNumber))
                .flatMap(entry -> entry.getValue().stream())
                .map(this::getCallPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String findDiscountedNumber(Map<String, List<CallRecord>> callHistoryMap) {
        return callHistoryMap.entrySet().stream()
                .max(Comparator.<Map.Entry<String, List<CallRecord>>>comparingInt(e -> e.getValue().size())
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private BigDecimal getCallPrice(CallRecord callRecord) {
        LocalDateTime startTime = callRecord.getStartTime();
        LocalDateTime endTime = callRecord.getEndTime();

        int totalMinutes = intervalToMinutes(startTime, endTime);
        BigDecimal price = BigDecimal.ZERO;

        for (int i = 0; i < Math.min(totalMinutes, FIRST_MINUTES_LIMIT); i++) {
            price = price.add(BigDecimal.valueOf(calculateMinuteCost(startTime)));
            startTime = startTime.plusMinutes(1);
        }

        int remainingMinutes = totalMinutes - FIRST_MINUTES_LIMIT;
        if (remainingMinutes > 0) {
            price = price.add(BigDecimal.valueOf(DISCOUNTED_RATE));
        }

        return price;
    }

    private static Map<String, List<CallRecord>> parseCallLog(String input) {
        Map<String, List<CallRecord>> callHistoryMap = new HashMap<>();

        String[] lines = input.split("\\r?\\n");
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String phoneNumber = parts[0].trim();
                String start = parts[1].trim();
                String end = parts[2].trim();

                if (callHistoryMap.containsKey(phoneNumber)){
                    callHistoryMap.get(phoneNumber).add(new CallRecord(start, end));
                }else {
                    callHistoryMap.put(phoneNumber,new ArrayList<>(List.of(new CallRecord(start, end))));
                }
            } else {
                System.err.println("error wrong input: " + line);
            }
        }

        return callHistoryMap;
    }


    private double calculateMinuteCost(LocalDateTime time) {
        return isPeakTime(time) ? PEAK_RATE : RATE;
    }

    private boolean isPeakTime(LocalDateTime time) {
        LocalTime localTime = time.toLocalTime();
        LocalTime start = LocalTime.of(8, 0);   // inclusive
        LocalTime end = LocalTime.of(16, 0);    // exclusive

        return !localTime.isBefore(start) && localTime.isBefore(end);
    }

    private int intervalToMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
        return (int) Math.ceil(seconds / 60.0);
    }

}
