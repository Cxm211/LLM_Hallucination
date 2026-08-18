// org.apache.commons.cli.bug.BugCLI162Test::testLongLineChunkingIndentIgnored
    public void testLongLineChunkingIndentIgnored() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false, "This description is Long." );
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 22, this.getClass().getName(), "Header", options, 0, 5, "Footer");
        System.err.println(sw.toString());
        String expected = "usage:" + CR +
                          "       org.apache.comm" + CR +
                          "       ons.cli.bug.Bug" + CR +
                          "       CLI162Test" + CR +
                          "Header" + CR +
                          "-x,--extralongarg" + CR +
                          " This description is" + CR +
                          " Long." + CR +
                          "Footer" + CR;
        assertEquals( "Long arguments did not split as expected", expected, sw.toString() );
    }