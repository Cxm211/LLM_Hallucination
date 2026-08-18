// buggy code
    public static <T> T createValue(final String str, final Class<T> clazz) throws ParseException
    {
        if (PatternOptionBuilder.STRING_VALUE == clazz)
        {
            return (T) str;
        }
        else if (PatternOptionBuilder.OBJECT_VALUE == clazz)
        {
            return (T) createObject(str);
        }
        else if (PatternOptionBuilder.NUMBER_VALUE == clazz)
        {
            return (T) createNumber(str);
        }
        else if (PatternOptionBuilder.DATE_VALUE == clazz)
        {
            return (T) createDate(str);
        }
        else if (PatternOptionBuilder.CLASS_VALUE == clazz)
        {
            return (T) createClass(str);
        }
        else if (PatternOptionBuilder.FILE_VALUE == clazz)
        {
            return (T) createFile(str);
        }
        else if (PatternOptionBuilder.EXISTING_FILE_VALUE == clazz)
        {
            return (T) openFile(str);
        }
        else if (PatternOptionBuilder.FILES_VALUE == clazz)
        {
            return (T) createFiles(str);
        }
        else if (PatternOptionBuilder.URL_VALUE == clazz)
        {
            return (T) createURL(str);
        }
        else
        {
            return null;
        }
    }

// relevant test
// org.apache.commons.cli.CommandLineTest::testGetOptionProperties
    public void testGetOptionProperties() throws Exception
    {
        final String[] args = new String[] { "-Dparam1=value1", "-Dparam2=value2", "-Dparam3", "-Dparam4=value4", "-D", "--property", "foo=bar" };

        final Options options = new Options();
        options.addOption(OptionBuilder.withValueSeparator().hasOptionalArgs(2).create('D'));
        options.addOption(OptionBuilder.withValueSeparator().hasArgs(2).withLongOpt("property").create());

        final Parser parser = new GnuParser();
        final CommandLine cl = parser.parse(options, args);

        final Properties props = cl.getOptionProperties("D");
        assertNotNull("null properties", props);
        assertEquals("number of properties in " + props, 4, props.size());
        assertEquals("property 1", "value1", props.getProperty("param1"));
        assertEquals("property 2", "value2", props.getProperty("param2"));
        assertEquals("property 3", "true", props.getProperty("param3"));
        assertEquals("property 4", "value4", props.getProperty("param4"));

        assertEquals("property with long format", "bar", cl.getOptionProperties("property").getProperty("foo"));
    }

// org.apache.commons.cli.CommandLineTest::testGetOptionPropertiesWithOption
    public void testGetOptionPropertiesWithOption() throws Exception
    {
        final String[] args = new String[] { "-Dparam1=value1", "-Dparam2=value2", "-Dparam3", "-Dparam4=value4", "-D", "--property", "foo=bar" };

        final Options options = new Options();
        final Option option_D = OptionBuilder.withValueSeparator().hasOptionalArgs(2).create('D');
        final Option option_property = OptionBuilder.withValueSeparator().hasArgs(2).withLongOpt("property").create();
        options.addOption(option_D);
        options.addOption(option_property);

        final Parser parser = new GnuParser();
        final CommandLine cl = parser.parse(options, args);

        final Properties props = cl.getOptionProperties(option_D);
        assertNotNull("null properties", props);
        assertEquals("number of properties in " + props, 4, props.size());
        assertEquals("property 1", "value1", props.getProperty("param1"));
        assertEquals("property 2", "value2", props.getProperty("param2"));
        assertEquals("property 3", "true", props.getProperty("param3"));
        assertEquals("property 4", "value4", props.getProperty("param4"));

        assertEquals("property with long format", "bar", cl.getOptionProperties(option_property).getProperty("foo"));
    }

// org.apache.commons.cli.CommandLineTest::testGetOptions
    public void testGetOptions()
    {
        final CommandLine cmd = new CommandLine();
        assertNotNull(cmd.getOptions());
        assertEquals(0, cmd.getOptions().length);
        
        cmd.addOption(new Option("a", null));
        cmd.addOption(new Option("b", null));
        cmd.addOption(new Option("c", null));
        
        assertEquals(3, cmd.getOptions().length);
    }

// org.apache.commons.cli.CommandLineTest::testGetParsedOptionValue
    public void testGetParsedOptionValue() throws Exception {
        final Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withType(Number.class).create("i"));
        options.addOption(OptionBuilder.hasArg().create("f"));
        
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        
        assertEquals(123, ((Number) cmd.getParsedOptionValue("i")).intValue());
        assertEquals("foo", cmd.getParsedOptionValue("f"));
    }

// org.apache.commons.cli.CommandLineTest::testGetParsedOptionValueWithChar
    public void testGetParsedOptionValueWithChar() throws Exception {
        final Options options = new Options();
        options.addOption(Option.builder("i").hasArg().type(Number.class).build());
        options.addOption(Option.builder("f").hasArg().build());
        
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        
        assertEquals(123, ((Number) cmd.getParsedOptionValue('i')).intValue());
        assertEquals("foo", cmd.getParsedOptionValue('f'));
    }

