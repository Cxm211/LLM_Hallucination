// buggy code
    protected StringBuffer renderWrappedText(StringBuffer sb, int width, 
                                             int nextLineTabStop, String text)
    {
        int pos = findWrapPos(text, width, 0);

        if (pos == -1)
        {
            sb.append(rtrim(text));

            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            int lastPos = pos;
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, 0);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            } else
            if (pos == lastPos)
            {
                throw new RuntimeException("Text too long for line - throwing exception to avoid infinite loop [CLI-162]: " + text);
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
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

// org.apache.commons.cli.bug.BugCLI162Test::testInfiniteLoop
    public void testInfiniteLoop() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(20);
        formatter.printHelp("app", options); 
    }

// org.apache.commons.cli.bug.BugCLI162Test::testPrintHelpLongLines
    public void testPrintHelpLongLines() throws ParseException, IOException {
        
        final String OPT = "-";

        final String OPT_COLUMN_NAMES = "l";

        final String OPT_CONNECTION = "c";

        final String OPT_DESCRIPTION = "e";

        final String OPT_DRIVER = "d";

        final String OPT_DRIVER_INFO = "n";

        final String OPT_FILE_BINDING = "b";

        final String OPT_FILE_JDBC = "j";

        final String OPT_FILE_SFMD = "f";

        final String OPT_HELP = "h";

        final String OPT_HELP_ = "help";

        final String OPT_INTERACTIVE = "i";

        final String OPT_JDBC_TO_SFMD = "2";

        final String OPT_JDBC_TO_SFMD_L = "jdbc2sfmd";

        final String OPT_METADATA = "m";

        final String OPT_PARAM_MODES_INT = "o";

        final String OPT_PARAM_MODES_NAME = "O";

        final String OPT_PARAM_NAMES = "a";

        final String OPT_PARAM_TYPES_INT = "y";

        final String OPT_PARAM_TYPES_NAME = "Y";

        final String OPT_PASSWORD = "p";

        final String OPT_PASSWORD_L = "password";

        final String OPT_SQL = "s";

        final String OPT_SQL_L = "sql";

        final String OPT_SQL_SPLIT_DEFAULT = "###";

        final String OPT_SQL_SPLIT_L = "splitSql";

        final String OPT_STACK_TRACE = "t";

        final String OPT_TIMING = "g";

        final String OPT_TRIM_L = "trim";

        final String OPT_USER = "u";

        final String OPT_WRITE_TO_FILE = "w";
        
        final String _PMODE_IN = "IN";

        final String _PMODE_INOUT = "INOUT";

        final String _PMODE_OUT = "OUT";
        
        final String _PMODE_UNK = "Unknown";
        
        final String PMODES = _PMODE_IN + ", " + _PMODE_INOUT + ", " + _PMODE_OUT + ", " + _PMODE_UNK;
        
        
        Options commandLineOptions;
        commandLineOptions = new Options();
        commandLineOptions.addOption(OPT_HELP, OPT_HELP_, false, "Prints help and quits");
        commandLineOptions.addOption(OPT_DRIVER, "driver", true, "JDBC driver class name");
        commandLineOptions.addOption(OPT_DRIVER_INFO, "info", false, "Prints driver information and properties. If "
            + OPT
            + OPT_CONNECTION
            + " is not specified, all drivers on the classpath are displayed.");
        commandLineOptions.addOption(OPT_CONNECTION, "url", true, "Connection URL");
        commandLineOptions.addOption(OPT_USER, "user", true, "A database user name");
        commandLineOptions
                .addOption(
                        OPT_PASSWORD,
                        OPT_PASSWORD_L,
                        true,
                        "The database password for the user specified with the "
                            + OPT
                            + OPT_USER
                            + " option. You can obfuscate the password with org.mortbay.jetty.security.Password, see http://docs.codehaus.org/display/JETTY/Securing+Passwords");
        commandLineOptions.addOption(OPT_SQL, OPT_SQL_L, true, "Runs SQL or {call stored_procedure(?, ?)} or {?=call function(?, ?)}");
        commandLineOptions.addOption(OPT_FILE_SFMD, "sfmd", true, "Writes a SFMD file for the given SQL");
        commandLineOptions.addOption(OPT_FILE_BINDING, "jdbc", true, "Writes a JDBC binding node file for the given SQL");
        commandLineOptions.addOption(OPT_FILE_JDBC, "node", true, "Writes a JDBC node file for the given SQL (internal debugging)");
        commandLineOptions.addOption(OPT_WRITE_TO_FILE, "outfile", true, "Writes the SQL output to the given file");
        commandLineOptions.addOption(OPT_DESCRIPTION, "description", true,
                "SFMD description. A default description is used if omited. Example: " + OPT + OPT_DESCRIPTION + " \"Runs such and such\"");
        commandLineOptions.addOption(OPT_INTERACTIVE, "interactive", false,
                "Runs in interactive mode, reading and writing from the console, 'go' or '/' sends a statement");
        commandLineOptions.addOption(OPT_TIMING, "printTiming", false, "Prints timing information");
        commandLineOptions.addOption(OPT_METADATA, "printMetaData", false, "Prints metadata information");
        commandLineOptions.addOption(OPT_STACK_TRACE, "printStack", false, "Prints stack traces on errors");
        Option option = new Option(OPT_COLUMN_NAMES, "columnNames", true, "Column XML names; default names column labels. Example: "
            + OPT
            + OPT_COLUMN_NAMES
            + " \"cname1 cname2\"");
        commandLineOptions.addOption(option);
        option = new Option(OPT_PARAM_NAMES, "paramNames", true, "Parameter XML names; default names are param1, param2, etc. Example: "
            + OPT
            + OPT_PARAM_NAMES
            + " \"pname1 pname2\"");
        commandLineOptions.addOption(option);
        
        OptionGroup pOutTypesOptionGroup = new OptionGroup();
        String pOutTypesOptionGroupDoc = OPT + OPT_PARAM_TYPES_INT + " and " + OPT + OPT_PARAM_TYPES_NAME + " are mutually exclusive.";
        final String typesClassName = Types.class.getName();
        option = new Option(OPT_PARAM_TYPES_INT, "paramTypes", true, "Parameter types from "
            + typesClassName
            + ". "
            + pOutTypesOptionGroupDoc
            + " Example: "
            + OPT
            + OPT_PARAM_TYPES_INT
            + " \"-10 12\"");
        commandLineOptions.addOption(option);
        option = new Option(OPT_PARAM_TYPES_NAME, "paramTypeNames", true, "Parameter "
            + typesClassName
            + " names. "
            + pOutTypesOptionGroupDoc
            + " Example: "
            + OPT
            + OPT_PARAM_TYPES_NAME
            + " \"CURSOR VARCHAR\"");
        commandLineOptions.addOption(option);
        commandLineOptions.addOptionGroup(pOutTypesOptionGroup);
        
        OptionGroup modesOptionGroup = new OptionGroup();
        String modesOptionGroupDoc = OPT + OPT_PARAM_MODES_INT + " and " + OPT + OPT_PARAM_MODES_NAME + " are mutually exclusive.";
        option = new Option(OPT_PARAM_MODES_INT, "paramModes", true, "Parameters modes ("
            + ParameterMetaData.parameterModeIn
            + "=IN, "
            + ParameterMetaData.parameterModeInOut
            + "=INOUT, "
            + ParameterMetaData.parameterModeOut
            + "=OUT, "
            + ParameterMetaData.parameterModeUnknown
            + "=Unknown"
            + "). "
            + modesOptionGroupDoc
            + " Example for 2 parameters, OUT and IN: "
            + OPT
            + OPT_PARAM_MODES_INT
            + " \""
            + ParameterMetaData.parameterModeOut
            + " "
            + ParameterMetaData.parameterModeIn
            + "\"");
        modesOptionGroup.addOption(option);
        option = new Option(OPT_PARAM_MODES_NAME, "paramModeNames", true, "Parameters mode names ("
            + PMODES
            + "). "
            + modesOptionGroupDoc
            + " Example for 2 parameters, OUT and IN: "
            + OPT
            + OPT_PARAM_MODES_NAME
            + " \""
            + _PMODE_OUT
            + " "
            + _PMODE_IN
            + "\"");
        modesOptionGroup.addOption(option);
        commandLineOptions.addOptionGroup(modesOptionGroup);
        option = new Option(null, OPT_TRIM_L, true,
                "Trims leading and trailing spaces from all column values. Column XML names can be optionally specified to set which columns to trim.");
        option.setOptionalArg(true);
        commandLineOptions.addOption(option);
        option = new Option(OPT_JDBC_TO_SFMD, OPT_JDBC_TO_SFMD_L, true,
                "Converts the JDBC file in the first argument to an SMFD file specified in the second argument.");
        option.setArgs(2);
        commandLineOptions.addOption(option);
        this.testPrintHelp(commandLineOptions);
    }

// org.apache.commons.cli.bug.BugCLI18Test::testCLI18
    public void testCLI18()
    {
        Options options = new Options();
        options.addOption(new Option("a", "aaa", false, "aaaaaaa"));
        options.addOption(new Option(null, "bbb", false, "bbbbbbb dksh fkshd fkhs dkfhsdk fhskd hksdks dhfowehfsdhfkjshf skfhkshf sf jkshfk sfh skfh skf f"));
        options.addOption(new Option("c", null, false, "ccccccc"));

        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();

        formatter.printHelp(new PrintWriter(out), 80, "foobar", "dsfkfsh kdh hsd hsdh fkshdf ksdh fskdh fsdh fkshfk sfdkjhskjh fkjh fkjsh khsdkj hfskdhf skjdfh ksf khf s", options, 2, 2, "blort j jgj j jg jhghjghjgjhgjhg jgjhgj jhg jhg hjg jgjhghjg jhg hjg jhgjg jgjhghjg jg jgjhgjgjg jhg jhgjh" + '\r' + '\n' + "rarrr", true);
    }
