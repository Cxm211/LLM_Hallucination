    public Object getOptionObject(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        String res = getOptionValue(opt);

        if (!options.containsKey(opt))
        {
            return null;
        }

        Object type = ((Option) options.get(opt)).getType();

        return (res == null)        ? null : TypeHandler.createValue(res, type);
    }