// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldHandleSingleDashAsNonOption() throws Exception {
    String[] args = new String[]{"-"};
    
    Options opts = new Options();
    opts.addOption("t", false, "test option");
    
    final CommandLine commandLine = parser.parse(opts, args);
    
    assertFalse("Single dash should not be treated as short option", commandLine.hasOption("t"));
}