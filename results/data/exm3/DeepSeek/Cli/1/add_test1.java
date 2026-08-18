// org/apache/commons/cli/bug/BugCLI13Test.java
public void testShortOptionWithHyphen()
    throws ParseException
{
    Option debug = OptionBuilder
        .withArgName("debug")
        .withDescription("turn on debugging")
        .withLongOpt("debug")
        .hasArg()
        .create('d');
    Options options = new Options();
    options.addOption(debug);
    CommandLine commandLine = new PosixParser().parse(options, new String[]{"-d", "true"});
    assertTrue(commandLine.hasOption("-d"));
    assertTrue(commandLine.hasOption("d"));
    assertTrue(commandLine.hasOption("debug"));
    assertEquals("true", commandLine.getOptionValue("-d"));
    assertEquals("true", commandLine.getOptionValue("d"));
}
