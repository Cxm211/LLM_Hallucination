// org/apache/commons/cli/bug/BugCLI265Test.java::shouldRecognizeSingleDashLongOption
@Test
public void shouldRecognizeSingleDashLongOption() throws Exception {
    String[] args = new String[]{"-last"};
    final CommandLine commandLine = parser.parse(options, args);
    assertTrue("Single-dash long option not recognized", commandLine.hasOption("last"));
}