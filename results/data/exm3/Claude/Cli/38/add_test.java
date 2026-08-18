// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldParseSingleCharacterShortOption() throws Exception {
    String[] singleShortOption = new String[] { "-a" };

    final CommandLine commandLine = parser.parse(options, singleShortOption);

    assertTrue(commandLine.hasOption("a"));
    assertFalse(commandLine.hasOption("b"));
    assertFalse(commandLine.hasOption("t1"));
}