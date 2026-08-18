// org.apache.commons.cli.OptionGroupTest::testTwoLongOptionsFromGroup
    public void testTwoLongOptionsFromGroup() throws Exception
    {
        String[] args = new String[] { "--file", "--directory" };

        try
        {
            parser.parse(_options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (AlreadySelectedException e)
        {
            assertNotNull("null option group", e.getOptionGroup());
            assertEquals("selected option", "f", e.getOptionGroup().getSelected());
            assertEquals("option", "d", e.getOption().getOpt());
        }
    }