// buggy function
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
                int pos = token.indexOf('=');
                String opt = pos == -1 ? token : token.substring(0, pos); // --foo

                if (!options.hasOption(opt))
                {
                    processNonOptionToken(token);
                }
                else
                {
                    
                    tokens.add(opt);
                    if (pos != -1)
                    {
                        tokens.add(token.substring(pos + 1));
                    }
                }
            }

            // single hyphen
            else if ("-".equals(token))
            {
                tokens.add(token);
            }
            else if (token.startsWith("-"))
            {
                if (token.length() == 2 || options.hasOption(token))
                {
                    processOptionToken(token, stopAtNonOption);
                }
                // requires bursting
                else
                {
                    burstToken(token, stopAtNonOption);
                }
            }
            else if (stopAtNonOption)
            {
                processNonOptionToken(token);
            }
            else
            {
                tokens.add(token);
            }

            gobble(iter);
        }

        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    private void processNonOptionToken(String value)
    {
            eatTheRest = true;
            tokens.add("--");

        tokens.add(value);
    }

    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (stopAtNonOption && !options.hasOption(token))
        {
            eatTheRest = true;
        }


        tokens.add(token);
    }

    protected void burstToken(String token, boolean stopAtNonOption)
    {
        Option currentOption;
        for (int i = 1; i < token.length(); i++)
        {
            String ch = String.valueOf(token.charAt(i));

            if (options.hasOption(ch))
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
                processNonOptionToken(token.substring(i));
                break;
            }
            else
            {
                tokens.add(token);
                break;
            }
        }
    }

// trigger testcase
// org/apache/commons/cli/ApplicationTest.java::testGroovy
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

// org/apache/commons/cli/ParserTestCase.java::testStopAtExpectedArg
public void testStopAtExpectedArg() throws Exception
    {
        String[] args = new String[]{"-b", "foo"};

        CommandLine cl = parser.parse(options, args, true);

        assertTrue("Confirm -b is set", cl.hasOption('b'));
        assertEquals("Confirm -b is set", "foo", cl.getOptionValue('b'));
        assertTrue("Confirm no extra args: " + cl.getArgList().size(), cl.getArgList().size() == 0);
    }
