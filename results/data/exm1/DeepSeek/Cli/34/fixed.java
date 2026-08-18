// ===== FIXED org.apache.commons.cli.OptionBuilder :: reset() [lines 79-89] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-34-fixed/src/main/java/org/apache/commons/cli/OptionBuilder.java =====
    private static void reset()
    {
        description = null;
        argName = null;
        longopt = null;
        type = String.class;
        required = false;
        numberOfArgs = Option.UNINITIALIZED;
        optionalArg = false;
        valuesep = (char) 0;
    }
