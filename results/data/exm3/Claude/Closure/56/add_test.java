// com/google/javascript/jscomp/JSCompilerSourceExcerptProviderTest.java
public void testSingleLineNoNewLine() throws Exception {
  // Test single line file without trailing newline
  SimpleSourceExcerptProvider provider = new SimpleSourceExcerptProvider();
  provider.addSourceCode("single", "only one line");
  assertEquals("single:only one line", provider.getSourceLine("single", 1));
  assertEquals(null, provider.getSourceLine("single", 2));
}