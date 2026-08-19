// ===== FIXED org.apache.commons.cli.Parser :: checkRequiredOptions() [lines 290-311] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-4-fixed/src/java/org/apache/commons/cli/Parser.java =====
    private void checkRequiredOptions()
        throws MissingOptionException
    {
        // if there are required options that have not been
        // processsed
        if (requiredOptions.size() > 0)
        {
            Iterator iter = requiredOptions.iterator();
            StringBuffer buff = new StringBuffer("Missing required option");
            buff.append(requiredOptions.size() == 1 ? "" : "s");
            buff.append(": ");


            // loop through the required options
            while (iter.hasNext())
            {
                buff.append(iter.next());
            }

            throw new MissingOptionException(buff.toString());
        }
    }
