// org/jsoup/helper/HttpConnectionTest.java
@Test public void handlesHeaderEncodingOnRequestWithThreeByteUtf8() {
    Connection.Request req = new HttpConnection.Request();
    req.addHeader("xxx", "\u4E2D");
}