// ===== FIXED org.apache.commons.cli.Options :: getMatchingOptions(String) [lines 233-253] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-35-fixed/src/main/java/org/apache/commons/cli/Options.java =====
    public List<String> getMatchingOptions(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        
        List<String> matchingOpts = new ArrayList<String>();

        // for a perfect match return the single option only
        if(longOpts.keySet().contains(opt)) {
            return Collections.singletonList(opt);
        }

        for (String longOpt : longOpts.keySet())
        {
            if (longOpt.startsWith(opt))
            {
                matchingOpts.add(longOpt);
            }
        }
        
        return matchingOpts;
    }
