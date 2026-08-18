// org/jsoup/integration/ConnectTest.java
@Test
public void handlesPartialStreamWithReparse() throws IOException {
    // Test case where initial parse succeeds but full stream read is interrupted
    // This exercises the path where doc != null initially but fullyRead is false
    Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
        .timeout(200)
        .execute();

    boolean threw = false;
    try {
        Document document = res.parse();
        // If we got here, check if document is valid
        assertNotNull(document);
    } catch (IOException e) {
        threw = true;
    }
    // Either way should handle gracefully
    assertEquals(true, threw);
}