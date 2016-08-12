# HTTP_Post_DataSender
A Java class to send data to server with retries using exponential backoff

This class uses Java's HttpURLConnection class and Guava-Retrying, which is a small extension to 
Google's Guava Libray. Retries are set for a maximum of ten before failure. 
