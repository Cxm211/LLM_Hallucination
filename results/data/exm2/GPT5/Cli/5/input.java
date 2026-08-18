    static String stripLeadingHyphens(String str)
    {
        if (str.startsWith("--"))
        {
            return str.substring(2, str.length());
        }
        else if (str.startsWith("-"))
        {
            return str.substring(1, str.length());
        }

        return str;
    }

// trigger testcase
public void testStripLeadingHyphens() {
        assertEquals("f", Util.stripLeadingHyphens("-f"));
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
        assertNull(Util.stripLeadingHyphens(null));
    }

public void testOrder() throws ParseException {
        Option optionA = new Option("a", "first");
        Options opts = new Options();
        opts.addOption(optionA);
        PosixParser posixParser = new PosixParser();
        CommandLine line = posixParser.parse(opts, null);
        assertFalse(line.hasOption(null));
    }
