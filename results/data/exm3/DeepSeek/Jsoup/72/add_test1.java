// org/jsoup/parser/CharacterReaderTest.java
@Test
    public void cacheStringTwoChars() {
        CharacterReader r = new CharacterReader("aa");
        String s = r.consumeTo('b');
        assertEquals("aa", s);
    }
