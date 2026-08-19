// ===== FIXED org.apache.commons.cli.Util :: stripLeadingHyphens(String) [lines 34-49] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-5-fixed/src/java/org/apache/commons/cli/Util.java =====
    static String stripLeadingHyphens(String str)
    {
        if (str == null) {
            return null;
        }
        if (str.startsWith("--"))
        {
            return str.substring(2, str.length());
        }
        else if (str.startsWith("-"))
        {
            return str.substring(1, str.length());
        }

        return str;
    }
