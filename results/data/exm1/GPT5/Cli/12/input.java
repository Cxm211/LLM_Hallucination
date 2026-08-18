// buggy code
    protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
    {
        List tokens = new ArrayList();

        boolean eatTheRest = false;

        for (int i = 0; i < arguments.length; i++)
        {
            String arg = arguments[i];

            if ("--".equals(arg))
            {
                eatTheRest = true;
                tokens.add("--");
            }
            else if ("-".equals(arg))
            {
                tokens.add("-");
            }
            else if (arg.startsWith("-"))
            {
                String opt = Util.stripLeadingHyphens(arg);

                if (options.hasOption(opt))
                {
                    tokens.add(arg);
                }
                else
                {
                    if (options.hasOption(arg.substring(0, 2)))
                    {
                        // the format is --foo=value or -foo=value
                        // the format is a special properties option (-Dproperty=value)
                        tokens.add(arg.substring(0, 2)); // -D
                        tokens.add(arg.substring(2)); // property=value
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
                    }
                }
            }
            else
            {
                tokens.add(arg);
            }

            if (eatTheRest)
            {
                for (i++; i < arguments.length; i++)
                {
                    tokens.add(arguments[i]);
                }
            }
        }

        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

// relevant test
// org.apache.commons.cli.ApplicationTest::testLs
    public void testLs() throws Exception {
        
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption( "a", "all", false, "do not hide entries starting with ." );
        options.addOption( "A", "almost-all", false, "do not list implied . and .." );
        options.addOption( "b", "escape", false, "print octal escapes for nongraphic characters" );
        options.addOption( OptionBuilder.withLongOpt( "block-size" )
                                        .withDescription( "use SIZE-byte blocks" )
                                        .withValueSeparator( '=' )
                                        .hasArg()
                                        .create() );
        options.addOption( "B", "ignore-backups", false, "do not list implied entried ending with ~");
        options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last modification of file status information) with -l:show ctime and sort by name otherwise: sort by ctime" );
        options.addOption( "C", false, "list entries by columns" );

        String[] args = new String[]{ "--block-size=10" };

        CommandLine line = parser.parse( options, args );
        assertTrue( line.hasOption( "block-size" ) );
        assertEquals( line.getOptionValue( "block-size" ), "10" );
    }

// org.apache.commons.cli.ApplicationTest::testAnt
    public void testAnt() throws Exception {
        
        CommandLineParser parser = new GnuParser( );
        Options options = new Options();
        options.addOption( "help", false, "print this message" );
        options.addOption( "projecthelp", false, "print project help information" );
        options.addOption( "version", false, "print the version information and exit" );
        options.addOption( "quiet", false, "be extra quiet" );
        options.addOption( "verbose", false, "be extra verbose" );
        options.addOption( "debug", false, "print debug information" );
        options.addOption( "logfile", true, "use given file for log" );
        options.addOption( "logger", true, "the class which is to perform the logging" );
        options.addOption( "listener", true, "add an instance of a class as a project listener" );
        options.addOption( "buildfile", true, "use given buildfile" );
        options.addOption( OptionBuilder.withDescription( "use value for given property" )
                                        .hasArgs()
                                        .withValueSeparator()
                                        .create( 'D' ) );
                           
        options.addOption( "find", true, "search for buildfile towards the root of the filesystem and use it" );

        String[] args = new String[]{ "-buildfile", "mybuild.xml",
            "-Dproperty=value", "-Dproperty1=value1",
            "-projecthelp" };

        CommandLine line = parser.parse( options, args );

        
        String[] opts = line.getOptionValues( "D" );
        assertEquals( "property", opts[0] );
        assertEquals( "value", opts[1] );
        assertEquals( "property1", opts[2] );
        assertEquals( "value1", opts[3] );

        
        assertEquals( line.getOptionValue( "buildfile"), "mybuild.xml" );

        
        assertTrue( line.hasOption( "projecthelp") );
    }

// org.apache.commons.cli.BugsTest::test11457
    public void test11457() throws Exception
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("verbose")
                .create());
        String[] args = new String[]{"--verbose"};

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("verbose"));
    }

