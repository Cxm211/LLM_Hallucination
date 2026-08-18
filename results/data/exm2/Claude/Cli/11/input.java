    private static void appendOption(final StringBuffer buff, 
                                     final Option option, 
                                     final boolean required)
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

        // if the Option has a value
        if (option.hasArg() && (option.getArgName() != null))
        {
            buff.append(" <").append(option.getArgName()).append(">");
        }

        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }

// trigger testcase
public void testPrintOptionWithEmptyArgNameUsage() {
        Option option = new Option("f", true, null);
        option.setArgName("");
        option.setRequired(true);

        Options options = new Options();
        options.addOption(option);

        StringWriter out = new StringWriter();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(new PrintWriter(out), 80, "app", options);

        assertEquals("usage: app -f" + EOL, out.toString());
    }
