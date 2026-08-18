// com/google/javascript/jscomp/JSCompilerSourceExcerptProviderTest.java
public void testTrailingNewline() {
  SourceExcerptProvider provider = new SourceFile("dummy.js", "line1\n");
  assertEquals("line1", provider.getSourceLine("dummy.js", 1));
  assertEquals("", provider.getSourceLine("dummy.js", 2));
  assertEquals(null, provider.getSourceLine("dummy.js", 3));
}
