// org/jsoup/parser/HtmlParserTest.java
@Test public void preWithCommentBeforeNewline() {
        Document doc = Jsoup.parse("<pre><!-- comment -->\n\nOne</pre>");
        Element pre = doc.selectFirst("pre");
        assertTrue(pre.wholeText().startsWith("\nOne"));
        assertFalse(pre.wholeText().startsWith("\n\nOne"));
    }
