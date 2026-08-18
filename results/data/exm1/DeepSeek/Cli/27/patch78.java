// org.apache.commons.cli.OptionsTest::testLong
    public void testLong()
    {
        Options opts = new Options();

        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("b", "--b", true, "set -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }