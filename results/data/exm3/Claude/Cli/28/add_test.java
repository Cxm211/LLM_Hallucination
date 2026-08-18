// org/apache/commons/cli/ValueTest.java
public void testPropertyOptionMixedCases() throws Exception
{
    Properties properties = new Properties();
    properties.setProperty( "a", "Yes" );
    properties.setProperty( "c", "TRUE" );
    properties.setProperty( "e", "0" );

    Parser parser = new PosixParser();

    CommandLine cmd = parser.parse(opts, null, properties);
    assertTrue( cmd.hasOption("a") );
    assertTrue( cmd.hasOption("c") );
    assertTrue( cmd.hasOption("e") );

    properties = new Properties();
    properties.setProperty( "a", "YeS" );
    properties.setProperty( "c", "1" );
    properties.setProperty( "e", "100" );

    cmd = parser.parse(opts, null, properties);
    assertTrue( cmd.hasOption("a") );
    assertTrue( cmd.hasOption("c") );
    assertTrue( cmd.hasOption("e") );
}