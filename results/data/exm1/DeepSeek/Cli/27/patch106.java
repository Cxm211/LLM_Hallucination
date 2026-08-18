// org.apache.commons.cli.ParserTestCase::testNegativeArgument
    public void testNegativeArgument() throws Exception
    {
        String[] args = new String[] { "-b", "-1"} ;

        CommandLine cl = parser.parse(options, args);
        assertEquals("-1", cl.getOptionValue("b"));
    }