// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
    public void shouldParseConcatenatedShortOptionsWithValue() throws Exception {
        options.addOption(Option.builder("a").build());
        options.addOption(Option.builder("b").hasArg().build());
        String[] args = new String[] { "-ab=val" };
        final CommandLine commandLine = parser.parse(options, args);
        assertTrue(commandLine.hasOption("a"));
        assertTrue(commandLine.hasOption("b"));
        assertEquals("val", commandLine.getOptionValue("b"));
    }
