// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldParseTripleConcatenatedShortOptions() throws Exception {
    String[] tripleConcatenated = new String[] { "-abc" };

    final CommandLine commandLine = parser.parse(options, tripleConcatenated);

    assertTrue(commandLine.hasOption("a"));
    assertTrue(commandLine.hasOption("b"));
    assertTrue(commandLine.hasOption("c"));
    assertFalse(commandLine.hasOption("t1"));
}