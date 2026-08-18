public boolean hasOption(String opt)
    {
        if (opt == null)
        {
            return false;
        }

        opt = Util.stripLeadingHyphens(opt);
        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }
        return options.containsKey(key);
    }