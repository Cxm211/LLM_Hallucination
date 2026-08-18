// org/apache/commons/cli/ApplicationTest.java
public void testGroovyWithLongOptStopAtNonOption() throws Exception {
    Options options = new Options();

    options.addOption(
        OptionBuilder.withLongOpt("define").
            withDescription("define a system property").
            hasArg(true).
            withArgName("name=value").
            create('D'));
    options.addOption(
        OptionBuilder.hasArg(false)
        .withDescription("usage information")
        .withLongOpt("help")
        .create('h'));

    Parser parser = new PosixParser();
    CommandLine line = parser.parse(options, new String[] { "--unknown", "arg" }, true);

    assertFalse(line.hasOption("unknown"));
    assertEquals(2, line.getArgs().length);
    assertEquals("--unknown", line.getArgs()[0]);
    assertEquals("arg", line.getArgs()[1]);
}