// org.apache.commons.cli.OptionGroupTest::testTwoOptionsFromGroup
    public void testTwoOptionsFromGroup() throws Exception
    {
        String[] args = new String[] { "-f", "-d" };

        try
        {
            parser.parse( _options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (AlreadySelectedException e)
        {
            assertNotNull("null option group", e.getOptionGroup());
            assertEquals("selected option", "f", e.getOptionGroup().getSelected());
            assertEquals("option", "d", e.getOption().getOpt());
        }
    }