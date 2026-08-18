// buggy code
    protected void burstToken(String token, boolean stopAtNonOption)
    {
        int tokenLength = token.length();

        for (int i = 1; i < tokenLength; i++)
        {
            String ch = String.valueOf(token.charAt(i));
            boolean hasOption = options.hasOption(ch);

            if (hasOption)
            {
                tokens.add("-" + ch);
                currentOption = options.getOption(ch);

                if (currentOption.hasArg() && (token.length() != (i + 1)))
                {
                    tokens.add(token.substring(i + 1));

                    break;
                }
            }
            else if (stopAtNonOption)
            {
                process(token.substring(i));
            }
            else
            {
                tokens.add("-" + ch);
            }
        }
    }

// relevant test
// org.apache.commons.cli.ApplicationTest::testLs
    public void testLs() {
        
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

        try {
            CommandLine line = parser.parse( options, args );
            assertTrue( line.hasOption( "block-size" ) );
            assertEquals( line.getOptionValue( "block-size" ), "10" );
        }
        catch( ParseException exp ) {
            fail( "Unexpected exception:" + exp.getMessage() );
        }
    }

// org.apache.commons.cli.ApplicationTest::testAnt
    public void testAnt() {
        
        CommandLineParser parser = new GnuParser( );
        Options options = new Options();
        options.addOption( "help", false, "print this message" );
        options.addOption( "projecthelp", false, "print project help information" );
        options.addOption( "version", false, "print the version information and exit" );
        options.addOption( "quiet", false, "be extra quiet" );
        options.addOption( "verbose", false, "be extra verbose" );
        options.addOption( "debug", false, "print debug information" );
        options.addOption( "version", false, "produce logging information without adornments" );
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

        try {
            CommandLine line = parser.parse( options, args );

            
            String[] opts = line.getOptionValues( "D" );
            assertEquals( "property", opts[0] );
            assertEquals( "value", opts[1] );
            assertEquals( "property1", opts[2] );
            assertEquals( "value1", opts[3] );

            
            assertEquals( line.getOptionValue( "buildfile"), "mybuild.xml" );

            
            assertTrue( line.hasOption( "projecthelp") );
        }
        catch( ParseException exp ) {
            fail( "Unexpected exception:" + exp.getMessage() );
        }

    }

// org.apache.commons.cli.ArgumentIsOptionTest::testOptionAndOptionWithArgument
    public void testOptionAndOptionWithArgument() {
        String[] args = new String[] {
                "-p",
                "-attr",
                "p"
            };

        try {
            CommandLine cl = parser.parse(options, args);
            assertTrue("Confirm -p is set", cl.hasOption("p"));
            assertTrue("Confirm -attr is set", cl.hasOption("attr"));
            assertTrue("Confirm arg of -attr",
                cl.getOptionValue("attr").equals("p"));
            assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
        }
        catch (ParseException e) {
            fail(e.toString());
        }
    }

// org.apache.commons.cli.ArgumentIsOptionTest::testOptionWithArgument
    public void testOptionWithArgument() {
        String[] args = new String[] {
                "-attr",
                "p"
            };

        try {
            CommandLine cl = parser.parse(options, args);
            assertFalse("Confirm -p is set", cl.hasOption("p"));
            assertTrue("Confirm -attr is set", cl.hasOption("attr"));
            assertTrue("Confirm arg of -attr",
                cl.getOptionValue("attr").equals("p"));
            assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
        }
        catch (ParseException e) {
            fail(e.toString());
        }
    }

// org.apache.commons.cli.ArgumentIsOptionTest::testOption
    public void testOption() {
        String[] args = new String[] {
                "-p"
            };

        try {
            CommandLine cl = parser.parse(options, args);
            assertTrue("Confirm -p is set", cl.hasOption("p"));
            assertFalse("Confirm -attr is not set", cl.hasOption("attr"));
            assertTrue("Confirm all arguments recognized", cl.getArgs().length == 0);
        }
        catch (ParseException e) {
            fail(e.toString());
        }
    }

// org.apache.commons.cli.BugsTest::test11457
    public void test11457() {
        Options options = new Options();
        options.addOption( OptionBuilder.withLongOpt( "verbose" )
                           .create() );
        String[] args = new String[] { "--verbose" };

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine cmd = parser.parse( options, args );
            assertTrue( cmd.hasOption( "verbose" ) );
        }        
        catch( ParseException exp ) {
            exp.printStackTrace();
            fail( "Unexpected Exception: " + exp.getMessage() );
        }
    }

