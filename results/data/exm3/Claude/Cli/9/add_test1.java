// org/apache/commons/cli/ParseRequiredTest.java
public void testMissingThreeRequiredOptions()
{
    String[] args = new String[] {};

    _options.addOption( OptionBuilder.withLongOpt( "dfile" )
                                 .hasArg()
                                 .isRequired()
                                 .withDescription( "set the value of [d]" )
                                 .create( 'd' ) );

    try
    {
        CommandLine cl = parser.parse(_options,args);
        fail( "exception should have been thrown" );
    }
    catch (MissingOptionException e)
    {
        assertEquals( "Incorrect exception message", "Missing required options: a, b, d", e.getMessage() );
    }
    catch (ParseException e)
    {
        fail( "expected to catch MissingOptionException" );
    }
}