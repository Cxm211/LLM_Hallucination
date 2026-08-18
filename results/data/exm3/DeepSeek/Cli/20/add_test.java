// org/apache/commons/cli/PosixParserTest.java
public void testStopWithVariousTokens() throws Exception {
    Options options = new Options();
    options.addOption("a", false, "");
    options.addOption("b", false, "");
    options.addOption("c", false, "");
    options.addOption("ab", false, "");
    options.addOption("foo", false, "");

    String[] args = new String[]{"--foo", "-", "-a", "-ab", "-abc"};
    CommandLine cl = parser.parse(options, args, true);
    assertFalse("Option 'a' should not be set", cl.hasOption("a"));
    assertFalse("Option 'b' should not be set", cl.hasOption("b"));
    assertFalse("Option 'c' should not be set", cl.hasOption("c"));
    assertFalse("Option 'ab' should not be set", cl.hasOption("ab"));
    assertFalse("Option 'foo' should not be set", cl.hasOption("foo"));
    assertEquals("Should have 5 extra arguments", 5, cl.getArgList().size());
}
