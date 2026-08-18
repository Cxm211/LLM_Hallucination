// org/jsoup/helper/DataUtilTest.java::supportsBOMinFiles
@Test
    public void bomOverridesMetaUtf8() throws IOException {
        // HTML encoded in UTF-8 with a BOM but misleading meta declaring ISO-8859-1
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>BOM vs Meta</title></head><body>가각갂</body></html>";
        byte[] utf8 = html.getBytes("UTF-8");
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] data = new byte[bom.length + utf8.length];
        System.arraycopy(bom, 0, data, 0, bom.length);
        System.arraycopy(utf8, 0, data, bom.length, utf8.length);

        InputStream in = new ByteArrayInputStream(data);
        Document doc = Jsoup.parse(in, null, "http://example.com");
        assertTrue(doc.title().contains("BOM vs Meta"));
        assertTrue(doc.text().contains("가각갂"));
    }