// ===== FIXED org.apache.commons.cli.DefaultParser :: isShortOption(String) [lines 299-311] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-37-fixed/src/main/java/org/apache/commons/cli/DefaultParser.java =====
    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1)
        {
            return false;
        }

        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
        return options.hasShortOption(optName);
    }
