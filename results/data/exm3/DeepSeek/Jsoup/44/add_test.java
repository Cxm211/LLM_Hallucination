// org/jsoup/parser/HtmlParserTest.java
@Test
    public void testInvalidTableContentsWithAttributes() throws IOException {
        String html = "<table><!-- Comment --><td bgcolor=\"red\">Why am I here?</td></table>";
        Document doc = Jsoup.parse(html);
        String rendered = doc.toString();
        int commentPos = rendered.indexOf("Comment");
        int textPos = rendered.indexOf("Why am I here?");
        assertTrue("Comment not found", commentPos > -1);
        assertTrue("Text not found", textPos > -1);
        assertTrue("Comment should come before text", commentPos < textPos);
        Element td = doc.select("td").first();
        assertEquals("red", td.attr("bgcolor"));
    }