// org.apache.commons.cli.BugsTest::test11458
    public void test11458() throws Exception
    {
        Options options = new Options();
        options.addOption( OptionBuilder.withValueSeparator( '=' )
                           .hasArgs()
                           .create( 'D' ) );
        options.addOption( OptionBuilder.withValueSeparator( ':' )
                           .hasArgs()
                           .create( 'p' ) );
        String[] args = new String[] { "-DJAVA_HOME=/opt/java" , "-pfile1:file2:file3" };

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse(options, args);

        String[] values = cmd.getOptionValues('D');

        assertEquals(values[0], "JAVA_HOME");
        assertEquals(values[1], "/opt/java");

        values = cmd.getOptionValues('p');

        assertEquals(values[0], "file1");
        assertEquals(values[1], "file2");
        assertEquals(values[2], "file3");

        Iterator iter = cmd.iterator();
        while (iter.hasNext())
        {
            Option opt = (Option) iter.next();
            switch (opt.getId())
            {
                case 'D':
                    assertEquals(opt.getValue(0), "JAVA_HOME");
                    assertEquals(opt.getValue(1), "/opt/java");
                    break;
                case 'p':
                    assertEquals(opt.getValue(0), "file1");
                    assertEquals(opt.getValue(1), "file2");
                    assertEquals(opt.getValue(2), "file3");
                    break;
                default:
                    fail("-D option not found");
            }
        }
    }

// org.apache.commons.cli.BugsTest::test11680
    public void test11680() throws Exception
    {
        Options options = new Options();
        options.addOption("f", true, "foobar");
        options.addOption("m", true, "missing");
        String[] args = new String[]{"-f", "foo"};

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse(options, args);

        cmd.getOptionValue("f", "default f");
        cmd.getOptionValue("m", "default m");
    }

// org.apache.commons.cli.BugsTest::test11456
    public void test11456() throws Exception
    {
        
        Options options = new Options();
        options.addOption( OptionBuilder.hasOptionalArg()
                           .create( 'a' ) );
        options.addOption( OptionBuilder.hasArg()
                           .create( 'b' ) );
        String[] args = new String[] { "-a", "-bvalue" };

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse( options, args );
        assertEquals( cmd.getOptionValue( 'b' ), "value" );

        
        options = new Options();
        options.addOption( OptionBuilder.hasOptionalArg()
                           .create( 'a' ) );
        options.addOption( OptionBuilder.hasArg()
                           .create( 'b' ) );
        args = new String[] { "-a", "-b", "value" };

        parser = new GnuParser();

        cmd = parser.parse( options, args );
        assertEquals( cmd.getOptionValue( 'b' ), "value" );
    }

// org.apache.commons.cli.BugsTest::test12210
    public void test12210() throws Exception
    {
        
        Options mainOptions = new Options();
        

        

        String[] argv = new String[] { "-exec", "-exec_opt1", "-exec_opt2" };
        OptionGroup grp = new OptionGroup();

        grp.addOption(new Option("exec",false,"description for this option"));

        grp.addOption(new Option("rep",false,"description for this option"));

        mainOptions.addOptionGroup(grp);

        
        Options execOptions = new Options();
        execOptions.addOption("exec_opt1", false, " desc");
        execOptions.addOption("exec_opt2", false, " desc");

        
        Options repOptions = new Options();
        repOptions.addOption("repopto", false, "desc");
        repOptions.addOption("repoptt", false, "desc");

        
        GnuParser parser = new GnuParser();

        

        
        
        
        CommandLine cmd = parser.parse(mainOptions,argv,true);
        
        argv = cmd.getArgs();

        if(cmd.hasOption("exec"))
        {
            cmd = parser.parse(execOptions,argv,false);
            
            assertTrue( cmd.hasOption("exec_opt1") );
            assertTrue( cmd.hasOption("exec_opt2") );
        }
        else if(cmd.hasOption("rep"))
        {
            cmd = parser.parse(repOptions,argv,false);
            
        }
        else {
            fail( "exec option not found" );
        }
    }

