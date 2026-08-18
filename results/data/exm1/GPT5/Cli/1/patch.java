    public boolean hasOption(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }
        return options.containsKey(key);
    }

    public Object getOptionObject(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }

        String res = getOptionValue(opt);

        if (!options.containsKey(key))
        {
            return null;
        }

        Object type = ((Option) options.get(key)).getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }

    public String[] getOptionValues(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);

        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }
        if (options.containsKey(key))
        {
            return ((Option) options.get(key)).getValues();
        }

        return null;
        }

    void addOption(Option opt)
    {
        hashcodeMap.put(new Integer(opt.hashCode()), opt);
        String key = opt.getKey();
        if (key == null)
        {
            key = opt.getLongOpt();
        }
        else
        {
            names.put(opt.getLongOpt(), key);
        }
        options.put(key, opt);
    }

    public Iterator iterator()
    {
        return hashcodeMap.values().iterator();
    }

    public Option[] getOptions()
    {
        Collection processed = options.values();

        // reinitialise array
        Option[] optionsArray = new Option[processed.size()];

        // return the array
        return (Option[]) processed.toArray(optionsArray);
    }