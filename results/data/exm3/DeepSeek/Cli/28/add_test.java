// org/apache/commons/cli/ValueTest.java
public void testPropertyOptionFlagsWithZeroFlag() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "a", "0" );
        properties.setProperty( "e", "somevalue" );

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( cmd.hasOption("e") );
    }