// org.apache.commons.cli.BugsTest::test11458
    public void test11458()
    {
        Options options = new Options();
        options.addOption( OptionBuilder.withValueSeparator( '=' )
                           .hasArgs()
                           .create( 'D' ) );
        options.addOption( OptionBuilder.withValueSeparator( ':' )
                           .hasArgs()
                           .create( 'p' ) );
        String[] args = new String[] { "-DJAVA_HOME=/opt/java" ,
        "-pfile1:file2:file3" };

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine cmd = parser.parse( options, args );

            String[] values = cmd.getOptionValues( 'D' );

            assertEquals( values[0], "JAVA_HOME" );
            assertEquals( values[1], "/opt/java" );

            values = cmd.getOptionValues( 'p' );

            assertEquals( values[0], "file1" );
            assertEquals( values[1], "file2" );
            assertEquals( values[2], "file3" );

            java.util.Iterator iter = cmd.iterator();
            while( iter.hasNext() ) {
                Option opt = (Option)iter.next();
                switch( opt.getId() ) {
                    case 'D':
                        assertEquals( opt.getValue( 0 ), "JAVA_HOME" );
                        assertEquals( opt.getValue( 1 ), "/opt/java" );
                        break;
                    case 'p':
                        assertEquals( opt.getValue( 0 ), "file1" );
                        assertEquals( opt.getValue( 1 ), "file2" );
                        assertEquals( opt.getValue( 2 ), "file3" );
                        break;
                    default:
                        fail( "-D option not found" );
                }
            }
        }
        catch( ParseException exp ) {
            fail( "Unexpected Exception:\nMessage:" + exp.getMessage() 
                  + "Type: " + exp.getClass().getName() );
        }
    }

// org.apache.commons.cli.BugsTest::test11680
    public void test11680()
    {
        Options options = new Options();
        options.addOption("f", true, "foobar");
	options.addOption("m", true, "missing");
        String[] args = new String[] { "-f" , "foo" };

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine cmd = parser.parse( options, args );

            try {
                cmd.getOptionValue( "f", "default f");
                cmd.getOptionValue( "m", "default m");
            }
            catch( NullPointerException exp ) {
                fail( "NullPointer caught: " + exp.getMessage() );
            }
        }
        catch( ParseException exp ) {
            fail( "Unexpected Exception: " + exp.getMessage() );
        }
    }

// org.apache.commons.cli.BugsTest::test11456
    public void test11456()
    {
        
        Options options = new Options();
        options.addOption( OptionBuilder.hasOptionalArg()
                           .create( 'a' ) );
        options.addOption( OptionBuilder.hasArg()
                           .create( 'b' ) );
        String[] args = new String[] { "-a", "-bvalue" };

        CommandLineParser parser = new PosixParser();

        try {
            CommandLine cmd = parser.parse( options, args );
            assertEquals( cmd.getOptionValue( 'b' ), "value" );
        }
        catch( ParseException exp ) {
            fail( "Unexpected Exception: " + exp.getMessage() );
        }

        
        options = new Options();
        options.addOption( OptionBuilder.hasOptionalArg()
                           .create( 'a' ) );
        options.addOption( OptionBuilder.hasArg()
                           .create( 'b' ) );
        args = new String[] { "-a", "-b", "value" };

        parser = new GnuParser();

        try {
            CommandLine cmd = parser.parse( options, args );
            assertEquals( cmd.getOptionValue( 'b' ), "value" );
        }
        catch( ParseException exp ) {
            fail( "Unexpected Exception: " + exp.getMessage() );
        }

    }

