package com.example.com.Models;

public class User {
    private String email, name, number, password;

    public User(String user, String name, String number, String password) {
        this.email = user;
        this.name = name;
        this.number = number;
        this.password = password;
    }

 public User() {}

    public String getUser() {
        return email;
    }

    public void setEmail(String user) {
        this.email = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
