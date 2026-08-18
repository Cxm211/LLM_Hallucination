// org/jsoup/nodes/DocumentTest.java
@Test
    public void testNbspWithAsciiNonXhtml() throws Exception {
        String input = "<html><body>before&nbsp;after</body></html>";
        Document doc = Jsoup.parse(input);
        doc.outputSettings().charset("ASCII").escapeMode(Entities.EscapeMode.base);
        String output = doc.html();
        assertTrue("ASCII with non-xhtml mode should use &nbsp;",
                output.contains("&nbsp;"));
        assertFalse("Should not contain ?", output.contains("?"));
    }