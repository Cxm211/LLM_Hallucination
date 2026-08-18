// org/jsoup/parser/HtmlParserTest.java::handlesKnownEmptyIframe
@Test public void handlesKnownEmptyIframe() {
        String h = "<html><body><iframe />Hello</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><iframe></iframe>Hello</body></html>", TextUtil.stripNewlines(doc.html()));
    }