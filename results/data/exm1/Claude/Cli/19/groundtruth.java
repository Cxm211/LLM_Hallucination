// ===== FIXED org.apache.commons.cli.PosixParser :: processOptionToken(String, boolean) [lines 227-239] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-19-fixed/src/java/org/apache/commons/cli/PosixParser.java =====
    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
        }
        else if (stopAtNonOption)
        {
            eatTheRest = true;
        }

        tokens.add(token);
    }
