// org/jsoup/helper/DataUtilTest.java
@Test
    public void testBOMOverridesProvidedCharset() throws IOException {
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String html = "<html><body>Hello World</body></html>";
        byte[] contentBytes = html.getBytes("UTF-8");
        byte[] allBytes = new byte[bom.length + contentBytes.length];
        System.arraycopy(bom, 0, allBytes, 0, bom.length);
        System.arraycopy(contentBytes, 0, allBytes, bom.length, contentBytes.length);
        InputStream is = new ByteArrayInputStream(allBytes);
        Document doc = Jsoup.parse(is, "ISO-8859-1", "http://example.com");
        assertEquals("Hello World", doc.text());
    }
