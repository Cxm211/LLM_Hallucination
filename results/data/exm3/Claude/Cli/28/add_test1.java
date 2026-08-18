// org/apache/commons/cli/ValueTest.java
public void testPropertyOptionEmptyAndNull() throws Exception
{
    Properties properties = new Properties();
    properties.setProperty( "e", "" );

    Parser parser = new PosixParser();

    CommandLine cmd = parser.parse(opts, null, properties);
    assertTrue( cmd.hasOption("e") );

    properties = new Properties();
    properties.setProperty( "a", "" );
    properties.setProperty( "c", "" );

    cmd = parser.parse(opts, null, properties);
    assertTrue( !cmd.hasOption("a") );
    assertTrue( !cmd.hasOption("c") );
}