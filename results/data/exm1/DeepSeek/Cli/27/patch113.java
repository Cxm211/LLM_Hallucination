// org.apache.commons.cli.ParserTestCase::testPropertiesOption2
    public void testPropertiesOption2() throws Exception
    {
        String[] args = new String[] { "-Dparam1", "-Dparam2=value2", "-D"};

        Options options = new Options();
        options.addOption(OptionBuilder.withValueSeparator().hasOptionalArgs(2).create('D'));

        CommandLine cl = parser.parse(options, args);
        
        Properties props = cl.getOptionProperties("D");
        assertNotNull("null properties", props);
        assertEquals("number of properties in " + props, 2, props.size());
        assertEquals("property 1", "true", props.getProperty("param1"));
        assertEquals("property 2", "value2", props.getProperty("param2"));
        
        List argsleft = cl.getArgList();
        assertEquals("Should be no arg left", 0, argsleft.size());
    }