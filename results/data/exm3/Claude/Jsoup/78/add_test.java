// org/jsoup/integration/ConnectTest.java
@Test
public void handlesEmptyStreamWithCharsetSpecified() throws IOException {
    // Test when charset is pre-specified and stream gets interrupted
    Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
        .timeout(200)
        .execute();

    boolean threw = false;
    try {
        // Force a specific charset path
        res.charset("UTF-8");
        Document document = res.parse();
        assertEquals("Something", document.title());
    } catch (IOException e) {
        threw = true;
    }
    assertEquals(true, threw);
}