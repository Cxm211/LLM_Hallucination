// org.apache.commons.cli.HelpFormatterTest::testAutomaticUsage
    public void testAutomaticUsage() throws Exception
    {
        HelpFormatter hf = new HelpFormatter();
        Options options = null;
        String expected = "usage: app [-a]";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa");
        hf.printUsage(pw, 60, "app", options);
        pw.flush();
        assertEquals("simple auto usage", expected, out.toString().trim());
        out.reset();

        expected = "usage: app [-a] [-b]";
        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa")
                .addOption("b", false, "bbb");
        hf.printUsage(pw, 60, "app", options);
        pw.flush();
        assertEquals("simple auto usage", expected, out.toString().trim());
        out.reset();
    }