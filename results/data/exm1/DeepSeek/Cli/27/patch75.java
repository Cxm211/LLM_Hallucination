// org.apache.commons.cli.OptionGroupTest::testGetNames
    public void testGetNames()
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));

        assertNotNull("null names", group.getNames());
        assertEquals(2, group.getNames().size());
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getNames().contains("b"));
    }