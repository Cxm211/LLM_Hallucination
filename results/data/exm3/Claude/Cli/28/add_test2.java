// org/apache/commons/cli/ValueTest.java
public void testPropertyOptionMultipleProperties() throws Exception
{
    Properties properties = new Properties();
    properties.setProperty( "a", "1" );
    properties.setProperty( "c", "yes" );
    properties.setProperty( "e", "arbitrary" );

    Parser parser = new PosixParser();

    CommandLine cmd = parser.parse(opts, null, properties);
    assertTrue( cmd.hasOption("a") );
    assertTrue( cmd.hasOption("c") );
    assertTrue( cmd.hasOption("e") );

    properties = new Properties();
    properties.setProperty( "a", "maybe" );
    properties.setProperty( "c", "2" );

    cmd = parser.parse(opts, null, properties);
    assertTrue( !cmd.hasOption("a") );
    assertTrue( !cmd.hasOption("c") );
    assertTrue( !cmd.hasOption("e") );
}