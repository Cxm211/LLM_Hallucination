// org/jsoup/parser/CharacterReaderTest.java::cacheEmptyStringTwice
@Test
public void cacheEmptyStringTwice() {
    CharacterReader r = new CharacterReader("<!");
    assertTrue(r.matchConsume("<!"));
    assertTrue(r.isEmpty());

    String s1 = r.consumeTo('>');
    assertEquals("", s1);

    // call again to hit the cache retrieval path for empty string
    String s2 = r.consumeTo('>');
    assertEquals("", s2);
}