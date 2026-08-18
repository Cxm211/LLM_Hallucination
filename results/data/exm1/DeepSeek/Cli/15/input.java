// buggy code
    public List getValues(final Option option,
                          List defaultValues) {
        // initialize the return list
        List valueList = (List) values.get(option);

        // grab the correct default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        // augment the list with the default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = (List) this.defaultValues.get(option);
        }
                // if there are more default values as specified, add them to
                // the list.
                    // copy the list first
        
        return valueList == null ? Collections.EMPTY_LIST : valueList;
    }

// relevant test
// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Method
    public void testSwitch_Method() {
        final Option o = optionSwitchNull();
        final WriteableCommandLine cl = parsedSwitchNull(o);
        final Object v = methodSwitchOn(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Method_Option
    public void testSwitch_Method_Option() {
        final Option o = optionSwitchOff();
        final WriteableCommandLine cl = parsedSwitchNull(o);
        final Object v = methodSwitchOn(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Option
    public void testSwitch_Option() {
        final Option o = optionSwitchOn();
        final WriteableCommandLine cl = parsedSwitchNull(o);
        final Object v = methodSwitchNull(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Parsed
    public void testSwitch_Parsed() {
        final Option o = optionSwitchNull();
        final WriteableCommandLine cl = parsedSwitchOn(o);
        final Object v = methodSwitchNull(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Parsed_Method
    public void testSwitch_Parsed_Method() {
        final Option o = optionSwitchOff();
        final WriteableCommandLine cl = parsedSwitchOn(o);
        final Object v = methodSwitchNull(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Parsed_Method_Option
    public void testSwitch_Parsed_Method_Option() {
        final Option o = optionSwitchOff();
        final WriteableCommandLine cl = parsedSwitchOn(o);
        final Object v = methodSwitchOff(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testSwitch_Parsed_Option
    public void testSwitch_Parsed_Option() {
        final Option o = optionSwitchOff();
        final WriteableCommandLine cl = parsedSwitchOn(o);
        final Object v = methodSwitchNull(cl, o);
        assertEquals(Boolean.TRUE, v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues
    public void testValues() {
        final Option o = optionValueMissing();
        final WriteableCommandLine cl = parsedValueMissing(o);
        final Object v = methodValueMissing(cl, o);
        assertNull(v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Method
    public void testValues_Method() {
        final Option o = optionValueMissing();
        final WriteableCommandLine cl = parsedValueMissing(o);
        final Object v = methodValuePresent(cl, o);
        assertEquals("method", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Method_Option
    public void testValues_Method_Option() {
        final Option o = optionValuePresent();
        final WriteableCommandLine cl = parsedValueMissing(o);
        final Object v = methodValuePresent(cl, o);
        assertEquals("method", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Option
    public void testValues_Option() {
        final Option o = optionValuePresent();
        final WriteableCommandLine cl = parsedValueMissing(o);
        final Object v = methodValueMissing(cl, o);
        assertEquals("option", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Parsed
    public void testValues_Parsed() {
        final Option o = optionValueMissing();
        final WriteableCommandLine cl = parsedValuePresent(o);
        final Object v = methodValueMissing(cl, o);
        assertEquals("parsed", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Parsed_Method
    public void testValues_Parsed_Method() {
        final Option o = optionValueMissing();
        final WriteableCommandLine cl = parsedValuePresent(o);
        final Object v = methodValuePresent(cl, o);
        assertEquals("parsed", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Parsed_Method_Option
    public void testValues_Parsed_Method_Option() {
        final Option o = optionValuePresent();
        final WriteableCommandLine cl = parsedValuePresent(o);
        final Object v = methodValuePresent(cl, o);
        assertEquals("parsed", v);
    }

// org.apache.commons.cli2.CommandLineDefaultsTest::testValues_Parsed_Option
    public void testValues_Parsed_Option() {
        final Option o = optionValuePresent();
        final WriteableCommandLine cl = parsedValuePresent(o);
        final Object v = methodValueMissing(cl, o);
        assertEquals("parsed", v);
    }

// org.apache.commons.cli2.DocumentationTest::testBasicUsage
    public void testBasicUsage() throws IOException, OptionException {
        HelpFormatter helpFormatter = new HelpFormatter();
        
        helpFormatter.setPrintWriter(new PrintWriter(new StringWriter()));

        
        DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        Option version =
            obuilder
                .withLongName("version")
                .withDescription("Displays version information and then exits")
                .create();

        Option help =
            obuilder
                .withShortName("h")
                .withShortName("?")
                .withLongName("help")
                .withDescription("Displays help on usage and then exits")
                .create();

        ArgumentBuilder abuilder = new ArgumentBuilder();
        Argument logFile =
            abuilder
                .withDescription("The log file to write to")
                .withName("file")
                .withMinimum(1)
                .withMaximum(1)
                .create();
        Option log =
            obuilder
                .withArgument(logFile)
                .withShortName("log")
                .withDescription("Log progress information to a file")
                .create();

        GroupBuilder gbuilder = new GroupBuilder();
        Group outputQuality =
            gbuilder
                .withName("quality")
                .withDescription("Controls the quality of console output")
                .withMaximum(1)
                .withOption(
                    obuilder
                        .withShortName("s")
                        .withDescription("Silent")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("q")
                        .withDescription("Quiet")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("n")
                        .withDescription("Normal")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("v")
                        .withDescription("Verbose")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("d")
                        .withDescription("Debug")
                        .create())
                .create();

        Group options =
            new GroupBuilder()
                .withName("options")
                .withOption(version)
                .withOption(help)
                .withOption(log)
                .withOption(outputQuality)
                .create();

        final String[] args = new String[] { "--bad-option" };

        Parser parser = new Parser();
        parser.setHelpFormatter(helpFormatter);
        parser.setGroup(options);
        parser.setHelpOption(help);
        CommandLine commandLine = parser.parseAndHelp(args);
        if (commandLine != null) {
            if (commandLine.hasOption(version)) {
                System.out.println("MyApp ver 1.0");
                return;
            }
            if (commandLine.hasOption("-log")) {
                String filename = (String)commandLine.getValue("-log");
                
            }
        }

        try {
            commandLine = parser.parse(args);
            fail("Unexpected Option!");
        }
        catch (OptionException uoe) {
            assertEquals(
                "Unexpected --bad-option while processing options",
                uoe.getMessage());
        }
    }

// org.apache.commons.cli2.DocumentationTest::testManualIntroduction
    public void testManualIntroduction() {

        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();
        GroupBuilder gBuilder = new GroupBuilder();

        DefaultOption xmlOption =
            oBuilder
                .withLongName("xml")
                .withDescription("Output using xml format")
                .create();

        Argument pathArgument =
            aBuilder
                .withName("path")
                .withMinimum(1)
                .withMaximum(1)
                .create();

        Group outputChildren =
            gBuilder
                .withOption(xmlOption)
                .create();

        Option outputOption =
            oBuilder
                .withLongName("output")
                .withDescription("Outputs to a file")
                .withArgument(pathArgument)
                .withChildren(outputChildren)
                .create();

        

        Group options = outputChildren;
        HelpFormatter hf = new HelpFormatter();

        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);
        p.setHelpTrigger("--help");
        CommandLine cl = p.parseAndHelp(new String[]{});
        if(cl==null) {
            System.exit(-1);
        }

        

        cl = new WriteableCommandLineImpl(outputChildren,new ArrayList());

        
        if(cl.hasOption("--output")) {
            
            String path = (String)cl.getValue("--output");
            
            boolean xml = cl.hasOption("--xml");
            
            configureOutput(path,xml);
        }

    }

// org.apache.commons.cli2.DocumentationTest::testExampleAnt
    public void testExampleAnt() throws IOException, OptionException {
        

        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        Option help =
            obuilder
                .withShortName("help")
                .withShortName("h")
                .withDescription("print this message")
                .create();
        Option projecthelp =
            obuilder
                .withShortName("projecthelp")
                .withShortName("p")
                .withDescription("print project help information")
                .create();
        Option version =
            obuilder
                .withShortName("version")
                .withDescription("print the version information and exit")
                .create();
        Option diagnostics =
            obuilder
                .withShortName("diagnostics")
                .withDescription("print information that might be helpful to diagnose or report problems.")
                .create();
        Option quiet =
            obuilder
                .withShortName("quiet")
                .withShortName("q")
                .withDescription("be extra quiet")
                .create();
        Option verbose =
            obuilder
                .withShortName("verbose")
                .withShortName("v")
                .withDescription("be extra verbose")
                .create();
        Option debug =
            obuilder
                .withShortName("debug")
                .withShortName("d")
                .withDescription("print debugging information")
                .create();
        Option emacs =
            obuilder
                .withShortName("emacs")
                .withShortName("e")
                .withDescription("produce logging information without adornments")
                .create();
        Option lib =
            obuilder
                .withShortName("lib")
                .withDescription("specifies a path to search for jars and classes")
                .withArgument(
                    abuilder
                        .withName("path")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option logfile =
            obuilder
                .withShortName("logfile")
                .withShortName("l")
                .withDescription("use given file for log")
                .withArgument(
                    abuilder
                        .withName("file")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option logger =
            obuilder
                .withShortName("logger")
                .withDescription("the class which is to perform logging")
                .withArgument(
                    abuilder
                        .withName("classname")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option listener =
            obuilder
                .withShortName("listener")
                .withDescription("add an instance of class as a project listener")
                .withArgument(
                    abuilder
                        .withName("classname")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option noinput =
            obuilder
                .withShortName("noinput")
                .withDescription("do not allow interactive input")
                .create();
        Option buildfile =
            obuilder
                .withShortName("buildfile")
                .withShortName("file")
                .withShortName("f")
                .withDescription("use given buildfile")
                .withArgument(
                    abuilder
                        .withName("file")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option property = new PropertyOption();
        Option propertyfile =
            obuilder
                .withShortName("propertyfile")
                .withDescription("load all properties from file with -D properties taking precedence")
                .withArgument(
                    abuilder
                        .withName("name")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option inputhandler =
            obuilder
                .withShortName("inputhandler")
                .withDescription("the class which will handle input requests")
                .withArgument(
                    abuilder
                        .withName("class")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option find =
            obuilder
                .withShortName("find")
                .withShortName("s")
                .withDescription("search for buildfile towards the root of the filesystem and use it")
                .withArgument(
                    abuilder
                        .withName("file")
                        .withMinimum(1)
                        .withMaximum(1)
                        .create())
                .create();
        Option targets = abuilder.withName("target").create();

        Group options =
            gbuilder
                .withName("options")
                .withOption(help)
                .withOption(projecthelp)
                .withOption(version)
                .withOption(diagnostics)
                .withOption(quiet)
                .withOption(verbose)
                .withOption(debug)
                .withOption(emacs)
                .withOption(lib)
                .withOption(logfile)
                .withOption(logger)
                .withOption(listener)
                .withOption(noinput)
                .withOption(buildfile)
                .withOption(property)
                .withOption(propertyfile)
                .withOption(inputhandler)
                .withOption(find)
                .withOption(targets)
                .create();

        
        String[] args = new String[]{};

        Parser parser = new Parser();
        parser.setGroup(options);
        CommandLine cl = parser.parse(args);

        if(cl.hasOption(help)) {
            
            return;
        }
        if(cl.hasOption("-version")) {
            
            return;
        }
        if(cl.hasOption(logfile)) {
            String file = (String)cl.getValue(logfile);
            
        }
        List targetList = cl.getValues(targets);
        for (Iterator i = targetList.iterator(); i.hasNext();) {
            String target = (String) i.next();
            
        }

        

        HelpFormatter hf = new HelpFormatter();
        hf.setShellCommand("ant");
        hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_GROUP_NAME);
        hf.getFullUsageSettings().add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        hf.getFullUsageSettings().remove(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        hf.getLineUsageSettings().add(DisplaySetting.DISPLAY_PROPERTY_OPTION);
        hf.getLineUsageSettings().add(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        hf.getLineUsageSettings().add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);

        hf.getDisplaySettings().remove(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        hf.setGroup(options);
        
        hf.setPrintWriter(new PrintWriter(new StringWriter()));
        hf.print();

    }

// org.apache.commons.cli2.PrecedenceTest::testSimple
    public void testSimple() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();

        final Group options =
            new GroupBuilder()
                .withOption(oBuilder.withShortName("file").create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-file" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testArgument
    public void testArgument() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();

        final Group options =
            new GroupBuilder()
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withArgument(aBuilder.create())
                        .create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testBurst
    public void testBurst() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        final Group options =
            gBuilder
                .withOption(oBuilder.withShortName("f").create())
                .withOption(oBuilder.withShortName("i").create())
                .withOption(oBuilder.withShortName("l").create())
                .withOption(oBuilder.withShortName("e").create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f", "-i", "-l", "-e" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testChildren
    public void testChildren() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Group children =
            gBuilder
                .withOption(oBuilder.withShortName("i").create())
                .withOption(oBuilder.withShortName("l").create())
                .withOption(oBuilder.withShortName("e").create())
                .create();
        final Group options =
            gBuilder
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withChildren(children)
                        .create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f", "-i", "-l", "-e" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testArgumentVsBurst
    public void testArgumentVsBurst() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();

        final Group options =
            gBuilder
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withArgument(aBuilder.create())
                        .create())
                .withOption(oBuilder.withShortName("i").create())
                .withOption(oBuilder.withShortName("l").create())
                .withOption(oBuilder.withShortName("e").create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testArgumentVsChildren
    public void testArgumentVsChildren() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();

        final Group children =
            gBuilder
                .withOption(oBuilder.withShortName("i").create())
                .withOption(oBuilder.withShortName("l").create())
                .withOption(oBuilder.withShortName("e").create())
                .create();
        final Group options =
            gBuilder
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withChildren(children)
                        .withArgument(aBuilder.create())
                        .create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f" }, cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testBurstVsChildren
    public void testBurstVsChildren() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Group children =
            gBuilder
                .withOption(
                    oBuilder.withShortName("i").withLongName("ci").create())
                .withOption(
                    oBuilder.withShortName("l").withLongName("cl").create())
                .withOption(
                    oBuilder.withShortName("e").withLongName("ce").create())
                .create();

        final Group options =
            gBuilder
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withChildren(children)
                        .create())
                .withOption(
                    oBuilder.withShortName("i").withLongName("bi").create())
                .withOption(
                    oBuilder.withShortName("l").withLongName("bl").create())
                .withOption(
                    oBuilder.withShortName("e").withLongName("be").create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(
            new String[] { "-f", "-i", "--ci", "-l", "--cl", "-e", "--ce" },
            cl);
    }

// org.apache.commons.cli2.PrecedenceTest::testArgumentVsBurstVsChildren
    public void testArgumentVsBurstVsChildren() throws OptionException {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();

        final Group children =
            gBuilder
                .withOption(
                    oBuilder.withShortName("i").withLongName("ci").create())
                .withOption(
                    oBuilder.withShortName("l").withLongName("cl").create())
                .withOption(
                    oBuilder.withShortName("e").withLongName("ce").create())
                .create();

        final Group options =
            gBuilder
                .withOption(
                    oBuilder
                        .withShortName("f")
                        .withChildren(children)
                        .withArgument(aBuilder.create())
                        .create())
                .withOption(oBuilder.withShortName("i").create())
                .withOption(oBuilder.withShortName("l").create())
                .withOption(oBuilder.withShortName("e").create())
                .create();

        final CommandLine cl = buildCommandLine(options, args);
        assertEquals(new String[] { "-f" }, cl);
    }

// org.apache.commons.cli2.application.AntTest::testAnt
    public void testAnt() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Group options =
            gbuilder
                .withName("ant")
                .withOption(
                    obuilder
                        .withShortName("help")
                        .withDescription("print this message")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("projecthelp")
                        .withDescription("print project help information")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("version")
                        .withDescription("print the version information and exit")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("diagnostics")
                        .withDescription("print information that might be helpful to diagnose or report problems.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("quiet")
                        .withShortName("q")
                        .withDescription("be extra quiet")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("verbose")
                        .withShortName("v")
                        .withDescription("be extra verbose")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("debug")
                        .withDescription("print debugging information")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("emacs")
                        .withDescription("produce logging information without adornments")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("logfile")
                        .withShortName("l")
                        .withDescription("use given file for log")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("logger")
                        .withDescription("the class which is to perform logging")
                        .withArgument(
                            abuilder
                                .withName("classname")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("listener")
                        .withDescription("add an instance of class as a project listener")
                        .withArgument(
                            abuilder
                                .withName("classname")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("buildfile")
                        .withShortName("file")
                        .withShortName("f")
                        .withDescription("use given buildfile")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(PropertyOption.INSTANCE)
                .withOption(
                    obuilder
                        .withShortName("propertyfile")
                        .withDescription("load all properties from file with -D properties taking precedence")
                        .withArgument(
                            abuilder
                                .withName("name")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("inputhandler")
                        .withDescription("the class which will handle input requests")
                        .withArgument(
                            abuilder
                                .withName("class")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("find")
                        .withDescription("search for buildfile towards the root of the filesystem and use it")
                        .withArgument(
                            abuilder
                                .withName("file")
                                .withMinimum(1)
                                .withMaximum(1)
                                .create())
                        .create())
                .withOption(abuilder.withName("target").create())
                .create();

        Parser parser = new Parser();
        parser.setGroup(options);
        CommandLine line =
            parser.parse(
                new String[] {
                    "-buildfile",
                    "mybuild.xml",
                    "-Dproperty=value",
                    "-Dproperty1=value1",
                    "-projecthelp",
                    "compile",
                    "docs" });

        
        assertEquals(2, line.getProperties().size());
        assertEquals("value", line.getProperty("property"));
        assertEquals("value1", line.getProperty("property1"));

        
        assertEquals("mybuild.xml", line.getValue("-buildfile"));
        assertTrue(line.hasOption("-projecthelp"));
        assertFalse(line.hasOption("-help"));

        assertTrue(line.hasOption("target"));
        final List targets = new ArrayList();
        targets.add("compile");
        targets.add("docs");
        assertEquals(targets, line.getValues("target"));
    }

// org.apache.commons.cli2.application.CpTest::testNoSource
    public void testNoSource() {
        Parser parser = new Parser();
        parser.setGroup(options);
        try {
            parser.parse(new String[0]);
        }
        catch (OptionException mve) {
            assertEquals(
                "Missing value(s) SOURCE [SOURCE ...]",
                mve.getMessage());
        }
    }

// org.apache.commons.cli2.application.CpTest::testOneSource
    public void testOneSource() throws OptionException {
        final String[] args = new String[] { "source1", "dest1" };
        final Parser parser = new Parser();
        parser.setGroup(options);
        final CommandLine commandLine = parser.parse(args);

        assertTrue(commandLine.getValues(source).contains("source1"));
        assertEquals(1, commandLine.getValues(source).size());
        assertTrue(commandLine.getValues(dest).contains("dest1"));
        assertEquals(1, commandLine.getValues(dest).size());
    }

// org.apache.commons.cli2.application.CpTest::testMultiSource
    public void testMultiSource() throws OptionException {
        final String[] args =
            new String[] { "source1", "source2", "source3", "dest1" };
        final Parser parser = new Parser();
        parser.setGroup(options);
        final CommandLine commandLine = parser.parse(args);

        assertTrue(commandLine.getValues(source).contains("source1"));
        assertTrue(commandLine.getValues(source).contains("source2"));
        assertTrue(commandLine.getValues(source).contains("source3"));
        assertEquals(3, commandLine.getValues(source).size());

        assertTrue(commandLine.getValues(dest).contains("dest1"));
        assertEquals(1, commandLine.getValues(dest).size());
    }

// org.apache.commons.cli2.application.CpTest::testHelp
    public void testHelp() throws IOException {
        final StringWriter out = new StringWriter();
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setGroup(options);
        helpFormatter.setPrintWriter(new PrintWriter(out));
        helpFormatter.print();

        final BufferedReader in =
            new BufferedReader(new StringReader(out.toString()));
        assertEquals(
            "Usage:                                                                          ",
            in.readLine());
        assertEquals(
            " [-a -b -d -f -i -l -p -P -r --sparse <WHEN> -R -s -S <SUFFIX> -u -v -V <WORD>  ",
            in.readLine());
        assertEquals(
            "-x --help --version] <SOURCE1> [<SOURCE2> ...] <DEST>                           ",
            in.readLine());
        assertEquals(
            "OPTIONS                                                                         ",
            in.readLine());
        assertEquals(
            "  -a (--archive)                same as -dpR                                    ",
            in.readLine());
        assertEquals(
            "  -b (--backup)                 make backup before removal                      ",
            in.readLine());
        assertEquals(
            "  -d (--no-dereference)         preserve links                                  ",
            in.readLine());
        assertEquals(
            "  -f (--force)                  remove existing destinations, never prompt      ",
            in.readLine());
        assertEquals(
            "  -i (--interactive)            prompt before overwrite                         ",
            in.readLine());
        assertEquals(
            "  -l (--link)                   link files instead of copying                   ",
            in.readLine());
        assertEquals(
            "  -p (--preserve)               preserve file attributes if possible            ",
            in.readLine());
        assertEquals(
            "  -P (--parents)                append source path to DIRECTORY                 ",
            in.readLine());
        assertEquals(
            "  -r                            copy recursively, non-directories as files      ",
            in.readLine());
        assertEquals(
            "  --sparse WHEN                 control creation of sparse files                ",
            in.readLine());
        assertEquals(
            "  -R (--recursive)              copy directories recursively                    ",
            in.readLine());
        assertEquals(
            "  -s (--symbolic-link)          make symbolic links instead of copying          ",
            in.readLine());
        assertEquals(
            "  -S (--suffix) SUFFIX          override the usual backup suffix                ",
            in.readLine());
        assertEquals(
            "  -u (--update)                 copy only when the SOURCE file is newer than    ",
            in.readLine());
        assertEquals(
            "                                the destination file or when the destination    ",
            in.readLine());
        assertEquals(
            "                                file is missing                                 ",
            in.readLine());
        assertEquals(
            "  -v (--verbose)                explain what is being done                      ",
            in.readLine());
        assertEquals(
            "  -V (--version-contol) WORD    explain what is being done                      ",
            in.readLine());
        assertEquals(
            "  -x (--one-file-system)        stay on this file system                        ",
            in.readLine());
        assertEquals(
            "  --help                        display this help and exit                      ",
            in.readLine());
        assertEquals(
            "  --version                     output version information and exit             ",
            in.readLine());
        assertEquals(
            "  SOURCE [SOURCE ...]                                                           ",
            in.readLine());
        assertEquals(
            "  DEST                                                                          ",
            in.readLine());
        assertNull(in.readLine());
    }

// org.apache.commons.cli2.application.LsTest::testLs
    public void testLs() throws OptionException {
        
        Parser parser = new Parser();
        parser.setGroup(options);
        CommandLine line =
            parser.parse(new String[] { "--block-size=10", "--color=never" });

        assertTrue(line.hasOption("--block-size"));
        assertEquals(line.getValue("--block-size"), "10");
        assertFalse(line.hasOption("--ignore-backups"));
    }

// org.apache.commons.cli2.bug.Bug13886Test::testMandatoryGroup
    public void testMandatoryGroup() throws Exception {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option a = obuilder.withShortName("a").create();

        final Option b = obuilder.withShortName("b").create();

        final Group options =
            gbuilder
                .withOption(a)
                .withOption(b)
                .withMaximum(1)
                .withMinimum(1)
                .create();

        final Parser parser = new Parser();
        parser.setGroup(options);

        try {
            parser.parse(new String[] {
            });
            fail("Expected MissingOptionException not caught");
        }
        catch (final OptionException exp) {
            assertEquals("Missing option -a|-b", exp.getMessage());
        }

        try {
            parser.parse(new String[] { "-a" });
        }
        catch (final OptionException exp) {
            fail("Unexpected MissingOptionException caught");
        }

        try {
            parser.parse(new String[] { "-b" });
        }
        catch (final OptionException exp) {
            fail("Unexpected MissingOptionException caught");
        }

        try {
            parser.parse(new String[] { "-a", "-b" });
            fail("Expected UnexpectedOptionException not caught");
        }
        catch (final OptionException exp) {
            assertEquals(
                "Unexpected -b while processing -a|-b",
                exp.getMessage());
        }
    }

// org.apache.commons.cli2.bug.Bug13935Test::testRequiredGroup
    public void testRequiredGroup() throws Exception {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option testOption =
            obuilder
                .withShortName("a")
                .withArgument(abuilder.withName("quoted string").create())
                .create();

        final Group options = gbuilder.withOption(testOption).create();

        final Parser parser = new Parser();
        parser.setGroup(options);

        final CommandLine cmdLine =
            parser.parse(new String[] { "-a", "\"two tokens\"" });

        assertTrue(cmdLine.hasOption("-a"));
        assertEquals("two tokens", cmdLine.getValue("-a"));
    }

// org.apache.commons.cli2.bug.Bug15046Test::testParamNamedAsOption
    public void testParamNamedAsOption() throws Exception {
        final String[] CLI_ARGS = new String[] { "-z", "c" };

        DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        ArgumentBuilder abuilder = new ArgumentBuilder();

        Option option =
            obuilder
                .withShortName("z")
                .withLongName("timezone")
                .withDescription("affected option")
                .withArgument(abuilder.withName("timezone").create())
                .create();

        GroupBuilder gbuilder = new GroupBuilder();
        Group options =
            gbuilder.withName("bug15046").withOption(option).create();

        Parser parser = new Parser();
        parser.setGroup(options);
        CommandLine line = parser.parse(CLI_ARGS);

        assertEquals("c", line.getValue("-z"));

        Option c =
            obuilder
                .withShortName("c")
                .withLongName("conflict")
                .withDescription("conflicting option")
                .withArgument(abuilder.withName("conflict").create())
                .create();

        options =
            gbuilder
                .withName("bug15046")
                .withOption(option)
                .withOption(c)
                .create();

        parser.setGroup(options);
        line = parser.parse(CLI_ARGS);

        assertEquals("c", line.getValue("-z"));
    }

// org.apache.commons.cli2.bug.Bug15648Test::testQuotedArgumentValue
    public void testQuotedArgumentValue() throws Exception {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option testOption =
            obuilder
                .withShortName("a")
                .withArgument(abuilder.withName("quoted string").create())
                .create();

        final Group options = gbuilder.withOption(testOption).create();

        final Parser parser = new Parser();
        parser.setGroup(options);

        final CommandLine cmdLine =
            parser.parse(new String[] { "-a", "\"two tokens\"" });

        assertTrue(cmdLine.hasOption("-a"));
        assertEquals("two tokens", cmdLine.getValue("-a"));
    }

// org.apache.commons.cli2.bug.Bug28005Test::testInfiniteLoop
    public void testInfiniteLoop() {
        final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder argumentBuilder = new ArgumentBuilder();
        final GroupBuilder groupBuilder = new GroupBuilder();
        final CommandBuilder commandBuilder = new CommandBuilder();

        final Option inputFormatOption =
            optionBuilder
                .withLongName("input-format")
                
                .create();

        final Argument argument =
            argumentBuilder
                .withName("file")
                .create();

        final Group children =
            groupBuilder
                .withName("options")
                .withOption(inputFormatOption)
                .create();

        final Option command =
            commandBuilder
                .withName("convert")
                .withChildren(children)
                .withArgument(argument)
                .create();

        final Group root =
            groupBuilder
                .withName("commands")
                .withOption(command)
                .create();

        final Parser parser = new Parser();
        parser.setGroup(root);
        final String[] args = new String[]{"convert", "test.txt",
                "--input-format", "a"};

        try {
            parser.parse(args);
            fail("a isn't valid!!");
        } catch (OptionException e) {
            assertEquals("Unexpected a while processing commands",e.getMessage());
        }
    }

// org.apache.commons.cli2.bug.Bug32533Test::testBlah
    public void testBlah() throws OptionException {

        Option a1 = new DefaultOptionBuilder().withLongName("a1").create();
        Option b1 = new DefaultOptionBuilder().withLongName("b1").create();
        Option c1 = new DefaultOptionBuilder().withLongName("c1").create();

        Group b = new GroupBuilder().withOption(b1).create();
        Group c = new GroupBuilder().withOption(c1).create();
        Group a = new GroupBuilder().withOption(a1).withOption(b).withOption(c).create();

        Parser parser = new Parser();
        parser.setGroup(a);
        parser.parse(new String[]{"--a1","--b1"});
    }

// org.apache.commons.cli2.bug.BugCLI122Test::testArgumentWhichStartsWithDash
    public void testArgumentWhichStartsWithDash() throws OptionException {
        Argument wdArg = new ArgumentBuilder()
                .withName("anything")
                .withMaximum(1)
                .withMinimum(1)
                .withInitialSeparator('=')
                .create();

        Option wdOpt = new DefaultOptionBuilder().withArgument(wdArg)
                .withDescription("anything, foo or -foo")
                .withLongName("argument")
                .withShortName("a")
                .create();

        Group group = new GroupBuilder().withOption(wdOpt).create();

        Parser p = new Parser();
        p.setGroup(group);
        CommandLine normal = p.parse (new String[]{"-a", "foo"});
        assertNotNull(normal);
        assertEquals(normal.getValue(wdOpt), "foo");

        CommandLine withDash = p.parse (new String[]{"--argument", "\"-foo\""});
        assertNotNull(withDash);
        assertEquals("-foo", withDash.getValue(wdOpt));

        CommandLine withDashAndEquals = p.parse (new String[]{"--argument=-foo"});
        assertNotNull(withDashAndEquals);
        assertEquals("-foo", withDashAndEquals.getValue(wdOpt));
    }

// org.apache.commons.cli2.bug.BugCLI126Test::testMultiplePropertyArgs
    public void testMultiplePropertyArgs() throws OptionException {
        PropertyOption conf = new PropertyOption("-P", "Properties for this process", 1);
        PropertyOption env = new PropertyOption("-C", "Properties for child processes", 2);
        GroupBuilder builder = new GroupBuilder();
        Group options = builder.withOption(conf).withOption(env).create();

        Parser parser = new Parser();
        parser.setGroup(options);
        CommandLine line =
            parser.parseAndHelp(
                new String[] {
                    "-Phome=.",
                    "-Chome=/"
                    });
        assertEquals(".", line.getProperty(conf, "home"));
        assertEquals("/", line.getProperty(env, "home"));
    }

// org.apache.commons.cli2.bug.BugCLI12Test::testBug
  public void testBug() {
    Argument arg = new ArgumentBuilder().withName("file").create();

    PropertyOption option = new PropertyOption();

    Group group = new GroupBuilder().withOption(option).withOption(arg).create();

    Parser p = new Parser();
    p.setGroup(group);

    CommandLine cl = p.parseAndHelp( new String[] { "-Dmyprop1=myval1", "-Dmyprop2=myval2", "myfile" } );
    if(cl == null) {
      assertTrue("Couldn't parse valid commandLine", false);
    }

    assertEquals( "myval1", cl.getProperty(option, "myprop1"));
    assertEquals( "myval2", cl.getProperty(option, "myprop2"));

    String extraArgs = (String) cl.getValue(arg);
    assertEquals( "myfile", extraArgs);
  }

// org.apache.commons.cli2.bug.BugCLI144Test::testFileValidator
	public void testFileValidator() {
		final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        DefaultOption fileNameOption = obuilder.withShortName("f")
                .withLongName("file-name").withRequired(true).withDescription(
                        "name of an existing file").withArgument(
                        abuilder.withName("file-name").withValidator(
                                FileValidator.getExistingFileInstance())
                                .create()).create();
        Group options = gbuilder.withName("options").withOption(fileNameOption)
                .create();
        Parser parser = new Parser();
        parser.setHelpTrigger("--help");
        parser.setGroup(options);

        final String fileName = "src/test/org/apache/commons/cli2/bug/BugCLI144Test.java";
        CommandLine cl = parser
                .parseAndHelp(new String[] { "--file-name", fileName });
        assertNotNull(cl);
        assertEquals("Wrong file", new File(fileName), cl.getValue(fileNameOption));
	}

// org.apache.commons.cli2.bug.BugCLI145Test::testWithMaximum
    public void testWithMaximum() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        DefaultOption aOption = obuilder//
                .withShortName("a")
                .withLongName("a")
                .withArgument(abuilder
                        .withName("a")
                        .withDefault("10")
                        .create())
                .create();
        DefaultOption bOption = obuilder
                .withShortName("b")
                .withLongName("b")
                .withArgument(abuilder
                        .withName("b")
                        .withMinimum(2)
                        .withMaximum(4)
                        .withDefault("100")
                        .withDefault("1000")
                        .withDefault("10000")
                        .withDefault("1000000")
                        .create())
                .create();
        Group options = gbuilder
                .withName("options")
                .withOption(aOption)
                .withOption(bOption)
                .create();
        Parser parser = new Parser();
        parser.setHelpTrigger("--help");
        parser.setGroup(options);
        CommandLine cl = parser.parseAndHelp("-a 0 -b 1 2 3 4".split(" "));
        assertNotNull(cl);
        int a = Integer.parseInt(cl.getValue(aOption).toString());
        List b = cl.getValues(bOption);
        assertEquals(0, a);
        assertEquals(4, b.size());
    }

// org.apache.commons.cli2.bug.BugCLI145Test::testWithMaximumUsingDefaultValues
    public void testWithMaximumUsingDefaultValues() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        DefaultOption aOption = obuilder//
                .withShortName("a")
                .withLongName("a")
                .withArgument(abuilder
                        .withName("a")
                        .withDefault("10")
                        .create())
                .create();
        DefaultOption bOption = obuilder
                .withShortName("b")
                .withLongName("b")
                .withArgument(abuilder
                        .withName("b")
                        .withMinimum(2)
                        .withMaximum(4)
                        .withDefault("100")
                        .withDefault("1000")
                        .withDefault("10000")
                        .create())
                .create();
        Group options = gbuilder
                .withName("options")
                .withOption(aOption)
                .withOption(bOption)
                .create();
        Parser parser = new Parser();
        parser.setHelpTrigger("--help");
        parser.setGroup(options);
        CommandLine cl = parser.parseAndHelp("-a -b".split(" "));
        assertNotNull(cl);
        int a = Integer.parseInt(cl.getValue(aOption).toString());
        List b = cl.getValues(bOption);
        assertEquals(10, a);
        assertEquals(3, b.size());
        assertEquals("10000", b.get(2));
    }

// org.apache.commons.cli2.bug.BugCLI158Test::testSingleOptionSingleArgument
    public void testSingleOptionSingleArgument() throws Exception {
        Parser parser = createDefaultValueParser(new String[]{"100", "1000"});
        String enteredValue1 = "1";
        String[] args = new String[]{"-b", enteredValue1};
        CommandLine cl = parser.parse(args);
        CommandLine cmd = cl;
        assertNotNull(cmd);
        List b = cmd.getValues("-b");
        assertEquals("[" + enteredValue1 + ", 1000]", b + "");
    }

// org.apache.commons.cli2.bug.BugCLI158Test::testSingleOptionNoArgument
    public void testSingleOptionNoArgument() throws Exception {
        Parser parser = createDefaultValueParser(new String[]{"100", "1000"});
        String[] args = new String[]{"-b"};
        CommandLine cl = parser.parse(args);
        CommandLine cmd = cl;
        assertNotNull(cmd);
        List b = cmd.getValues("-b");
        assertEquals("[100, 1000]", b + "");
    }

// org.apache.commons.cli2.bug.BugCLI158Test::testSingleOptionMaximumNumberOfArgument
    public void testSingleOptionMaximumNumberOfArgument() throws Exception {
        String[] args = new String[]{"-b", "1", "2"};
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        DefaultOption bOption = obuilder.withShortName("b")
                .withLongName("b")
                .withArgument(abuilder.withName("b")
                        .withMinimum(2)
                        .withMaximum(4)
                        .withDefault("100")
                        .withDefault("1000")
                        .withDefault("10000")
                        .create())
                .create();

        Group options = gbuilder
                .withName("options")
                .withOption(bOption)
                .create();

        Parser parser = new Parser();
        parser.setHelpTrigger("--help");
        parser.setGroup(options);
        CommandLine cl = parser.parse(args);
        CommandLine cmd = cl;
        assertNotNull(cmd);
        List b = cmd.getValues("-b");
        assertEquals("[1, 2, 10000]", b + "");
    }

// org.apache.commons.cli2.bug.BugCLI80Test::testBug
    public void testBug() {
        final String optName = "option";

        Argument arg = new ArgumentBuilder().withName(optName)
                                            .withMaximum(1)
                                            .create();

        Option option = new DefaultOptionBuilder().withArgument(arg)
                                                  .withDescription("singular option")
                                                  .withLongName(optName)
                                                  .withShortName("o")
                                                  .create();

        Group group = new GroupBuilder().withOption(option).create();

        Parser p = new Parser();
        p.setGroup(group);

        CommandLine cl = p.parseAndHelp( new String[] { "-o", "yes" } );
        assertNotNull("Couldn't parse valid commandLine", cl);

        assertEquals("Couldn't look up value by short name", "yes", cl.getValue("-o") );

        try {
            cl = p.parse( new String[] { "-o", "yes", "-o", "jam" } );
            fail("Parsed invalid commandLine");
        } catch(OptionException e) {
          
        }
    }

// org.apache.commons.cli2.bug.BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike
    public void testLoopingOptionLookAlike() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group options = gbuilder
            .withName("ant")
            .withOption(obuilder.withShortName("help").withDescription("print this message").create())
            .withOption(obuilder.withShortName("projecthelp").withDescription("print project help information").create())
            .withOption(abuilder.withName("target").create())
            .create();
        
        final Parser parser = new Parser();
        parser.setGroup(options);
        try {
            parser.parse(new String[] { "-abcdef",
                    "testfile.txt ", });
            fail("OptionException");
        } catch (OptionException e) {
            assertEquals("Unexpected -abcdef while processing ant",e.getMessage());
        }
    }

// org.apache.commons.cli2.bug.BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2
    public void testLoopingOptionLookAlike2() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Argument inputfile_opt = abuilder.withName("input").withMinimum(1).withMaximum(1).create();
        final Argument outputfile_opt = abuilder.withName("output").withMinimum(1).withMaximum(1).create();
        final Argument targets = new SourceDestArgument(inputfile_opt, outputfile_opt);
        final Group options = gbuilder.withOption(targets).create();
        final Parser parser = new Parser();
        parser.setGroup(options);
        try {
            parser.parse(new String[] { "testfile.txt", "testfile.txt", "testfile.txt", "testfile.txt" });
            fail("OptionException");
        } catch (OptionException e) {
            assertEquals("Unexpected testfile.txt while processing ", e.getMessage());
        }
    }

// org.apache.commons.cli2.commandline.DefaultingCommandLineTest::testTriggers
    public void testTriggers() {
        final DefaultingCommandLine defaults = new DefaultingCommandLine();
        defaults.appendCommandLine(first);
        defaults.appendCommandLine(second);

        Set set = defaults.getOptionTriggers();
        Iterator iter = set.iterator();
        assertEquals("wrong # of triggers", 3, set.size());
        assertTrue("cannot find trigger", set.contains("--insecond"));
        assertTrue("cannot find trigger", set.contains("--inboth"));
        assertTrue("cannot find trigger", set.contains("--infirst"));
    }

// org.apache.commons.cli2.commandline.DefaultingCommandLineTest::testDefaults
    public void testDefaults() {
        final DefaultingCommandLine defaults = new DefaultingCommandLine();

        assertEquals("wrong # of defaults", 0, defaults.getValues("--insecond").size());
        assertEquals("wrong Set of defaults", Collections.EMPTY_LIST, defaults.getValues("--insecond", null));
    }

// org.apache.commons.cli2.commandline.ParserTest::testParse_Successful
    public void testParse_Successful() throws OptionException {
        final CommandLine cl = parser.parse(new String[]{"-hv"});
        
        assertTrue(cl.hasOption(helpOption));
        assertTrue(cl.hasOption(verboseOption));
        
        assertEquals("--help --verbose",cl.toString());
        
        final WriteableCommandLineImpl wcli = (WriteableCommandLineImpl)cl;
        assertEquals("[--help, --verbose]",wcli.getNormalised().toString());
    }

// org.apache.commons.cli2.commandline.ParserTest::testParse_WithUnexpectedOption
    public void testParse_WithUnexpectedOption() {
        try {
            parser.parse(new String[]{"--unexpected"});
            fail("OptionException");
        }
        catch(OptionException e) {
            assertEquals(options,e.getOption());
            assertEquals("Unexpected --unexpected while processing --help|--verbose",e.getMessage());
        }
    }

// org.apache.commons.cli2.commandline.ParserTest::testParseAndHelp_Successful
    public void testParseAndHelp_Successful() throws IOException {
        final CommandLine cl = parser.parseAndHelp(new String[]{"-v"});
        
        assertTrue(cl.hasOption(verboseOption));
        assertEquals("",out.getBuffer().toString());
    }

// org.apache.commons.cli2.commandline.ParserTest::testParseAndHelp_ByHelpOption
    public void testParseAndHelp_ByHelpOption() throws IOException {
        parser.setHelpOption(helpOption);
        
        assertNull(parser.parseAndHelp(new String[]{"-hv"}));
        
        inReader();
        assertInReaderUsage();
        assertInReaderEOF();
    }

// org.apache.commons.cli2.commandline.ParserTest::testParseAndHelp_ByHelpTrigger
    public void testParseAndHelp_ByHelpTrigger() throws IOException {
        parser.setHelpTrigger("--help");
        
        assertNull(parser.parseAndHelp(new String[]{"-hv"}));
        
        inReader();
        assertInReaderUsage();
        assertInReaderEOF();
    }

// org.apache.commons.cli2.commandline.ParserTest::testParseAndHelp_WithUnexpectedOption
    public void testParseAndHelp_WithUnexpectedOption() throws IOException {
        assertNull(parser.parseAndHelp(new String[]{"--unexpected"}));
        
        inReader();
        assertInReaderLine("Unexpected --unexpected while processing --help|--verbose");
        assertInReaderUsage();
        assertInReaderEOF();
    }

// org.apache.commons.cli2.commandline.PreferencesCommandLineTest::testPropertyValues
    public void testPropertyValues() {
        
    	CommandLine cmdline = createCommandLine();

    	assertEquals("wrong value", "present value", cmdline.getValue("--present"));
    	assertEquals("wrong value", "present value", cmdline.getValue("--alsopresent"));
    	assertEquals("wrong # of values", 3, cmdline.getValues("--multiple").size());
    	assertEquals("wrong value 1", "value 1", cmdline.getValues("--multiple").get(0));
    	assertEquals("wrong value 2", "value 2", cmdline.getValues("--multiple").get(1));
    	assertEquals("wrong value 3", "value 3", cmdline.getValues("--multiple").get(2));
    }

// org.apache.commons.cli2.commandline.PreferencesCommandLineTest::testNoSeparator
    public void testNoSeparator() {
        
    	CommandLine cmdline = createCommandLineNoSep();

    	assertEquals("wrong value", "present value", cmdline.getValue("--present"));
    	assertEquals("wrong value", "present value", cmdline.getValue("--alsopresent"));
    	assertEquals("wrong # of values", 1, cmdline.getValues("--multiple").size());
    	assertEquals("wrong value", "value 1|value 2|value 3", cmdline.getValue("--multiple"));
    	assertFalse("expected a false", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.PreferencesCommandLineTest::testNullOption
    public void testNullOption() {
        
    	CommandLine cmdline = createCommandLine();

    	assertFalse("should not find null option", cmdline.hasOption((String) null));
    	assertTrue("expected a true", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.PreferencesCommandLineTest::testPreferenceTriggers
    public void testPreferenceTriggers() {
        
    	CommandLine cmdline = createCommandLine();

    	Set triggers = cmdline.getOptionTriggers();
        Iterator iter = triggers.iterator();
        assertEquals("wrong # of triggers", 4, triggers.size());
        assertTrue("cannot find trigger", triggers.contains("--bool"));
        assertTrue("cannot find trigger", triggers.contains("--present"));
        assertTrue("cannot find trigger", triggers.contains("--multiple"));
        assertTrue("cannot find trigger", triggers.contains("--alsopresent"));

    	assertFalse("should not find null option", cmdline.hasOption((String) null));
    	assertTrue("expected a true", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.PropertiesCommandLineTest::testPropertyValues
    public void testPropertyValues() {
        
    	CommandLine cmdline = createCommandLine();

    	assertEquals("wrong value", "present value", cmdline.getValue("--present"));
    	assertEquals("wrong value", "present value", cmdline.getValue("--alsopresent"));
    	assertEquals("wrong # of values", 3, cmdline.getValues("--multiple").size());
    	assertEquals("wrong value 1", "value 1", cmdline.getValues("--multiple").get(0));
    	assertEquals("wrong value 2", "value 2", cmdline.getValues("--multiple").get(1));
    	assertEquals("wrong value 3", "value 3", cmdline.getValues("--multiple").get(2));
    }

// org.apache.commons.cli2.commandline.PropertiesCommandLineTest::testNoSeparator
    public void testNoSeparator() {
        
    	CommandLine cmdline = createCommandLineNoSep();

    	assertEquals("wrong value", "present value", cmdline.getValue("--present"));
    	assertEquals("wrong value", "present value", cmdline.getValue("--alsopresent"));
    	assertEquals("wrong # of values", 1, cmdline.getValues("--multiple").size());
    	assertEquals("wrong value", "value 1|value 2|value 3", cmdline.getValue("--multiple"));
    	assertFalse("expected a false", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.PropertiesCommandLineTest::testNullOption
    public void testNullOption() {
        
    	CommandLine cmdline = createCommandLine();

    	assertFalse("should not find null option", cmdline.hasOption((String) null));
    	assertTrue("expected a true", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.PropertiesCommandLineTest::testPropertyTriggers
    public void testPropertyTriggers() {
        
    	CommandLine cmdline = createCommandLine();

    	Set triggers = cmdline.getOptionTriggers();
        Iterator iter = triggers.iterator();
        assertEquals("wrong # of triggers", 4, triggers.size());
        assertTrue("cannot find trigger", triggers.contains("--bool"));
        assertTrue("cannot find trigger", triggers.contains("--present"));
        assertTrue("cannot find trigger", triggers.contains("--multiple"));
        assertTrue("cannot find trigger", triggers.contains("--alsopresent"));

    	assertFalse("should not find null option", cmdline.hasOption((String) null));
    	assertTrue("expected a true", cmdline.getSwitch("--bool").booleanValue());
    }

// org.apache.commons.cli2.commandline.WriteableCommandLineImplTest::testToMakeEclipseSpotTheTestCase
    public void testToMakeEclipseSpotTheTestCase() {
        
    }

// org.apache.commons.cli2.option.ArgumentTest::testNew
    public void testNew() {
        try {
            new ArgumentImpl("limit", "the last acceptable date", 10, 5, '=', '\0',
                             new DateValidator(DateValidatorTest.YYYY_MM_DD), null, null, 0);
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage("Argument.minimum.exceeds.maximum"), e.getMessage());
        }

        {
            ArgumentImpl arg =
                new ArgumentImpl(null, "the last acceptable date", 5, 5, '=', '\0',
                                 new DateValidator(DateValidatorTest.YYYY_MM_DD), null, null, 0);
            assertEquals("wrong arg name", "arg", arg.getPreferredName());
        }

        {
            List defaults = new ArrayList();

            try {
                new ArgumentImpl(null, "the last acceptable date", 1, 1, '=', '\0',
                                 new DateValidator(DateValidatorTest.YYYY_MM_DD), null, defaults, 0);
            } catch (IllegalArgumentException exp) {
                assertEquals(resources.getMessage("Argument.too.few.defaults"), exp.getMessage());
            }
        }

        try {
            List defaults = new ArrayList();
            defaults.add("1");
            defaults.add("2");

            new ArgumentImpl(null, "the last acceptable date", 1, 1, '=', '\0',
                             new DateValidator(DateValidatorTest.YYYY_MM_DD), null, defaults, 0);
        } catch (IllegalArgumentException exp) {
            assertEquals(resources.getMessage("Argument.too.many.defaults"), exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues
    public void testProcessValues()
        throws OptionException {
        final Argument option = buildUsernameArgument();
        final List args = list("rob");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processValues(commandLine, iterator, option);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("username"));
        assertEquals("rob", commandLine.getValue(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_BoundaryQuotes
    public void testProcessValues_BoundaryQuotes()
        throws OptionException {
        final Argument option = buildUsernameArgument();
        final List args = list("\"rob\"");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processValues(commandLine, iterator, option);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("username"));
        assertEquals("rob", commandLine.getValue(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_SpareValues
    public void testProcessValues_SpareValues()
        throws OptionException {
        final Argument option = buildUsernameArgument();
        final List args = list("rob", "secret");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processValues(commandLine, iterator, option);

        assertTrue(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("username"));
        assertEquals("rob", commandLine.getValue(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_Optional
    public void testProcessValues_Optional() {
        final Argument option = buildTargetsArgument();
        final List args = list();
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.processValues(commandLine, iterator, option);
        } catch (final OptionException mve) {
            assertEquals(option, mve.getOption());
            assertEquals("Missing value(s) target [target ...]", mve.getMessage());
        }

        assertFalse(iterator.hasNext());
        assertFalse(commandLine.hasOption(option));
        assertFalse(commandLine.hasOption("username"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_Multiple
    public void testProcessValues_Multiple()
        throws OptionException {
        final Argument option = buildTargetsArgument();
        final List args = list("compile", "test", "docs");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processValues(commandLine, iterator, option);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("target"));
        assertFalse(commandLine.getValues(option).isEmpty());
        assertListContentsEqual(args, commandLine.getValues(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_Contracted
    public void testProcessValues_Contracted()
        throws OptionException {
        final Argument option = buildTargetsArgument();
        final List args = list("compile,test,javadoc", "checkstyle,jdepend");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processValues(commandLine, iterator, option);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("target"));
        assertListContentsEqual(list("compile", "test", "javadoc", "checkstyle", "jdepend"),
                                commandLine.getValues(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_ContractedTooFew
    public void testProcessValues_ContractedTooFew() {
        final Argument option = buildHostArgument();
        final List args = list("box1");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.processValues(commandLine, iterator, option);
            option.validate(commandLine);
            fail("Expected MissingValueException");
        } catch (OptionException mve) {
            assertSame(option, mve.getOption());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcessValues_ContractedTooMany
    public void testProcessValues_ContractedTooMany() {
        final Argument option = buildHostArgument();
        final List args = list("box1,box2,box3,box4");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.processValues(commandLine, iterator, option);
            option.validate(commandLine);
            fail("Expected MissingValueException");
        } catch (OptionException mve) {
            assertSame(option, mve.getOption());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testCanProcess
    public void testCanProcess() {
        final Argument option = buildTargetsArgument();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "any value"));
    }

// org.apache.commons.cli2.option.ArgumentTest::testPrefixes
    public void testPrefixes() {
        final Argument option = buildTargetsArgument();
        assertTrue(option.getPrefixes().isEmpty());
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess
    public void testProcess()
        throws OptionException {
        final Argument option = buildPathArgument();
        final List args = list("-path=/lib;/usr/lib;/usr/local/lib");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("path"));
        assertListContentsEqual(list("-path=/lib", "/usr/lib", "/usr/local/lib"),
                                commandLine.getValues(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testTriggers
    public void testTriggers() {
        final Argument option = buildTargetsArgument();
        assertTrue(option.getTriggers().isEmpty());
    }

// org.apache.commons.cli2.option.ArgumentTest::testValidate
    public void testValidate()
        throws OptionException {
        final Argument option = buildUsernameArgument();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addValue(option, "rob");

        option.validate(commandLine);
    }

// org.apache.commons.cli2.option.ArgumentTest::testValidate_Minimum
    public void testValidate_Minimum() {
        final Argument option = buildUsernameArgument();
        final WriteableCommandLine commandLine = commandLine(option, list());

        try {
            option.validate(commandLine);
            fail("UnexpectedValue");
        } catch (OptionException mve) {
            assertEquals(option, mve.getOption());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testRequired
    public void testRequired() {
        {
            final Argument arg = buildBoundsArgument();

            assertTrue("not required", arg.isRequired());
        }

        {
            final Argument arg = buildTargetsArgument();

            assertFalse("should not be required", arg.isRequired());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testValidate_Maximum
    public void testValidate_Maximum() {
        final Argument option = buildUsernameArgument();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addValue(option, "rob");
        commandLine.addValue(option, "oxspring");

        try {
            option.validate(commandLine);
            fail("UnexpectedValue");
        } catch (OptionException uve) {
            assertEquals(option, uve.getOption());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testValidate_Validator
    public void testValidate_Validator()
        throws OptionException, ParseException {
        final Argument option = buildDateLimitArgument();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addValue(option, "2004-01-01");

        option.validate(commandLine, option);
        assertContentsEqual(Arrays.asList(new Object[] {
                                              DateValidatorTest.YYYY_MM_DD.parse("2004-01-01")
                                          }), commandLine.getValues(option));
    }

// org.apache.commons.cli2.option.ArgumentTest::testValidate_ValidatorInvalidDate
    public void testValidate_ValidatorInvalidDate()
        throws OptionException, ParseException {
        final Argument option = buildDateLimitArgument();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addValue(option, "12-12-2004");

        try {
            option.validate(commandLine, option);
        } catch (OptionException exp) {
            OptionException e =
                new OptionException(option, ResourceConstants.ARGUMENT_UNEXPECTED_VALUE,
                                    "12-12-2004");
            assertEquals("wrong exception message", e.getMessage(), exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildUsernameArgument();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("<username>", buffer.toString());
    }

// org.apache.commons.cli2.option.ArgumentTest::testAppendUsage_Infinite
    public void testAppendUsage_Infinite() {
        final Option option = buildTargetsArgument();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("[<target1> [<target2> ...]]", buffer.toString());
    }

// org.apache.commons.cli2.option.ArgumentTest::testAppendUsage_InfiniteNoOptional
    public void testAppendUsage_InfiniteNoOptional() {
        final Option option = buildTargetsArgument();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_OPTIONAL);
        option.appendUsage(buffer, settings, null);

        assertEquals("<target1> [<target2> ...]", buffer.toString());
    }

// org.apache.commons.cli2.option.ArgumentTest::testAppendUsage_InfiniteNoNumbering
    public void testAppendUsage_InfiniteNoNumbering() {
        final Option option = buildTargetsArgument();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        option.appendUsage(buffer, settings, null);

        assertEquals("[<target> [<target> ...]]", buffer.toString());
    }

// org.apache.commons.cli2.option.ArgumentTest::testAppendUsage_Minimum
    public void testAppendUsage_Minimum() {
        final Option option = buildHostArgument();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("<host1> <host2> [<host3>]", buffer.toString());
    }

// org.apache.commons.cli2.option.ArgumentTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildPathArgument();
        assertEquals("path", option.getPreferredName());
    }

// org.apache.commons.cli2.option.ArgumentTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildHostArgument();
        assertEquals("The host name", option.getDescription());
    }

// org.apache.commons.cli2.option.ArgumentTest::testHelpLines
    public void testHelpLines() {
        final Option option = buildHostArgument();
        final List lines = option.helpLines(0, DisplaySetting.ALL, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.ArgumentTest::testCanProcess_ConsumeRemaining
    public void testCanProcess_ConsumeRemaining() {
        final Option option = buildUsernameArgument();

        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "--"));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess_ConsumeRemaining
    public void testProcess_ConsumeRemaining()
        throws OptionException {
        final Option option = buildPathArgument();
        final List args = list("options", "--", "--ignored", "-Dprop=val");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        option.process(commandLine, iterator);

        final List values = commandLine.getValues(option);
        assertTrue(values.contains("options"));
        assertTrue(values.contains("--ignored"));
        assertTrue(values.contains("-Dprop=val"));
        assertEquals(3, values.size());
        assertFalse(iterator.hasNext());
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess_ConsumeNothing
    public void testProcess_ConsumeNothing() {
        final Option option = buildPathArgument();
        final List args = list("--");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.process(commandLine, iterator);
            option.validate(commandLine);
            fail("Missing Value!");
        } catch (OptionException mve) {
            assertEquals(option, mve.getOption());
            assertEquals("Missing value(s) path [path ...]", mve.getMessage());
        }

        assertTrue(commandLine.getValues(option).isEmpty());
        assertFalse(iterator.hasNext());
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess_InterrogatedDefaultValue
    public void testProcess_InterrogatedDefaultValue()
        throws OptionException {
        final Option size = buildSizeArgument();
        final List args = list();
        final WriteableCommandLine commandLine = commandLine(size, args);
        final ListIterator iterator = args.listIterator();

        size.process(commandLine, iterator);

        assertEquals(new Integer(20), commandLine.getValue(size, new Integer(20)));
    }

// org.apache.commons.cli2.option.ArgumentTest::testTooFewDefaults
    public void testTooFewDefaults() {
        List defaults = new ArrayList();
        defaults.add("5");

        try {
            new ArgumentImpl("size", "The number of units", 2, 2, '\0', '\0', null,
                             ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.ARGUMENT_TOO_FEW_DEFAULTS),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testTooManyDefaults
    public void testTooManyDefaults() {
        List defaults = new ArrayList();
        defaults.add("5");
        defaults.add("10");
        defaults.add("15");

        try {
            new ArgumentImpl("size", "The number of units", 2, 2, '\0', '\0', null,
                             ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.ARGUMENT_TOO_MANY_DEFAULTS),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess_InterrogatedDefaultValues
    public void testProcess_InterrogatedDefaultValues()
        throws OptionException {
        final Option bounds = buildBoundsArgument();
        final List args = list();
        final WriteableCommandLine commandLine = commandLine(bounds, args);
        final ListIterator iterator = args.listIterator();

        bounds.process(commandLine, iterator);

        
        List values = new ArrayList();
        values.add("50");
        values.add("100");
        assertEquals(values, commandLine.getValues(bounds, values));

        
        assertEquals(Collections.EMPTY_LIST, commandLine.getValues(bounds, null));
    }

// org.apache.commons.cli2.option.ArgumentTest::testProcess_StripBoundaryQuotes
    public void testProcess_StripBoundaryQuotes()
        throws OptionException {
        final Option bounds = buildBoundsArgument();
        final List args = list();
        final WriteableCommandLine commandLine = commandLine(bounds, args);
        final ListIterator iterator = args.listIterator();

        bounds.process(commandLine, iterator);

        List values = new ArrayList();
        values.add("50\"");
        values.add("\"100");
        assertEquals(values, commandLine.getValues(bounds, values));
    }

// org.apache.commons.cli2.option.ArgumentTest::testSourceDestArgument
    public void testSourceDestArgument() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Argument inputfiles =
            abuilder.withName("input").withMinimum(0).withMaximum(0).create();
        final Argument bad_outputfile =
            abuilder.withName("output").withMinimum(1).withMaximum(2).create();

        try {
            final Argument targets = new SourceDestArgument(inputfiles, bad_outputfile);
        } catch (final IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SOURCE_DEST_MUST_ENFORCE_VALUES),
                         exp.getMessage());
        }

        final Argument outputfile =
            abuilder.withName("output").withMinimum(1).withMaximum(1).create();

        final Argument targets = new SourceDestArgument(inputfiles, outputfile);
        final StringBuffer buffer = new StringBuffer("test content");
        targets.appendUsage(buffer, Collections.EMPTY_SET, null);

        assertTrue("buffer not added", buffer.toString().startsWith("test content"));
        assertFalse("space added", buffer.charAt(12) == ' ');
    }

// org.apache.commons.cli2.option.CommandTest::testProcessParent
    public void testProcessParent()
        throws OptionException {
        final Command option = buildStartCommand();
        final List args = list("go");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("start"));
        assertTrue(commandLine.hasOption("go"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.CommandTest::testProcessParent_Spare
    public void testProcessParent_Spare()
        throws OptionException {
        final Command option = buildLoginCommand();
        final List args = list("login", "rob");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertEquals("rob", iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("login"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.CommandTest::testCanProcess
    public void testCanProcess() {
        final Command option = buildStartCommand();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "start"));
    }

// org.apache.commons.cli2.option.CommandTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final Command option = buildStartCommand();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option, null), "stop"));
    }

// org.apache.commons.cli2.option.CommandTest::testCanProcess_Alias
    public void testCanProcess_Alias() {
        final Command option = buildStartCommand();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "go"));
    }

// org.apache.commons.cli2.option.CommandTest::testPrefixes
    public void testPrefixes() {
        final Command option = buildStartCommand();
        assertTrue(option.getPrefixes().isEmpty());
    }

// org.apache.commons.cli2.option.CommandTest::testProcess
    public void testProcess()
        throws OptionException {
        final Command option = buildLoginCommand();
        final List args = list("login", "rob");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("login"));
        assertEquals("rob", commandLine.getValue(option));
    }

// org.apache.commons.cli2.option.CommandTest::testTriggers
    public void testTriggers() {
        final Command option = buildStartCommand();
        final Set triggers = option.getTriggers();
        assertContentsEqual(list("start", "go"), triggers);
    }

// org.apache.commons.cli2.option.CommandTest::testValidate
    public void testValidate() {
        final Parent option = buildCommitCommand();
        final WriteableCommandLine commandLine = commandLine(option, list());

        try {
            option.validate(commandLine);
            fail("Missing an option");
        } catch (OptionException moe) {
            assertSame(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.CommandTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildStartCommand();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("[start (go)]", buffer.toString());
    }

// org.apache.commons.cli2.option.CommandTest::testNullPreferredName
    public void testNullPreferredName() {
        try {
            new Command(null, "", Collections.singleton("go"), false, null, null, 0);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception name",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.COMMAND_PREFERRED_NAME_TOO_SHORT),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.CommandTest::testEmotyPreferredName
    public void testEmotyPreferredName() {
        try {
            new Command("", "", Collections.singleton("go"), false, null, null, 0);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception name",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.COMMAND_PREFERRED_NAME_TOO_SHORT),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.CommandTest::testAppendUsage_NoOptional
    public void testAppendUsage_NoOptional() {
        final Option option = buildStartCommand();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_OPTIONAL);
        option.appendUsage(buffer, settings, null);

        assertEquals("start (go)", buffer.toString());
    }

// org.apache.commons.cli2.option.CommandTest::testAppendUsage_NoAlias
    public void testAppendUsage_NoAlias() {
        final Option option = buildStartCommand();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_ALIASES);
        option.appendUsage(buffer, settings, null);

        assertEquals("[start]", buffer.toString());
    }

// org.apache.commons.cli2.option.CommandTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildStartCommand();
        assertEquals("start", option.getPreferredName());
    }

// org.apache.commons.cli2.option.CommandTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildLoginCommand();
        assertEquals("Initiates a session for the user", option.getDescription());
    }

// org.apache.commons.cli2.option.CommandTest::testHelpLines
    public void testHelpLines() {
        
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testProcessParent
    public void testProcessParent() throws OptionException {
        final DefaultOption option = buildHelpOption();
        final List args = list("--help");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("--help"));
        assertTrue(commandLine.hasOption("-?"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testProcessParent_Burst
    public void testProcessParent_Burst() throws OptionException {
        final DefaultOption option = buildHelpOption();
        final List args = list("-help");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertEquals("-elp", iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("--help"));
        assertTrue(commandLine.hasOption("-?"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testCanProcess
    public void testCanProcess() {
        final DefaultOption option = buildHelpOption();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option,null), "-?"));
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final DefaultOption option = buildHelpOption();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option,null), "-H"));
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testPrefixes
    public void testPrefixes() {
        final DefaultOption option = buildHelpOption();
        assertContentsEqual(list("-", "--"), option.getPrefixes());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testProcess
    public void testProcess() {
        

    }

// org.apache.commons.cli2.option.DefaultOptionTest::testTriggers
    public void testTriggers() {
        final DefaultOption option = buildHelpOption();
        assertContentsEqual(list("-?", "-h", "--help"), option.getTriggers());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testValidate
    public void testValidate() {
        final Parent option = buildXOption();
        final WriteableCommandLine commandLine = commandLine(option, list());

        try {
            option.validate(commandLine);
            fail("Missing an option");
        }
        catch (OptionException moe) {
            assertSame(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildHelpOption();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("[--help (-?,-h)]", buffer.toString());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testAppendUsage_NoOptional
    public void testAppendUsage_NoOptional() {
        final Option option = buildHelpOption();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_OPTIONAL);
        option.appendUsage(buffer, settings, null);

        assertEquals("--help (-?,-h)", buffer.toString());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testAppendUsage_NoAlias
    public void testAppendUsage_NoAlias() {
        final Option option = buildHelpOption();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_ALIASES);
        option.appendUsage(buffer, settings, null);

        assertEquals("[--help]", buffer.toString());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildHelpOption();
        assertEquals("--help", option.getPreferredName());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildHelpOption();
        assertEquals("Displays the help", option.getDescription());
    }

// org.apache.commons.cli2.option.DefaultOptionTest::testHelpLines
    public void testHelpLines() {
        
    }

// org.apache.commons.cli2.option.GroupTest::testProcessAnonymousArguments
    public void testProcessAnonymousArguments()
        throws OptionException {
        final Group option = buildAntGroup();
        final List args = list("compile,test", "dist");
        final ListIterator iterator = args.listIterator();
        final WriteableCommandLine commandLine = commandLine(option, args);
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption("target"));
        assertListContentsEqual(commandLine.getValues("target"), args);
        assertListContentsEqual(list("compile", "test", "dist"), args);
    }

// org.apache.commons.cli2.option.GroupTest::testProcessOptions
    public void testProcessOptions()
        throws OptionException {
        final Group option = buildApachectlGroup();
        final List args = list("-?", "-k");
        final ListIterator iterator = args.listIterator();
        final WriteableCommandLine commandLine = commandLine(option, args);
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption("--help"));
        assertTrue(commandLine.hasOption("-k"));
        assertFalse(commandLine.hasOption("start"));
        assertListContentsEqual(list("--help", "-k"), args);
    }

// org.apache.commons.cli2.option.GroupTest::testCanProcess
    public void testCanProcess() {
        final Group option = buildApacheCommandGroup();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "start"));
    }

// org.apache.commons.cli2.option.GroupTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final Group option = buildApacheCommandGroup();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option, null), "begin"));
    }

// org.apache.commons.cli2.option.GroupTest::testCanProcess_NullMatch
    public void testCanProcess_NullMatch() {
        final Group option = buildApacheCommandGroup();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option, null), (String) null));
    }

// org.apache.commons.cli2.option.GroupTest::testPrefixes
    public void testPrefixes() {
        final Group option = buildApachectlGroup();
        assertContentsEqual(list("-", "--"), option.getPrefixes());
    }

// org.apache.commons.cli2.option.GroupTest::testProcess
    public void testProcess()
        throws OptionException {
        final Group option = buildAntGroup();
        final List args = list("--help", "compile,test", "dist");
        final ListIterator iterator = args.listIterator();
        final WriteableCommandLine commandLine = commandLine(option, args);
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption("-?"));
        assertListContentsEqual(list("compile", "test", "dist"), commandLine.getValues("target"));
    }

// org.apache.commons.cli2.option.GroupTest::testProcess_Nested
    public void testProcess_Nested()
        throws OptionException {
        final Group option = buildApachectlGroup();
        final List args = list("-h", "-k", "graceful");
        final ListIterator iterator = args.listIterator();
        final WriteableCommandLine commandLine = commandLine(option, args);
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption("-?"));
        assertTrue(commandLine.hasOption("-k"));
        assertTrue(commandLine.hasOption("graceful"));
        assertFalse(commandLine.hasOption("stop"));
        assertTrue(commandLine.getValues("start").isEmpty());
        assertListContentsEqual(list("--help", "-k", "graceful"), args);
    }

// org.apache.commons.cli2.option.GroupTest::testTriggers
    public void testTriggers() {
        final Group option = buildApachectlGroup();
        assertContentsEqual(list("--help", "-?", "-h", "-k"), option.getTriggers());
    }

// org.apache.commons.cli2.option.GroupTest::testValidate
    public void testValidate()
        throws OptionException {
        final Group option = buildApacheCommandGroup();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addOption(COMMAND_RESTART);

        option.validate(commandLine);
    }

// org.apache.commons.cli2.option.GroupTest::testValidate_UnexpectedOption
    public void testValidate_UnexpectedOption() {
        final Group option = buildApacheCommandGroup();
        final WriteableCommandLine commandLine = commandLine(option, list());

        commandLine.addOption(COMMAND_RESTART);
        commandLine.addOption(COMMAND_GRACEFUL);

        try {
            option.validate(commandLine);
            fail("Too many options");
        } catch (OptionException uoe) {
            assertEquals(option, uoe.getOption());
        }
    }

// org.apache.commons.cli2.option.GroupTest::testValidate_MissingOption
    public void testValidate_MissingOption() {
        final Group option = buildApacheCommandGroup();
        final WriteableCommandLine commandLine = commandLine(option, list());

        try {
            option.validate(commandLine);
            fail("Missing an option");
        } catch (OptionException moe) {
            assertEquals(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.GroupTest::testValidate_RequiredChild
    public void testValidate_RequiredChild()
        throws OptionException {
        final Option required =
            new DefaultOptionBuilder().withLongName("required").withRequired(true).create();
        final Option optional =
            new DefaultOptionBuilder().withLongName("optional").withRequired(false).create();
        final Group group =
            new GroupBuilder().withOption(required).withOption(optional).withMinimum(1).create();

        WriteableCommandLine commandLine;

        commandLine = commandLine(group, list());

        try {
            group.validate(commandLine);
            fail("Missing option 'required'");
        } catch (OptionException moe) {
            assertEquals(required, moe.getOption());
        }

        commandLine = commandLine(group, list());
        commandLine.addOption(optional);

        try {
            group.validate(commandLine);
            fail("Missing option 'required'");
        } catch (OptionException moe) {
            assertEquals(required, moe.getOption());
        }

        commandLine = commandLine(group, list());
        commandLine.addOption(required);
        group.validate(commandLine);
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildApacheCommandGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);

        
        option.appendUsage(buffer, settings, null);

        assertEquals("httpd-cmds (graceful|restart|start|stop)", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage_NoOptional
    public void testAppendUsage_NoOptional() {
        final Option option = buildApacheCommandGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_OPTIONAL);
        option.appendUsage(buffer, settings, null);

        assertEquals("httpd-cmds (graceful|restart|start|stop)", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage_NoExpand
    public void testAppendUsage_NoExpand() {
        final Option option = buildApacheCommandGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        option.appendUsage(buffer, settings, null);

        assertEquals("httpd-cmds", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage_NoExpandOrName
    public void testAppendUsage_NoExpandOrName() {
        final Option option = buildApacheCommandGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.remove(DisplaySetting.DISPLAY_GROUP_NAME);
        option.appendUsage(buffer, settings, null);

        assertEquals("httpd-cmds", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage_NoName
    public void testAppendUsage_NoName() {
        final Option option = buildApacheCommandGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_NAME);
        option.appendUsage(buffer, settings, null);

        assertEquals("graceful|restart|start|stop", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testAppendUsage_WithArgs
    public void testAppendUsage_WithArgs() {
        final Option option = buildAntGroup();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_OUTER);
        option.appendUsage(buffer, settings, null);

        assertEquals("[ant (--help (-?,-h)) [<target1> [<target2> ...]]]", buffer.toString());
    }

// org.apache.commons.cli2.option.GroupTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildAntGroup();
        assertEquals("ant", option.getPreferredName());
    }

// org.apache.commons.cli2.option.GroupTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildApachectlGroup();
        assertEquals("Controls the apache http deamon", option.getDescription());
    }

// org.apache.commons.cli2.option.GroupTest::testHelpLines
    public void testHelpLines() {
        final Option option = buildApacheCommandGroup();
        final List lines = option.helpLines(0, DisplaySetting.ALL, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        final HelpLine line2 = (HelpLine) i.next();
        assertEquals(1, line2.getIndent());
        assertEquals(COMMAND_GRACEFUL, line2.getOption());

        final HelpLine line3 = (HelpLine) i.next();
        assertEquals(1, line3.getIndent());
        assertEquals(COMMAND_RESTART, line3.getOption());

        final HelpLine line4 = (HelpLine) i.next();
        assertEquals(1, line4.getIndent());
        assertEquals(COMMAND_START, line4.getOption());

        final HelpLine line5 = (HelpLine) i.next();
        assertEquals(1, line5.getIndent());
        assertEquals(COMMAND_STOP, line5.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.GroupTest::testHelpLines_NoExpanded
    public void testHelpLines_NoExpanded() {
        final Option option = buildApacheCommandGroup();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final List lines = option.helpLines(0, settings, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.GroupTest::testHelpLines_NoName
    public void testHelpLines_NoName() {
        final Option option = buildApacheCommandGroup();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_NAME);

        final List lines = option.helpLines(0, settings, null);
        final Iterator i = lines.iterator();

        final HelpLine line2 = (HelpLine) i.next();
        assertEquals(1, line2.getIndent());
        assertEquals(COMMAND_GRACEFUL, line2.getOption());

        final HelpLine line3 = (HelpLine) i.next();
        assertEquals(1, line3.getIndent());
        assertEquals(COMMAND_RESTART, line3.getOption());

        final HelpLine line4 = (HelpLine) i.next();
        assertEquals(1, line4.getIndent());
        assertEquals(COMMAND_START, line4.getOption());

        final HelpLine line5 = (HelpLine) i.next();
        assertEquals(1, line5.getIndent());
        assertEquals(COMMAND_STOP, line5.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.NestedGroupTest::testNestedGroup
    public void testNestedGroup()
        throws OptionException {
        final String[] args = {
                "-eb",
                "--file",
                "/tmp/filename.txt"
            };

        Group[] nestedGroups = {
                buildActionGroup(),
                buildAlgorithmGroup(),
                buildInputGroup()
            };

        Parser parser = new Parser();
        parser.setGroup(buildEncryptionServiceGroup(nestedGroups));

        CommandLine commandLine = parser.parse(args);

        assertTrue("/tmp/filename.txt".equals(commandLine.getValue("-f")));
        assertTrue(commandLine.hasOption("-e"));
        assertTrue(commandLine.hasOption("-b"));
        assertFalse(commandLine.hasOption("-d"));
    }

// org.apache.commons.cli2.option.NestedGroupTest::testNestedGroupHelp
    public void testNestedGroupHelp() {
        Group[] nestedGroups = {
                buildActionGroup(),
                buildAlgorithmGroup(),
                buildInputGroup()
            };

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setGroup(buildEncryptionServiceGroup(nestedGroups));

        final StringWriter out = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(out));

        try {
            helpFormatter.print();

            final BufferedReader bufferedReader = new BufferedReader(new StringReader(
                        out.toString()));
            final String[] expected = new String[] {
                    "Usage:                                                                          ",
                    " [-h -k -e|-d -b|-3 -f <file>|-s <string>]                                      ",
                    "encryptionService                                                               ",
                    "  -h (--help)               Print this message                                  ",
                    "  -k (--key)                Encryption key                                      ",
                    "  Action                    Action                                              ",
                    "    -e (--encrypt)          Encrypt input                                       ",
                    "    -d (--decrypt)          Decrypt input                                       ",
                    "  Algorithm                 Encryption Algorithm                                ",
                    "    -b (--blowfish)         Blowfish                                            ",
                    "    -3 (--3DES)             Triple DES                                          ",
                    "  Input                     Input                                               ",
                    "    -f (--file) file        Input file                                          ",
                    "    -s (--string) string    Input string                                        "
                };

            List actual = new ArrayList(expected.length);
            String input;

            while ((input = bufferedReader.readLine()) != null) {
                actual.add(input);
            }

            
            assertEquals("Help text lines should be " + expected.length,
                actual.size(), expected.length);

            for (int i = 0; i < expected.length; i++) {
                if (!expected[i].equals(actual.get(i))) {
                    for (int x = 0; x < expected.length; i++) {
                        System.out.println("   " + expected[i]);
                        System.out.println((expected[i].equals(actual.get(i))
                            ? "== "
                            : "!= ") + actual.get(i));
                    }
                }

                assertEquals(expected[i], actual.get(i));
            }
        }
        catch (IOException e) {
            fail(e.getLocalizedMessage());
        }
    }

// org.apache.commons.cli2.option.ParentTest::testProcessParent
    public void testProcessParent()
        throws OptionException {
        final Parent option = buildKParent();
        final List args = list("-k", "start");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertEquals("start", iterator.next());
        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("-k"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.ParentTest::testCanProcess
    public void testCanProcess() {
        final Parent option = buildKParent();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "-k"));
    }

// org.apache.commons.cli2.option.ParentTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final Parent option = buildKParent();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option, null), "-K"));
    }

// org.apache.commons.cli2.option.ParentTest::testCanProcess_ContractedArgument
    public void testCanProcess_ContractedArgument() {
        final Parent option = buildLibParent();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "--lib=/usr/lib"));
    }

// org.apache.commons.cli2.option.ParentTest::testPrefixes
    public void testPrefixes() {
        final Parent option = buildKParent();
        assertContentsEqual(list("-", "--"), option.getPrefixes());
    }

// org.apache.commons.cli2.option.ParentTest::testProcess
    public void testProcess()
        throws OptionException {
        final Parent option = CommandTest.buildStartCommand();
        final List args = list("start");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("start"));
        assertFalse(commandLine.hasOption("stop"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.ParentTest::testProcess_NoMatch
    public void testProcess_NoMatch()
        throws OptionException {
        final Parent option = CommandTest.buildStartCommand();
        final List args = list("whatever");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.process(commandLine, iterator);
            fail("unexpected token not thrown");
        } catch (OptionException exp) {
            OptionException e =
                new OptionException(option, ResourceConstants.UNEXPECTED_TOKEN, "whatever");
            assertEquals("wrong exception message", e.getMessage(), exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ParentTest::testProcess_Children
    public void testProcess_Children()
        throws OptionException {
        final Parent option = buildKParent();
        final List args = list("-k", "start");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertNull(option.findOption("whatever"));
        assertNotNull(option.findOption("start"));

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("-k"));
        assertTrue(commandLine.hasOption("start"));
        assertFalse(commandLine.hasOption("stop"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.ParentTest::testProcess_Argument
    public void testProcess_Argument()
        throws OptionException {
        final Parent option = buildLibParent();
        final List args = list("--lib=C:\\WINDOWS;C:\\WINNT;C:\\");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("--lib"));
        assertContentsEqual(list("C:\\WINDOWS", "C:\\WINNT", "C:\\"), commandLine.getValues(option));
    }

// org.apache.commons.cli2.option.ParentTest::testTriggers
    public void testTriggers() {
        final Parent option = buildKParent();
        assertContentsEqual(list("-k"), option.getTriggers());
    }

// org.apache.commons.cli2.option.ParentTest::testValidate
    public void testValidate()
        throws OptionException {
        final Parent option = CommandTest.buildStartCommand();
        final WriteableCommandLine commandLine = commandLine(option, list());

        option.validate(commandLine);

        commandLine.addOption(option);

        option.validate(commandLine);
    }

// org.apache.commons.cli2.option.ParentTest::testValidate_Children
    public void testValidate_Children()
        throws OptionException {
        final Parent option = buildKParent();
        final WriteableCommandLine commandLine = commandLine(option, list());

        option.validate(commandLine);
        commandLine.addOption(option);

        try {
            option.validate(commandLine);
            fail("Missing a command");
        } catch (OptionException moe) {
            assertNotNull(moe.getOption());
            assertNotSame(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.ParentTest::testValidate_Argument
    public void testValidate_Argument()
        throws OptionException {
        final Command option = CommandTest.buildLoginCommand();
        final WriteableCommandLine commandLine = commandLine(option, list());

        option.validate(commandLine);

        commandLine.addOption(option);

        try {
            option.validate(commandLine);
            fail("Missing a value");
        } catch (OptionException moe) {
            assertSame(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.ParentTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildComplexParent();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_GROUP_OUTER);
        option.appendUsage(buffer, settings, null);

        assertEquals("[login (l,lo) <username> [login-opts (--basic (-b)|--digest (-d)|--ssl (-s))]]",
                     buffer.toString());
    }

// org.apache.commons.cli2.option.ParentTest::testAppendUsage_NoArguments
    public void testAppendUsage_NoArguments() {
        final Option option = buildComplexParent();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        settings.remove(DisplaySetting.DISPLAY_GROUP_OUTER);
        option.appendUsage(buffer, settings, null);

        assertEquals("[login (l,lo) [login-opts (--basic (-b)|--digest (-d)|--ssl (-s))]]",
                     buffer.toString());
    }

// org.apache.commons.cli2.option.ParentTest::testAppendUsage_NoChildren
    public void testAppendUsage_NoChildren() {
        final Option option = buildComplexParent();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        option.appendUsage(buffer, settings, null);

        assertEquals("[login (l,lo) <username>]", buffer.toString());
    }

// org.apache.commons.cli2.option.ParentTest::testAppendUsage_NoArgumentsOrChildren
    public void testAppendUsage_NoArgumentsOrChildren() {
        final Option option = buildComplexParent();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        settings.remove(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        option.appendUsage(buffer, settings, null);

        assertEquals("[login (l,lo)]", buffer.toString());
    }

// org.apache.commons.cli2.option.ParentTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildLibParent();
        assertEquals("--lib", option.getPreferredName());
    }

// org.apache.commons.cli2.option.ParentTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildLibParent();
        assertEquals("Specifies library search path", option.getDescription());
    }

// org.apache.commons.cli2.option.ParentTest::testHelpLines
    public void testHelpLines() {
        final Option option = buildComplexParent();
        final List lines = option.helpLines(0, DisplaySetting.ALL, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        final HelpLine line2 = (HelpLine) i.next();
        assertEquals(1, line2.getIndent());
        assertEquals(COMPLEX_ARGUMENT, line2.getOption());

        final HelpLine line3 = (HelpLine) i.next();
        assertEquals(1, line3.getIndent());
        assertEquals(COMPLEX_CHILDREN, line3.getOption());

        final HelpLine line4 = (HelpLine) i.next();
        assertEquals(2, line4.getIndent());
        assertEquals(COMPLEX_CHILD_BASIC, line4.getOption());

        final HelpLine line5 = (HelpLine) i.next();
        assertEquals(2, line5.getIndent());
        assertEquals(COMPLEX_CHILD_DIGEST, line5.getOption());

        final HelpLine line6 = (HelpLine) i.next();
        assertEquals(2, line6.getIndent());
        assertEquals(COMPLEX_CHILD_SSL, line6.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.ParentTest::testHelpLines_NoArgument
    public void testHelpLines_NoArgument() {
        final Option option = buildComplexParent();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PARENT_ARGUMENT);

        final List lines = option.helpLines(0, settings, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        final HelpLine line3 = (HelpLine) i.next();
        assertEquals(1, line3.getIndent());
        assertEquals(COMPLEX_CHILDREN, line3.getOption());

        final HelpLine line4 = (HelpLine) i.next();
        assertEquals(2, line4.getIndent());
        assertEquals(COMPLEX_CHILD_BASIC, line4.getOption());

        final HelpLine line5 = (HelpLine) i.next();
        assertEquals(2, line5.getIndent());
        assertEquals(COMPLEX_CHILD_DIGEST, line5.getOption());

        final HelpLine line6 = (HelpLine) i.next();
        assertEquals(2, line6.getIndent());
        assertEquals(COMPLEX_CHILD_SSL, line6.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.ParentTest::testHelpLines_NoChildren
    public void testHelpLines_NoChildren() {
        final Option option = buildComplexParent();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PARENT_CHILDREN);

        final List lines = option.helpLines(0, settings, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine) i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        final HelpLine line2 = (HelpLine) i.next();
        assertEquals(1, line2.getIndent());
        assertEquals(COMPLEX_ARGUMENT, line2.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.ParentTest::testNullPreferredName
    public void testNullPreferredName() {
        try {
        	new CommandBuilder().create();
        } catch (IllegalStateException exp) {
        	assertEquals(ResourceHelper.getResourceHelper().getMessage(ResourceConstants.OPTION_NO_NAME), exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.ParentTest::testRequired
    public void testRequired() {
    	Command cmd = new CommandBuilder().withRequired(true).withName("blah").create();
    	assertTrue("cmd is not required", cmd.isRequired());
    	assertEquals("id is incorrect", 0, cmd.getId());
    }

// org.apache.commons.cli2.option.ParentTest::testID
    public void testID() {
    	Command cmd = new CommandBuilder().withId('c').withName("blah").create();
    	assertEquals("id is incorrect", 'c', cmd.getId());
    }

// org.apache.commons.cli2.option.ParentTest::testGetId
    public void testGetId() {
        assertEquals('h', DefaultOptionTest.buildHelpOption().getId());
        assertEquals('X', DefaultOptionTest.buildXOption().getId());
        assertEquals(0, CommandTest.buildStartCommand().getId());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testCanProcess
    public void testCanProcess() {
        final Option option = new PropertyOption();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option,null), "-Dmyprop=myval"));
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testCanProcess_Null
    public void testCanProcess_Null() {
        final Option option = new PropertyOption();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option,null), (String) null));
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testCanProcess_TooShort
    public void testCanProcess_TooShort() {
        final Option option = new PropertyOption();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option,null), "-D"));
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final Option option = new PropertyOption();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option,null),"-dump"));
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testPrefixes
    public void testPrefixes() {
        final Option option = new PropertyOption();
        assertContentsEqual(list("-D"), option.getPrefixes());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testProcess
    public void testProcess() throws OptionException {
        final PropertyOption option = new PropertyOption();
        final List args = list("-Dmyprop=myvalue");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        option.process(commandLine, iterator);
        assertEquals("myvalue", commandLine.getProperty(option, "myprop"));
        assertFalse(iterator.hasNext());
        assertEquals(1, commandLine.getProperties(option).size());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testProcess_UnexpectedOptionException
    public void testProcess_UnexpectedOptionException() {
        final Option option = new PropertyOption();
        final List args = list("--help");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        try {
            option.process(commandLine, iterator);
            fail("UnexpectedOption");
        }
        catch (final OptionException uoe) {
            assertEquals(option, uoe.getOption());
            assertEquals(
                "Unexpected --help while processing -Dproperty=value",
                uoe.getMessage());
        }
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testProcess_BadPropertyException
    public void testProcess_BadPropertyException() throws OptionException {
        final PropertyOption option = new PropertyOption();
        final List args = list("-Dmyprop");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        option.process(commandLine, iterator);

        assertEquals("true", commandLine.getProperty(option, "myprop"));
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testProcess_SetToEmpty
    public void testProcess_SetToEmpty() throws OptionException {
        final PropertyOption option = new PropertyOption();
        final List args = list("-Dmyprop=");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        option.process(commandLine, iterator);
        assertEquals("", commandLine.getProperty(option, "myprop"));
        assertFalse(iterator.hasNext());
        assertEquals(1, commandLine.getProperties(option).size());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testTriggers
    public void testTriggers() {
        final Option option = new PropertyOption();

        assertContentsEqual(list("-D"), option.getTriggers());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testValidate
    public void testValidate() throws OptionException {
        final Option option = new PropertyOption();
        final List args = list("-Dproperty=value");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();

        option.process(commandLine, iterator);

        option.validate(commandLine);
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = new PropertyOption();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("-D<property>=<value>", buffer.toString());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testAppendUsage_Hidden
    public void testAppendUsage_Hidden() {
        final Option option = new PropertyOption();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PROPERTY_OPTION);
        option.appendUsage(buffer, settings, null);

        assertEquals("", buffer.toString());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = new PropertyOption();
        assertEquals("-D", option.getPreferredName());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testGetDescription
    public void testGetDescription() {
        final Option option = new PropertyOption();
        assertEquals(
            "Passes properties and values to the application",
            option.getDescription());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testHelpLines
    public void testHelpLines() {
        final Option option = new PropertyOption();
        final List lines = option.helpLines(0, DisplaySetting.ALL, null);
        final Iterator i = lines.iterator();

        final HelpLine line1 = (HelpLine)i.next();
        assertEquals(0, line1.getIndent());
        assertEquals(option, line1.getOption());

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.PropertyOptionTest::testHelpLines_NoDisplay
    public void testHelpLines_NoDisplay() {
        final Option option = new PropertyOption();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_PROPERTY_OPTION);
        final List lines = option.helpLines(0, settings, null);
        final Iterator i = lines.iterator();

        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.option.SwitchTest::testProcessParent
    public void testProcessParent()
        throws OptionException {
        final Switch option = buildDisplaySwitch();
        final List args = list("+d");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.processParent(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("+d"));
        assertTrue(commandLine.hasOption("-display"));
        assertEquals(Boolean.TRUE, commandLine.getSwitch("-d"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.SwitchTest::testProcessParent_Disabled
    public void testProcessParent_Disabled()
        throws OptionException {
        final Switch option = buildDisplaySwitch();
        final List args = list("-disp");
        final WriteableCommandLine commandLine = commandLine(option, args);
        final ListIterator iterator = args.listIterator();
        option.process(commandLine, iterator);

        assertFalse(iterator.hasNext());
        assertTrue(commandLine.hasOption(option));
        assertTrue(commandLine.hasOption("+d"));
        assertTrue(commandLine.hasOption("-display"));
        assertEquals(Boolean.FALSE, commandLine.getSwitch("-d"));
        assertTrue(commandLine.getValues(option).isEmpty());
    }

// org.apache.commons.cli2.option.SwitchTest::testCanProcess
    public void testCanProcess() {
        final Switch option = buildDisplaySwitch();
        assertTrue(option.canProcess(new WriteableCommandLineImpl(option, null), "+d"));
    }

// org.apache.commons.cli2.option.SwitchTest::testCanProcess_BadMatch
    public void testCanProcess_BadMatch() {
        final Switch option = buildDisplaySwitch();
        assertFalse(option.canProcess(new WriteableCommandLineImpl(option, null), "-dont"));
    }

// org.apache.commons.cli2.option.SwitchTest::testPrefixes
    public void testPrefixes() {
        final Switch option = buildDisplaySwitch();
        assertContentsEqual(list("-", "+"), option.getPrefixes());
    }

// org.apache.commons.cli2.option.SwitchTest::testProcess
    public void testProcess() {
        
    }

// org.apache.commons.cli2.option.SwitchTest::testTriggers
    public void testTriggers() {
        final Switch option = buildDisplaySwitch();
        assertContentsEqual(list("-d", "+d", "-disp", "+disp", "+display", "-display"),
                            option.getTriggers());
    }

// org.apache.commons.cli2.option.SwitchTest::testValidate
    public void testValidate() {
        final Parent option = buildDisplaySwitch();
        final WriteableCommandLine commandLine = commandLine(option, list());

        try {
            option.validate(commandLine);
            fail("Missing an option");
        } catch (OptionException moe) {
            assertSame(option, moe.getOption());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testAppendUsage
    public void testAppendUsage() {
        final Option option = buildDisplaySwitch();
        final StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, DisplaySetting.ALL, null);

        assertEquals("+display|-display (+d|-d,+disp|-disp)", buffer.toString());
    }

// org.apache.commons.cli2.option.SwitchTest::testAppendUsage_NoAlias
    public void testAppendUsage_NoAlias() {
        final Option option = buildDisplaySwitch();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_ALIASES);
        option.appendUsage(buffer, settings, null);

        assertEquals("+display|-display", buffer.toString());
    }

// org.apache.commons.cli2.option.SwitchTest::testAppendUsage_NoDisabled
    public void testAppendUsage_NoDisabled() {
        final Option option = buildDisplaySwitch();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_SWITCH_DISABLED);
        option.appendUsage(buffer, settings, null);

        assertEquals("+display (+d,+disp)", buffer.toString());
    }

// org.apache.commons.cli2.option.SwitchTest::testAppendUsage_NoEnabled
    public void testAppendUsage_NoEnabled() {
        final Option option = buildDisplaySwitch();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_SWITCH_ENABLED);
        option.appendUsage(buffer, settings, null);

        assertEquals("-display (-d,-disp)", buffer.toString());
    }

// org.apache.commons.cli2.option.SwitchTest::testAppendUsage_NoDisabledOrEnabled
    public void testAppendUsage_NoDisabledOrEnabled() {
        final Option option = buildDisplaySwitch();
        final StringBuffer buffer = new StringBuffer();
        final Set settings = new HashSet(DisplaySetting.ALL);
        settings.remove(DisplaySetting.DISPLAY_SWITCH_DISABLED);
        settings.remove(DisplaySetting.DISPLAY_SWITCH_ENABLED);
        option.appendUsage(buffer, settings, null);

        assertEquals("+display (+d,+disp)", buffer.toString());
    }

// org.apache.commons.cli2.option.SwitchTest::testGetPreferredName
    public void testGetPreferredName() {
        final Option option = buildDisplaySwitch();
        assertEquals("+display", option.getPreferredName());
    }

// org.apache.commons.cli2.option.SwitchTest::testGetDescription
    public void testGetDescription() {
        final Option option = buildDisplaySwitch();
        assertEquals("Sets whether to display to screen", option.getDescription());
    }

// org.apache.commons.cli2.option.SwitchTest::testNullPreferredName
    public void testNullPreferredName() {
        try {
            new Switch("+", "-", null, null, "Sets whether to display to screen", true, null, null,
                       'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testEmptyPreferredName
    public void testEmptyPreferredName() {
        try {
            new Switch("+", "-", "", null, "Sets whether to display to screen", true, null, null,
                       'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testNullAliases
    public void testNullAliases() {
        try {
            new Switch("+", "-", "display", null, "Sets whether to display to screen", true, null,
                       null, 'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testNullEnablePrefix
    public void testNullEnablePrefix() {
        try {
            new Switch(null, "-", "display", null, "Sets whether to display to screen", true, null,
                       null, 'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_NO_ENABLED_PREFIX),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testNullDisablePrefix
    public void testNullDisablePrefix() {
        try {
            new Switch("+", null, "display", null, "Sets whether to display to screen", true, null,
                       null, 'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_NO_DISABLED_PREFIX),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testEnabledPrefixStartsWithDisabledPrefix
    public void testEnabledPrefixStartsWithDisabledPrefix() {
        try {
            new Switch("-", "-", "display", null, "Sets whether to display to screen", true, null,
                       null, 'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_ENABLED_STARTS_WITH_DISABLED),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testDisabledPrefixStartsWithEnabledPrefix
    public void testDisabledPrefixStartsWithEnabledPrefix() {
        try {
            new Switch("o", "on", "display", null, "Sets whether to display to screen", true, null,
                       null, 'd', null);
        } catch (IllegalArgumentException exp) {
            assertEquals("wrong exception message",
                         ResourceHelper.getResourceHelper().getMessage(ResourceConstants.SWITCH_DISABLED_STARTWS_WITH_ENABLED),
                         exp.getMessage());
        }
    }

// org.apache.commons.cli2.option.SwitchTest::testHelpLines
    public void testHelpLines() {
        
    }