// org.apache.commons.cli.BugsTest::test13425
    public void test13425() throws Exception
    {
        Options options = new Options();
        Option oldpass = OptionBuilder.withLongOpt( "old-password" )
            .withDescription( "Use this option to specify the old password" )
            .hasArg()
            .create( 'o' );
        Option newpass = OptionBuilder.withLongOpt( "new-password" )
            .withDescription( "Use this option to specify the new password" )
            .hasArg()
            .create( 'n' );

        String[] args = { 
            "-o", 
            "-n", 
            "newpassword" 
        };

        options.addOption( oldpass );
        options.addOption( newpass );

        Parser parser = new PosixParser();

        try
        {
            parser.parse( options, args );
        }
        
        catch( Exception exp )
        {
            assertTrue( exp != null );
            return;
        }
        fail( "MissingArgumentException not caught." );
    }

// org.apache.commons.cli.BugsTest::test13666
    public void test13666() throws Exception
    {
        Options options = new Options();
        Option dir = OptionBuilder.withDescription( "dir" )
                                       .hasArg()
                                       .create( 'd' );
        options.addOption( dir );
        
        
        final PrintStream oldSystemOut = System.out;
        try
        {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final PrintStream print = new PrintStream(bytes);
            
            
            print.println();
            final String eol = bytes.toString();
            bytes.reset();
            
            System.setOut(new PrintStream(bytes));

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "dir", options );

            assertEquals("usage: dir"+eol+" -d <arg>   dir"+eol,bytes.toString());
        }
        finally
        {
            System.setOut(oldSystemOut);
        }
    }

// org.apache.commons.cli.BugsTest::test13935
    public void test13935() throws Exception
    {
        OptionGroup directions = new OptionGroup();

        Option left = new Option( "l", "left", false, "go left" );
        Option right = new Option( "r", "right", false, "go right" );
        Option straight = new Option( "s", "straight", false, "go straight" );
        Option forward = new Option( "f", "forward", false, "go forward" );
        forward.setRequired( true );

        directions.addOption( left );
        directions.addOption( right );
        directions.setRequired( true );

        Options opts = new Options();
        opts.addOptionGroup( directions );
        opts.addOption( straight );

        CommandLineParser parser = new PosixParser();
        boolean exception = false;

        String[] args = new String[] {  };
        try {
            CommandLine line = parser.parse( opts, args );
        }
        catch( ParseException exp ) {
            exception = true;
        }

        if( !exception ) {
            fail( "Expected exception not caught.");
        }

        exception = false;

        args = new String[] { "-s" };
        try {
            CommandLine line = parser.parse( opts, args );
        }
        catch( ParseException exp ) {
            exception = true;
        }

        if( !exception ) {
            fail( "Expected exception not caught.");
        }

        exception = false;

        args = new String[] { "-s", "-l" };
        try
        {
            parser.parse(opts, args);
        }
        catch (ParseException exp)
        {
            fail("Unexpected exception: " + exp.getClass().getName() + ":" + exp.getMessage());
        }

        opts.addOption( forward );
        args = new String[] { "-s", "-l", "-f" };
        try
        {
            parser.parse(opts, args);
        }
        catch (ParseException exp)
        {
            fail("Unexpected exception: " + exp.getClass().getName() + ":" + exp.getMessage());
        }
    }

// org.apache.commons.cli.BugsTest::test14786
    public void test14786() throws Exception {
        Option o = OptionBuilder.isRequired().withDescription("test").create("test");
        Options opts = new Options();
        opts.addOption(o);
        opts.addOption(o);

        CommandLineParser parser = new GnuParser();

        String[] args = new String[] { "-test" };

        CommandLine line = parser.parse( opts, args );
        assertTrue( line.hasOption( "test" ) );
    }

// org.apache.commons.cli.BugsTest::test15046
    public void test15046() throws Exception {
        CommandLineParser parser = new PosixParser();
        final String[] CLI_ARGS = new String[] {"-z", "c"};
        Option option = new Option("z", "timezone", true, 
                                   "affected option");
        Options cliOptions = new Options();
        cliOptions.addOption(option);
        parser.parse(cliOptions, CLI_ARGS);
		
        
        cliOptions.addOption("c", "conflict", true, "conflict option");
        CommandLine line = parser.parse(cliOptions, CLI_ARGS);
        assertEquals( option.getValue(), "c" );
        assertTrue( !line.hasOption("c") );
    }

