// ===== FIXED org.apache.commons.cli.TypeHandler :: createNumber(String) [lines 157-179] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-3-fixed/src/java/org/apache/commons/cli/TypeHandler.java =====
    public static Number createNumber(String str)
    {
        try
        {
            if( str != null )
            {
                if( str.indexOf('.') != -1 )
                {
                    return Double.valueOf(str);
                }
                else
                {
                    return Long.valueOf(str);
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            System.err.println(nfe.getMessage());
        }

        return null;
    }
