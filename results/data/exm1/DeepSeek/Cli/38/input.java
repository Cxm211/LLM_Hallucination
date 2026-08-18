// buggy code
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

// relevant test
// org.apache.commons.cli.CommandLineTest::testGetOptionProperties
    public void testGetOptionProperties() throws Exception
    {
        String[] args = new String[] { "-Dparam1=value1", "-Dparam2=value2", "-Dparam3", "-Dparam4=value4", "-D", "--property", "foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withValueSeparator().hasOptionalArgs(2).create('D'));
        options.addOption(OptionBuilder.withValueSeparator().hasArgs(2).withLongOpt("property").create());

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        Properties props = cl.getOptionProperties("D");
        assertNotNull("null properties", props);
        assertEquals("number of properties in " + props, 4, props.size());
        assertEquals("property 1", "value1", props.getProperty("param1"));
        assertEquals("property 2", "value2", props.getProperty("param2"));
        assertEquals("property 3", "true", props.getProperty("param3"));
        assertEquals("property 4", "value4", props.getProperty("param4"));

        assertEquals("property with long format", "bar", cl.getOptionProperties("property").getProperty("foo"));
    }

// org.apache.commons.cli.CommandLineTest::testGetOptions
    public void testGetOptions()
    {
        CommandLine cmd = new CommandLine();
        assertNotNull(cmd.getOptions());
        assertEquals(0, cmd.getOptions().length);
        
        cmd.addOption(new Option("a", null));
        cmd.addOption(new Option("b", null));
        cmd.addOption(new Option("c", null));
        
        assertEquals(3, cmd.getOptions().length);
    }

// org.apache.commons.cli.CommandLineTest::testGetParsedOptionValue
    public void testGetParsedOptionValue() throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withType(Number.class).create("i"));
        options.addOption(OptionBuilder.hasArg().create("f"));
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        
        assertEquals(123, ((Number) cmd.getParsedOptionValue("i")).intValue());
        assertEquals("foo", cmd.getParsedOptionValue("f"));
    }

// org.apache.commons.cli.bug.BugCLI252Test::testExactOptionNameMatch
    public void testExactOptionNameMatch() throws ParseException {
        new DefaultParser().parse(getOptions(), new String[]{"--prefix"});
    }

// org.apache.commons.cli.bug.BugCLI252Test::testAmbiquousOptionName
    public void testAmbiquousOptionName() throws ParseException {
        new DefaultParser().parse(getOptions(), new String[]{"--pref"});
    }

// org.apache.commons.cli.bug.BugCLI265Test::shouldParseShortOptionWithValue
    public void shouldParseShortOptionWithValue() throws Exception {
        String[] shortOptionWithValue = new String[]{"-t1", "path/to/my/db"};

        final CommandLine commandLine = parser.parse(options, shortOptionWithValue);

        assertEquals("path/to/my/db", commandLine.getOptionValue("t1"));
        assertFalse(commandLine.hasOption("last"));
    }

// org.apache.commons.cli.bug.BugCLI265Test::shouldParseShortOptionWithoutValue
    public void shouldParseShortOptionWithoutValue() throws Exception {
        String[] twoShortOptions = new String[]{"-t1", "-last"};

        final CommandLine commandLine = parser.parse(options, twoShortOptions);

        assertTrue(commandLine.hasOption("t1"));
        assertNotEquals("Second option has been used as value for first option", "-last", commandLine.getOptionValue("t1"));
        assertTrue("Second option has not been detected", commandLine.hasOption("last"));
    }

// org.apache.commons.cli.bug.BugCLI265Test::shouldParseConcatenatedShortOptions
    public void shouldParseConcatenatedShortOptions() throws Exception {
        String[] concatenatedShortOptions = new String[] { "-t1", "-ab" };

        final CommandLine commandLine = parser.parse(options, concatenatedShortOptions);

        assertTrue(commandLine.hasOption("t1"));
        assertNull(commandLine.getOptionValue("t1"));
        assertTrue(commandLine.hasOption("a"));
        assertTrue(commandLine.hasOption("b"));
        assertFalse(commandLine.hasOption("last"));
    }
