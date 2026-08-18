// org/jsoup/parser/HtmlParserTest.java::testNoAttributeLeakToImpliedTbody
@Test
public void testNoAttributeLeakToImpliedTbody() {
    Document doc = Jsoup.parse("<table id=tbl><tr><td>x</td></tr></table>");
    Element tbody = doc.selectFirst("tbody");
    assertNotNull(tbody);
    assertFalse("Implied tbody should not inherit attributes from table", tbody.hasAttr("id"));
}