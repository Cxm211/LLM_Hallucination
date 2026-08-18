    private static void reset()
    {
        description = null;
        argName = null;
        longopt = null;
        type = null;
        required = false;
        numberOfArgs = Option.UNINITIALIZED;
        optionalArg = false;
        valuesep = (char) 0;
    }

// trigger testcase
public void testGetParsedOptionValue() throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withType(Number.class).create("i"));
        options.addOption(OptionBuilder.hasArg().create("f"));
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        
        assertEquals(123, ((Number) cmd.getParsedOptionValue("i")).intValue());
        assertEquals("foo", cmd.getParsedOptionValue("f"));
    }

public void testTwoCompleteOptions( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasArgs( )
                                     .withType( Float.class )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasArgs() );

        simple = OptionBuilder.withLongOpt( "dimple option")
                              .hasArg( )
                              .withDescription( "this is a dimple option" )
                              .create( 'd' );

        assertEquals( "d", simple.getOpt() );
        assertEquals( "dimple option", simple.getLongOpt() );
        assertEquals( "this is a dimple option", simple.getDescription() );
        assertEquals( String.class, simple.getType() );
        assertTrue( simple.hasArg() );
        assertTrue( !simple.isRequired() );
        assertTrue( !simple.hasArgs() );
    }
