// org/apache/commons/cli/ValueTest.java
public void testPropertyOptionFlagsWithEmptyFlag() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "a", "" );
        properties.setProperty( "e", "somevalue" );

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( cmd.hasOption("e") );
    }
