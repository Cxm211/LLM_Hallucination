// org/jsoup/helper/HttpConnectionTest.java
@Test public void emptyHeaderValuesList() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    List<String> values = new ArrayList<String>();
    headers.put("X-Empty-Header", values);
    HttpConnection.Response res = new HttpConnection.Response();
    res.processResponseHeaders(headers);
    assertNull(res.header("X-Empty-Header"));
}