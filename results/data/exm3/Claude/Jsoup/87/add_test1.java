// org/jsoup/parser/HtmlParserTest.java
@Test public void preservedCaseMultipleFormattingElementsAdoptionAgency() {
    String html = "<I><B>One<div>Two</div></B></I>";
    Document doc = Parser.htmlParser()
        .settings(ParseSettings.preserveCase)
        .parseInput(html, "");
    assertEquals("<I><B>One</B></I> <I><B> <div> Two </div> </B></I>", StringUtil.normaliseWhitespace(doc.body().html()));
}