public List<String> getMatchingOptions(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        
        List<String> matchingOpts = new ArrayList<String>();

        if (opt == null) {
            return matchingOpts;
        }

        // If there is an exact match, return it only
        if (longOpts.containsKey(opt)) {
            matchingOpts.add(opt);
            return matchingOpts;
        }

        // Otherwise, return all options starting with the given prefix
        for (String longOpt : longOpts.keySet())
        {
            if (longOpt.startsWith(opt))
            {
                matchingOpts.add(longOpt);
            }
        }
        
        return matchingOpts;
    }