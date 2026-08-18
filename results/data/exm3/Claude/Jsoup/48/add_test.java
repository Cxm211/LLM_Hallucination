// org/jsoup/helper/HttpConnectionTest.java
@Test public void singleHeaderValue() {
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    List<String> values = new ArrayList<String>();
    values.add("application/json");
    headers.put("Content-Type", values);
    HttpConnection.Response res = new HttpConnection.Response();
    res.processResponseHeaders(headers);
    assertEquals("application/json", res.header("Content-Type"));
}