// org.apache.commons.cli.HelpFormatterTest::testOptionWithoutShortFormat2
    public void testOptionWithoutShortFormat2()
    {
        
        Option help = new Option("h", "help", false, "print this message");
        Option version = new Option("v", "version", false, "print version information");
        Option newRun = new Option("n", "new", false, "Create NLT cache entries only for new items");
        Option trackerRun = new Option("t", "tracker", false, "Create NLT cache entries only for tracker items");
        
        Option timeLimit = OptionBuilder.withLongOpt("limit")
                                        .hasArg()
                                        .withValueSeparator()
                                        .withDescription("Set time limit for execution, in mintues")
                                        .create("l");
        
        Option age = OptionBuilder.withLongOpt("age")
                                        .hasArg()
                                        .withValueSeparator()
                                        .withDescription("Age (in days) of cache item before being recomputed")
                                        .create("a");
        
        Option server = OptionBuilder.withLongOpt("server")
                                        .hasArg()
                                        .withValueSeparator()
                                        .withDescription("The NLT server address")
                                        .create("s");
        
        Option numResults = OptionBuilder.withLongOpt("results")
                                        .hasArg()
                                        .withValueSeparator()
                                        .withDescription("Number of results per item")
                                        .create("r");
        
        Option configFile = OptionBuilder.withLongOpt("config")
                                        .hasArg()
                                        .withValueSeparator()
                                        .withDescription("Use the specified configuration file")
                                        .create();
        
        Options mOptions = new Options();
        mOptions.addOption(help);
        mOptions.addOption(version);
        mOptions.addOption(newRun);
        mOptions.addOption(trackerRun);
        mOptions.addOption(timeLimit);
        mOptions.addOption(age);
        mOptions.addOption(server);
        mOptions.addOption(numResults);
        mOptions.addOption(configFile);
        
        HelpFormatter formatter = new HelpFormatter();
        final String EOL = System.getProperty("line.separator");
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out),80,"commandline","header",mOptions,2,2,"footer",true);
        assertEquals(
                "usage: commandline [-a <arg>] [--config <arg>] [-h] [-l <arg>] [-n] [-r <arg>]" + EOL +
                "       [-s <arg>] [-t] [-v]" + EOL +
                "header"+EOL+
                "  -a,--age <arg>      Age (in days) of cache item before being recomputed"+EOL+
                "     --config <arg>   Use the specified configuration file"+EOL+
                "  -h,--help           print this message"+EOL+
                "  -l,--limit <arg>    Set time limit for execution, in mintues"+EOL+
                "  -n,--new            Create NLT cache entries only for new items"+EOL+
                "  -r,--results <arg>  Number of results per item"+EOL+
                "  -s,--server <arg>   The NLT server address"+EOL+
                "  -t,--tracker        Create NLT cache entries only for tracker items"+EOL+
                "  -v,--version        print version information"+EOL+
                "footer"+EOL
                ,out.toString());
    }