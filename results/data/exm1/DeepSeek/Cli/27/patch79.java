// org.apache.commons.cli.OptionsTest::testDuplicateLong
    public void testDuplicateLong()
    {
        Options opts = new Options();
        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("a", "--a", false, "toggle -a*");
        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }