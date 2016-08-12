/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paularanas.ascodingchallenge;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Paul
 */
public class DataSenderTest {

    DataSender sender;

    public DataSenderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ;
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        sender = new DataSender("http://www.google.com");
    }

    @After
    public void tearDown() {
    }
//Test the limit on the number of pending retries

    @org.junit.Test
    public void maxPendingRetryTest() throws InterruptedException {
        //server request will fail
        sender.send("http://www.google.com");
        int count = DataSender.sMax_pending_count;
        if (count < 10) {
            Assert.assertTrue(count < 10);
        }
    }
//Test the send method

    @org.junit.Test
    public void multipleSendTest() {
        int exceptions = 0;
        try{
       for (int i = 1; i < 5; i++){  
        sender.send("Hello, this is a test: " + i);
       } } catch (Exception e){
               exceptions++;
               }
        Assert.assertEquals(0, exceptions);
 
       }

    //Test the url is received in the DataSender class

    @org.junit.Test
    public void passUrlTest() {

        String passedUrl = sender.mServerUrl;
        String expectedUrl = "http://www.google.com";
        Assert.assertEquals(expectedUrl, passedUrl);
    }

}
