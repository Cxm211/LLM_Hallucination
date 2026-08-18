public Object getOptionObject(String opt)
    {
        String res = getOptionValue(opt);

        if (opt == null)
        {
            return null;
        }

        opt = Util.stripLeadingHyphens(opt);
        String key = opt;
        if (names.containsKey(opt))
        {
            key = (String) names.get(opt);
        }

        if (!options.containsKey(key))
        {
            return null;
        }

        Object type = ((Option) options.get(key)).getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }