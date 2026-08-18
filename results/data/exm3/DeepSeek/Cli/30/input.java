// buggy function
    private void handleProperties(Properties properties) throws ParseException
    {
        if (properties == null)
        {
            return;
        }
        
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
        {
            String option = e.nextElement().toString();
            
            if (!cmd.hasOption(option))
            {
                Option opt = options.getOption(option);
            
            // if the option is part of a group, check if another option of the group has been selected
            
                // get the value from the properties
                String value = properties.getProperty(option);
                
                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        opt.addValueForProcessing(value);
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the option to the CommandLine
                    continue;
                }
                
                handleOption(opt);
                currentOption = null;
            }
        }
    }

    protected void processProperties(Properties properties) throws ParseException
    {
        if (properties == null)
        {
            return;
        }

        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
        {
            String option = e.nextElement().toString();
            
            if (!cmd.hasOption(option))
            {
                Option opt = getOptions().getOption(option);
            
            // if the option is part of a group, check if another option of the group has been selected
            
                // get the value from the properties instance
                String value = properties.getProperty(option);

                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        try
                        {
                            opt.addValueForProcessing(value);
                        }
                        catch (RuntimeException exp)
                        {
                            // if we cannot add the value don't worry about it
                        }
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the
                    // option to the CommandLine
                    continue;
                }

                cmd.addOption(opt);
                updateRequiredOptions(opt);
            }
        }
    }

// trigger testcase
// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionGroup
public void testPropertyOptionGroup() throws Exception
    {
        Options opts = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", null));
        group1.addOption(new Option("b", null));
        opts.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("x", null));
        group2.addOption(new Option("y", null));
        opts.addOptionGroup(group2);
        
        String[] args = new String[] { "-a" };
        
        Properties properties = new Properties();
        properties.put("b", "true");
        properties.put("x", "true");
        
        CommandLine cmd = parse(parser, opts, args, properties);
        
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionUnexpected
public void testPropertyOptionUnexpected() throws Exception
    {
        Options opts = new Options();
        
        Properties properties = new Properties();
        properties.setProperty("f", "true");
        
        try {
            parse(parser, opts, null, properties);
            fail("UnrecognizedOptionException expected");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionGroup
public void testPropertyOptionGroup() throws Exception
    {
        Options opts = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", null));
        group1.addOption(new Option("b", null));
        opts.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("x", null));
        group2.addOption(new Option("y", null));
        opts.addOptionGroup(group2);
        
        String[] args = new String[] { "-a" };
        
        Properties properties = new Properties();
        properties.put("b", "true");
        properties.put("x", "true");
        
        CommandLine cmd = parse(parser, opts, args, properties);
        
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionUnexpected
public void testPropertyOptionUnexpected() throws Exception
    {
        Options opts = new Options();
        
        Properties properties = new Properties();
        properties.setProperty("f", "true");
        
        try {
            parse(parser, opts, null, properties);
            fail("UnrecognizedOptionException expected");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionGroup
public void testPropertyOptionGroup() throws Exception
    {
        Options opts = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", null));
        group1.addOption(new Option("b", null));
        opts.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("x", null));
        group2.addOption(new Option("y", null));
        opts.addOptionGroup(group2);
        
        String[] args = new String[] { "-a" };
        
        Properties properties = new Properties();
        properties.put("b", "true");
        properties.put("x", "true");
        
        CommandLine cmd = parse(parser, opts, args, properties);
        
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionUnexpected
public void testPropertyOptionUnexpected() throws Exception
    {
        Options opts = new Options();
        
        Properties properties = new Properties();
        properties.setProperty("f", "true");
        
        try {
            parse(parser, opts, null, properties);
            fail("UnrecognizedOptionException expected");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }

// org/apache/commons/cli/OptionGroupTest.java::testTwoOptionsFromGroupWithProperties
public void testTwoOptionsFromGroupWithProperties() throws Exception
    {
        String[] args = new String[] { "-f" };
        
        Properties properties = new Properties();
        properties.put("d", "true");
        
        CommandLine cl = parser.parse( _options, args, properties);
        assertTrue(cl.hasOption("f"));
        assertTrue(!cl.hasOption("d"));
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionGroup
public void testPropertyOptionGroup() throws Exception
    {
        Options opts = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(new Option("a", null));
        group1.addOption(new Option("b", null));
        opts.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(new Option("x", null));
        group2.addOption(new Option("y", null));
        opts.addOptionGroup(group2);
        
        String[] args = new String[] { "-a" };
        
        Properties properties = new Properties();
        properties.put("b", "true");
        properties.put("x", "true");
        
        CommandLine cmd = parse(parser, opts, args, properties);
        
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("x"));
        assertFalse(cmd.hasOption("y"));
    }

// org/apache/commons/cli/ParserTestCase.java::testPropertyOptionUnexpected
public void testPropertyOptionUnexpected() throws Exception
    {
        Options opts = new Options();
        
        Properties properties = new Properties();
        properties.setProperty("f", "true");
        
        try {
            parse(parser, opts, null, properties);
            fail("UnrecognizedOptionException expected");
        } catch (UnrecognizedOptionException e) {
            // expected
        }
    }
