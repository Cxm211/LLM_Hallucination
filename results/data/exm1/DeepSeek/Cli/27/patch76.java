// org.apache.commons.cli.OptionsTest::testSimple
    public void testSimple()
    {
        Options opts = new Options();

        opts.addOption("a", false, "toggle -a");
        opts.addOption("b", true, "toggle -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }