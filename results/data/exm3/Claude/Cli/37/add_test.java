// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
public void shouldParseLongOptionAsNonShortOption() throws Exception {
    String[] args = new String[]{"--long"};
    
    Options opts = new Options();
    opts.addOption("l", "long", false, "long option");
    
    final CommandLine commandLine = parser.parse(opts, args);
    
    assertTrue("Long option should be recognized", commandLine.hasOption("long"));
    assertTrue("Long option should be recognized by long name", commandLine.hasOption("l"));
}