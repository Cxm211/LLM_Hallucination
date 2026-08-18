// org/jsoup/helper/DataUtilTest.java
@Test
public void supportsNoBOM() throws IOException {
    String html = "<html><head><title>No BOM</title></head><body>Test content</body></html>";
    byte[] bytes = html.getBytes("UTF-8");
    ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    Document doc = DataUtil.load(stream, null, "http://example.com");
    assertEquals("No BOM", doc.title());
    assertTrue(doc.text().contains("Test content"));
}