// org/jsoup/integration/ConnectTest.java
@Test(expected = IOException.class)
    public void handlesEmptyInputStream() throws IOException {
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        Jsoup.parse(empty, null, "http://example.com");
    }
