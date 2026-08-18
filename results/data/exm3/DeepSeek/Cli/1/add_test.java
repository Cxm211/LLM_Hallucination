// org/apache/commons/cli/bug/BugCLI13Test.java
public void testLongOptionWithHyphen()
    throws ParseException
{
    final String verboseOpt = "verbose";
    Option verbose = OptionBuilder
        .withDescription("verbose mode")
        .withLongOpt(verboseOpt)
        .create();
    Options options = new Options();
    options.addOption(verbose);
    CommandLine commandLine = new PosixParser().parse(options, new String[]{"--verbose"});
    assertTrue(commandLine.hasOption("--verbose"));
    assertTrue(commandLine.hasOption("verbose"));
}
