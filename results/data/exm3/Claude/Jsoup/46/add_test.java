// org/jsoup/nodes/DocumentTest.java
@Test
    public void testNbspWithUtf8Xhtml() throws Exception {
        String input = "<html><body>before&nbsp;after</body></html>";
        Document doc = Jsoup.parse(input);
        doc.outputSettings().charset("UTF-8").escapeMode(Entities.EscapeMode.xhtml);
        String output = doc.html();
        assertTrue("UTF-8 with xhtml should preserve nbsp as character or encode it",
                output.contains("\u00A0") || output.contains("&#xa0;"));
        assertFalse("Should not contain &nbsp; in xhtml mode", output.contains("&nbsp;"));
    }