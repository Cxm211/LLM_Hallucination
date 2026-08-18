// org/jsoup/parser/ParserTest.java
@Test public void handlesTextAfterTitle() {
    String h = "<div><title>Inner Title</title> after title text</div>";
    Document doc = Jsoup.parse(h);
    assertEquals("<div><title>Inner Title</title> after title text</div>", TextUtil.stripNewlines(doc.html()));
}
