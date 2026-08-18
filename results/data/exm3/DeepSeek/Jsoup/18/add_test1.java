// org/jsoup/parser/CharacterReaderTest.java
@Test public void consumeToEndSingleChar() {
        String in = "a";
        CharacterReader r = new CharacterReader(in);
        String first = r.consumeToEnd();
        assertEquals("a", first);
        assertTrue(r.isEmpty());
        String second = r.consumeToEnd();
        assertEquals("", second);
        assertTrue(r.isEmpty());
    }