// org.apache.commons.cli.BugsTest::test12210
    public void test12210() {
        
        Options mainOptions = new Options();
        

        

        String[] argv = new String[] { "-exec", "-exec_opt1", "-exec_opt2" };
        OptionGroup grp = new OptionGroup();

        grp.addOption(new Option("exec",false,"description for this option"));

        grp.addOption(new Option("rep",false,"description for this option"));

        mainOptions.addOptionGroup(grp);

        
        Options execOptions = new Options();
        execOptions.addOption("exec_opt1",false," desc");
        execOptions.addOption("exec_opt2",false," desc");

        
        Options repOptions = new Options();
        repOptions.addOption("repopto",false,"desc");
        repOptions.addOption("repoptt",false,"desc");

        
        GnuParser parser = new GnuParser();

        

        
        
        
        try {
            CommandLine cmd = parser.parse(mainOptions,argv,true);
            
            argv = cmd.getArgs();

            if(cmd.hasOption("exec")){
                cmd = parser.parse(execOptions,argv,false);
                
                assertTrue( cmd.hasOption("exec_opt1") );
                assertTrue( cmd.hasOption("exec_opt2") );
            }
            else if(cmd.hasOption("rep")){
                cmd = parser.parse(repOptions,argv,false);
                
            }
            else {
                fail( "exec option not found" );
            }
        }
        catch( ParseException exp ) {
            fail( "Unexpected exception: " + exp.getMessage() );
        }
    }

// org.apache.commons.cli.BugsTest::test13425
    public void test13425() {
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

        try {
            CommandLine line = parser.parse( options, args );
        }
        
        catch( Exception exp ) {
            assertTrue( exp != null );
            return;
        }
        fail( "MissingArgumentException not caught." );
    }

// org.apache.commons.cli.BugsTest::test13666
    public void test13666() {
        Options options = new Options();
        Option dir = OptionBuilder.withDescription( "dir" )
                                       .hasArg()
                                       .create( 'd' );
        options.addOption( dir );
        
        
        final PrintStream oldSystemOut = System.out;
        try{
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final PrintStream print = new PrintStream(bytes);
            
            
            print.println();
            final String eol = bytes.toString();
            bytes.reset();
            
            System.setOut(new PrintStream(bytes));
            try {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "dir", options );
            }
            catch( Exception exp ) {
                fail( "Unexpected Exception: " + exp.getMessage() );
            }
            assertEquals("usage: dir"+eol+" -d <arg>   dir"+eol,bytes.toString());
        }
        finally {
            System.setOut(oldSystemOut);
        }
    }

