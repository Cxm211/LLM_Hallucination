// org/jsoup/nodes/EntitiesTest.java
@Test public void unescapeSupplementaryCharacter() {
    String string = "Hello &#x1F600;";
    assertEquals("Hello \uD83D\uDE00", Entities.unescape(string));
}
