// org/jsoup/helper/HttpConnectionTest.java
@Test public void handlesHeaderEncodingOnRequestTwoByteLead() {
        Connection.Request req = new HttpConnection.Request();
        req.addHeader("xxx", "Â");
    }