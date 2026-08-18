// org.apache.commons.cli.ParserTestCase::testOptionGroup
    public void testOptionGroup() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        
        Options options = new Options();
        options.addOptionGroup(group);
        
        parser.parse(options, new String[] { "-b" });
        
        assertEquals("selected option", "b", group.getSelected());
    }