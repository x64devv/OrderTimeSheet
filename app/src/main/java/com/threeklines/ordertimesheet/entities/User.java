package com.threeklines.ordertimesheet.entities;

public class User {
    private String username;
    private String userid;
    private String password;
    private int ordersStarted;
    private int ordersCompleted;
    private String role;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getOrdersStarted() {
        return ordersStarted;
    }

    public void setOrdersStarted(int ordersStarted) {
        this.ordersStarted = ordersStarted;
    }

    public int getOrdersCompleted() {
        return ordersCompleted;
    }

    public void setOrdersCompleted(int ordersCompleted) {
        this.ordersCompleted = ordersCompleted;
    }
}
