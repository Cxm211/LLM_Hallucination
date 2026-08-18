// org/jsoup/nodes/EntitiesTest.java
@Test
public void escapeXhtmlInAttributeNormaliseWhiteNoStripLeading() {
    Document.OutputSettings out = new Document.OutputSettings();
    out.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
    out.charset(java.nio.charset.Charset.forName("UTF-8"));
    StringBuilder accum = new StringBuilder();
    String string = "   <x";
    boolean inAttribute = true;
    boolean normaliseWhite = true;
    boolean stripLeadingWhite = false;
    org.jsoup.nodes.Entities.escape(accum, string, out, inAttribute, normaliseWhite, stripLeadingWhite);
    assertEquals(" &lt;x", accum.toString());
}
