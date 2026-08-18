// org/jsoup/parser/ParserTest.java
@Test public void handlesTextAfterTextarea() {
    String h = "<div><textarea>Enter text</textarea> after textarea text</div>";
    Document doc = Jsoup.parse(h);
    assertEquals("<div><textarea>Enter text</textarea> after textarea text</div>", TextUtil.stripNewlines(doc.html()));
}
