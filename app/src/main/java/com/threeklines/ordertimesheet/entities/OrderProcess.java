package com.threeklines.ordertimesheet.entities;

import com.google.common.base.Stopwatch;

public class OrderProcess {
    private String identifier;
    private long startTime;
    private long endTime;
    private String station;
    private String department;
    private String deptProcess;
    private String extraPersonnel;
    private String breaks;
    private Stopwatch stopwatch = null;
    private String syncState = "false";
    private long elapsedTime;

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getSyncState() {
        return syncState;
    }

    public void setSyncState(String syncState) {
        this.syncState = syncState;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDeptProcess() {
        return deptProcess;
    }

    public void setDeptProcess(String deptProcess) {
        this.deptProcess = deptProcess;
    }

    public String getExtraPersonnel() {
        return extraPersonnel;
    }

    public void setExtraPersonnel(String extraPersonnel) {
        this.extraPersonnel = extraPersonnel;
    }

    public String getBreaks() {
        return breaks;
    }

    public void setBreaks(String breaks) {
        this.breaks = breaks;
    }

    public Stopwatch getStopwatch() {
        return stopwatch;
    }

    public void setStopwatch(Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
    }
}
