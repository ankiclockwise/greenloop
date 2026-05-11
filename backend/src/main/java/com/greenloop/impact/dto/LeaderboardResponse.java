package com.greenloop.impact.dto;

import java.util.List;

public class LeaderboardResponse {
    private int page;
    private int limit;
    private int total;
    private List<LeaderboardItemDto> items;

    public LeaderboardResponse(int page, int limit, int total, List<LeaderboardItemDto> items) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.items = items;
    }

    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public int getTotal() { return total; }
    public List<LeaderboardItemDto> getItems() { return items; }
}