// org.apache.commons.cli.CommandLineTest::testGetParsedOptionValueWithOption
    public void testGetParsedOptionValueWithOption() throws Exception {
        final Options options = new Options();
        final Option opt_i = Option.builder("i").hasArg().type(Number.class).build();
        final Option opt_f = Option.builder("f").hasArg().build();
        options.addOption(opt_i);
        options.addOption(opt_f);
        
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        
        assertEquals(123, ((Number) cmd.getParsedOptionValue(opt_i)).intValue());
        assertEquals("foo", cmd.getParsedOptionValue(opt_f));
    }

// org.apache.commons.cli.CommandLineTest::testNullhOption
    public void testNullhOption() throws Exception {
        final Options options = new Options();
        final Option opt_i = Option.builder("i").hasArg().type(Number.class).build();
        final Option opt_f = Option.builder("f").hasArg().build();
        options.addOption(opt_i);
        options.addOption(opt_f);
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, new String[] { "-i", "123", "-f", "foo" });
        assertNull(cmd.getOptionValue((Option)null));
        assertNull(cmd.getParsedOptionValue((Option)null));
    }

// org.apache.commons.cli.CommandLineTest::testBuilder
    public void testBuilder()
        throws Exception
    {
        final CommandLine.Builder builder = new CommandLine.Builder();
        builder.addArg( "foo" ).addArg( "bar" );
        builder.addOption( Option.builder( "T" ).build() );
        final CommandLine cmd = builder.build();

        assertEquals( "foo", cmd.getArgs()[0] );
        assertEquals( "bar", cmd.getArgList().get( 1 ) );
        assertEquals( "T", cmd.getOptions()[0].getOpt() );
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testSimplePattern
    public void testSimplePattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("a:b@cde>f+n%t/m*z#");
        final String[] args = new String[] {"-c", "-a", "foo", "-b", "java.util.Vector", "-e", "build.xml", "-f", "java.util.Calendar", "-n", "4.5", "-t", "http://commons.apache.org", "-z", "Thu Jun 06 17:48:57 EDT 2002", "-m", "test*"};

        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, args);

        assertEquals("flag a", "foo", line.getOptionValue("a"));
        assertEquals("string flag a", "foo", line.getOptionObject("a"));
        assertEquals("object flag b", new Vector<Object>(), line.getOptionObject("b"));
        assertTrue("boolean true flag c", line.hasOption("c"));
        assertFalse("boolean false flag d", line.hasOption("d"));
        assertEquals("file flag e", new File("build.xml"), line.getOptionObject("e"));
        assertEquals("class flag f", Calendar.class, line.getOptionObject("f"));
        assertEquals("number flag n", new Double(4.5), line.getOptionObject("n"));
        assertEquals("url flag t", new URL("http://commons.apache.org"), line.getOptionObject("t"));

        
        assertEquals("flag a", "foo", line.getOptionValue('a'));
        assertEquals("string flag a", "foo", line.getOptionObject('a'));
        assertEquals("object flag b", new Vector<Object>(), line.getOptionObject('b'));
        assertTrue("boolean true flag c", line.hasOption('c'));
        assertFalse("boolean false flag d", line.hasOption('d'));
        assertEquals("file flag e", new File("build.xml"), line.getOptionObject('e'));
        assertEquals("class flag f", Calendar.class, line.getOptionObject('f'));
        assertEquals("number flag n", new Double(4.5), line.getOptionObject('n'));
        assertEquals("url flag t", new URL("http://commons.apache.org"), line.getOptionObject('t'));

        
        try {
            assertEquals("files flag m", new File[0], line.getOptionObject('m'));
            fail("Multiple files are not supported yet, should have failed");
        } catch(final UnsupportedOperationException uoe) {
            
        }

        
        try {
            assertEquals("date flag z", new Date(1023400137276L), line.getOptionObject('z'));
            fail("Date is not supported yet, should have failed");
        } catch(final UnsupportedOperationException uoe) {
            
        }
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testEmptyPattern
    public void testEmptyPattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("");
        assertTrue(options.getOptions().isEmpty());
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testUntypedPattern
    public void testUntypedPattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("abc");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-abc" });

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
        final Options options = PatternOptionBuilder.parsePattern("n%d%x%");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-n", "1", "-d", "2.1", "-x", "3,5" });

        assertEquals("n object class", Long.class, line.getOptionObject("n").getClass());
        assertEquals("n value", new Long(1), line.getOptionObject("n"));

        assertEquals("d object class", Double.class, line.getOptionObject("d").getClass());
        assertEquals("d value", new Double(2.1), line.getOptionObject("d"));

        assertNull("x object", line.getOptionObject("x"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testClassPattern
    public void testClassPattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("c+d+");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-c", "java.util.Calendar", "-d", "System.DateTime" });

        assertEquals("c value", Calendar.class, line.getOptionObject("c"));
        assertNull("d value", line.getOptionObject("d"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testObjectPattern
    public void testObjectPattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("o@i@n@");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-o", "java.lang.String", "-i", "java.util.Calendar", "-n", "System.DateTime" });

        assertEquals("o value", "", line.getOptionObject("o"));
        assertNull("i value", line.getOptionObject("i"));
        assertNull("n value", line.getOptionObject("n"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testURLPattern
    public void testURLPattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("u/v/");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-u", "http://commons.apache.org", "-v", "foo://commons.apache.org" });

        assertEquals("u value", new URL("http://commons.apache.org"), line.getOptionObject("u"));
        assertNull("v value", line.getOptionObject("v"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testExistingFilePattern
    public void testExistingFilePattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("g<");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-g", "src/test/resources/existing-readable.file" });

        final Object parsedReadableFileStream = line.getOptionObject("g");

        assertNotNull("option g not parsed", parsedReadableFileStream);
        assertTrue("option g not FileInputStream", parsedReadableFileStream instanceof FileInputStream);
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testExistingFilePatternFileNotExist
    public void testExistingFilePatternFileNotExist() throws Exception {
        final Options options = PatternOptionBuilder.parsePattern("f<");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-f", "non-existing.file" });

        assertNull("option f parsed", line.getOptionObject("f"));
    }

// org.apache.commons.cli.PatternOptionBuilderTest::testRequiredOption
    public void testRequiredOption() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("!n%m%");
        final CommandLineParser parser = new PosixParser();

        try
        {
            parser.parse(options, new String[]{""});
            fail("MissingOptionException wasn't thrown");
        }
        catch (final MissingOptionException e)
        {
            assertEquals(1, e.getMissingOptions().size());
            assertTrue(e.getMissingOptions().contains("n"));
        }
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueString
    public void testCreateValueString()
        throws Exception
    {
        assertEquals("String", TypeHandler.createValue("String", PatternOptionBuilder.STRING_VALUE));
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueObject_unknownClass
    public void testCreateValueObject_unknownClass()
        throws Exception
    {
        TypeHandler.createValue("unknown", PatternOptionBuilder.OBJECT_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueObject_notInstantiableClass
    public void testCreateValueObject_notInstantiableClass()
        throws Exception
    {
        TypeHandler.createValue(NotInstantiable.class.getName(), PatternOptionBuilder.OBJECT_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueObject_InstantiableClass
    public void testCreateValueObject_InstantiableClass()
        throws Exception
    {
        Object result = TypeHandler.createValue(Instantiable.class.getName(), PatternOptionBuilder.OBJECT_VALUE);
        assertTrue(result instanceof Instantiable);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueNumber_noNumber
    public void testCreateValueNumber_noNumber()
        throws Exception
    {
        TypeHandler.createValue("not a number", PatternOptionBuilder.NUMBER_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueNumber_Double
    public void testCreateValueNumber_Double()
        throws Exception
    {
        assertEquals(1.5d, TypeHandler.createValue("1.5", PatternOptionBuilder.NUMBER_VALUE));
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueNumber_Long
    public void testCreateValueNumber_Long()
        throws Exception
    {
        assertEquals(Long.valueOf(15), TypeHandler.createValue("15", PatternOptionBuilder.NUMBER_VALUE));
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueDate
    public void testCreateValueDate()
        throws Exception
    {
        TypeHandler.createValue("what ever", PatternOptionBuilder.DATE_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueClass_notFound
    public void testCreateValueClass_notFound()
        throws Exception
    {
        TypeHandler.createValue("what ever", PatternOptionBuilder.CLASS_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueClass
    public void testCreateValueClass()
        throws Exception
    {
        Object clazz = TypeHandler.createValue(Instantiable.class.getName(), PatternOptionBuilder.CLASS_VALUE);
        assertEquals(Instantiable.class, clazz);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueFile
    public void testCreateValueFile()
            throws Exception
    {
        File result = TypeHandler.createValue("some-file.txt", PatternOptionBuilder.FILE_VALUE);
        assertEquals("some-file.txt", result.getName());
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueExistingFile
    public void testCreateValueExistingFile()
            throws Exception
    {
        FileInputStream result = TypeHandler.createValue("src/test/resources/existing-readable.file", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(result);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueExistingFile_nonExistingFile
    public void testCreateValueExistingFile_nonExistingFile()
            throws Exception
    {
        TypeHandler.createValue("non-existing.file", PatternOptionBuilder.EXISTING_FILE_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueFiles
    public void testCreateValueFiles()
            throws Exception
    {
        TypeHandler.createValue("some.files", PatternOptionBuilder.FILES_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueURL
    public void testCreateValueURL()
            throws Exception
    {
        String urlString = "http://commons.apache.org";
        URL result = TypeHandler.createValue(urlString, PatternOptionBuilder.URL_VALUE);
        assertEquals(urlString, result.toString());
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueURL_malformed
    public void testCreateValueURL_malformed()
            throws Exception
    {
        TypeHandler.createValue("malformed-url", PatternOptionBuilder.URL_VALUE);
    }

// org.apache.commons.cli.TypeHandlerTest::testCreateValueInteger_failure
    public void testCreateValueInteger_failure()
            throws Exception
    {
        TypeHandler.createValue("just-a-string", Integer.class);
    }
