// org/jsoup/parser/ParserTest.java
@Test public void handlesEmptyDataTag() {
        String h = "<html><body><script></script>text</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><script></script>text</body></html>", TextUtil.stripNewlines(doc.html()));
    }