// org/jsoup/integration/ParseTest.java
@Test
    public void testContainsIgnoreCaseEmptyString() {
        CharacterReader reader = new CharacterReader(new StringReader("test"), 1024);
        assertFalse(reader.containsIgnoreCase(""));
    }
