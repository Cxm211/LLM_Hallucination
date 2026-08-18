// org.apache.commons.cli.OptionGroupTest::testToString
    public void testToString()
    {
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option(null, "foo", false, "Foo"));
        group1.addOption(new Option(null, "bar", false, "Bar"));

        if (!"[--bar Bar, --foo Foo]".equals(group1.toString())) {
            assertEquals("[--foo Foo, --bar Bar]", group1.toString());
        }

        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("f", "foo", false, "Foo"));
        group2.addOption(new Option("b", "bar", false, "Bar"));

        if (!"[-b Bar, -f Foo]".equals(group2.toString())) {
            assertEquals("[-f Foo, -b Bar]", group2.toString());
        }
    }