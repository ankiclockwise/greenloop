package com.greenloop.analytics;

public class PeakSlotDto {
    private final int hour;
    private final String label;
    private final long count;

    public PeakSlotDto(int hour, long count) {
        this.hour = hour;
        this.count = count;
        this.label = formatHour(hour);
    }

    private static String formatHour(int hour) {
        if (hour == 0) return "12 AM";
        if (hour < 12) return hour + " AM";
        if (hour == 12) return "12 PM";
        return (hour - 12) + " PM";
    }

    public int getHour() { return hour; }
    public String getLabel() { return label; }
    public long getCount() { return count; }
}
