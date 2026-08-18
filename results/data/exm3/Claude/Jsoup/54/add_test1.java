// org/jsoup/helper/W3CDomTest.java
@Test
public void handlesAttributeNamesBecomeEmpty() {
    String html = "<html><body @='value1' #='value2' $='value3'></body></html>";
    org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
    Element body = jsoupDoc.select("body").first();
    
    Document w3Doc = new W3CDom().fromJsoup(jsoupDoc);
    org.w3c.dom.Element w3Body = (org.w3c.dom.Element) w3Doc.getElementsByTagName("body").item(0);
    
    // Attributes that become empty after filtering should not be added
    assertEquals(0, w3Body.getAttributes().getLength());
}