// org.apache.commons.cli.BugsTest::test15648
    public void test15648() throws Exception {
        CommandLineParser parser = new PosixParser();
        final String[] args = new String[] { "-m", "\"Two Words\"" };
        Option m = OptionBuilder.hasArgs().create("m");
        Options options = new Options();
        options.addOption( m );
        CommandLine line = parser.parse( options, args );
        assertEquals( "Two Words", line.getOptionValue( "m" ) );
    }

// org.apache.commons.cli.BugsTest::test27635
    public void test27635() {}

// org.apache.commons.cli.BugsTest::test31148
    public void test31148() throws ParseException {
        Option multiArgOption = new Option("o","option with multiple args");
        multiArgOption.setArgs(1);
        
        Options options = new Options();
        options.addOption(multiArgOption);
        
        Parser parser = new PosixParser();
        String[] args = new String[]{};
        Properties props = new Properties();
        props.setProperty("o","ovalue");
        CommandLine cl = parser.parse(options,args,props);
        
        assertTrue(cl.hasOption('o'));
        assertEquals("ovalue",cl.getOptionValue('o'));
    }

// org.apache.commons.cli.BugsTest::test21215
    public void test21215() {
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        String SEP = System.getProperty("line.separator");
        String header = SEP+"Header";
        String footer = "Footer";
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out),80, "foobar", header, options, 2, 2, footer, true);
        assertEquals(
                "usage: foobar"+SEP+
                ""+SEP+
                "Header"+SEP+
                ""+SEP+
                "Footer"+SEP
                ,out.toString());
    }

// org.apache.commons.cli.BugsTest::test19383
    public void test19383() {
        Options options = new Options();
        options.addOption(new Option("a","aaa",false,"aaaaaaa"));
        options.addOption(new Option(null,"bbb",false,"bbbbbbb"));
        options.addOption(new Option("c",null,false,"ccccccc"));
        
        HelpFormatter formatter = new HelpFormatter();
        String SEP = System.getProperty("line.separator");
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out),80, "foobar", "", options, 2, 2, "", true);
        assertEquals(
                "usage: foobar [-a] [--bbb] [-c]"+SEP+
                "  -a,--aaa  aaaaaaa"+SEP+
                "     --bbb  bbbbbbb"+SEP+
                "  -c        ccccccc"+SEP
                ,out.toString());
    }

