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
            String opt = pos == -1 ? token : token.substring(0, pos);

            if (!options.hasOption(opt))
            {
                if (stopAtNonOption)
                {
                    processNonOptionToken(token);
                }
                else
                {
                    tokens.add(token);
                }
            }
            else
            {
                tokens.add(opt);
                if (pos != -1)
                {
                    tokens.add(token.substring(pos + 1));
                }
            }
        }
        else if ("-".equals(token))
        {
            tokens.add(token);
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
            processNonOptionToken(token);
        }
        else
        {
            tokens.add(token);
        }

        gobble(iter);
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}