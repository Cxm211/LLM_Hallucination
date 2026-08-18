// org/apache/commons/lang3/text/translate/LookupTranslatorTest.java
@Test
public void testLang882MultipleEntries() throws IOException {
    final LookupTranslator lt = new LookupTranslator(
        new CharSequence[][] {
            { new StringBuffer("a"), new StringBuffer("alpha") },
            { new StringBuffer("ab"), new StringBuffer("beta") },
            { new StringBuffer("abc"), new StringBuffer("gamma") }
        }
    );
    final StringWriter out = new StringWriter();
    final int result = lt.translate(new StringBuffer("abc"), 0, out);
    assertEquals("Incorrect codepoint consumption", 3, result);
    assertEquals("Incorrect value", "gamma", out.toString());
}