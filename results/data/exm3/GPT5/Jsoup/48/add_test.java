// org/jsoup/helper/HttpConnectionTest.java::multipleHeaderValuesAreJoinedWithComma
@Test public void multipleHeaderValuesAreJoinedWithComma() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("a");
        values.add("b");
        values.add("c");
        headers.put("X-Test-Header", values);
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals("a, b, c", res.header("X-Test-Header"));
    }