// org/jsoup/nodes/ElementTest.java
@Test public void orphanedElementWithChildren() {
    Element parent = new Element("div");
    Element child = new Element("span");
    child.text("Hello");
    parent.appendChild(child);
    parent.remove();
    assertEquals("<div>\n <span>Hello</span>\n</div>", parent.toString());
}