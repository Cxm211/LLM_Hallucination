// org.apache.commons.cli.OptionsTest::testDuplicateSimple
    public void testDuplicateSimple()
    {
        Options opts = new Options();
        opts.addOption("a", false, "toggle -a");
        opts.addOption("a", true, "toggle -a*");

        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }