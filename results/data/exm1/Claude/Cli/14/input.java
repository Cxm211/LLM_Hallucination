// buggy code
    public void validate(final WriteableCommandLine commandLine)
        throws OptionException {
        // number of options found
        int present = 0;

        // reference to first unexpected option
        Option unexpected = null;

        for (final Iterator i = options.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();

            // needs validation?
            boolean validate = option.isRequired() || option instanceof Group;
            if (validate) {
                option.validate(commandLine);
            }

            // if the child option is present then validate it
            if (commandLine.hasOption(option)) {
                if (++present > maximum) {
                    unexpected = option;

                    break;
                }

                option.validate(commandLine);
            }
        }

        // too many options
        if (unexpected != null) {
            throw new OptionException(this, ResourceConstants.UNEXPECTED_TOKEN,
                                      unexpected.getPreferredName());
        }

        // too few option
        if (present < minimum) {
            throw new OptionException(this, ResourceConstants.MISSING_OPTION);
        }

        // validate each anonymous argument
        for (final Iterator i = anonymous.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();
            option.validate(commandLine);
        }
    }

// relevant test
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

// org.apache.commons.cli2.application.CvsTest::testCVS
    public void testCVS() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final CommandBuilder cbuilder = new CommandBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Group commands =
            gbuilder
                .withName("commands")
                .withOption(
                    cbuilder
                        .withName("add")
                        .withName("ad")
                        .withName("new")
                        .withDescription("Add a new file/directory to the repository")
                        .create())
                .withOption(
                    cbuilder
                        .withName("admin")
                        .withName("adm")
                        .withName("rcs")
                        .withDescription("Administration front end for rcs")
                        .create())
                .withOption(
                    cbuilder
                        .withName("annotate")
                        .withName("ann")
                        .withDescription("Show last revision where each line was modified")
                        .create())
                .withOption(
                    cbuilder
                        .withName("checkout")
                        .withName("co")
                        .withName("get")
                        .withDescription("Checkout sources for editing")
                        .create())
                .withOption(
                    cbuilder
                        .withName("commit")
                        .withName("ci")
                        .withName("com")
                        .withDescription("Check files into the repository")
                        .create())
                .withOption(
                    cbuilder
                        .withName("diff")
                        .withName("di")
                        .withName("dif")
                        .withDescription("Show differences between revisions")
                        .create())
                .withOption(
                    cbuilder
                        .withName("edit")
                        .withDescription("Get ready to edit a watched file")
                        .create())
                .withOption(
                    cbuilder
                        .withName("editors")
                        .withDescription("See who is editing a watched file")
                        .create())
                .withOption(
                    cbuilder
                        .withName("export")
                        .withName("exp")
                        .withName("ex")
                        .withDescription("Export sources from CVS, similar to checkout")
                        .create())
                .withOption(
                    cbuilder
                        .withName("history")
                        .withName("hi")
                        .withName("his")
                        .withDescription("Show repository access history")
                        .create())
                .withOption(
                    cbuilder
                        .withName("import")
                        .withName("im")
                        .withName("imp")
                        .withDescription("Import sources into CVS, using vendor branches")
                        .create())
                .withOption(
                    cbuilder
                        .withName("init")
                        .withDescription("Create a CVS repository if it doesn't exist")
                        .create())
                .withOption(
                    cbuilder
                        .withName("log")
                        .withName("lo")
                        .withName("rlog")
                        .withDescription("Print out history information for files")
                        .create())
                .withOption(
                    cbuilder
                        .withName("login")
                        .withName("logon")
                        .withName("lgn")
                        .withDescription("Prompt for password for authenticating server")
                        .create())
                .withOption(
                    cbuilder
                        .withName("logout")
                        .withDescription("Removes entry in .cvspass for remote repository")
                        .create())
                .withOption(
                    cbuilder
                        .withName("rdiff")
                        .withName("patch")
                        .withName("pa")
                        .withDescription("Create 'patch' format diffs between releases")
                        .create())
                .withOption(
                    cbuilder
                        .withName("release")
                        .withName("re")
                        .withName("rel")
                        .withDescription("Indicate that a Module is no longer in use")
                        .create())
                .withOption(
                    cbuilder
                        .withName("remove")
                        .withName("rm")
                        .withName("delete")
                        .withDescription("Remove an entry from the repository")
                        .create())
                .withOption(
                    cbuilder
                        .withName("rtag")
                        .withName("rt")
                        .withName("rfreeze")
                        .withDescription("Add a symbolic tag to a module")
                        .create())
                .withOption(
                    cbuilder
                        .withName("status")
                        .withName("st")
                        .withName("stat")
                        .withDescription("Display status information on checked out files")
                        .create())
                .withOption(
                    cbuilder
                        .withName("tag")
                        .withName("ta")
                        .withName("freeze")
                        .withDescription("Add a symbolic tag to checked out version of files")
                        .create())
                .withOption(
                    cbuilder
                        .withName("unedit")
                        .withDescription("Undo an edit command")
                        .create())
                .withOption(
                    cbuilder
                        .withName("update")
                        .withName("up")
                        .withName("upd")
                        .withDescription("Bring work tree in sync with repository")
                        .create())
                .withOption(
                    cbuilder
                        .withName("watch")
                        .withDescription("Set watches")
                        .create())
                .withOption(
                    cbuilder
                        .withName("watchers")
                        .withDescription("See who is watching a file")
                        .create())
                .withOption(
                    cbuilder
                        .withName("version")
                        .withName("ve")
                        .withName("ver")
                        .withDescription("????")
                        .create())
                .withOption(ArgumentTest.buildTargetsArgument())
                .create();

        final Group cvsOptions =
            new GroupBuilder()
                .withName("cvs-options")
                .withOption(
                    obuilder
                        .withShortName("H")
                        .withDescription("Displays usage information for command.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("Q")
                        .withDescription("Cause CVS to be really quiet.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("q")
                        .withDescription("Cause CVS to be somewhat quiet.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("r")
                        .withDescription("Make checked-out files read-only.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("w")
                        .withDescription("Make checked-out files read-write (default).")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("l")
                        .withDescription("Turn history logging off.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("n")
                        .withDescription("Do not execute anything that will change the disk.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("t")
                        .withDescription("Show trace of program execution -- try with -n.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("v")
                        .withDescription("CVS version and copyright.")
                        .create())
                .withOption(
                    obuilder
                        .withLongName("crlf")
                        .withDescription("Use the Dos line feed for text files (default).")
                        .create())
                .withOption(
                    obuilder
                        .withLongName("lf")
                        .withDescription("Use the Unix line feed for text files.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("T")
                        .withDescription("Use 'tmpdir' for temporary files.")
                        .withArgument(abuilder.withName("tmpdir").create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("e")
                        .withDescription("Use 'editor' for editing log information.")
                        .withArgument(abuilder.withName("editor").create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("d")
                        .withDescription("Overrides $CVSROOT as the root of the CVS tree.")
                        .withArgument(abuilder.withName("CVS_root").create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("f")
                        .withDescription("Do not use the ~/.cvsrc file.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("z")
                        .withDescription("Use compression level '#' for net traffic.")
                        .withArgument(abuilder.withName("#").create())
                        .create())
                .withOption(
                    obuilder
                        .withShortName("a")
                        .withDescription("Authenticate all net traffic.")
                        .create())
                .withOption(
                    obuilder
                        .withShortName("s")
                        .withDescription("Set CVS user variable.")
                        .withArgument(abuilder.withName("VAR=VAL").create())
                        .create())
                .withOption(commands)
                .create();

        assertNotNull(cvsOptions);
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

// org.apache.commons.cli2.bug.Bug27575Test::testRequiredOptions
	public void testRequiredOptions(){
		PatternBuilder builder = new PatternBuilder();
		builder.withPattern("hc!<");
		Option option = builder.create();
		assertTrue(option instanceof GroupImpl);

		GroupImpl group = (GroupImpl)option;
		Iterator i = group.getOptions().iterator();
		assertEquals("[-h]",i.next().toString());
		assertEquals("-c <arg>",i.next().toString());
		assertFalse(i.hasNext());
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

// org.apache.commons.cli2.bug.BugCLI18Test::testBug
  public void testBug() {
    Option a = new DefaultOptionBuilder().withLongName("aaa").withShortName("a").withDescription("aaaaaaa").create();
    Option b = new DefaultOptionBuilder().withLongName("bbb").withDescription("bbbbbbbb dksh fkshd fkhs dkfhsdk fhskd hksdks dhfowehfsdhfkjshf skfhkshf sf jkshfk sfh skfh skf f").create();
    Option c = new DefaultOptionBuilder().withLongName("ccc").withShortName("c").withDescription("ccccccc").create();

    Group g = new GroupBuilder().withOption(a).withOption(b).withOption(c).create();

    HelpFormatter formatter = new HelpFormatter();
    StringWriter out = new StringWriter();

    formatter.setPrintWriter(new PrintWriter(out));
    formatter.setHeader("dsfkfsh kdh hsd hsdh fkshdf ksdh fskdh fsdh fkshfk sfdkjhskjh fkjh fkjsh khsdkj hfskdhf skjdfh ksf khf s");
    formatter.setFooter("blort j jgj j jg jhghjghjgjhgjhg jgjhgj jhg jhg hjg jgjhghjg jhg hjg jhgjg jgjhghjg jg jgjhgjgjg jhg jhgjh" + '\r' + '\n' + "rarrr");
    formatter.setGroup(g);
    formatter.setShellCommand("foobar");

    formatter.print();

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

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testNew_NullShortPrefix
    public void testNew_NullShortPrefix() {
        try {
            new DefaultOptionBuilder(null, null, false);
            fail("null short prefix is not permitted");
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage(ResourceConstants.OPTION_ILLEGAL_SHORT_PREFIX),
                         e.getMessage());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testNew_EmptyShortPrefix
    public void testNew_EmptyShortPrefix() {
        try {
            new DefaultOptionBuilder("", null, false);
            fail("empty short prefix is not permitted");
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage(ResourceConstants.OPTION_ILLEGAL_SHORT_PREFIX),
                         e.getMessage());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testNew_NullLongPrefix
    public void testNew_NullLongPrefix() {
        try {
            new DefaultOptionBuilder("-", null, false);
            fail("null long prefix is not permitted");
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage(ResourceConstants.OPTION_ILLEGAL_LONG_PREFIX),
                         e.getMessage());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testNew_EmptyLongPrefix
    public void testNew_EmptyLongPrefix() {
        try {
            new DefaultOptionBuilder("-", "", false);
            fail("empty long prefix is not permitted");
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage(ResourceConstants.OPTION_ILLEGAL_LONG_PREFIX),
                         e.getMessage());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testCreate
    public void testCreate() {
        try {
            this.defaultOptionBuilder.create();
            fail("options must have a name");
        } catch (IllegalStateException e) {
            assertEquals(resources.getMessage(ResourceConstants.OPTION_NO_NAME), e.getMessage());
        }

        this.defaultOptionBuilder.withShortName("j");
        this.defaultOptionBuilder.create();
        this.defaultOptionBuilder.withLongName("jkeyes");
        this.defaultOptionBuilder.create();

        {
            DefaultOptionBuilder builder = new DefaultOptionBuilder("-", "--", true);
            builder.withShortName("mx");
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testName
    public void testName() {
        
        {
            this.defaultOptionBuilder.withShortName("a");
            this.defaultOptionBuilder.withLongName("apples");
        }
        
        {
            this.defaultOptionBuilder.withLongName("apples");
            this.defaultOptionBuilder.withShortName("a");
        }
        
        {
            this.defaultOptionBuilder.withLongName("apples");
            this.defaultOptionBuilder.withShortName("a");
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testWithDescription
    public void testWithDescription() {
        String description = "desc";
        this.defaultOptionBuilder.withShortName("a");
        this.defaultOptionBuilder.withDescription(description);

        DefaultOption opt = this.defaultOptionBuilder.create();
        assertEquals("wrong description found", description, opt.getDescription());
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testWithRequired
    public void testWithRequired() {
        {
            boolean required = false;
            this.defaultOptionBuilder.withShortName("a");
            this.defaultOptionBuilder.withRequired(required);

            DefaultOption opt = this.defaultOptionBuilder.create();
            assertEquals("wrong required found", required, opt.isRequired());
        }

        {
            boolean required = true;
            this.defaultOptionBuilder.withShortName("a");
            this.defaultOptionBuilder.withRequired(required);

            DefaultOption opt = this.defaultOptionBuilder.create();
            assertEquals("wrong required found", required, opt.isRequired());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testWithChildren
    public void testWithChildren() {
        GroupBuilder gbuilder = new GroupBuilder();

        this.defaultOptionBuilder.withShortName("a");
        this.defaultOptionBuilder.withRequired(true);

        DefaultOption opt = this.defaultOptionBuilder.create();

        Group group = gbuilder.withName("withchildren").withOption(opt).create();

        {
            this.defaultOptionBuilder.withShortName("b");
            this.defaultOptionBuilder.withChildren(group);

            DefaultOption option = this.defaultOptionBuilder.create();
            assertEquals("wrong children found", group, option.getChildren());
        }
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testWithArgument
    public void testWithArgument() {
        ArgumentBuilder abuilder = new ArgumentBuilder();
        abuilder.withName("myarg");

        Argument arg = abuilder.create();

        this.defaultOptionBuilder.withShortName("a");
        this.defaultOptionBuilder.withRequired(true);
        this.defaultOptionBuilder.withArgument(arg);

        DefaultOption opt = this.defaultOptionBuilder.create();

        assertEquals("wrong argument found", arg, opt.getArgument());
    }

// org.apache.commons.cli2.builder.DefaultOptionBuilderTest::testWithId
    public void testWithId() {
        this.defaultOptionBuilder.withShortName("a");
        this.defaultOptionBuilder.withId(0);

        DefaultOption opt = this.defaultOptionBuilder.create();

        assertEquals("wrong id found", 0, opt.getId());
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

// org.apache.commons.cli2.util.ComparatorsTest::testGroupFirst
    public void testGroupFirst() {
        final Option o1 = GroupTest.buildAntGroup();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.groupFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testGroupLast
    public void testGroupLast() {
        final Option o1 = GroupTest.buildAntGroup();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.groupLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testSwitchFirst
    public void testSwitchFirst() {
        final Option o1 = SwitchTest.buildDisplaySwitch();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.switchFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testSwitchLast
    public void testSwitchLast() {
        final Option o1 = SwitchTest.buildDisplaySwitch();
        final Option o2 = ParentTest.buildLibParent();
        
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.switchLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testCommandFirst
    public void testCommandFirst() {
        final Option o1 = CommandTest.buildCommitCommand();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.commandFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testCommandLast
    public void testCommandLast() {
        final Option o1 = CommandTest.buildCommitCommand();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.commandLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testDefaultOptionFirst
    public void testDefaultOptionFirst() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = CommandTest.buildCommitCommand();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.defaultOptionFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testDefaultOptionLast
    public void testDefaultOptionLast() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = CommandTest.buildCommitCommand();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.defaultOptionLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testNamedFirst
    public void testNamedFirst() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.namedFirst("--help"));

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testNamedLast
    public void testNamedLast() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.namedLast("--help"));

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testPreferredNameFirst
    public void testPreferredNameFirst() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.preferredNameFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testPreferredNameLast
    public void testPreferredNameLast() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = ParentTest.buildLibParent();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.preferredNameLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testRequiredFirst
    public void testRequiredFirst() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = DefaultOptionTest.buildXOption();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.requiredFirst());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o2, o1),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testRequiredLast
    public void testRequiredLast() {
        final Option o1 = DefaultOptionTest.buildHelpOption();
        final Option o2 = DefaultOptionTest.buildXOption();
        final List list = CLITestCase.list(o1, o2);

        Collections.sort(list, Comparators.requiredLast());

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o1, o2),
            list);
    }

// org.apache.commons.cli2.util.ComparatorsTest::testChained
    public void testChained() {
        final Option o1 = CommandTest.buildCommitCommand();
        final Option o2 = SwitchTest.buildDisplaySwitch();
        final Option o3 = DefaultOptionTest.buildHelpOption();
        final List list = CLITestCase.list(o1, o2, o3);

        Collections.sort(
            list,
            Comparators.chain(
                Comparators.namedFirst("--help"),
                Comparators.commandFirst()));

        CLITestCase.assertListContentsEqual(
            CLITestCase.list(o3, o1, o2),
            list);
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrint
    public void testPrint()
        throws IOException {
        final StringWriter writer = new StringWriter();
        final PrintWriter pw = new PrintWriter(writer);
        helpFormatter.setPrintWriter(pw);
        helpFormatter.print();

        
        assertEquals("incorrect shell command", "ant", helpFormatter.getShellCommand());

        
        assertEquals("incorrect group", this.options, helpFormatter.getGroup());

        
        assertEquals("incorrect page width", 76, helpFormatter.getPageWidth());

        
        assertEquals("incorrect print writer", pw, helpFormatter.getPrintWriter());

        
        assertEquals("incorrect divider",
                     "+------------------------------------------------------------------------------+",
                     helpFormatter.getDivider());

        
        assertEquals("incorrect header", "Apache Commons CLI", helpFormatter.getHeader());

        
        assertEquals("incorrect footer", "Copyright 2003\nApache Software Foundation",
                     helpFormatter.getFooter());

        
        assertEquals("incorrect left gutter", "|*", helpFormatter.getGutterLeft());
        assertEquals("incorrect right gutter", "*|", helpFormatter.getGutterRight());
        assertEquals("incorrect center gutter", "*-*", helpFormatter.getGutterCenter());

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Apache Commons CLI                                                          *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Usage:                                                                      *|",
                     reader.readLine());
        assertEquals("|*ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2>    *|",
                     reader.readLine());
        assertEquals("|*...]]                                                                       *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*options              *-*                                                    *|",
                     reader.readLine());
        assertEquals("|*  --help (-?,-h)     *-*Displays the help                                   *|",
                     reader.readLine());
        assertEquals("|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
                     reader.readLine());
        assertEquals("|*                     *-*or report problems.                                 *|",
                     reader.readLine());
        assertEquals("|*  --projecthelp      *-*print project help information                      *|",
                     reader.readLine());
        assertEquals("|*  --verbose          *-*print the version information and exit              *|",
                     reader.readLine());
        assertEquals("|*  target [target ...]*-*The targets ant should build                        *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Copyright 2003                                                              *|",
                     reader.readLine());
        assertEquals("|*Apache Software Foundation                                                  *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testComparator
    public void testComparator()
        throws IOException {
        final StringWriter writer = new StringWriter();
        final PrintWriter pw = new PrintWriter(writer);
        helpFormatter.setPrintWriter(pw);

        final Comparator comparator = new OptionComparator();
        helpFormatter.setComparator(comparator);
        helpFormatter.print();

        
        assertEquals("invalid comparator", comparator, helpFormatter.getComparator());

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Apache Commons CLI                                                          *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Usage:                                                                      *|",
                     reader.readLine());
        assertEquals("|*ant [--verbose --projecthelp --help --diagnostics] [<target1> [<target2>    *|",
                     reader.readLine());
        assertEquals("|*...]]                                                                       *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*options              *-*                                                    *|",
                     reader.readLine());
        assertEquals("|*  --verbose          *-*print the version information and exit              *|",
                     reader.readLine());
        assertEquals("|*  --projecthelp      *-*print project help information                      *|",
                     reader.readLine());
        assertEquals("|*  --help (-?,-h)     *-*Displays the help                                   *|",
                     reader.readLine());
        assertEquals("|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
                     reader.readLine());
        assertEquals("|*                     *-*or report problems.                                 *|",
                     reader.readLine());
        assertEquals("|*  target [target ...]*-*The targets ant should build                        *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Copyright 2003                                                              *|",
                     reader.readLine());
        assertEquals("|*Apache Software Foundation                                                  *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintHelp
    public void testPrintHelp()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printHelp();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*options              *-*                                                    *|",
                     reader.readLine());
        assertEquals("|*  --help (-?,-h)     *-*Displays the help                                   *|",
                     reader.readLine());
        assertEquals("|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
                     reader.readLine());
        assertEquals("|*                     *-*or report problems.                                 *|",
                     reader.readLine());
        assertEquals("|*  --projecthelp      *-*print project help information                      *|",
                     reader.readLine());
        assertEquals("|*  --verbose          *-*print the version information and exit              *|",
                     reader.readLine());
        assertEquals("|*  target [target ...]*-*The targets ant should build                        *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintHelp_WithException
    public void testPrintHelp_WithException()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.setException(new OptionException(verbose));
        helpFormatter.printHelp();

        
        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*--verbose*-*print the version information and exit                          *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintHelp_TooNarrow
    public void testPrintHelp_TooNarrow()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter = new HelpFormatter("<", "=", ">", 4);
        helpFormatter.setGroup(options);
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printHelp();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("<options              = >", reader.readLine());
        assertEquals("<  --help (-?,-h)     =D>", reader.readLine());
        assertEquals("<                     =i>", reader.readLine());

        
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintException
    public void testPrintException()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.setException(new OptionException(verbose, ResourceConstants.MISSING_OPTION));
        helpFormatter.printException();

        
        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Missing option --verbose                                                    *|",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintUsage
    public void testPrintUsage()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printUsage();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Usage:                                                                      *|",
                     reader.readLine());
        assertEquals("|*ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2>    *|",
                     reader.readLine());
        assertEquals("|*...]]                                                                       *|",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintHeader
    public void testPrintHeader()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printHeader();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertEquals("|*Apache Commons CLI                                                          *|",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintFooter
    public void testPrintFooter()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printFooter();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("|*Copyright 2003                                                              *|",
                     reader.readLine());
        assertEquals("|*Apache Software Foundation                                                  *|",
                     reader.readLine());
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPrintDivider
    public void testPrintDivider()
        throws IOException {
        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.printDivider();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("+------------------------------------------------------------------------------+",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap
    public void testWrap() {
        final Iterator i = HelpFormatter.wrap("Apache Software Foundation", 30).iterator();
        assertEquals("Apache Software Foundation", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_WrapNeeded
    public void testWrap_WrapNeeded() {
        final Iterator i = HelpFormatter.wrap("Apache Software Foundation", 20).iterator();
        assertEquals("Apache Software", i.next());
        assertEquals("Foundation", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_BeforeSpace
    public void testWrap_BeforeSpace() {
        final Iterator i = HelpFormatter.wrap("Apache Software Foundation", 16).iterator();
        assertEquals("Apache Software", i.next());
        assertEquals("Foundation", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_AfterSpace
    public void testWrap_AfterSpace() {
        final Iterator i = HelpFormatter.wrap("Apache Software Foundation", 17).iterator();
        assertEquals("Apache Software", i.next());
        assertEquals("Foundation", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_InWord
    public void testWrap_InWord() {
        final Iterator i = HelpFormatter.wrap("Apache Software Foundation", 8).iterator();
        assertEquals("Apache", i.next());
        assertEquals("Software", i.next());
        assertEquals("Foundati", i.next());
        assertEquals("on", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_NewLine
    public void testWrap_NewLine() {
        final Iterator i = HelpFormatter.wrap("\nApache Software Foundation\n", 30).iterator();
        assertEquals("", i.next());
        assertEquals("Apache Software Foundation", i.next());
        assertEquals("", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_NewLine2
    public void testWrap_NewLine2() {
        List wrapped =
            HelpFormatter.wrap("A really quite long general description of the option with specific alternatives documented:\n" +
                               "  Indented special case\n" + "  Alternative scenario", 30);

        final Iterator i = wrapped.iterator();

        assertEquals("A really quite long general", i.next());
        assertEquals("description of the option", i.next());
        assertEquals("with specific alternatives", i.next());
        assertEquals("documented:", i.next());
        assertEquals("  Indented special case", i.next());
        assertEquals("  Alternative scenario", i.next());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testWrap_Below1Length
    public void testWrap_Below1Length() {
        try {
            HelpFormatter.wrap("Apache Software Foundation", -1);
            fail("IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(resources.getMessage(ResourceConstants.HELPFORMATTER_WIDTH_TOO_NARROW,
                                              new Object[] { new Integer(-1) }), e.getMessage());
        }
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPad
    public void testPad()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad("hello", 10, new PrintWriter(writer));
        assertEquals("hello     ", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPad_Null
    public void testPad_Null()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad(null, 10, new PrintWriter(writer));
        assertEquals("          ", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPad_TooLong
    public void testPad_TooLong()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad("hello world", 10, new PrintWriter(writer));
        assertEquals("hello world", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPad_TooShort
    public void testPad_TooShort()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad("hello world", -5, new PrintWriter(writer));
        assertEquals("hello world", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testGutters
    public void testGutters()
        throws IOException {
        helpFormatter = new HelpFormatter(null, null, null, 80);
        helpFormatter.setShellCommand("ant");

        final Set lusage = new HashSet();
        lusage.add(DisplaySetting.DISPLAY_ALIASES);
        lusage.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpFormatter.setLineUsageSettings(lusage);

        
        assertEquals("incorrect line usage", lusage, helpFormatter.getLineUsageSettings());

        final Set fusage = new HashSet();
        fusage.add(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        fusage.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        fusage.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        fusage.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        fusage.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        fusage.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        fusage.add(DisplaySetting.DISPLAY_SWITCH_ENABLED);
        fusage.add(DisplaySetting.DISPLAY_SWITCH_DISABLED);
        fusage.add(DisplaySetting.DISPLAY_PROPERTY_OPTION);
        fusage.add(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        fusage.add(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        fusage.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpFormatter.setFullUsageSettings(fusage);

        
        assertEquals("incorrect full usage", fusage, helpFormatter.getFullUsageSettings());

        final Set dsettings = new HashSet();
        dsettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        dsettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        dsettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        helpFormatter.setDisplaySettings(dsettings);

        verbose =
            new DefaultOptionBuilder().withLongName("verbose")
                                      .withDescription("print the version information and exit")
                                      .create();

        options =
            new GroupBuilder().withName("options").withOption(DefaultOptionTest.buildHelpOption())
                              .withOption(ArgumentTest.buildTargetsArgument())
                              .withOption(new DefaultOptionBuilder().withLongName("diagnostics")
                                                                    .withDescription("print information that might be helpful to diagnose or report problems.")
                                                                    .create())
                              .withOption(new DefaultOptionBuilder().withLongName("projecthelp")
                                                                    .withDescription("print project help information")
                                                                    .create()).withOption(verbose)
                              .create();

        helpFormatter.setGroup(options);

        
        assertEquals("incorrect left gutter", HelpFormatter.DEFAULT_GUTTER_LEFT,
                     helpFormatter.getGutterLeft());
        assertEquals("incorrect right gutter", HelpFormatter.DEFAULT_GUTTER_RIGHT,
                     helpFormatter.getGutterRight());
        assertEquals("incorrect center gutter", HelpFormatter.DEFAULT_GUTTER_CENTER,
                     helpFormatter.getGutterCenter());

        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.print();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("Usage:                                                                          ",
                     reader.readLine());
        assertEquals("ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2> ...]]  ",
                     reader.readLine());
        assertEquals("options                                                                         ",
                     reader.readLine());
        assertEquals("  --help (-?,-h)         Displays the help                                      ",
                     reader.readLine());
        assertEquals("  --diagnostics          print information that might be helpful to diagnose or ",
                     reader.readLine());
        assertEquals("                         report problems.                                       ",
                     reader.readLine());
        assertEquals("  --projecthelp          print project help information                         ",
                     reader.readLine());
        assertEquals("  --verbose              print the version information and exit                 ",
                     reader.readLine());
        assertEquals("  target [target ...]    The targets ant should build                           ",
                     reader.readLine());
        assertNull(reader.readLine());
    }
