/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paularanas.ascodingchallenge;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paul Aranas
 */
public class DataSender {

    static int sMax_pending_count = 0;
    private static volatile int sTotalRetries = 0;
    private static volatile int sTotalFailures = 0;
    String mServerUrl;
    boolean mRetrying = false;

    public DataSender(String url) {
        mServerUrl = url;

    }

    public void send(String data) {
        httpRequestWithRetry(data);
    }
    //Connect to network, POST request

    private void httpRequestWithRetry(String data) {

        new Thread() {
            public void run() {
                String stringData = data;

                Callable<Boolean> callable = new Callable<Boolean>() {
                    public Boolean call() throws Exception {

                        //Connect
                        HttpURLConnection connect = connectAndPost(data);

                        switch (connect.getResponseCode()) {
                            case HttpURLConnection.HTTP_OK:
                                // OK
                                Logger.getLogger(DataSender.class.getName())
                                        .log(Level.INFO, "Successful connection");
                                // otherwise, if any other status code is returned, 
                                //or no status code is returned, do stuff in the else block

                                if (mRetrying) {
                                    synchronized (this) {
                                        //decrement max retry pending counter
                                        sMax_pending_count--;
                                    }
                                }
                                return true; // EXIT 
                            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:

                                break;// retry
                            case HttpURLConnection.HTTP_UNAVAILABLE:

                                break;// retry, server unstable
                            default:

                                break;
                        }

                        //Disconnect
                        connect.disconnect();
                        mRetrying = true;
                        Logger.getLogger(DataSender.class.getName())
                                .log(Level.INFO, "Total Number of Retries: " + sTotalRetries++);

                        if (sMax_pending_count > 10) {
                            Logger.getLogger(DataSender.class.getName())
                                    .log(Level.WARNING, "The retry queue is full");
                            return true;
                        }
                        // retry
                        if (mRetrying) {
                            synchronized (this) {
                                //increment max pending retry counter
                                if (sMax_pending_count == 0) {
                                    sMax_pending_count += 1;
                                }
                            }

                            // Server returned HTTP error code.
                            //Throw Exception for Retry
                            System.out.println("Error, Retrying Request with Exponential Backoff");

                            throw new IOException("Connection timed out");

                        }
                        return true;
                    }
                };
                //Exponential backoff with a maximum of 10 attempts
                Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                        .retryIfExceptionOfType(IOException.class)
                        .retryIfRuntimeException()
                        .withWaitStrategy(WaitStrategies.exponentialWait(500, 512, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterAttempt(10))
                        .build();
                try {
                    retryer.call(callable);
                } catch (RetryException e) {
                    sTotalFailures++;
                    Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, "Failure Total: " + sTotalFailures, e);
                    e.printStackTrace();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }

        }.start();

    }

    private URL constructURL() {
        URL theUrl = null;
        try {
            theUrl = new URL(mServerUrl);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DataSender.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return theUrl;
    }
    //connect to HttpURLConnection
    //POST the string 

    private HttpURLConnection connectAndPost(String data) {

        HttpURLConnection connection = null;
        OutputStreamWriter theWriter = null;
        URL theUrl = constructURL();
        try {
            connection = (HttpURLConnection) theUrl.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            theWriter = new OutputStreamWriter(
                    connection.getOutputStream());
            theWriter.write("message=" + data);
            theWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(DataSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;

    }

}
