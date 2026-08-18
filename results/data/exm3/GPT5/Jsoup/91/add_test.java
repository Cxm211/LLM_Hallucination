// org/jsoup/integration/ParseTest.java::testBinaryThrowsExceptionFromStream
@Test
public void testBinaryThrowsExceptionFromStream() throws Exception {
    File in = getFile("/htmltests/thumb.jpg");

    boolean threw = false;
    try (InputStream is = new FileInputStream(in)) {
        Jsoup.parse(is, "UTF-8", "http://example.com/");
    } catch (IOException e) {
        threw = true;
        assertEquals("Input is binary and unsupported", e.getMessage());
    }
    assertTrue(threw);
}