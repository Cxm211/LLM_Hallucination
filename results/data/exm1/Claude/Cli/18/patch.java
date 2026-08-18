protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
{
    init();
    this.options = options;

    Iterator iter = Arrays.asList(arguments).iterator();

    while (iter.hasNext())
    {
        String token = (String) iter.next();

        if (token.startsWith("--"))
        {
            if (token.indexOf('=') != -1)
            {
                tokens.add(token.substring(0, token.indexOf('=')));
                tokens.add(token.substring(token.indexOf('=') + 1, token.length()));
            }
            else
            {
                tokens.add(token);
            }
        }
        else if ("-".equals(token))
        {
            processSingleHyphen(token);
        }
        else if (token.startsWith("-"))
        {
            if (token.length() == 2 || options.hasOption(token))
            {
                processOptionToken(token, stopAtNonOption);
            }
            else
            {
                burstToken(token, stopAtNonOption);
            }
        }
        else if (stopAtNonOption)
        {
            process(token);
        }
        else
        {
            tokens.add(token);
        }

        gobble(iter);
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}