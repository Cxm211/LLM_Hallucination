// org/jsoup/integration/ParseTest.java
@Test
public void testBinaryExceptionCauseChain() throws IOException {
    File in = getFile("/htmltests/thumb.jpg");

    boolean threw = false;
    Throwable caughtException = null;
    try {
        Document doc = Jsoup.parse(in, "UTF-8");
    } catch (IOException e) {
        threw = true;
        caughtException = e;
    }
    assertTrue(threw);
    assertNotNull(caughtException.getMessage());
    assertEquals("Input is binary and unsupported", caughtException.getMessage());
}