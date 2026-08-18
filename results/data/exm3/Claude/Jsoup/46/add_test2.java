// org/jsoup/nodes/DocumentTest.java
@Test
    public void testNbspWithIso88591Xhtml() throws Exception {
        String input = "<html><body>test&nbsp;content</body></html>";
        Document doc = Jsoup.parse(input);
        doc.outputSettings().charset("ISO-8859-1").escapeMode(Entities.EscapeMode.xhtml);
        String output = doc.html();
        byte[] bytes = output.getBytes(Charset.forName("ISO-8859-1"));
        String decoded = new String(bytes, Charset.forName("ISO-8859-1"));
        assertFalse("Should not contain ?", decoded.contains("?"));
        assertTrue("Should contain nbsp as character in ISO-8859-1",
                decoded.contains("\u00A0"));
    }