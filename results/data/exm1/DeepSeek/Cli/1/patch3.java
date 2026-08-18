void addOption(Option opt)
{
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