// org/jsoup/parser/CharacterReaderTest.java::nextIndexOfUnmatched
@Test public void nextIndexOfNoOverflow() {
        CharacterReader r = new CharacterReader("ab");
        assertEquals(-1, r.nextIndexOf("abc"));
    }