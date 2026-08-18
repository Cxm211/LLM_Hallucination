// buggy function
    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1)
        {
            return false;
        }

        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
        return options.hasShortOption(optName);
        // check for several concatenated short options
    }

// trigger testcase
// org/apache/commons/cli/bug/BugCLI265Test.java::shouldParseConcatenatedShortOptions
@Test
    public void shouldParseConcatenatedShortOptions() throws Exception {
        String[] concatenatedShortOptions = new String[] { "-t1", "-ab" };

        final CommandLine commandLine = parser.parse(options, concatenatedShortOptions);

        assertTrue(commandLine.hasOption("t1"));
        assertNull(commandLine.getOptionValue("t1"));
        assertTrue(commandLine.hasOption("a"));
        assertTrue(commandLine.hasOption("b"));
        assertFalse(commandLine.hasOption("last"));
    }
