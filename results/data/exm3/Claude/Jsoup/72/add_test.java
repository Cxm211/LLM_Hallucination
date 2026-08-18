// org/jsoup/parser/CharacterReaderTest.java
@Test
public void cacheStringWithDifferentHashCollisions() {
    CharacterReader r1 = new CharacterReader("a");
    String s1 = r1.consumeTo('z');
    assertEquals("a", s1);
    
    CharacterReader r2 = new CharacterReader("b");
    String s2 = r2.consumeTo('z');
    assertEquals("b", s2);
    
    CharacterReader r3 = new CharacterReader("c");
    String s3 = r3.consumeTo('z');
    assertEquals("c", s3);
}