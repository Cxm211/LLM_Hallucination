// org/jsoup/parser/HtmlParserTest.java
@Test public void nestedPreSkipsInnerFirstNewline() {
        Document doc = Jsoup.parse("<pre><pre>\n\nInner</pre></pre>");
        Elements pres = doc.select("pre");
        assertEquals(2, pres.size());
        Element innerPre = pres.get(1);
        assertEquals("Inner", innerPre.text());
        assertEquals("\nInner", innerPre.wholeText());
    }
