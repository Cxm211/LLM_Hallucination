// buggy function
    public static Object createValue(final String str, final Class<?> clazz) throws ParseException
    {
        if (PatternOptionBuilder.STRING_VALUE == clazz)
        {
            return str;
        }
        else if (PatternOptionBuilder.OBJECT_VALUE == clazz)
        {
            return createObject(str);
        }
        else if (PatternOptionBuilder.NUMBER_VALUE == clazz)
        {
            return createNumber(str);
        }
        else if (PatternOptionBuilder.DATE_VALUE == clazz)
        {
            return createDate(str);
        }
        else if (PatternOptionBuilder.CLASS_VALUE == clazz)
        {
            return createClass(str);
        }
        else if (PatternOptionBuilder.FILE_VALUE == clazz)
        {
            return createFile(str);
        }
        else if (PatternOptionBuilder.EXISTING_FILE_VALUE == clazz)
        {
            return createFile(str);
        }
        else if (PatternOptionBuilder.FILES_VALUE == clazz)
        {
            return createFiles(str);
        }
        else if (PatternOptionBuilder.URL_VALUE == clazz)
        {
            return createURL(str);
        }
        else
        {
            return null;
        }
    }

    public static File createFile(final String str)
    {
        return new File(str);
    }

// trigger testcase
// org/apache/commons/cli/PatternOptionBuilderTest.java::testExistingFilePattern
@Test
    public void testExistingFilePattern() throws Exception
    {
        final Options options = PatternOptionBuilder.parsePattern("g<");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-g", "src/test/resources/existing-readable.file" });

        final Object parsedReadableFileStream = line.getOptionObject("g");

        assertNotNull("option g not parsed", parsedReadableFileStream);
        assertTrue("option g not FileInputStream", parsedReadableFileStream instanceof FileInputStream);
    }

// org/apache/commons/cli/PatternOptionBuilderTest.java::testExistingFilePatternFileNotExist
@Test
    public void testExistingFilePatternFileNotExist() throws Exception {
        final Options options = PatternOptionBuilder.parsePattern("f<");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-f", "non-existing.file" });

        assertNull("option f parsed", line.getOptionObject("f"));
    }
