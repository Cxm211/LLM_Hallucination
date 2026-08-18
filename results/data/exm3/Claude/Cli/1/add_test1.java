// org/apache/commons/cli/bug/BugCLI13Test.java
public void testCLI13LongOptOnly()
        throws ParseException
{
    Option verbose = OptionBuilder
        .withArgName( "verbose" )
        .withDescription( "verbose output" )
        .withLongOpt( "verbose" )
        .hasArg()
        .create();
    Options options = new Options();
    options.addOption( verbose );
    CommandLine commandLine = new PosixParser().parse( options, new String[]{"--verbose", "yes"} );

    assertTrue(commandLine.hasOption( "verbose"));
    assertTrue(commandLine.hasOption( "--verbose"));
    assertEquals("yes", commandLine.getOptionValue( "verbose" ));
    assertEquals("yes", commandLine.getOptionValue( "--verbose" ));
}