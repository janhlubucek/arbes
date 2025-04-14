package com.phonecompany.common;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class CallRecord {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public CallRecord(String start, String end) {
        this.startTime = LocalDateTime.parse(start, formatter);
        this.endTime = LocalDateTime.parse(end, formatter);
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallRecord that = (CallRecord) o;
        return startTime.equals(that.startTime) && endTime.equals(that.endTime);
    }
}
