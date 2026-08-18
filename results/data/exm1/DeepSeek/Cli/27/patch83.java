// org.apache.commons.cli.OptionsTest::testToString
    public void testToString()
    {
        Options options = new Options();
        options.addOption("f", "foo", true, "Foo");
        options.addOption("b", "bar", false, "Bar");

        String s = options.toString();
        assertNotNull("null string returned", s);
        assertTrue("foo option missing", s.toLowerCase().indexOf("foo") != -1);
        assertTrue("bar option missing", s.toLowerCase().indexOf("bar") != -1);
    }