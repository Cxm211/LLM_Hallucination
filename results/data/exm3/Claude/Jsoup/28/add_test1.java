// org/jsoup/nodes/EntitiesTest.java
@Test public void invalidHighCharacterValue() {
    String text = "&#1114112;";
    String result = Entities.unescape(text);
    assertEquals("&#1114112;", result);
}