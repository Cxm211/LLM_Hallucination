// com/google/javascript/jscomp/JSCompilerSourceExcerptProviderTest.java
public void testEmptyLastLine() throws Exception {
  // Test file ending with newline (empty last line)
  SimpleSourceExcerptProvider provider = new SimpleSourceExcerptProvider();
  provider.addSourceCode("emptyEnd", "line1\nline2\n");
  assertEquals("emptyEnd:line1", provider.getSourceLine("emptyEnd", 1));
  assertEquals("emptyEnd:line2", provider.getSourceLine("emptyEnd", 2));
  assertEquals(null, provider.getSourceLine("emptyEnd", 3));
}