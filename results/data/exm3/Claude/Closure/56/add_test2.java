// com/google/javascript/jscomp/JSCompilerSourceExcerptProviderTest.java
public void testMultipleLinesLastNoNewLine() throws Exception {
  // Test multiple lines where last line has no trailing newline
  SimpleSourceExcerptProvider provider = new SimpleSourceExcerptProvider();
  provider.addSourceCode("multi", "first\nsecond\nthird");
  assertEquals("multi:first", provider.getSourceLine("multi", 1));
  assertEquals("multi:second", provider.getSourceLine("multi", 2));
  assertEquals("multi:third", provider.getSourceLine("multi", 3));
  assertEquals(null, provider.getSourceLine("multi", 4));
}