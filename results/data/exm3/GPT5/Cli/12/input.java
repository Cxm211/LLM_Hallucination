// buggy function
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

// trigger testcase
// org/apache/commons/cli/GnuParserTest.java::testLongWithEqual
public void testLongWithEqual() throws Exception
    {
        String[] args = new String[] { "--foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org/apache/commons/cli/GnuParserTest.java::testLongWithEqualSingleDash
public void testLongWithEqualSingleDash() throws Exception
    {
        String[] args = new String[] { "-foo=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }

// org/apache/commons/cli/GnuParserTest.java::testShortWithEqual
public void testShortWithEqual() throws Exception
    {
        String[] args = new String[] { "-f=bar" };

        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").hasArg().create('f'));

        Parser parser = new GnuParser();
        CommandLine cl = parser.parse(options, args);

        assertEquals("bar", cl.getOptionValue("foo"));
    }
