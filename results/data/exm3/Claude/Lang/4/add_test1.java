// org/apache/commons/lang3/text/translate/LookupTranslatorTest.java
@Test
public void testLang882StringBuilder() throws IOException {
    final LookupTranslator lt = new LookupTranslator(
        new CharSequence[][] {
            { new StringBuilder("test"), new StringBuilder("result") }
        }
    );
    final StringWriter out = new StringWriter();
    final int result = lt.translate(new StringBuilder("test"), 0, out);
    assertEquals("Incorrect codepoint consumption", 4, result);
    assertEquals("Incorrect value", "result", out.toString());
}