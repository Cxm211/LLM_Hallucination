// org/apache/commons/lang3/text/translate/LookupTranslatorTest.java::testLang882
@Test
public void testGreedyWithStringBuilder() throws IOException {
    final LookupTranslator lt = new LookupTranslator(new CharSequence[][] {
        { new StringBuilder("ab"), new StringBuilder("X") },
        { new StringBuilder("abc"), new StringBuilder("Y") }
    });
    final StringWriter out = new StringWriter();
    final int result = lt.translate(new StringBuilder("abcd"), 0, out);
    assertEquals("Incorrect codepoint consumption (greedy)", 3, result);
    assertEquals("Incorrect value (greedy)", "Y", out.toString());
}