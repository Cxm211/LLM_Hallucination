// org/apache/commons/cli/bug/BugCLI265Test.java
@Test
    public void shouldParseMultiCharShortOptionWithValue() throws Exception {
        Options options = new Options();
        options.addOption("foo", true, "a multi-char short option");
        String[] args = new String[]{"-foo=bar"};
        CommandLine commandLine = parser.parse(options, args);
        assertTrue(commandLine.hasOption("foo"));
        assertEquals("bar", commandLine.getOptionValue("foo"));
    }
