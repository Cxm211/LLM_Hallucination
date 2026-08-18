// org/jsoup/parser/CharacterReaderTest.java
@Test public void consumeToEndAfterPartialConsume() {
    String in = "hello world";
    CharacterReader r = new CharacterReader(in);
    r.consume();
    String toEnd = r.consumeToEnd();
    assertEquals("ello world", toEnd);
    assertTrue(r.isEmpty());
}