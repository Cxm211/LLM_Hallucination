// org/jsoup/nodes/EntitiesTest.java
@Test
public void escapeXhtmlInAttributeWithNormaliseWhiteAndVariousChars() {
    Document.OutputSettings out = new Document.OutputSettings();
    out.escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);
    out.charset(java.nio.charset.Charset.forName("US-ASCII"));
    StringBuilder accum = new StringBuilder();
    String string = "\t\n <a\u00A0\"&\uD83D\uDE00\u20AC>";
    boolean inAttribute = true;
    boolean normaliseWhite = true;
    boolean stripLeadingWhite = true;
    org.jsoup.nodes.Entities.escape(accum, string, out, inAttribute, normaliseWhite, stripLeadingWhite);
    assertEquals("&lt;a&#xa0;&quot;&amp;&#x1f600;&#x20ac;>", accum.toString());
}
