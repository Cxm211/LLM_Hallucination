// org/jsoup/parser/HtmlParserTest.java
@Test
    public void testInvalidTableContentsEndTag() throws IOException {
        String html = "<table><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("table").size());
        assertEquals(1, doc.select("tr").size());
        assertEquals(1, doc.select("td").size());
        html = "<table><!-- Comment --><td>Content</td></table>";
        doc = Jsoup.parse(html);
        String rendered = doc.toString();
        int commentPos = rendered.indexOf("Comment");
        int contentPos = rendered.indexOf("Content");
        assertTrue(commentPos > -1);
        assertTrue(contentPos > -1);
        assertTrue(commentPos < contentPos);
    }
