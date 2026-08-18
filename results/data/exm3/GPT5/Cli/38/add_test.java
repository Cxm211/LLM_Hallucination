// org/apache/commons/cli/bug/BugCLI265Test.java::shouldParseConcatenatedShortOptionsReversed
@Test
    public void shouldParseConcatenatedShortOptionsReversed() throws Exception {
        String[] concatenatedShortOptions = new String[] { "-ba" };

        final CommandLine commandLine = parser.parse(options, concatenatedShortOptions);

        assertTrue(commandLine.hasOption("a"));
        assertTrue(commandLine.hasOption("b"));
        assertFalse(commandLine.hasOption("last"));
    }