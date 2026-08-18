// buggy code
    protected StringBuffer renderWrappedText(StringBuffer sb, int width, 
                                             int nextLineTabStop, String text)
    {
        int pos = findWrapPos(text, width, 0);  // 计算第一行的换行位置：找出在 width 限制下，应该在哪个位置断行

        if (pos == -1) //pos == -1 → 整段文本长度小于 width
        {
            sb.append(rtrim(text));

            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine); //截取 [0, pos) → 第一行内容

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

// relevant test
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

// org.apache.commons.cli.HelpFormatterTest::testFindWrapPos
   public void testFindWrapPos()
      throws Exception
   {
      HelpFormatter hf = new HelpFormatter();

      String text = "This is a test.";
      
      assertEquals("wrap position", 7, hf.findWrapPos(text, 8, 0));
      
      assertEquals("wrap position 2", -1, hf.findWrapPos(text, 8, 8));
      
      text = "aaaa aa";
      assertEquals("wrap position 3", 4, hf.findWrapPos(text, 3, 0));
   }

// org.apache.commons.cli.HelpFormatterTest::testPrintWrapped
   public void testPrintWrapped()
      throws Exception
   {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();

      String text = "This is a test.";
      String expected;

      expected = "This is a" + hf.getNewLine() + "test.";
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

      text =
         "aaaa aaaa aaaa" + hf.getNewLine() +
         "aaaaaa" + hf.getNewLine() +
         "aaaaa";

      expected = text;
      sb.setLength(0);
      hf.renderWrappedText(sb, 16, 0, text);
      assertEquals("multi line text", expected, sb.toString());

      expected =
         "aaaa aaaa aaaa" + hf.getNewLine() +
         "    aaaaaa" + hf.getNewLine() +
         "    aaaaa";
      sb.setLength(0);
      hf.renderWrappedText(sb, 16, 4, text);
      assertEquals("multi-line padded text", expected, sb.toString());
   }

// org.apache.commons.cli.HelpFormatterTest::testPrintOptions
   public void testPrintOptions()
   throws Exception
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

       int nextLineTabStop = leftPad+descPad+"-a".length();
       expected =
           lpad + "-a" + dpad + "aaaa aaaa aaaa" + hf.getNewLine() +
           hf.createPadding(nextLineTabStop) + "aaaa aaaa";
       sb.setLength(0);
       hf.renderOptions(sb, nextLineTabStop+17, options, leftPad, descPad);
       assertEquals("simple wrapped option", expected, sb.toString());

       options = new Options().addOption("a", "aaa", false, "dddd dddd dddd dddd");
       expected = lpad + "-a,--aaa" + dpad + "dddd dddd dddd dddd";
       sb.setLength(0);
       hf.renderOptions(sb, 60, options, leftPad, descPad);
       assertEquals("long non-wrapped option", expected, sb.toString());

       nextLineTabStop = leftPad+descPad+"-a,--aaa".length();
       expected =
           lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
           hf.createPadding(nextLineTabStop) + "dddd dddd";
       sb.setLength(0);
       hf.renderOptions(sb, 25, options, leftPad, descPad);
       assertEquals("long wrapped option", expected, sb.toString());

       options = new Options().
           addOption("a", "aaa", false, "dddd dddd dddd dddd").
           addOption("b", false, "feeee eeee eeee eeee");
       expected =
           lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
           hf.createPadding(nextLineTabStop) + "dddd dddd" + hf.getNewLine() +
           lpad + "-b      " + dpad + "feeee eeee" + hf.getNewLine() +
           hf.createPadding(nextLineTabStop) + "eeee eeee";
       sb.setLength(0);
       hf.renderOptions(sb, 25, options, leftPad, descPad);
       assertEquals("multiple wrapped options", expected, sb.toString());
   }

// org.apache.commons.cli.HelpFormatterTest::testAutomaticUsage
   public void testAutomaticUsage()
   throws Exception
   {
       HelpFormatter hf = new HelpFormatter();
       Options options = null;
       String expected = "usage: app [-a]";
       ByteArrayOutputStream out = new ByteArrayOutputStream( );
       PrintWriter pw = new PrintWriter( out );

       options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa");
       hf.printUsage( pw, 60, "app", options );
       pw.flush();
       assertEquals("simple auto usage", expected, out.toString().trim());
       out.reset();

       expected = "usage: app [-a] [-b]";
       options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa")
       .addOption("b", false, "bbb" );
       hf.printUsage( pw, 60, "app", options );
       pw.flush();
       assertEquals("simple auto usage", expected, out.toString().trim());
       out.reset();
   }

// org.apache.commons.cli.HelpFormatterTest::testPrintUsage
    public void testPrintUsage() {
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

// org.apache.commons.cli.bug.BugCLI18Test::testCLI18
  public void testCLI18() {
    Options options = new Options();
    options.addOption(new Option("a","aaa",false,"aaaaaaa"));
    options.addOption(new Option(null,"bbb",false,"bbbbbbb dksh fkshd fkhs dkfhsdk fhskd hksdks dhfowehfsdhfkjshf skfhkshf sf jkshfk sfh skfh skf f"));
    options.addOption(new Option("c",null,false,"ccccccc"));

    HelpFormatter formatter = new HelpFormatter();
    StringWriter out = new StringWriter();

    formatter.printHelp(new PrintWriter(out),80, "foobar", "dsfkfsh kdh hsd hsdh fkshdf ksdh fskdh fsdh fkshfk sfdkjhskjh fkjh fkjsh khsdkj hfskdhf skjdfh ksf khf s", options, 2, 2, "blort j jgj j jg jhghjghjgjhgjhg jgjhgj jhg jhg hjg jgjhghjg jhg hjg jhgjg jgjhghjg jg jgjhgjgjg jhg jhgjh" + '\r' + '\n' + "rarrr", true);
  }