// org.apache.commons.cli.BugsTest::test13935
    public void test13935() {
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
        try {
            CommandLine line = parser.parse( opts, args );
        }
        catch( ParseException exp ) {
            fail( "Unexpected exception: " + exp.getClass().getName() + ":" + exp.getMessage() );
        }

        opts.addOption( forward );
        args = new String[] { "-s", "-l", "-f" };
        try {
            CommandLine line = parser.parse( opts, args );
        }
        catch( ParseException exp ) {
            fail( "Unexpected exception: " + exp.getClass().getName() + ":" + exp.getMessage() );
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
    public void test19383() {}

// org.apache.commons.cli.OptionGroupTest::testSingleOptionFromGroup
    public void testSingleOptionFromGroup()
    {
        String[] args = new String[] { "-f" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
            assertTrue( "Confirm -f is set", cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testSingleOption
    public void testSingleOption()
    {
        String[] args = new String[] { "-r" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is set", cl.hasOption("r") );
            assertTrue( "Confirm -f is NOT set", !cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoValidOptions
    public void testTwoValidOptions()
    {
        String[] args = new String[] { "-r", "-f" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is set", cl.hasOption("r") );
            assertTrue( "Confirm -f is set", cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testSingleLongOption
    public void testSingleLongOption()
    {
        String[] args = new String[] { "--file" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
            assertTrue( "Confirm -f is set", cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoValidLongOptions
    public void testTwoValidLongOptions()
    {
        String[] args = new String[] { "--revision", "--file" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is set", cl.hasOption("r") );
            assertTrue( "Confirm -f is set", cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm no extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testNoOptionsExtraArgs
    public void testNoOptionsExtraArgs()
    {
        String[] args = new String[] { "arg1", "arg2" };

        try
        {
            CommandLine cl = parser.parse( _options, args);

            assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
            assertTrue( "Confirm -f is NOT set", !cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is NOT set", !cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm TWO extra args", cl.getArgList().size() == 2);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoOptionsFromGroup
    public void testTwoOptionsFromGroup()
    {
        String[] args = new String[] { "-f", "-d" };

        try
        {
            CommandLine cl = parser.parse( _options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (ParseException e)
        {
            if( !( e instanceof AlreadySelectedException ) )
            {
                fail( "incorrect exception caught:" + e.getMessage() );
            }
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoLongOptionsFromGroup
    public void testTwoLongOptionsFromGroup()
    {
        String[] args = new String[] { "--file", "--directory" };

        try
        {
            CommandLine cl = parser.parse( _options, args);
            fail( "two arguments from group not allowed" );
        }
        catch (ParseException e)
        {
            if( !( e instanceof AlreadySelectedException ) )
            {
                fail( "incorrect exception caught:" + e.getMessage() );
            }
        }
    }

// org.apache.commons.cli.OptionGroupTest::testTwoOptionsFromDifferentGroup
    public void testTwoOptionsFromDifferentGroup()
    {
        String[] args = new String[] { "-f", "-s" };

        try
        {
            CommandLine cl = parser.parse( _options, args);
            assertTrue( "Confirm -r is NOT set", !cl.hasOption("r") );
            assertTrue( "Confirm -f is set", cl.hasOption("f") );
            assertTrue( "Confirm -d is NOT set", !cl.hasOption("d") );
            assertTrue( "Confirm -s is set", cl.hasOption("s") );
            assertTrue( "Confirm -c is NOT set", !cl.hasOption("c") );
            assertTrue( "Confirm NO extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.OptionGroupTest::testValidLongOnlyOptions
    public void testValidLongOnlyOptions()
    {
        try
        {
            CommandLine cl = parser.parse( _options, new String[]{"--export"});
            assertTrue( "Confirm --export is set", cl.hasOption("export") );
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
                            
        try
        {
            CommandLine cl = parser.parse( _options, new String[]{"--import"});
            assertTrue( "Confirm --import is set", cl.hasOption("import") );
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseRequiredTest::testWithRequiredOption
    public void testWithRequiredOption()
    {
        String[] args = new String[] {  "-b", "file" };

        try
        {
            CommandLine cl = parser.parse(_options,args);
            
            assertTrue( "Confirm -a is NOT set", !cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
            assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseRequiredTest::testOptionAndRequiredOption
    public void testOptionAndRequiredOption()
    {
        String[] args = new String[] {  "-a", "-b", "file" };

        try
        {
            CommandLine cl = parser.parse(_options,args);

            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("file") );
            assertTrue( "Confirm NO of extra args", cl.getArgList().size() == 0);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
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
        catch (ParseException e)
        {
            if( !( e instanceof MissingOptionException ) )
            {
                fail( "expected to catch MissingOptionException" );
            }
        }
    }

// org.apache.commons.cli.ParseTest::testSimpleShort
    public void testSimpleShort()
    {
        String[] args = new String[] { "-a",
                                       "-b", "toast",
                                       "foo", "bar" };

        try
        {
            CommandLine cl = _parser.parse(_options, args);
            
            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testSimpleLong
    public void testSimpleLong()
    {
        String[] args = new String[] { "--enable-a",
                                       "--bfile", "toast",
                                       "foo", "bar" };

        try
        {
            CommandLine cl = _parser.parse(_options, args);
            
            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm arg of --bfile", cl.getOptionValue( "bfile" ).equals( "toast" ) );
            assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
        } 
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testComplexShort
    public void testComplexShort()
    {
        String[] args = new String[] { "-acbtoast",
                                       "foo", "bar" };

        try
        {
            CommandLine cl = _parser.parse(_options, args);
            
            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm -c is set", cl.hasOption("c") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm size of extra args", cl.getArgList().size() == 2);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testExtraOption
    public void testExtraOption()
    {
        String[] args = new String[] { "-adbtoast",
                                       "foo", "bar" };

        boolean caught = false;

        try
        {
            CommandLine cl = _parser.parse(_options, args);
            
            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm size of extra args", cl.getArgList().size() == 3);
        }
        catch (UnrecognizedOptionException e)
        {
            caught = true;
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
        assertTrue( "Confirm UnrecognizedOptionException caught", caught );
    }

// org.apache.commons.cli.ParseTest::testMissingArg
    public void testMissingArg()
    {

        String[] args = new String[] { "-acb" };

        boolean caught = false;

        try
        {
            CommandLine cl = _parser.parse(_options, args);
        }
        catch (MissingArgumentException e)
        {
            caught = true;
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }

        assertTrue( "Confirm MissingArgumentException caught", caught );
    }

// org.apache.commons.cli.ParseTest::testStop
    public void testStop()
    {
        String[] args = new String[] { "-c",
                                       "foober",
                                       "-btoast" };

        try
        {
            CommandLine cl = _parser.parse(_options, args, true);
            assertTrue( "Confirm -c is set", cl.hasOption("c") );
            assertTrue( "Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testMultiple
    public void testMultiple()
    {
        String[] args = new String[] { "-c",
                                       "foobar",
                                       "-btoast" };

        try
        {
            CommandLine cl = _parser.parse(_options, args, true);
            assertTrue( "Confirm -c is set", cl.hasOption("c") );
            assertTrue( "Confirm  2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

            cl = _parser.parse(_options, cl.getArgs() );

            assertTrue( "Confirm -c is not set", ! cl.hasOption("c") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
            assertTrue( "Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar") );
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testMultipleWithLong
    public void testMultipleWithLong()
    {
        String[] args = new String[] { "--copt",
                                       "foobar",
                                       "--bfile", "toast" };

        try
        {
            CommandLine cl = _parser.parse(_options,args,
                                            true);
            assertTrue( "Confirm -c is set", cl.hasOption("c") );
            assertTrue( "Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);

            cl = _parser.parse(_options, cl.getArgs() );

            assertTrue( "Confirm -c is not set", ! cl.hasOption("c") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("toast") );
            assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
            assertTrue( "Confirm  value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("foobar") );
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testDoubleDash
    public void testDoubleDash()
    {
        String[] args = new String[] { "--copt",
                                       "--",
                                       "-b", "toast" };

        try
        {
            CommandLine cl = _parser.parse(_options, args);

            assertTrue( "Confirm -c is set", cl.hasOption("c") );
            assertTrue( "Confirm -b is not set", ! cl.hasOption("b") );
            assertTrue( "Confirm 2 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 2);

        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
    }

// org.apache.commons.cli.ParseTest::testSingleDash
    public void testSingleDash()
    {
        String[] args = new String[] { "--copt",
                                       "-b", "-",
                                       "-a",
                                       "-" };

        try
        {
            CommandLine cl = _parser.parse(_options, args);

            assertTrue( "Confirm -a is set", cl.hasOption("a") );
            assertTrue( "Confirm -b is set", cl.hasOption("b") );
            assertTrue( "Confirm arg of -b", cl.getOptionValue("b").equals("-") );
            assertTrue( "Confirm 1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
            assertTrue( "Confirm value of extra arg: " + cl.getArgList().get(0), cl.getArgList().get(0).equals("-") );
        }
        catch (ParseException e)
        {
            fail( e.toString() );
        }
        
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testSimplePattern
   public void testSimplePattern()
   {
       try {
           Options options = PatternOptionBuilder.parsePattern("a:b@cde>f+n%t/");
           String[] args = new String[] { "-c", "-a", "foo", "-b", "java.util.Vector", "-e", "build.xml", "-f", "java.util.Calendar", "-n", "4.5", "-t", "http://jakarta.apache.org/" };
      
           CommandLineParser parser = new PosixParser();
           CommandLine line = parser.parse(options,args);

           
           
           assertEquals("flag a", "foo", line.getOptionValue("a"));
           assertEquals("flag a", "foo", line.getOptionValue('a'));
           assertEquals("string flag a", "foo", line.getOptionObject("a"));
           assertEquals("string flag a", "foo", line.getOptionObject('a'));
           assertEquals("object flag b", new java.util.Vector(), line.getOptionObject("b"));
           assertEquals("object flag b", new java.util.Vector(), line.getOptionObject('b'));
           assertEquals("boolean true flag c", true, line.hasOption("c"));
           assertEquals("boolean true flag c", true, line.hasOption('c'));
           assertEquals("boolean false flag d", false, line.hasOption("d"));
           assertEquals("boolean false flag d", false, line.hasOption('d'));
           assertEquals("file flag e", new java.io.File("build.xml"), line.getOptionObject("e"));
           assertEquals("file flag e", new java.io.File("build.xml"), line.getOptionObject('e'));
           assertEquals("class flag f", java.util.Calendar.class, line.getOptionObject("f"));
           assertEquals("class flag f", java.util.Calendar.class, line.getOptionObject('f'));
           assertEquals("number flag n", new Float(4.5), line.getOptionObject("n"));
           assertEquals("number flag n", new Float(4.5), line.getOptionObject('n'));
           assertEquals("url flag t", new java.net.URL("http://jakarta.apache.org/"), line.getOptionObject("t"));
           assertEquals("url flag t", new java.net.URL("http://jakarta.apache.org/"), line.getOptionObject('t'));
           
           
           
       }
       catch( ParseException exp ) {
           fail( exp.getMessage() );
       }
       catch( java.net.MalformedURLException exp ) {
           fail( exp.getMessage() );
       }
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
    public void testShortOptionalArgNoValue()
    {
        String[] args = new String[] { "-e"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("e") );
            assertNull( cmd.getOptionValue("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testShortOptionalArgValue
    public void testShortOptionalArgValue()
    {
        String[] args = new String[] { "-e", "everything"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("e") );
            assertEquals( "everything", cmd.getOptionValue("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testLongOptionalNoValue
    public void testLongOptionalNoValue()
    {
        String[] args = new String[] { "--fish"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("fish") );
            assertNull( cmd.getOptionValue("fish") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testLongOptionalArgValue
    public void testLongOptionalArgValue()
    {
        String[] args = new String[] { "--fish", "face"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("fish") );
            assertEquals( "face", cmd.getOptionValue("fish") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testShortOptionalArgValues
    public void testShortOptionalArgValues()
    {
        String[] args = new String[] { "-j", "ink", "idea"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("j") );
            assertEquals( "ink", cmd.getOptionValue("j") );
            assertEquals( "ink", cmd.getOptionValues("j")[0] );
            assertEquals( "idea", cmd.getOptionValues("j")[1] );
            assertEquals( cmd.getArgs().length, 0 );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testLongOptionalArgValues
    public void testLongOptionalArgValues()
    {
        String[] args = new String[] { "--gravy", "gold", "garden"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("gravy") );
            assertEquals( "gold", cmd.getOptionValue("gravy") );
            assertEquals( "gold", cmd.getOptionValues("gravy")[0] );
            assertEquals( "garden", cmd.getOptionValues("gravy")[1] );
            assertEquals( cmd.getArgs().length, 0 );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testShortOptionalNArgValues
    public void testShortOptionalNArgValues()
    {
        String[] args = new String[] { "-i", "ink", "idea", "isotope", "ice"
        };
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("i") );
            assertEquals( "ink", cmd.getOptionValue("i") );
            assertEquals( "ink", cmd.getOptionValues("i")[0] );
            assertEquals( "idea", cmd.getOptionValues("i")[1] );
            assertEquals( cmd.getArgs().length, 2 );
            assertEquals( "isotope", cmd.getArgs()[0] );
            assertEquals( "ice", cmd.getArgs()[1] );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testLongOptionalNArgValues
    public void testLongOptionalNArgValues()
    {
        String[] args = new String[] { 
            "--hide", "house", "hair", "head"
        };

        CommandLineParser parser = new PosixParser();

        try
        {
            CommandLine cmd = parser.parse(opts,args);
            assertTrue( cmd.hasOption("hide") );
            assertEquals( "house", cmd.getOptionValue("hide") );
            assertEquals( "house", cmd.getOptionValues("hide")[0] );
            assertEquals( "hair", cmd.getOptionValues("hide")[1] );
            assertEquals( cmd.getArgs().length, 1 );
            assertEquals( "head", cmd.getArgs()[0] );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testPropertyOptionSingularValue
    public void testPropertyOptionSingularValue()
    {
        Properties properties = new Properties();
        properties.setProperty( "hide", "seek" );

        CommandLineParser parser = new PosixParser();
        
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( cmd.hasOption("hide") );
            assertEquals( "seek", cmd.getOptionValue("hide") );
            assertTrue( !cmd.hasOption("fake") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testPropertyOptionFlags
    public void testPropertyOptionFlags()
    {
        Properties properties = new Properties();
        properties.setProperty( "a", "true" );
        properties.setProperty( "c", "yes" );
        properties.setProperty( "e", "1" );

        CommandLineParser parser = new PosixParser();
        
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( cmd.hasOption("a") );
            assertTrue( cmd.hasOption("c") );
            assertTrue( cmd.hasOption("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }

        properties = new Properties();
        properties.setProperty( "a", "false" );
        properties.setProperty( "c", "no" );
        properties.setProperty( "e", "0" );
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( !cmd.hasOption("a") );
            assertTrue( !cmd.hasOption("c") );
            assertTrue( !cmd.hasOption("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }

        properties = new Properties();
        properties.setProperty( "a", "TRUE" );
        properties.setProperty( "c", "nO" );
        properties.setProperty( "e", "TrUe" );
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( cmd.hasOption("a") );
            assertTrue( !cmd.hasOption("c") );
            assertTrue( cmd.hasOption("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }

        properties = new Properties();
        properties.setProperty( "a", "just a string" );
        properties.setProperty( "e", "" );
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( !cmd.hasOption("a") );
            assertTrue( !cmd.hasOption("c") );
            assertTrue( !cmd.hasOption("e") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }

    }

// org.apache.commons.cli.ValueTest::testPropertyOptionMultipleValues
    public void testPropertyOptionMultipleValues()
    {
        Properties properties = new Properties();
        properties.setProperty( "k", "one,two" );

        CommandLineParser parser = new PosixParser();
        
        String[] values = new String[] {
            "one", "two"
        };
        try
        {
            CommandLine cmd = parser.parse(opts, null, properties);
            assertTrue( cmd.hasOption("k") );
            assertTrue( Arrays.equals( values, cmd.getOptionValues('k') ) );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
    }

// org.apache.commons.cli.ValueTest::testPropertyOverrideValues
    public void testPropertyOverrideValues()
    {
        String[] args = new String[] { 
            "-j",
            "found",
            "-i",
            "ink"
        };

        Properties properties = new Properties();
        properties.setProperty( "j", "seek" );
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(opts, args, properties);
            assertTrue( cmd.hasOption("j") );
            assertEquals( "found", cmd.getOptionValue("j") );
            assertTrue( cmd.hasOption("i") );
            assertEquals( "ink", cmd.getOptionValue("i") );
            assertTrue( !cmd.hasOption("fake") );
        }
        catch (ParseException e)
        {
            fail("Cannot setUp() CommandLine: " + e.toString());
        }
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
        assertTrue( _cmdline.getOptionValues("b").length == 1);

        assertTrue( _cmdline.hasOption("d") );
        assertTrue( _cmdline.getOptionValue("d").equals("bar"));
        assertTrue( _cmdline.getOptionValues("d").length == 1);
    }

// org.apache.commons.cli.ValuesTest::testMultipleArgValues
    public void testMultipleArgValues()
    {
        String[] result = _cmdline.getOptionValues("e");
        String[] values = new String[] { "one", "two" };
        assertTrue( _cmdline.hasOption("e") );
        assertTrue( _cmdline.getOptionValues("e").length == 2);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("e") ) );
    }

// org.apache.commons.cli.ValuesTest::testTwoArgValues
    public void testTwoArgValues()
    {
        String[] result = _cmdline.getOptionValues("g");
        String[] values = new String[] { "val1", "val2" };
        assertTrue( _cmdline.hasOption("g") );
        assertTrue( _cmdline.getOptionValues("g").length == 2);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("g") ) );
    }

// org.apache.commons.cli.ValuesTest::testComplexValues
    public void testComplexValues()
    {
        String[] result = _cmdline.getOptionValues("h");
        String[] values = new String[] { "val1", "val2" };
        assertTrue( _cmdline.hasOption("i") );
        assertTrue( _cmdline.hasOption("h") );
        assertTrue( _cmdline.getOptionValues("h").length == 2);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues("h") ) );
    }

// org.apache.commons.cli.ValuesTest::testExtraArgs
    public void testExtraArgs()
    {
        String[] args = new String[] { "arg1", "arg2", "arg3" };
        assertTrue( _cmdline.getArgs().length == 3 );         
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
        assertTrue( _cmdline.getOptionValues( "k" ).length == 4 );
        assertTrue( _cmdline.getOptionValues( 'k' ).length == 4 );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( "k" ) ) );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( 'k' ) ) );

        values = new String[] { "key", "value" };
        assertTrue( _cmdline.hasOption( "m" ) );
        assertTrue( _cmdline.hasOption( 'm' ) );
        assertTrue( _cmdline.getOptionValues( "m" ).length == 2);
        assertTrue( _cmdline.getOptionValues( 'm' ).length == 2);
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( "m" ) ) );
        assertTrue( Arrays.equals( values, _cmdline.getOptionValues( 'm' ) ) );
    }

// org.apache.commons.cli.bug.BugCLI13Test::testCLI13
    public void testCLI13()
        throws ParseException
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

// org.apache.commons.cli.bug.BugCLI51Test::test
    public void test() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "-something" };
        CommandLine commandLine;
        commandLine = parser.parse( options, args );
        assertEquals("-something", commandLine.getOptionValue( 't'));
    }
