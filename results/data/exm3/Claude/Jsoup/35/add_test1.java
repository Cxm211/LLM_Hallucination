// org/jsoup/parser/HtmlParserTest.java
@Test public void handlesFormattingElementsWithAttributes() {
    String h = "<a href='first.html' class='link'>Link<p>Para</a>Text";
    Document doc = Jsoup.parse(h);
    String want = "<a href=\"first.html\" class=\"link\">Link</a>\n<p><a href=\"first.html\" class=\"link\">Para</a></p><a href=\"first.html\" class=\"link\">Text</a>";
    assertEquals(want, doc.body().html());
}