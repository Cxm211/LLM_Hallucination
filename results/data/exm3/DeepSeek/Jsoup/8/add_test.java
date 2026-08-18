// org/jsoup/nodes/ElementTest.java
@Test public void newlyCreatedElementToString() {
    Element div = new Element("div", "");
    assertEquals("<div></div>", div.toString());
}
