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
            int pos = token.indexOf('=');
            if (pos != -1)
            {
                String opt = token.substring(0, pos);
                String optionName = opt.substring(2);
                if (options.hasOption(optionName))
                {
                    tokens.add(opt);
                    tokens.add(token.substring(pos + 1));
                }
                else
                {
                    tokens.add(token);
                }
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
            if (token.length() == 2)
            {
                processOptionToken(token, stopAtNonOption);
            }
            else if (options.hasOption(token))
            {
                tokens.add(token);
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