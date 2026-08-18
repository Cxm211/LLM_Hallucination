// org/apache/commons/cli/bug/BugCLI51Test.java
public void testStopAtNonOptionSingleChar() throws Exception
{
    Options options = buildCommandLineOptions();
    CommandLineParser parser = new PosixParser();
    String[] args = new String[] {"-t", "-x" };
    CommandLine commandLine;
    commandLine = parser.parse( options, args );
    assertEquals("-x", commandLine.getOptionValue( 't'));
}