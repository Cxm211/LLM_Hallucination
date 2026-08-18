// buggy function
    private void appendOption(final StringBuffer buff, final Option option, final boolean required)
    {
        if (!required)
        {
            buff.append("[");
        }

        if (option.getOpt() != null)
        {
            buff.append("-").append(option.getOpt());
        }
        else
        {
            buff.append("--").append(option.getLongOpt());
        }
        
        // if the Option has a value and a non blank argname
        if (option.hasArg() && option.hasArgName())
        {
            buff.append(option.getOpt() == null ? longOptSeparator : " ");
            buff.append("<").append(option.getArgName()).append(">");
        }
        
        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }

    private static void reset()
    {
        description = null;
        argName = "arg";
        longopt = null;
        type = null;
        required = false;
        numberOfArgs = Option.UNINITIALIZED;
        optionalArg = false;
        valuesep = (char) 0;
    }

// trigger testcase
// org/apache/commons/cli/HelpFormatterTest.java::testDefaultArgName
public void testDefaultArgName()
    {
        Option option = OptionBuilder.hasArg().isRequired().create("f");
        
        Options options = new Options();
        options.addOption(option);
        
        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("argument");
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app -f <argument>" + EOL, out.toString());
    }
