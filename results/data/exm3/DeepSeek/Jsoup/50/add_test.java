// org/jsoup/helper/DataUtilTest.java
@Test
    public void testBOMOverridesMeta() throws IOException {
        byte[] bom = {(byte)0xFF, (byte)0xFE};
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>가각갂</body></html>";
        byte[] contentBytes = html.getBytes("UTF-16LE");
        byte[] allBytes = new byte[bom.length + contentBytes.length];
        System.arraycopy(bom, 0, allBytes, 0, bom.length);
        System.arraycopy(contentBytes, 0, allBytes, bom.length, contentBytes.length);
        InputStream is = new ByteArrayInputStream(allBytes);
        Document doc = Jsoup.parse(is, null, "http://example.com");
        assertTrue(doc.text().contains("가각갂"));
    }
