// org/jsoup/nodes/EntitiesTest.java
@Test public void boundaryCharacterValue() {
    String text = "&#65535;";
    assertEquals("\uFFFF", Entities.unescape(text));
}