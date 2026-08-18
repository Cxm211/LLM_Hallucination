// org/apache/commons/lang3/text/translate/LookupTranslatorTest.java
@Test
    public void testLookupTranslatorMaxReduction() throws IOException {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] {
            { new StringBuffer("abcd"), new StringBuffer("4") },
            { new StringBuffer("abc"), new StringBuffer("3") }
        });
        final StringWriter out = new StringWriter();
        final int result = lt.translate(new StringBuffer("abc"), 0, out);
        assertEquals("Max reduction match should consume 3 characters", 3, result);
        assertEquals("Max reduction match output should be '3'", "3", out.toString());
    }
