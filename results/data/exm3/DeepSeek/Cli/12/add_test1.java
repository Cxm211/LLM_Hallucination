// org/apache/commons/cli/GnuParserTest.java
public void testOptionWithEqualNoArgument() throws Exception
    {
        String[] args = new String[] { "-v=foo" };
        Options options = new Options();
        options.addOption("v", false, "verbose");
        Parser parser = new GnuParser();
        try {
            parser.parse(options, args);
            fail("Expected ParseException");
        } catch (ParseException e) {
            // expected
        }
    }
