// org/jsoup/parser/ParserTest.java::parsesUnterminatedTextarea
@Test public void parsesUppercaseTextareaEndTag() {
        Document doc = Jsoup.parse("<body><textarea>one</TEXTAREA><p>two");
        Element t = doc.select("textarea").first();
        assertEquals("one", t.text());
        assertEquals("two", doc.select("p").first().text());
    }