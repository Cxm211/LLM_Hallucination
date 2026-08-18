// org/jsoup/helper/W3CDomTest.java::handlesLeadingHyphenAttribute
@Test
public void handlesLeadingHyphenAttribute() {
    String html = "<html><head></head><body -name=\"1\"></body></html>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
    org.jsoup.nodes.Element body = jsoupDoc.select("body").first();
    assertTrue(body.hasAttr("-name"));

    Document w3Doc = new W3CDom().fromJsoup(jsoupDoc);
}