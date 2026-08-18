// org/jsoup/helper/HttpConnectionTest.java
@Test public void threeHeaderValuesCombine() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    List<String> values = new ArrayList<String>();
    values.add("gzip");
    values.add("deflate");
    values.add("br");
    headers.put("Accept-Encoding", values);
    HttpConnection.Response res = new HttpConnection.Response();
    res.processResponseHeaders(headers);
    assertEquals("gzip, deflate, br", res.header("Accept-Encoding"));
}