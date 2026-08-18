// org/jsoup/helper/HttpConnectionTest.java
@Test public void headersWithNullValues() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    List<String> values = new ArrayList<String>();
    values.add(null);
    values.add("value1");
    values.add("value2");
    headers.put("X-Test", values);
    HttpConnection.Response res = new HttpConnection.Response();
    res.processResponseHeaders(headers);
    assertEquals("value1, value2", res.header("X-Test"));
}
