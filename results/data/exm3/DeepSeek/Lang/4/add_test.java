// org/apache/commons/lang3/text/translate/LookupTranslatorTest.java
@Test
    public void testLookupTranslatorGreedy() throws IOException {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] {
            { new StringBuilder("a"), new StringBuilder("1") },
            { new StringBuilder("ab"), new StringBuilder("2") },
            { new StringBuilder("abc"), new StringBuilder("3") }
        });
        final StringWriter out = new StringWriter();
        final int result = lt.translate(new StringBuilder("abcd"), 0, out);
        assertEquals("Greedy match should consume 3 characters", 3, result);
        assertEquals("Greedy match output should be '3'", "3", out.toString());
    }
