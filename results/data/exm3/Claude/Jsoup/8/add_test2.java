// org/jsoup/nodes/ElementTest.java
@Test public void orphanedSelfClosingElement() {
    Element br = new Element("br");
    assertEquals("<br />", br.toString());
}