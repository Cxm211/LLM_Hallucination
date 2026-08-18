// org/apache/commons/cli/bug/BugCLI51Test.java
public void testArgumentWithDashAndOptionChar() throws Exception
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("test").hasArg().create('t'));
        options.addOption(OptionBuilder.withLongOpt("example").create('x'));
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "-x"};
        CommandLine commandLine = parser.parse(options, args);
        assertEquals("-x", commandLine.getOptionValue('t'));
    }
