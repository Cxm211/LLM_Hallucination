// com/google/javascript/jscomp/JSCompilerSourceExcerptProviderTest.java
public void testSingleLineNoNewline() {
  SourceExcerptProvider provider = new SourceFile("dummy.js", "single line");
  assertEquals("single line", provider.getSourceLine("dummy.js", 1));
  assertEquals(null, provider.getSourceLine("dummy.js", 2));
}
