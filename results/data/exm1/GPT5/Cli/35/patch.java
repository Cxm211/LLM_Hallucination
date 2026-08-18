public List<String> getMatchingOptions(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        
        List<String> matchingOpts = new ArrayList<String>();

        if (opt == null)
        {
            return matchingOpts;
        }

        // if there is an exact match, return it only
        if (longOpts.containsKey(opt))
        {
            matchingOpts.add(opt);
            return matchingOpts;
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