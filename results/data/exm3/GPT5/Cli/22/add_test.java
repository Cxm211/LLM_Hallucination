// org/apache/commons/cli/ApplicationTest.java::testGroovy
public void testStopAtNonOptionDoesNotBlockFollowingOptions() throws Exception {
        Options options = new Options();

        options.addOption(
            OptionBuilder.withArgName("charset")
            .hasArg()
            .withDescription("specify the encoding of the files")
            .withLongOpt("encoding")
            .create('c'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("process files line by line using implicit 'line' variable")
            .create('n'));

        Parser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-c", "UTF-8", "-n" }, true);

        assertTrue(line.hasOption('c'));
        assertEquals("UTF-8", line.getOptionValue('c'));
        assertTrue(line.hasOption('n'));
    }