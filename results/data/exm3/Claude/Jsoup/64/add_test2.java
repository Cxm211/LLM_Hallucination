// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesKnownEmptyTextarea() {
    String h = "<html><body><textarea /><div>Content</div></body></html>";
    Document doc = Jsoup.parse(h);
    assertEquals("<html><head></head><body><textarea></textarea><div>Content</div></body></html>", TextUtil.stripNewlines(doc.html()));
}