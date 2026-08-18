// org/jsoup/helper/DataUtilTest.java
@Test
public void supportsBOMWithMetaCharset() throws IOException {
    byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
    String html = "<html><head><meta charset=\"ISO-8859-1\"><title>BOM UTF-8 with meta</title></head><body>Content</body></html>";
    byte[] htmlBytes = html.getBytes("UTF-8");
    byte[] combined = new byte[bom.length + htmlBytes.length];
    System.arraycopy(bom, 0, combined, 0, bom.length);
    System.arraycopy(htmlBytes, 0, combined, bom.length, htmlBytes.length);
    ByteArrayInputStream stream = new ByteArrayInputStream(combined);
    Document doc = DataUtil.load(stream, null, "http://example.com");
    assertEquals("BOM UTF-8 with meta", doc.title());
    assertEquals("UTF-8", doc.outputSettings().charset().name());
}