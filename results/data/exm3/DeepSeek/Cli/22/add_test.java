// org/apache/commons/cli/ParserTestCase.java
public void testUnknownShortOptionWithStop() throws Exception
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("test").hasArg().create('b'));
        
        String[] args = new String[]{"-z", "foo"};
        CommandLine cl = parser.parse(options, args, true);
        
        assertFalse("Confirm -z is not set", cl.hasOption('z'));
        assertEquals("Number of extra args", 2, cl.getArgList().size());
        assertEquals("First arg", "-z", cl.getArgList().get(0));
        assertEquals("Second arg", "foo", cl.getArgList().get(1));
    }
