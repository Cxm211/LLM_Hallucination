// org/jsoup/helper/HttpConnectionTest.java
@Test public void handlesHeaderEncodingOnRequestWithFourByteUtf8() {
    Connection.Request req = new HttpConnection.Request();
    req.addHeader("xxx", "\uD83D\uDE00");
}