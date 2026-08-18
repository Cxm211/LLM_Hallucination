// org.apache.commons.cli.ParserTestCase::testArgumentStartingWithHyphen
    public void testArgumentStartingWithHyphen() throws Exception
    {
        String[] args = new String[]{"-b", "-foo"};

        CommandLine cl = parser.parse(options, args);
        assertEquals("-foo", cl.getOptionValue("b"));
    }