// org.apache.commons.cli.GnuParserTest::testSimpleShort
    public void testSimpleShort() throws Exception
    {
        String[] args = new String[] { "-a",
                                       "-b", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm size of extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.GnuParserTest::testSimpleLong
    public void testSimpleLong() throws Exception
    {
        String[] args = new String[] { "--enable-a",
                                       "--bfile", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm size of extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.GnuParserTest::testExtraOption
    public void testExtraOption() throws Exception
    {
        String[] args = new String[] { "-a", "-d", "-b", "toast",
                                       "foo", "bar" };

        boolean caught = false;

        try
        {
            CommandLine cl = parser.parse(options, args);

            assertTrue("Confirm -a is set", cl.hasOption("a"));
            assertTrue("Confirm -b is set", cl.hasOption("b"));
            assertTrue("confirm arg of -b", cl.getOptionValue("b").equals("toast"));
            assertTrue("Confirm size of extra args", cl.getArgList().size() == 3);
        }
        catch (UnrecognizedOptionException e)
        {
            caught = true;
        }

        assertTrue( "Confirm UnrecognizedOptionException caught", caught );
    }

// org.apache.commons.cli.GnuParserTest::testMissingArg
    public void testMissingArg() throws Exception
    {
        String[] args = new String[] { "-b" };

        boolean caught = false;

        try
        {
            parser.parse(options, args);
        }
        catch (MissingArgumentException e)
        {
            caught = true;
        }

        assertTrue( "Confirm MissingArgumentException caught", caught );
    }

// org.apache.commons.cli.GnuParserTest::testStop
    public void testStop() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foober",
                                       "-b",
                                       "toast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);
    }

// org.apache.commons.cli.GnuParserTest::testMultiple
    public void testMultiple() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foobar",
                                       "-b",
                                       "toast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);

        cl = parser.parse(options, cl.getArgs());

        assertTrue("Confirm -c is not set", !cl.hasOption("c"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar"));
    }

// org.apache.commons.cli.GnuParserTest::testMultipleWithLong
    public void testMultipleWithLong() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "foobar",
                                       "--bfile", "toast" };

        CommandLine cl = parser.parse(options,args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);

        cl = parser.parse(options, cl.getArgs());

        assertTrue("Confirm -c is not set", !cl.hasOption("c"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar"));
    }

// org.apache.commons.cli.GnuParserTest::testDoubleDash
    public void testDoubleDash() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "--",
                                       "-b", "toast" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm -b is not set", !cl.hasOption("b"));
        assertTrue("Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.GnuParserTest::testSingleDash
    public void testSingleDash() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "-b", "-",
                                       "-a",
                                       "-" };

        CommandLine cl = parser.parse(options, args);

        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("-"));
        assertTrue("Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("-"));
    }

// org.apache.commons.cli.GnuParserTest::testNegativeArgument
    public void testNegativeArgument() throws Exception
    {
        String[] args = new String[] { "-a", "-1"} ;

        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().create("a"));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);
        assertEquals("-1", cl.getOptionValue("a"));
    }

// org.apache.commons.cli.GnuParserTest::testShortWithEqual
    public void testShortWithEqual() throws Exception
    {
        String[] args = new String[] { "-f=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org.apache.commons.cli.GnuParserTest::testShortWithoutEqual
    public void testShortWithoutEqual() throws Exception
    {
        String[] args = new String[] { "-fbar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org.apache.commons.cli.GnuParserTest::testLongWithEqual
    public void testLongWithEqual() throws Exception
    {
        String[] args = new String[] { "--foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org.apache.commons.cli.GnuParserTest::testLongWithEqualSingleDash
    public void testLongWithEqualSingleDash() throws Exception
    {
        String[] args = new String[] { "-foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org.apache.commons.cli.ParseRequiredTest::testWithRequiredOption
    public void testWithRequiredOption() throws Exception
    {
        String[] args = new String[] {  "-b", "file" };

        CommandLine cl = parser.parse(_options,args);

        assertTrue( "Confirm -a is NOT set", !cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
        assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.ParseRequiredTest::testOptionAndRequiredOption
    public void testOptionAndRequiredOption() throws Exception
    {
        String[] args = new String[] {  "-a", "-b", "file" };

        CommandLine cl = parser.parse(_options,args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
        assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.ParseRequiredTest::testMissingRequiredOption
    public void testMissingRequiredOption()
    {
        String[] args = new String[] { "-a" };

        try
        {
            CommandLine cl = parser.parse(_options,args);
            fail( "exception should have been thrown" );
        }
        catch (MissingOptionException e)
        {
            assertEquals( "Incorrect exception message", "Missing required option: b", e.getMessage() );
        }
        catch (ParseException e)
        {
            fail( "expected to catch MissingOptionException" );
        }
    }

// org.apache.commons.cli.ParseRequiredTest::testMissingRequiredOptions
    public void testMissingRequiredOptions()
    {
        String[] args = new String[] { "-a" };

        _options.addOption( OptionBuilder.withLongOpt( "cfile" )
                                     .hasArg()
                                     .isRequired()
                                     .withDescription( "set the value of [c]" )
                                     .create( 'c' ) );

        try
        {
            CommandLine cl = parser.parse(_options,args);
            fail( "exception should have been thrown" );
        }
        catch (MissingOptionException e)
        {
            assertEquals( "Incorrect exception message", "Missing required options: b, c", e.getMessage() );
        }
        catch (ParseException e)
        {
            fail( "expected to catch MissingOptionException" );
        }
    }

// org.apache.commons.cli.ParseRequiredTest::testReuseOptionsTwice
    public void testReuseOptionsTwice() throws Exception
    {
        Options opts = new Options();
		opts.addOption(OptionBuilder.isRequired().create('v'));

		GnuParser parser = new GnuParser();

        
        parser.parse(opts, new String[] { "-v" });

        try
        {
            
            parser.parse(opts, new String[0]);
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            
        }
    }
