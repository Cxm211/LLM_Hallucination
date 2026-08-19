// ===== FIXED org.apache.commons.cli.Util :: stripLeadingAndTrailingQuotes(String) [lines 63-72] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-29-fixed/src/java/org/apache/commons/cli/Util.java =====
    static String stripLeadingAndTrailingQuotes(String str)
    {
        int length = str.length();
        if (length > 1 && str.startsWith("\"") && str.endsWith("\"") && str.substring(1, length - 1).indexOf('"') == -1)
        {
            str = str.substring(1, length - 1);
        }
        
        return str;
    }
