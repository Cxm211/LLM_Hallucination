// buggy function
    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        return token.startsWith("-") && token.length() >= 2 && options.hasShortOption(token.substring(1, 2));

        // remove leading "-" and "=value"
    }

// trigger testcase
// org/apache/commons/cli/bug/BugCLI265Test.java::shouldParseShortOptionWithoutValue
@Test
    public void shouldParseShortOptionWithoutValue() throws Exception {
        String[] twoShortOptions = new String[]{"-t1", "-last"};

        final CommandLine commandLine = parser.parse(options, twoShortOptions);

        assertTrue(commandLine.hasOption("t1"));
        assertNotEquals("Second option has been used as value for first option", "-last", commandLine.getOptionValue("t1"));
        assertTrue("Second option has not been detected", commandLine.hasOption("last"));
    }
