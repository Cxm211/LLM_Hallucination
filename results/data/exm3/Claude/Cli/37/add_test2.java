// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldParseMultipleShortOptionsWithDifferentPrefixes() throws Exception {
    String[] args = new String[]{"-a", "-bc", "--long"};
    
    Options opts = new Options();
    opts.addOption("a", false, "option a");
    opts.addOption("b", false, "option b");
    opts.addOption("c", false, "option c");
    opts.addOption("l", "long", false, "long option");
    
    final CommandLine commandLine = parser.parse(opts, args);
    
    assertTrue("Option a should be recognized", commandLine.hasOption("a"));
    assertTrue("Option b should be recognized", commandLine.hasOption("b"));
    assertTrue("Option c should be recognized", commandLine.hasOption("c"));
    assertTrue("Long option should be recognized", commandLine.hasOption("long"));
}