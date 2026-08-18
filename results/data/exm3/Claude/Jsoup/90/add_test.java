// org/jsoup/helper/HttpConnectionTest.java
@Test public void handlesHeaderEncodingOnRequestWithTruncatedUtf8() {
    Connection.Request req = new HttpConnection.Request();
    req.addHeader("xxx", "\u00E9");
}