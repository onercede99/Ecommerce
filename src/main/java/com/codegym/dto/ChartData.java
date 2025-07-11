package com.codegym.dto;

import java.util.List;

public class ChartData {
    private List<String> labels; // Ví dụ: ["01-07-2025", "02-07-2025", ...]
    private List<Double> data;   // Ví dụ: [100.50, 200.75, ...]

    public ChartData() {
    }

    public ChartData(List<String> labels, List<Double> data) {
        this.labels = labels;
        this.data = data;
    }

    // --- Getters and Setters ---
    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }
}
