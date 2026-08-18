// buggy code
    public static Option create(String opt) throws IllegalArgumentException
    {
            // create the option
        Option option = new Option(opt, description);

            // set the option properties
            option.setLongOpt(longopt);
            option.setRequired(required);
            option.setOptionalArg(optionalArg);
            option.setArgs(numberOfArgs);
            option.setType(type);
            option.setValueSeparator(valuesep);
            option.setArgName(argName);
            // reset the OptionBuilder properties
            OptionBuilder.reset();

        // return the Option instance
        return option;
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
                                        .hasArg()
                                        .withArgName("SIZE")
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

// org.apache.commons.cli.ApplicationTest::testGroovy
    public void testGroovy() throws Exception {
        Options options = new Options();

        options.addOption(
            OptionBuilder.withLongOpt("define").
                withDescription("define a system property").
                hasArg(true).
                withArgName("name=value").
                create('D'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("usage information")
            .withLongOpt("help")
            .create('h'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("debug mode will print out full stack traces")
            .withLongOpt("debug")
            .create('d'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("display the Groovy and JVM versions")
            .withLongOpt("version")
            .create('v'));
        options.addOption(
            OptionBuilder.withArgName("charset")
            .hasArg()
            .withDescription("specify the encoding of the files")
            .withLongOpt("encoding")
            .create('c'));
        options.addOption(
            OptionBuilder.withArgName("script")
            .hasArg()
            .withDescription("specify a command line script")
            .create('e'));
        options.addOption(
            OptionBuilder.withArgName("extension")
            .hasOptionalArg()
            .withDescription("modify files in place; create backup if extension is given (e.g. \'.bak\')")
            .create('i'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("process files line by line using implicit 'line' variable")
            .create('n'));
        options.addOption(
            OptionBuilder.hasArg(false)
            .withDescription("process files line by line and print result (see also -n)")
            .create('p'));
        options.addOption(
            OptionBuilder.withArgName("port")
            .hasOptionalArg()
            .withDescription("listen on a port and process inbound lines")
            .create('l'));
        options.addOption(
            OptionBuilder.withArgName("splitPattern")
            .hasOptionalArg()
            .withDescription("split lines using splitPattern (default '\\s') using implicit 'split' variable")
            .withLongOpt("autosplit")
            .create('a'));

        Parser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-e", "println 'hello'" }, true);

        assertTrue(line.hasOption('e'));
        assertEquals("println 'hello'", line.getOptionValue('e'));
    }

// org.apache.commons.cli.ApplicationTest::testMan
    public void testMan()
    {
        String cmdLine =
                "man [-c|-f|-k|-w|-tZT device] [-adlhu7V] [-Mpath] [-Ppager] [-Slist] " +
                        "[-msystem] [-pstring] [-Llocale] [-eextension] [section] page ...";
        Options options = new Options().
                addOption("a", "all", false, "find all matching manual pages.").
                addOption("d", "debug", false, "emit debugging messages.").
                addOption("e", "extension", false, "limit search to extension type 'extension'.").
                addOption("f", "whatis", false, "equivalent to whatis.").
                addOption("k", "apropos", false, "equivalent to apropos.").
                addOption("w", "location", false, "print physical location of man page(s).").
                addOption("l", "local-file", false, "interpret 'page' argument(s) as local filename(s)").
                addOption("u", "update", false, "force a cache consistency check.").
                
                addOption("r", "prompt", true, "provide 'less' pager with prompt.").
                addOption("c", "catman", false, "used by catman to reformat out of date cat pages.").
                addOption("7", "ascii", false, "display ASCII translation or certain latin1 chars.").
                addOption("t", "troff", false, "use troff format pages.").
                
                addOption("T", "troff-device", true, "use groff with selected device.").
                addOption("Z", "ditroff", false, "use groff with selected device.").
                addOption("D", "default", false, "reset all options to their default values.").
                
                addOption("M", "manpath", true, "set search path for manual pages to 'path'.").
                
                addOption("P", "pager", true, "use program 'pager' to display output.").
                
                addOption("S", "sections", true, "use colon separated section list.").
                
                addOption("m", "systems", true, "search for man pages from other unix system(s).").
                
                addOption("L", "locale", true, "define the locale for this particular man search.").
                
                addOption("p", "preprocessor", true, "string indicates which preprocessor to run.\n" +
                         " e - [n]eqn  p - pic     t - tbl\n" +
                         " g - grap    r - refer   v - vgrind").
                addOption("V", "version", false, "show version.").
                addOption("h", "help", false, "show this usage message.");

        HelpFormatter hf = new HelpFormatter();
        
        hf.printHelp(60, cmdLine, null, options, null);
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

// org.apache.commons.cli.BugsTest::test27635
    public void test27635() {}

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

// org.apache.commons.cli.HelpFormatterTest::testFindWrapPos
    public void testFindWrapPos() throws Exception
    {
        HelpFormatter hf = new HelpFormatter();

        String text = "This is a test.";
        
        assertEquals("wrap position", 7, hf.findWrapPos(text, 8, 0));
        
        assertEquals("wrap position 2", -1, hf.findWrapPos(text, 8, 8));
        
        text = "aaaa aa";
        assertEquals("wrap position 3", 4, hf.findWrapPos(text, 3, 0));
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintWrapped
    public void testPrintWrapped() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        HelpFormatter hf = new HelpFormatter();

        String text = "This is a test.";

        String expected = "This is a" + hf.getNewLine() + "test.";
        hf.renderWrappedText(sb, 12, 0, text);
        assertEquals("single line text", expected, sb.toString());

        sb.setLength(0);
        expected = "This is a" + hf.getNewLine() + "    test.";
        hf.renderWrappedText(sb, 12, 4, text);
        assertEquals("single line padded text", expected, sb.toString());

        text = "  -p,--period <PERIOD>  PERIOD is time duration of form " +
               "DATE[-DATE] where DATE has form YYYY[MM[DD]]";

        sb.setLength(0);
        expected = "  -p,--period <PERIOD>  PERIOD is time duration of" +
                hf.getNewLine() +
                "                        form DATE[-DATE] where DATE" +
                hf.getNewLine() +
                "                        has form YYYY[MM[DD]]";
        hf.renderWrappedText(sb, 53, 24, text);
        assertEquals("single line padded text 2", expected, sb.toString());

        text = "aaaa aaaa aaaa" + hf.getNewLine() +
               "aaaaaa" + hf.getNewLine() +
               "aaaaa";

        expected = text;
        sb.setLength(0);
        hf.renderWrappedText(sb, 16, 0, text);
        assertEquals("multi line text", expected, sb.toString());

        expected = "aaaa aaaa aaaa" + hf.getNewLine() +
                   "    aaaaaa" + hf.getNewLine() +
                   "    aaaaa";
        sb.setLength(0);
        hf.renderWrappedText(sb, 16, 4, text);
        assertEquals("multi-line padded text", expected, sb.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintOptions
    public void testPrintOptions() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        HelpFormatter hf = new HelpFormatter();
        final int leftPad = 1;
        final int descPad = 3;
        final String lpad = hf.createPadding(leftPad);
        final String dpad = hf.createPadding(descPad);
        Options options = null;
        String expected = null;

        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa");
        expected = lpad + "-a" + dpad + "aaaa aaaa aaaa aaaa aaaa";
        hf.renderOptions(sb, 60, options, leftPad, descPad);
        assertEquals("simple non-wrapped option", expected, sb.toString());

        int nextLineTabStop = leftPad + descPad + "-a".length();
        expected = lpad + "-a" + dpad + "aaaa aaaa aaaa" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "aaaa aaaa";
        sb.setLength(0);
        hf.renderOptions(sb, nextLineTabStop + 17, options, leftPad, descPad);
        assertEquals("simple wrapped option", expected, sb.toString());

        options = new Options().addOption("a", "aaa", false, "dddd dddd dddd dddd");
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd dddd dddd";
        sb.setLength(0);
        hf.renderOptions(sb, 60, options, leftPad, descPad);
        assertEquals("long non-wrapped option", expected, sb.toString());

        nextLineTabStop = leftPad + descPad + "-a,--aaa".length();
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "dddd dddd";
        sb.setLength(0);
        hf.renderOptions(sb, 25, options, leftPad, descPad);
        assertEquals("long wrapped option", expected, sb.toString());

        options = new Options().
                addOption("a", "aaa", false, "dddd dddd dddd dddd").
                addOption("b", false, "feeee eeee eeee eeee");
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "dddd dddd" + hf.getNewLine() +
                   lpad + "-b      " + dpad + "feeee eeee" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "eeee eeee";
        sb.setLength(0);
        hf.renderOptions(sb, 25, options, leftPad, descPad);
        assertEquals("multiple wrapped options", expected, sb.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintHelpWithEmptySyntax
    public void testPrintHelpWithEmptySyntax()
    {
        HelpFormatter formatter = new HelpFormatter();
        try
        {
            formatter.printHelp(null, new Options());
            fail("null command line syntax should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            
        }

        try
        {
            formatter.printHelp("", new Options());
            fail("empty command line syntax should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            
        }
    }

// org.apache.commons.cli.HelpFormatterTest::testAutomaticUsage
    public void testAutomaticUsage() throws Exception
    {
        HelpFormatter hf = new HelpFormatter();
        Options options = null;
        String expected = "usage: app [-a]";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa");
        hf.printUsage(pw, 60, "app", options);
        pw.flush();
        assertEquals("simple auto usage", expected, out.toString().trim());
        out.reset();

        expected = "usage: app [-a] [-b]";
        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa")
                .addOption("b", false, "bbb");
        hf.printUsage(pw, 60, "app", options);
        pw.flush();
        assertEquals("simple auto usage", expected, out.toString().trim());
        out.reset();
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintUsage
    public void testPrintUsage()
    {
        Option optionA = new Option("a", "first");
        Option optionB = new Option("b", "second");
        Option optionC = new Option("c", "third");
        Options opts = new Options();
        opts.addOption(optionA);
        opts.addOption(optionB);
        opts.addOption(optionC);
        HelpFormatter helpFormatter = new HelpFormatter();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bytesOut);
        helpFormatter.printUsage(printWriter, 80, "app", opts);
        printWriter.close();
        assertEquals("usage: app [-a] [-b] [-c]" + EOL, bytesOut.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintSortedUsage
    public void testPrintSortedUsage()
    {
        Options opts = new Options();
        opts.addOption(new Option("a", "first"));
        opts.addOption(new Option("b", "second"));
        opts.addOption(new Option("c", "third"));

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        StringWriter out = new StringWriter();
        helpFormatter.printUsage(new PrintWriter(out), 80, "app", opts);

        assertEquals("usage: app [-c] [-b] [-a]" + EOL, out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintSortedUsageWithNullComparator
    public void testPrintSortedUsageWithNullComparator()
    {
        Options opts = new Options();
        opts.addOption(new Option("a", "first"));
        opts.addOption(new Option("b", "second"));
        opts.addOption(new Option("c", "third"));

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(null);

        StringWriter out = new StringWriter();
        helpFormatter.printUsage(new PrintWriter(out), 80, "app", opts);

        assertEquals("usage: app [-a] [-b] [-c]" + EOL, out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintOptionGroupUsage
    public void testPrintOptionGroupUsage()
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        group.addOption(OptionBuilder.create("c"));

        Options options = new Options();
        options.addOptionGroup(group);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app [-a | -b | -c]" + EOL, out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintRequiredOptionGroupUsage
    public void testPrintRequiredOptionGroupUsage()
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        group.addOption(OptionBuilder.create("c"));
        group.setRequired(true);

        Options options = new Options();
        options.addOptionGroup(group);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app -a | -b | -c" + EOL, out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testPrintOptionWithEmptyArgNameUsage
    public void testPrintOptionWithEmptyArgNameUsage()
    {
        Option option = new Option("f", true, null);
        option.setArgName("");
        option.setRequired(true);

        Options options = new Options();
        options.addOption(option);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app -f" + EOL, out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testRtrim
    public void testRtrim()
    {
        HelpFormatter formatter = new HelpFormatter();

        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("  foo", formatter.rtrim("  foo  "));
    }

// org.apache.commons.cli.HelpFormatterTest::testAccessors
    public void testAccessors()
    {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setArgName("argname");
        assertEquals("arg name", "argname", formatter.getArgName());

        formatter.setDescPadding(3);
        assertEquals("desc padding", 3, formatter.getDescPadding());

        formatter.setLeftPadding(7);
        assertEquals("left padding", 7, formatter.getLeftPadding());

        formatter.setLongOptPrefix("~~");
        assertEquals("long opt prefix", "~~", formatter.getLongOptPrefix());

        formatter.setNewLine("\n");
        assertEquals("new line", "\n", formatter.getNewLine());

        formatter.setOptPrefix("~");
        assertEquals("opt prefix", "~", formatter.getOptPrefix());

        formatter.setSyntaxPrefix("-> ");
        assertEquals("syntax prefix", "-> ", formatter.getSyntaxPrefix());

        formatter.setWidth(80);
        assertEquals("width", 80, formatter.getWidth());
    }

// org.apache.commons.cli.HelpFormatterTest::testHeaderStartingWithLineSeparator
    public void testHeaderStartingWithLineSeparator()
    {
        
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        String header = EOL + "Header";
        String footer = "Footer";
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, "foobar", header, options, 2, 2, footer, true);
        assertEquals(
                "usage: foobar" + EOL +
                "" + EOL +
                "Header" + EOL +
                "" + EOL +
                "Footer" + EOL
                , out.toString());
    }

// org.apache.commons.cli.HelpFormatterTest::testOptionWithoutShortFormat
    public void testOptionWithoutShortFormat()
    {
        
        Options options = new Options();
        options.addOption(new Option("a", "aaa", false, "aaaaaaa"));
        options.addOption(new Option(null, "bbb", false, "bbbbbbb"));
        options.addOption(new Option("c", null, false, "ccccccc"));

        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, "foobar", "", options, 2, 2, "", true);
        assertEquals(
                "usage: foobar [-a] [--bbb] [-c]" + EOL +
                "  -a,--aaa  aaaaaaa" + EOL +
                "     --bbb  bbbbbbb" + EOL +
                "  -c        ccccccc" + EOL
                , out.toString());
    }

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

// org.apache.commons.cli.OptionBuilderTest::testCompleteOption
    public void testCompleteOption( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasArgs() );
    }

// org.apache.commons.cli.OptionBuilderTest::testTwoCompleteOptions
    public void testTwoCompleteOptions( ) {
        Option simple = OptionBuilder.withLongOpt( "simple option")
                                     .hasArg( )
                                     .isRequired( )
                                     .hasArgs( )
                                     .withType( new Float( 10 ) )
                                     .withDescription( "this is a simple option" )
                                     .create( 's' );

        assertEquals( "s", simple.getOpt() );
        assertEquals( "simple option", simple.getLongOpt() );
        assertEquals( "this is a simple option", simple.getDescription() );
        assertEquals( simple.getType().getClass(), Float.class );
        assertTrue( simple.hasArg() );
        assertTrue( simple.isRequired() );
        assertTrue( simple.hasArgs() );

        simple = OptionBuilder.withLongOpt( "dimple option")
                              .hasArg( )
                              .withDescription( "this is a dimple option" )
                              .create( 'd' );

        assertEquals( "d", simple.getOpt() );
        assertEquals( "dimple option", simple.getLongOpt() );
        assertEquals( "this is a dimple option", simple.getDescription() );
        assertNull( simple.getType() );
        assertTrue( simple.hasArg() );
        assertTrue( !simple.isRequired() );
        assertTrue( !simple.hasArgs() );
    }

// org.apache.commons.cli.OptionBuilderTest::testBaseOptionCharOpt
    public void testBaseOptionCharOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( 'o' );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
        assertTrue( !base.hasArg() );
    }

// org.apache.commons.cli.OptionBuilderTest::testBaseOptionStringOpt
    public void testBaseOptionStringOpt() {
        Option base = OptionBuilder.withDescription( "option description")
                                   .create( "o" );

        assertEquals( "o", base.getOpt() );
        assertEquals( "option description", base.getDescription() );
        assertTrue( !base.hasArg() );
    }

// org.apache.commons.cli.OptionBuilderTest::testSpecialOptChars
    public void testSpecialOptChars() throws Exception
    {
        
        Option opt1 = OptionBuilder.withDescription("help options").create('?');
        assertEquals("?", opt1.getOpt());

        
        Option opt2 = OptionBuilder.withDescription("read from stdin").create('@');
        assertEquals("@", opt2.getOpt());
    }

// org.apache.commons.cli.OptionBuilderTest::testOptionArgNumbers
    public void testOptionArgNumbers()
    {
        Option opt = OptionBuilder.withDescription( "option description" )
                                  .hasArgs( 2 )
                                  .create( 'o' );
        assertEquals( 2, opt.getArgs() );
    }

// org.apache.commons.cli.OptionBuilderTest::testIllegalOptions
    public void testIllegalOptions() {
        
        try {
            OptionBuilder.withDescription( "option description" ).create( '"' );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            
        }

        
        try {
            Option opt = OptionBuilder.create( "opt`" );
            fail( "IllegalArgumentException not caught" );
        }
        catch( IllegalArgumentException exp ) {
            
        }

        
        try {
            Option opt = OptionBuilder.create( "opt" );
            
        }
        catch( IllegalArgumentException exp ) {
            fail( "IllegalArgumentException caught" );
        }
    }

// org.apache.commons.cli.OptionBuilderTest::testCreateIncompleteOption
    public void testCreateIncompleteOption() {
        try
        {
            OptionBuilder.hasArg().create();
            fail("Incomplete option should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            
            
            
            OptionBuilder.create( "opt" );
        }
    }

// org.apache.commons.cli.OptionBuilderTest::testBuilderIsResettedAlways
    public void testBuilderIsResettedAlways() {
        try
        {
            OptionBuilder.withDescription("JUnit").create('"');
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());

        try
        {
            OptionBuilder.withDescription("JUnit").create();
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());
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
    public void testToString()
    {
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option(null, "foo", false, "Foo"));
        group1.addOption(new Option(null, "bar", false, "Bar"));

        if (!"[--bar Bar, --foo Foo]".equals(group1.toString())) {
            assertEquals("[--foo Foo, --bar Bar]", group1.toString());
        }

        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("f", "foo", false, "Foo"));
        group2.addOption(new Option("b", "bar", false, "Bar"));

        if (!"[-b Bar, -f Foo]".equals(group2.toString())) {
            assertEquals("[-f Foo, -b Bar]", group2.toString());
        }
    }

// org.apache.commons.cli.OptionGroupTest::testGetNames
    public void testGetNames()
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create('a'));
        group.addOption(OptionBuilder.create('b'));

        assertNotNull("null names", group.getNames());
        assertEquals(2, group.getNames().size());
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getNames().contains("b"));
    }

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

// org.apache.commons.cli.OptionsTest::testGetOptionsGroups
    public void testGetOptionsGroups()
    {
        Options options = new Options();

        OptionGroup group1 = new OptionGroup();
        group1.addOption(OptionBuilder.create('a'));
        group1.addOption(OptionBuilder.create('b'));

        OptionGroup group2 = new OptionGroup();
        group2.addOption(OptionBuilder.create('x'));
        group2.addOption(OptionBuilder.create('y'));

        options.addOptionGroup(group1);
        options.addOptionGroup(group2);

        assertNotNull(options.getOptionGroups());
        assertEquals(2, options.getOptionGroups().size());
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
        Options options = PatternOptionBuilder.parsePattern("a:b@cde>f+n%t/m*z#");
        String[] args = new String[] {"-c", "-a", "foo", "-b", "java.util.Vector", "-e", "build.xml", "-f", "java.util.Calendar", "-n", "4.5", "-t", "http://commons.apache.org", "-z", "Thu Jun 06 17:48:57 EDT 2002", "-m", "test*"};

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
        assertEquals("url flag t", new URL("http://commons.apache.org"), line.getOptionObject("t"));

        
        assertEquals("flag a", "foo", line.getOptionValue('a'));
        assertEquals("string flag a", "foo", line.getOptionObject('a'));
        assertEquals("object flag b", new Vector(), line.getOptionObject('b'));
        assertTrue("boolean true flag c", line.hasOption('c'));
        assertFalse("boolean false flag d", line.hasOption('d'));
        assertEquals("file flag e", new File("build.xml"), line.getOptionObject('e'));
        assertEquals("class flag f", Calendar.class, line.getOptionObject('f'));
        assertEquals("number flag n", new Double(4.5), line.getOptionObject('n'));
        assertEquals("url flag t", new URL("http://commons.apache.org"), line.getOptionObject('t'));

        
        try {
            assertEquals("files flag m", new File[0], line.getOptionObject('m'));
            fail("Multiple files are not supported yet, should have failed");
        } catch(UnsupportedOperationException uoe) {
            
        }

        
        try {
            assertEquals("date flag z", new Date(1023400137276L), line.getOptionObject('z'));
            fail("Date is not supported yet, should have failed");
        } catch(UnsupportedOperationException uoe) {
            
        }
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

// org.apache.commons.cli.PosixParserTest::testBursting
    public void testBursting() throws Exception
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

// org.apache.commons.cli.PosixParserTest::testUnrecognizedOptionWithBursting
    public void testUnrecognizedOptionWithBursting() throws Exception
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

// org.apache.commons.cli.PosixParserTest::testMissingArgWithBursting
    public void testMissingArgWithBursting() throws Exception
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

// org.apache.commons.cli.PosixParserTest::testStopBursting2
    public void testStopBursting2() throws Exception
    {
        String[] args = new String[] { "-c",
                                       "foobar",
                                       "-btoast" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertTrue("Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

        cl = parser.parse(options, cl.getArgs());

        assertTrue("Confirm -c is not set", !cl.hasOption("c"));
        assertTrue("Confirm -b is set", cl.hasOption("b"));
        assertTrue("Confirm arg of -b", cl.getOptionValue("b").equals("toast"));
        assertTrue("Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue("Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar"));
    }

// org.apache.commons.cli.PosixParserTest::testLongOptionWithShort
    public void testLongOptionWithShort() throws Exception {
        Option help = new Option("h", "help", false, "print this message");
        Option version = new Option("v", "version", false, "print version information");
        Option newRun = new Option("n", "new", false, "Create NLT cache entries only for new items");
        Option trackerRun = new Option("t", "tracker", false, "Create NLT cache entries only for tracker items");

        Option timeLimit = OptionBuilder.withLongOpt("limit").hasArg()
                                        .withValueSeparator()
                                        .withDescription("Set time limit for execution, in minutes")
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

// org.apache.commons.cli.PosixParserTest::testLongWithEqualSingleDash
    public void testLongWithEqualSingleDash() throws Exception
    {
        
    }

// org.apache.commons.cli.PosixParserTest::testShortWithEqual
    public void testShortWithEqual() throws Exception
    {
        
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
