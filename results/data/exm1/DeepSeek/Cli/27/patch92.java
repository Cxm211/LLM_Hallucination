// org.apache.commons.cli.bug.BugCLI162Test::testLongLineChunking
    public void testLongLineChunking() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false,
                                     "This description has ReallyLongValuesThatAreLongerThanTheWidthOfTheColumns " +
                                     "and also other ReallyLongValuesThatAreHugerAndBiggerThanTheWidthOfTheColumnsBob, " +
                                     "yes. ");
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 35, this.getClass().getName(), "Header", options, 0, 5, "Footer");
        String expected = "usage:" + CR +
                          "       org.apache.commons.cli.bug.B" + CR +
                          "       ugCLI162Test" + CR +
                          "Header" + CR +
                          "-x,--extralongarg     This" + CR +
                          "                      description" + CR +
                          "                      has" + CR +
                          "                      ReallyLongVal" + CR +
                          "                      uesThatAreLon" + CR +
                          "                      gerThanTheWid" + CR +
                          "                      thOfTheColumn" + CR +
                          "                      s and also" + CR +
                          "                      other" + CR +
                          "                      ReallyLongVal" + CR +
                          "                      uesThatAreHug" + CR +
                          "                      erAndBiggerTh" + CR +
                          "                      anTheWidthOfT" + CR +
                          "                      heColumnsBob," + CR +
                          "                      yes." + CR +
                          "Footer" + CR;
        assertEquals( "Long arguments did not split as expected", expected, sw.toString() );
    }