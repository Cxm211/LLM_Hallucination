// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
    public void shouldParseShortOptionWithAttachedArgument() throws Exception {
        options.addOption(Option.builder("f").hasArg().build());
        String[] args = new String[] { "-fbar" };
        final CommandLine commandLine = parser.parse(options, args);
        assertTrue(commandLine.hasOption("f"));
        assertEquals("bar", commandLine.getOptionValue("f"));
    }
