// org/jsoup/parser/CharacterReaderTest.java
@Test
    public void cacheStringSingleChar() {
        CharacterReader r = new CharacterReader("a");
        String s = r.consumeTo('b');
        assertEquals("a", s);
    }
