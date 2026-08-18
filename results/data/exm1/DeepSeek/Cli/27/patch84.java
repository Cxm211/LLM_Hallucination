// org.apache.commons.cli.OptionsTest::testGetOptionsGroups
    public void testGetOptionsGroups()
    {
        Options options = new Options();

        OptionGroup group1 = new OptionGroup();
        group1.addOption(OptionBuilder.create('a'));
        group1.addOption(OptionBuilder.create('b'));

        OptionGroup group2 = new OptionGroup();
        group2.addOption(OptionBuilder.create('x'));
        group2.addOption(OptionBuilder.create('y'));

        options.addOptionGroup(group1);
        options.addOptionGroup(group2);

        assertNotNull(options.getOptionGroups());
        assertEquals(2, options.getOptionGroups().size());
    }