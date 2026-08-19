// ===== FIXED org.apache.commons.cli.CommandLine :: addOption(Option) [lines 286-289] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    void addOption(Option opt)
    {
        options.add(opt);
    }

// ===== FIXED org.apache.commons.cli.CommandLine :: getOptionObject(String) [lines 87-100] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    public Object getOptionObject(String opt)
    {
        String res = getOptionValue(opt);

        Option option = resolveOption(opt);
        if (option == null)
        {
            return null;
        }

        Object type = option.getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }

// ===== FIXED org.apache.commons.cli.CommandLine :: getOptionValues(String) [lines 146-156] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    public String[] getOptionValues(String opt)
    {
        Option key = resolveOption( opt );

        if (options.contains(key))
        {
            return key.getValues();
        }

        return null;
        }

// ===== FIXED org.apache.commons.cli.CommandLine :: getOptions() [lines 307-316] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    public Option[] getOptions()
    {
        Collection processed = options;

        // reinitialise array
        Option[] optionsArray = new Option[processed.size()];

        // return the array
        return (Option[]) processed.toArray(optionsArray);
    }

// ===== FIXED org.apache.commons.cli.CommandLine :: hasOption(String) [lines 65-68] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    public boolean hasOption(String opt)
    {
        return options.contains( resolveOption(opt));
    }

// ===== FIXED org.apache.commons.cli.CommandLine :: iterator() [lines 297-300] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-1-fixed/src/java/org/apache/commons/cli/CommandLine.java =====
    public Iterator iterator()
    {
        return options.iterator();
    }
