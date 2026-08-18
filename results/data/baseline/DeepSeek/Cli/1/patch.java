    public boolean hasOption(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        return options.containsKey(opt);
    }