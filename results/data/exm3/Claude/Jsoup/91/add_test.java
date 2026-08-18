// org/jsoup/integration/ConnectTest.java
@Test
public void testBinaryExceptionMessagePreserved() {
    Connection con = Jsoup.connect(FileServlet.Url);
    con.data(FileServlet.LocationParam, "/htmltests/thumb.jpg");
    con.data(FileServlet.ContentTypeParam, "application/octet-stream");
    con.ignoreContentType(true);

    boolean threw = false;
    String exceptionMessage = null;
    try {
        con.execute();
        Document doc = con.response().parse();
    } catch (IOException e) {
        threw = true;
        exceptionMessage = e.getMessage();
    }
    assertTrue(threw);
    assertNotNull(exceptionMessage);
    assertEquals("Input is binary and unsupported", exceptionMessage);
}