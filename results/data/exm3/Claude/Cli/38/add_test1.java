// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldParseShortOptionWithValue() throws Exception {
    String[] shortOptionWithValue = new String[] { "-a=value" };

    final CommandLine commandLine = parser.parse(options, shortOptionWithValue);

    assertTrue(commandLine.hasOption("a"));
    assertEquals("value", commandLine.getOptionValue("a"));
}