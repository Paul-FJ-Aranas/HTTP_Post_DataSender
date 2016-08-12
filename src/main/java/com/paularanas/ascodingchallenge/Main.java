/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paularanas.ascodingchallenge;

/**
 *
 * @author Paul
 */
public class Main {

    public static void main(String[] args) {

        DataSender dataSender = new DataSender("http://www.google.com");
        dataSender.send("Hello");
        System.out.println("Control immediately back to the caller");

    }

}
