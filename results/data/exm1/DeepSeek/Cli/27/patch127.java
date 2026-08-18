// org.apache.commons.cli.ParserTestCase::testMissingRequiredGroup
    public void testMissingRequiredGroup() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        group.setRequired(true);

        Options options = new Options();
        options.addOptionGroup(group);
        options.addOption(OptionBuilder.isRequired().create("c"));

        try
        {
            parser.parse(options, new String[] { "-c" });
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals(1, e.getMissingOptions().size());
            assertTrue(e.getMissingOptions().get(0) instanceof OptionGroup);
        }
        catch (ParseException e)
        {
            fail("Expected to catch MissingOptionException");
        }
    }