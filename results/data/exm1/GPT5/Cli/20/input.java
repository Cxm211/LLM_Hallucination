// buggy code
    protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
    {
        init();
        this.options = options;

        // an iterator for the command line tokens
        Iterator iter = Arrays.asList(arguments).iterator();

        // process each command line token
        while (iter.hasNext())
        {
            // get the next command line token
            String token = (String) iter.next();

            // handle long option --foo or --foo=bar
            if (token.startsWith("--"))
            {
                if (token.indexOf('=') != -1)
                {
                    tokens.add(token.substring(0, token.indexOf('=')));
                    tokens.add(token.substring(token.indexOf('=') + 1, token.length()));
                }
                else
                {
                    tokens.add(token);
                }
            }

            // single hyphen
            else if ("-".equals(token))
            {
                tokens.add(token);
            }
            else if (token.startsWith("-"))
            {
                if (token.length() == 2)
                {
                    processOptionToken(token, stopAtNonOption);
                }
                else if (options.hasOption(token))
                {
                    tokens.add(token);
                }
                // requires bursting
                else
                {
                    burstToken(token, stopAtNonOption);
                }
            }
            else if (stopAtNonOption)
            {
                process(token);
            }
            else
            {
                tokens.add(token);
            }

            gobble(iter);
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

// org.apache.commons.cli.ArgumentIsOptionTest::testOptionAndOptionWithArgument
    public void testOptionAndOptionWithArgument() throws Exception
    {
        String[] args = new String[]{
                "-p",
                "-attr",
                "p"
        };

        CommandLine cl = parser.parse(options, args);
        assertTrue("Confirm -p is set", cl.hasOption("p"));
        assertTrue("Confirm -attr is set", cl.hasOption("attr"));
        assertTrue("Confirm arg of -attr",
                cl.getOptionValue("attr").equals("p"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
    }

// org.apache.commons.cli.ArgumentIsOptionTest::testOptionWithArgument
    public void testOptionWithArgument() throws Exception
    {
        String[] args = new String[]{
                "-attr",
                "p"
        };

        CommandLine cl = parser.parse(options, args);
        assertFalse("Confirm -p is set", cl.hasOption("p"));
        assertTrue("Confirm -attr is set", cl.hasOption("attr"));
        assertTrue("Confirm arg of -attr",
                cl.getOptionValue("attr").equals("p"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
    }

// org.apache.commons.cli.ArgumentIsOptionTest::testOption
    public void testOption() throws Exception
    {
        String[] args = new String[]{
                "-p"
        };

        CommandLine cl = parser.parse(options, args);
        assertTrue("Confirm -p is set", cl.hasOption("p"));
        assertFalse("Confirm -attr is not set", cl.hasOption("attr"));
        assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
    }

// org.apache.commons.cli.BugsTest::test11457
    public void test11457() throws Exception
    {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("verbose").create());
        String[] args = new String[]{"--verbose"};

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("verbose"));
    }

// org.apache.commons.cli.BugsTest::test11458
    public void test11458() throws Exception
    {
        Options options = new Options();
        options.addOption( OptionBuilder.withValueSeparator( '=' ).hasArgs().create( 'D' ) );
        options.addOption( OptionBuilder.withValueSeparator( ':' ).hasArgs().create( 'p' ) );
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
        options.addOption( OptionBuilder.hasOptionalArg().create( 'a' ) );
        options.addOption( OptionBuilder.hasArg().create( 'b' ) );
        String[] args = new String[] { "-a", "-bvalue" };

        CommandLineParser parser = new PosixParser();

        CommandLine cmd = parser.parse( options, args );
        assertEquals( cmd.getOptionValue( 'b' ), "value" );

        
        options = new Options();
        options.addOption( OptionBuilder.hasOptionalArg().create( 'a' ) );
        options.addOption( OptionBuilder.hasArg().create( 'b' ) );
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
        Option dir = OptionBuilder.withDescription( "dir" ).hasArg().create( 'd' );
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
        try
        {
            CommandLine line = parser.parse(opts, args);
        }
        catch (ParseException exp)
        {
            exception = true;
        }

        if (!exception)
        {
            fail("Expected exception not caught.");
        }

        exception = false;

        args = new String[] { "-s" };
        try
        {
            CommandLine line = parser.parse(opts, args);
        }
        catch (ParseException exp)
        {
            exception = true;
        }

        if (!exception)
        {
            fail("Expected exception not caught.");
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
    public void test14786() throws Exception
    {
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
    public void test15046() throws Exception
    {
        CommandLineParser parser = new PosixParser();
        String[] CLI_ARGS = new String[] {"-z", "c"};

        Options options = new Options();
        options.addOption(new Option("z", "timezone", true, "affected option"));

        parser.parse(options, CLI_ARGS);
        
        
        options.addOption("c", "conflict", true, "conflict option");
        CommandLine line = parser.parse(options, CLI_ARGS);
        assertEquals( line.getOptionValue('z'), "c" );
        assertTrue( !line.hasOption("c") );
    }

// org.apache.commons.cli.BugsTest::test15648
    public void test15648() throws Exception
    {
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
    public void test31148() throws ParseException
    {
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
    public void test21215()
    {
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
    public void test19383()
    {
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

// org.apache.commons.cli.OptionGroupTest::testSingleOptionFromGroup
    public void testSingleOptionFromGroup() throws Exception
    {
        String[] args = new String[] { "-f" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testSingleOption
    public void testSingleOption() throws Exception
    {
        String[] args = new String[] { "-r" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is set", cl.hasOption("r") );
        assertTrue( "Confirm -f is NOT set", !cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testTwoValidOptions
    public void testTwoValidOptions() throws Exception
    {
        String[] args = new String[] { "-r", "-f" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is set", cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testSingleLongOption
    public void testSingleLongOption() throws Exception
    {
        String[] args = new String[] { "--file" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testTwoValidLongOptions
    public void testTwoValidLongOptions() throws Exception
    {
        String[] args = new String[] { "--revision", "--file" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is set", cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testNoOptionsExtraArgs
    public void testNoOptionsExtraArgs() throws Exception
    {
        String[] args = new String[] { "arg1", "arg2" };

        CommandLine cl = parser.parse( _options, args);

        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is NOT set", !cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm TWO extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.OptionGroupTest::testTwoOptionsFromGroup
    public void testTwoOptionsFromGroup() throws Exception
    {
        String[] args = new String[] { "-f", "-d" };

        try
        {
            parser.parse( _options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (AlreadySelectedException e)
        {
            assertNotNull("null option group", e.getOptionGroup());
            assertEquals("selected option", "f", e.getOptionGroup().getSelected());
            assertEquals("option", "d", e.getOption().getOpt());
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoLongOptionsFromGroup
    public void testTwoLongOptionsFromGroup() throws Exception
    {
        String[] args = new String[] { "--file", "--directory" };

        try
        {
            parser.parse(_options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (AlreadySelectedException e)
        {
            assertNotNull("null option group", e.getOptionGroup());
            assertEquals("selected option", "f", e.getOptionGroup().getSelected());
            assertEquals("option", "d", e.getOption().getOpt());
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoOptionsFromDifferentGroup
    public void testTwoOptionsFromDifferentGroup() throws Exception
    {
        String[] args = new String[] { "-f", "-s" };

        CommandLine cl = parser.parse( _options, args);
        assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
        assertTrue( "Confirm -f is set", cl.hasOption("f") );
        assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
        assertTrue( "Confirm -s is set", cl.hasOption("s") );
        assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
        assertTrue( "Confirm NO extra args", cl.getArgList().size() == 0);
    }

// org.apache.commons.cli.OptionGroupTest::testValidLongOnlyOptions
    public void testValidLongOnlyOptions() throws Exception
    {
        CommandLine cl1 = parser.parse(_options, new String[]{"--export"});
        assertTrue("Confirm --export is set", cl1.hasOption("export"));

        CommandLine cl2 = parser.parse(_options, new String[]{"--import"});
        assertTrue("Confirm --import is set", cl2.hasOption("import"));
    }

// org.apache.commons.cli.OptionGroupTest::testToString
    public void testToString() {}

// org.apache.commons.cli.OptionsTest::testSimple
    public void testSimple()
    {
        Options opts = new Options();

        opts.addOption("a", false, "toggle -a");
        opts.addOption("b", true, "toggle -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }

// org.apache.commons.cli.OptionsTest::testDuplicateSimple
    public void testDuplicateSimple()
    {
        Options opts = new Options();
        opts.addOption("a", false, "toggle -a");
        opts.addOption("a", true, "toggle -a*");

        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }

// org.apache.commons.cli.OptionsTest::testLong
    public void testLong()
    {
        Options opts = new Options();

        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("b", "--b", true, "set -b");

        assertTrue(opts.hasOption("a"));
        assertTrue(opts.hasOption("b"));
    }

// org.apache.commons.cli.OptionsTest::testDuplicateLong
    public void testDuplicateLong()
    {
        Options opts = new Options();
        opts.addOption("a", "--a", false, "toggle -a");
        opts.addOption("a", "--a", false, "toggle -a*");
        assertEquals("last one in wins", "toggle -a*", opts.getOption("a").getDescription());
    }

// org.apache.commons.cli.OptionsTest::testHelpOptions
    public void testHelpOptions()
    {
        Option longOnly1 = OptionBuilder.withLongOpt("long-only1").create();
        Option longOnly2 = OptionBuilder.withLongOpt("long-only2").create();
        Option shortOnly1 = OptionBuilder.create("1");
        Option shortOnly2 = OptionBuilder.create("2");
        Option bothA = OptionBuilder.withLongOpt("bothA").create("a");
        Option bothB = OptionBuilder.withLongOpt("bothB").create("b");
        
        Options options = new Options();
        options.addOption(longOnly1);
        options.addOption(longOnly2);
        options.addOption(shortOnly1);
        options.addOption(shortOnly2);
        options.addOption(bothA);
        options.addOption(bothB);
        
        Collection allOptions = new ArrayList();
        allOptions.add(longOnly1);
        allOptions.add(longOnly2);
        allOptions.add(shortOnly1);
        allOptions.add(shortOnly2);
        allOptions.add(bothA);
        allOptions.add(bothB);
        
        Collection helpOptions = options.helpOptions();
        
        assertTrue("Everything in all should be in help", helpOptions.containsAll(allOptions));
        assertTrue("Everything in help should be in all", allOptions.containsAll(helpOptions));        
    }

// org.apache.commons.cli.OptionsTest::testMissingOptionException
    public void testMissingOptionException() throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        try
        {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals("Missing required option: f", e.getMessage());
        }
    }

// org.apache.commons.cli.OptionsTest::testMissingOptionsException
    public void testMissingOptionsException() throws ParseException
    {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        options.addOption(OptionBuilder.isRequired().create("x"));
        try
        {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals("Missing required options: f, x", e.getMessage());
        }
    }

// org.apache.commons.cli.OptionsTest::testToString
    public void testToString()
    {
        Options options = new Options();
        options.addOption("f", "foo", true, "Foo");
        options.addOption("b", "bar", false, "Bar");

        String s = options.toString();
        assertNotNull("null string returned", s);
        assertTrue("foo option missing", s.toLowerCase().indexOf("foo") != -1);
        assertTrue("bar option missing", s.toLowerCase().indexOf("bar") != -1);
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
            assertTrue(e.getMissingOptions().contains("b"));
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
            assertTrue(e.getMissingOptions().contains("b"));
            assertTrue(e.getMissingOptions().contains("c"));
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

// org.apache.commons.cli.PatternOptionBuilderTest::testSimplePattern
    public void testSimplePattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("a:b@cde>f+n%t/");
        String[] args = new String[]{"-c", "-a", "foo", "-b", "java.util.Vector", "-e", "build.xml", "-f", "java.util.Calendar", "-n", "4.5", "-t", "http://jakarta.apache.org/"};

        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);

        assertEquals("flag a", "foo", line.getOptionValue("a"));
        assertEquals("string flag a", "foo", line.getOptionObject("a"));
        assertEquals("object flag b", new Vector(), line.getOptionObject("b"));
        assertTrue("boolean true flag c", line.hasOption("c"));
        assertFalse("boolean false flag d", line.hasOption("d"));
        assertEquals("file flag e", new File("build.xml"), line.getOptionObject("e"));
        assertEquals("class flag f", Calendar.class, line.getOptionObject("f"));
        assertEquals("number flag n", new Double(4.5), line.getOptionObject("n"));
        assertEquals("url flag t", new URL("http://jakarta.apache.org/"), line.getOptionObject("t"));

        
        assertEquals("flag a", "foo", line.getOptionValue('a'));
        assertEquals("string flag a", "foo", line.getOptionObject('a'));
        assertEquals("object flag b", new Vector(), line.getOptionObject('b'));
        assertTrue("boolean true flag c", line.hasOption('c'));
        assertFalse("boolean false flag d", line.hasOption('d'));
        assertEquals("file flag e", new File("build.xml"), line.getOptionObject('e'));
        assertEquals("class flag f", Calendar.class, line.getOptionObject('f'));
        assertEquals("number flag n", new Double(4.5), line.getOptionObject('n'));
        assertEquals("url flag t", new URL("http://jakarta.apache.org/"), line.getOptionObject('t'));

        
        
        
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testEmptyPattern
    public void testEmptyPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("");
        assertTrue(options.getOptions().isEmpty());
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testUntypedPattern
    public void testUntypedPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("abc");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-abc" });

        assertTrue(line.hasOption('a'));
        assertNull("value a", line.getOptionObject('a'));
        assertTrue(line.hasOption('b'));
        assertNull("value b", line.getOptionObject('b'));
        assertTrue(line.hasOption('c'));
        assertNull("value c", line.getOptionObject('c'));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testNumberPattern
    public void testNumberPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("n%d%x%");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-n", "1", "-d", "2.1", "-x", "3,5" });

        assertEquals("n object class", Long.class, line.getOptionObject("n").getClass());
        assertEquals("n value", new Long(1), line.getOptionObject("n"));

        assertEquals("d object class", Double.class, line.getOptionObject("d").getClass());
        assertEquals("d value", new Double(2.1), line.getOptionObject("d"));

        assertNull("x object", line.getOptionObject("x"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testClassPattern
    public void testClassPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("c+d+");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-c", "java.util.Calendar", "-d", "System.DateTime" });

        assertEquals("c value", Calendar.class, line.getOptionObject("c"));
        assertNull("d value", line.getOptionObject("d"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testObjectPattern
    public void testObjectPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("o@i@n@");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-o", "java.lang.String", "-i", "java.util.Calendar", "-n", "System.DateTime" });

        assertEquals("o value", "", line.getOptionObject("o"));
        assertNull("i value", line.getOptionObject("i"));
        assertNull("n value", line.getOptionObject("n"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testURLPattern
    public void testURLPattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("u/v/");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-u", "http://commons.apache.org", "-v", "foo://commons.apache.org" });

        assertEquals("u value", new URL("http://commons.apache.org"), line.getOptionObject("u"));
        assertNull("v value", line.getOptionObject("v"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testExistingFilePattern
    public void testExistingFilePattern() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("f<");
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-f", "test.properties" });

        assertEquals("f value", new File("test.properties"), line.getOptionObject("f"));

        
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testRequiredOption
    public void testRequiredOption() throws Exception
    {
        Options options = PatternOptionBuilder.parsePattern("!n%m%");
        CommandLineParser parser = new PosixParser();

        try
        {
            parser.parse(options, new String[]{""});
            fail("MissingOptionException wasn't thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals(1, e.getMissingOptions().size());
            assertTrue(e.getMissingOptions().contains("n"));
        }
    }

// org.apache.commons.cli.PosixParserTest::testSimpleShort
    public void testSimpleShort() throws Exception
    {
        String[] args = new String[] { "-a",
                                       "-b", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.PosixParserTest::testSimpleLong
    public void testSimpleLong() throws Exception
    {
        String[] args = new String[] { "--enable-a",
                                       "--bfile", "toast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm arg of --bfile", cl.getOptionValue( "bfile" ).equals( "toast" ) );
        assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.PosixParserTest::testComplexShort
    public void testComplexShort() throws Exception
    {
        String[] args = new String[] { "-acbtoast",
                                       "foo", "bar" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.PosixParserTest::testUnrecognizedOption
    public void testUnrecognizedOption() throws Exception
    {
        String[] args = new String[] { "-adbtoast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-adbtoast", e.getOption());
        }
    }

// org.apache.commons.cli.PosixParserTest::testUnrecognizedOption2
    public void testUnrecognizedOption2() throws Exception
    {
        String[] args = new String[] { "-z", "-abtoast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-z", e.getOption());
        }
    }

// org.apache.commons.cli.PosixParserTest::testMissingArg
    public void testMissingArg() throws Exception
    {
        String[] args = new String[] { "-acb" };

        boolean caught = false;

        try
        {
            parser.parse(options, args);
        }
        catch (MissingArgumentException e)
        {
            caught = true;
            assertEquals("option missing an argument", "b", e.getOption().getOpt());
        }

        assertTrue( "Confirm MissingArgumentException caught", caught );
    }

// org.apache.commons.cli.PosixParserTest::testStop
    public void testStop() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foober",
                                       "-btoast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.PosixParserTest::testStop2
    public void testStop2() throws Exception
    {
        String[] args = new String[]{"-z",
                                     "-a",
                                     "-btoast"};

        CommandLine cl = parser.parse(options, args, true);
        assertFalse("Confirm -a is not set", cl.hasOption("a"));
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);
    }

// org.apache.commons.cli.PosixParserTest::testStop3
    public void testStop3() throws Exception
    {
        String[] args = new String[]{"--zop==1",
                                     "-abtoast",
                                     "--b=bar"};

        CommandLine cl = parser.parse(options, args, true);

        assertFalse("Confirm -a is not set", cl.hasOption("a"));
        assertFalse("Confirm -b is not set", cl.hasOption("b"));
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);
    }

// org.apache.commons.cli.PosixParserTest::testStopBursting
    public void testStopBursting() throws Exception
    {
        String[] args = new String[] { "-azc" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertFalse( "Confirm -c is not set", cl.hasOption("c") );

        assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue(cl.getArgList().contains("zc"));
    }

// org.apache.commons.cli.PosixParserTest::testMultiple
    public void testMultiple() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foobar",
                                       "-btoast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

        cl = parser.parse(options, cl.getArgs() );

        assertTrue( "Confirm -c is not set", ! cl.hasOption("c") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue( "Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar") );
    }

// org.apache.commons.cli.PosixParserTest::testMultipleWithLong
    public void testMultipleWithLong() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "foobar",
                                       "--bfile", "toast" };

        CommandLine cl = parser.parse(options,args, true);
        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);

        cl = parser.parse(options, cl.getArgs() );

        assertTrue( "Confirm -c is not set", ! cl.hasOption("c") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
        assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue( "Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar") );
    }

// org.apache.commons.cli.PosixParserTest::testDoubleDash
    public void testDoubleDash() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "--",
                                       "-b", "toast" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -c is set", cl.hasOption("c") );
        assertTrue( "Confirm -b is not set", ! cl.hasOption("b") );
        assertTrue( "Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
    }

// org.apache.commons.cli.PosixParserTest::testSingleDash
    public void testSingleDash() throws Exception
    {
        String[] args = new String[] { "--copt",
                                       "-b", "-",
                                       "-a",
                                       "-" };

        CommandLine cl = parser.parse(options, args);

        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertTrue( "Confirm -b is set", cl.hasOption("b") );
        assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("-") );
        assertTrue( "Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue( "Confirm value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("-") );
    }

// org.apache.commons.cli.PosixParserTest::testLongOptionWithShort
    public void testLongOptionWithShort() throws Exception {
        Option help = new Option("h", "help", false, "print this message");
        Option version = new Option("v", "version", false, "print version information");
        Option newRun = new Option("n", "new", false, "Create NLT cache entries only for new items");
        Option trackerRun = new Option("t", "tracker", false, "Create NLT cache entries only for tracker items");

        Option timeLimit = OptionBuilder.withLongOpt("limit").hasArg()
                                        .withValueSeparator()
                                        .withDescription("Set time limit for execution, in mintues")
                                        .create("l");

        Option age = OptionBuilder.withLongOpt("age").hasArg()
                                  .withValueSeparator()
                                  .withDescription("Age (in days) of cache item before being recomputed")
                                  .create("a");

        Option server = OptionBuilder.withLongOpt("server").hasArg()
                                     .withValueSeparator()
                                     .withDescription("The NLT server address")
                                     .create("s");

        Option numResults = OptionBuilder.withLongOpt("results").hasArg()
                                         .withValueSeparator()
                                         .withDescription("Number of results per item")
                                         .create("r");

        Option configFile = OptionBuilder.withLongOpt("file").hasArg()
                                         .withValueSeparator()
                                         .withDescription("Use the specified configuration file")
                                         .create();

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(newRun);
        options.addOption(trackerRun);
        options.addOption(timeLimit);
        options.addOption(age);
        options.addOption(server);
        options.addOption(numResults);
        options.addOption(configFile);

        
        CommandLineParser parser = new PosixParser();

        String[] args = new String[] {
                "-v",
                "-l",
                "10",
                "-age",
                "5",
                "-file",
                "filename"
            };

        CommandLine line = parser.parse(options, args);
        assertTrue(line.hasOption("v"));
        assertEquals(line.getOptionValue("l"), "10");
        assertEquals(line.getOptionValue("limit"), "10");
        assertEquals(line.getOptionValue("a"), "5");
        assertEquals(line.getOptionValue("age"), "5");
        assertEquals(line.getOptionValue("file"), "filename");
    }

// org.apache.commons.cli.PosixParserTest::testPropertiesOption
    public void testPropertiesOption() throws Exception
    {
        String[] args = new String[] { "-Jsource=1.5", "-J", "target", "1.5", "foo" };

        Options options = new Options();
        options.addOption(OptionBuilder.withValueSeparator().hasArgs(2).create('J'));

        Parser parser = new PosixParser();
        CommandLine cl = parser.parse(options, args);

        List values = Arrays.asList(cl.getOptionValues("J"));
        assertNotNull("null values", values);
        assertEquals("number of values", 4, values.size());
        assertEquals("value 1", "source", values.get(0));
        assertEquals("value 2", "1.5", values.get(1));
        assertEquals("value 3", "target", values.get(2));
        assertEquals("value 4", "1.5", values.get(3));
        List argsleft = cl.getArgList();
        assertEquals("Should be 1 arg left",1,argsleft.size());
        assertEquals("Expecting foo","foo",argsleft.get(0));
    }

// org.apache.commons.cli.ValueTest::testShortNoArg
    public void testShortNoArg()
    {
        assertTrue( _cl.hasOption("a") );
        assertNull( _cl.getOptionValue("a") );
    }

// org.apache.commons.cli.ValueTest::testShortWithArg
    public void testShortWithArg()
    {
        assertTrue( _cl.hasOption("b") );
        assertNotNull( _cl.getOptionValue("b") );
        assertEquals( _cl.getOptionValue("b"), "foo");
    }

// org.apache.commons.cli.ValueTest::testLongNoArg
    public void testLongNoArg()
    {
        assertTrue( _cl.hasOption("c") );
        assertNull( _cl.getOptionValue("c") );
    }

// org.apache.commons.cli.ValueTest::testLongWithArg
    public void testLongWithArg()
    {
        assertTrue( _cl.hasOption("d") );
        assertNotNull( _cl.getOptionValue("d") );
        assertEquals( _cl.getOptionValue("d"), "bar");
    }

// org.apache.commons.cli.ValueTest::testShortOptionalArgNoValue
    public void testShortOptionalArgNoValue() throws Exception
    {
        String[] args = new String[] { "-e" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("e") );
        assertNull( cmd.getOptionValue("e") );
    }

// org.apache.commons.cli.ValueTest::testShortOptionalArgValue
    public void testShortOptionalArgValue() throws Exception
    {
        String[] args = new String[] { "-e", "everything" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("e") );
        assertEquals( "everything", cmd.getOptionValue("e") );
    }

// org.apache.commons.cli.ValueTest::testLongOptionalNoValue
    public void testLongOptionalNoValue() throws Exception
    {
        String[] args = new String[] { "--fish" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("fish") );
        assertNull( cmd.getOptionValue("fish") );
    }

// org.apache.commons.cli.ValueTest::testLongOptionalArgValue
    public void testLongOptionalArgValue() throws Exception
    {
        String[] args = new String[] { "--fish", "face" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("fish") );
        assertEquals( "face", cmd.getOptionValue("fish") );
    }

// org.apache.commons.cli.ValueTest::testShortOptionalArgValues
    public void testShortOptionalArgValues() throws Exception
    {
        String[] args = new String[] { "-j", "ink", "idea" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("j") );
        assertEquals( "ink", cmd.getOptionValue("j") );
        assertEquals( "ink", cmd.getOptionValues("j")[0] );
        assertEquals( "idea", cmd.getOptionValues("j")[1] );
        assertEquals( cmd.getArgs().length, 0 );
    }

// org.apache.commons.cli.ValueTest::testLongOptionalArgValues
    public void testLongOptionalArgValues() throws Exception
    {
        String[] args = new String[] { "--gravy", "gold", "garden" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("gravy") );
        assertEquals( "gold", cmd.getOptionValue("gravy") );
        assertEquals( "gold", cmd.getOptionValues("gravy")[0] );
        assertEquals( "garden", cmd.getOptionValues("gravy")[1] );
        assertEquals( cmd.getArgs().length, 0 );
    }

// org.apache.commons.cli.ValueTest::testShortOptionalNArgValues
    public void testShortOptionalNArgValues() throws Exception
    {
        String[] args = new String[] { "-i", "ink", "idea", "isotope", "ice" };

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("i") );
        assertEquals( "ink", cmd.getOptionValue("i") );
        assertEquals( "ink", cmd.getOptionValues("i")[0] );
        assertEquals( "idea", cmd.getOptionValues("i")[1] );
        assertEquals( cmd.getArgs().length, 2 );
        assertEquals( "isotope", cmd.getArgs()[0] );
        assertEquals( "ice", cmd.getArgs()[1] );
    }

// org.apache.commons.cli.ValueTest::testLongOptionalNArgValues
    public void testLongOptionalNArgValues() throws Exception
    {
        String[] args = new String[] { 
            "--hide", "house", "hair", "head"
        };

        Parser parser = new PosixParser();

        CommandLine cmd = parser.parse(opts,args);
        assertTrue( cmd.hasOption("hide") );
        assertEquals( "house", cmd.getOptionValue("hide") );
        assertEquals( "house", cmd.getOptionValues("hide")[0] );
        assertEquals( "hair", cmd.getOptionValues("hide")[1] );
        assertEquals( cmd.getArgs().length, 1 );
        assertEquals( "head", cmd.getArgs()[0] );
    }

// org.apache.commons.cli.ValueTest::testPropertyOptionSingularValue
    public void testPropertyOptionSingularValue() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "hide", "seek" );

        Parser parser = new PosixParser();
        
        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("hide") );
        assertEquals( "seek", cmd.getOptionValue("hide") );
        assertTrue( !cmd.hasOption("fake") );
    }

// org.apache.commons.cli.ValueTest::testPropertyOptionFlags
    public void testPropertyOptionFlags() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "a", "true" );
        properties.setProperty( "c", "yes" );
        properties.setProperty( "e", "1" );

        Parser parser = new PosixParser();

        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("a") );
        assertTrue( cmd.hasOption("c") );
        assertTrue( cmd.hasOption("e") );

        properties = new Properties();
        properties.setProperty( "a", "false" );
        properties.setProperty( "c", "no" );
        properties.setProperty( "e", "0" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( !cmd.hasOption("e") );

        properties = new Properties();
        properties.setProperty( "a", "TRUE" );
        properties.setProperty( "c", "nO" );
        properties.setProperty( "e", "TrUe" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( cmd.hasOption("e") );

        
        properties = new Properties();
        properties.setProperty( "a", "just a string" );
        properties.setProperty( "e", "" );

        cmd = parser.parse(opts, null, properties);
        assertTrue( !cmd.hasOption("a") );
        assertTrue( !cmd.hasOption("c") );
        assertTrue( !cmd.hasOption("e") );
    }

// org.apache.commons.cli.ValueTest::testPropertyOptionMultipleValues
    public void testPropertyOptionMultipleValues() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty( "k", "one,two" );

        Parser parser = new PosixParser();
        
        String[] values = new String[] {
            "one", "two"
        };

        CommandLine cmd = parser.parse(opts, null, properties);
        assertTrue( cmd.hasOption("k") );
        assertTrue( Arrays.equals( values, cmd.getOptionValues('k') ) );
    }

// org.apache.commons.cli.ValueTest::testPropertyOverrideValues
    public void testPropertyOverrideValues() throws Exception
    {
        String[] args = new String[] { 
            "-j",
            "found",
            "-i",
            "ink"
        };

        Properties properties = new Properties();
        properties.setProperty( "j", "seek" );

        Parser parser = new PosixParser();
        CommandLine cmd = parser.parse(opts, args, properties);
        assertTrue( cmd.hasOption("j") );
        assertEquals( "found", cmd.getOptionValue("j") );
        assertTrue( cmd.hasOption("i") );
        assertEquals( "ink", cmd.getOptionValue("i") );
        assertTrue( !cmd.hasOption("fake") );
    }

// org.apache.commons.cli.ValuesTest::testShortArgs
    public void testShortArgs()
    {
        assertTrue( _cmdline.hasOption("a") );
        assertTrue( _cmdline.hasOption("c") );

        assertNull( _cmdline.getOptionValues("a") );
        assertNull( _cmdline.getOptionValues("c") );
    }

// org.apache.commons.cli.ValuesTest::testShortArgsWithValue
    public void testShortArgsWithValue()
    {
        assertTrue( _cmdline.hasOption("b") );
        assertTrue( _cmdline.getOptionValue("b").equals("foo"));
        assertEquals(1, _cmdline.getOptionValues("b").length);

        assertTrue( _cmdline.hasOption("d") );
        assertTrue( _cmdline.getOptionValue("d").equals("bar"));
        assertEquals(1, _cmdline.getOptionValues("d").length);
    }

// org.apache.commons.cli.ValuesTest::testMultipleArgValues
    public void testMultipleArgValues()
    {
        String[] result = _cmdline.getOptionValues("e");
        String[] values = new String[] { "one", "two" };
        assertTrue( _cmdline.hasOption("e") );
        assertEquals(2, _cmdline.getOptionValues("e").length);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("e") ) );
    }

// org.apache.commons.cli.ValuesTest::testTwoArgValues
    public void testTwoArgValues()
    {
        String[] result = _cmdline.getOptionValues("g");
        String[] values = new String[] { "val1", "val2" };
        assertTrue( _cmdline.hasOption("g") );
        assertEquals(2, _cmdline.getOptionValues("g").length);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("g") ) );
    }

// org.apache.commons.cli.ValuesTest::testComplexValues
    public void testComplexValues()
    {
        String[] result = _cmdline.getOptionValues("h");
        String[] values = new String[] { "val1", "val2" };
        assertTrue( _cmdline.hasOption("i") );
        assertTrue( _cmdline.hasOption("h") );
        assertEquals(2, _cmdline.getOptionValues("h").length);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("h") ) );
    }

// org.apache.commons.cli.ValuesTest::testExtraArgs
    public void testExtraArgs()
    {
        String[] args = new String[] { "arg1", "arg2", "arg3" };
        assertEquals(3, _cmdline.getArgs().length);
        assertTrue( Arrays.equals( args, _cmdline.getArgs() ) );
    }

// org.apache.commons.cli.ValuesTest::testCharSeparator
    public void testCharSeparator()
    {
        
        
        String[] values = new String[] { "key", "value", "key", "value" };
        assertTrue( _cmdline.hasOption( "j" ) );
        assertTrue( _cmdline.hasOption( 'j' ) );
        assertEquals( 4, _cmdline.getOptionValues( "j" ).length );
        assertEquals( 4, _cmdline.getOptionValues( 'j' ).length );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( "j" ) ) );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( 'j' ) ) );

        values = new String[] { "key1", "value1", "key2", "value2" };
        assertTrue( _cmdline.hasOption( "k" ) );
        assertTrue( _cmdline.hasOption( 'k' ) );
        assertEquals(4, _cmdline.getOptionValues( "k" ).length);
        assertEquals(4, _cmdline.getOptionValues( 'k' ).length);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( "k" ) ) );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( 'k' ) ) );

        values = new String[] { "key", "value" };
        assertTrue( _cmdline.hasOption( "m" ) );
        assertTrue( _cmdline.hasOption( 'm' ) );
        assertEquals(2, _cmdline.getOptionValues( "m" ).length);
        assertEquals(2, _cmdline.getOptionValues( 'm' ).length);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( "m" ) ) );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( 'm' ) ) );
    }

// org.apache.commons.cli.bug.BugCLI133Test::testOrder
    public void testOrder() throws ParseException {
        Option optionA = new Option("a", "first");
        Options opts = new Options();
        opts.addOption(optionA);
        PosixParser posixParser = new PosixParser();
        CommandLine line = posixParser.parse(opts, null);
        assertFalse(line.hasOption(null));
    }

// org.apache.commons.cli.bug.BugCLI13Test::testCLI13
    public void testCLI13() throws ParseException
    {
        final String debugOpt = "debug";
        Option debug = OptionBuilder
            .withArgName( debugOpt )
            .withDescription( "turn on debugging" )
            .withLongOpt( debugOpt )
            .hasArg()
            .create( 'd' );
        Options options = new Options();
        options.addOption( debug );
        CommandLine commandLine = new PosixParser().parse( options, new String[]{"-d", "true"} );

        assertEquals("true", commandLine.getOptionValue( debugOpt ));
        assertEquals("true", commandLine.getOptionValue( 'd' ));
        assertTrue(commandLine.hasOption( 'd'));
        assertTrue(commandLine.hasOption( debugOpt));
    }

// org.apache.commons.cli.bug.BugCLI148Test::testWorkaround1
    public void testWorkaround1() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t-something" };
        CommandLine commandLine;
        commandLine = parser.parse( options, args );
        assertEquals("-something", commandLine.getOptionValue( 't'));
    }

// org.apache.commons.cli.bug.BugCLI148Test::testWorkaround2
    public void testWorkaround2() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "\"-something\"" };
        CommandLine commandLine;
        commandLine = parser.parse( options, args );
        assertEquals("-something", commandLine.getOptionValue( 't'));
    }

// org.apache.commons.cli.bug.BugCLI51Test::test
    public void test() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[]{"-t", "-something"};

        CommandLine commandLine = parser.parse(options, args);
        assertEquals("-something", commandLine.getOptionValue('t'));
    }

// org.apache.commons.cli.bug.BugCLI71Test::testBasic
    public void testBasic() throws Exception {
        String[] args = new String[] { "-a", "Caesar", "-k", "A" };
        CommandLine line = parser.parse( options, args);
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "A", line.getOptionValue("k") );
    }

// org.apache.commons.cli.bug.BugCLI71Test::testMistakenArgument
    public void testMistakenArgument() throws Exception {
        String[] args = new String[] { "-a", "Caesar", "-k", "A" };
        CommandLine line = parser.parse( options, args);
        args = new String[] { "-a", "Caesar", "-k", "a" };
        line = parser.parse( options, args);
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "a", line.getOptionValue("k") );
    }

// org.apache.commons.cli.bug.BugCLI71Test::testLackOfError
    public void testLackOfError() throws Exception {
        String[] args = new String[] { "-k", "-a",  "Caesar" };
        try {
            CommandLine line = parser.parse( options, args);
            fail("MissingArgumentException expected");
        } catch(MissingArgumentException e) {
            assertEquals("option missing an argument", "k", e.getOption().getOpt());
        }
    }

// org.apache.commons.cli.bug.BugCLI71Test::testGetsDefaultIfOptional
    public void testGetsDefaultIfOptional() throws Exception {
        String[] args = new String[] { "-k", "-a", "Caesar" };
        options.getOption("k").setOptionalArg(true);
        CommandLine line = parser.parse( options, args);
        
        assertEquals( "Caesar", line.getOptionValue("a") );
        assertEquals( "a", line.getOptionValue("k", "a") );
    }
