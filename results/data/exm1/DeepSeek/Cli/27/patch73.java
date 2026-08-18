// org.apache.commons.cli.OptionGroupTest::testValidLongOnlyOptions
    public void testValidLongOnlyOptions() throws Exception
    {
        CommandLine cl1 = parser.parse(_options, new String[]{"--export"});
        assertTrue("Confirm --export is set", cl1.hasOption("export"));

        CommandLine cl2 = parser.parse(_options, new String[]{"--import"});
        assertTrue("Confirm --import is set", cl2.hasOption("import"));
    }