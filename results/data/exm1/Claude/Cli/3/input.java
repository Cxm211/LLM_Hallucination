// buggy code
    public static Number createNumber(String str)
    {
        try
        {
            return org.apache.commons.lang.math.NumberUtils.createNumber(str);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println(nfe.getMessage());
        }

        return null;
    }

// relevant test
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
           assertEquals("number flag n", new Double(4.5), line.getOptionObject("n"));
           assertEquals("number flag n", new Double(4.5), line.getOptionObject('n'));
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
