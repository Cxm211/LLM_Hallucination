protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
{
    init();
    this.options = options;

    // an iterator for the command line tokens
    Iterator iter = Arrays.asList(arguments).iterator();

    // process each command line token
    while (iter.hasNext())
    {
        // get the next command line token
        String token = (String) iter.next();
        boolean skipGobble = false;

        // handle long option --foo or --foo=bar
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

        // single hyphen
        else if ("-".equals(token))
        {
            tokens.add(token);
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
            // check for attached argument (e.g., -Dfoo where D takes an argument)
            else if (token.length() > 2)
            {
                String opt = token.substring(0, 2);
                if (options.hasOption(opt.substring(1)) && options.getOption(opt.substring(1)).hasArg())
                {
                    tokens.add(opt);
                    tokens.add(token.substring(2));
                    skipGobble = true;
                }
                else
                {
                    burstToken(token, stopAtNonOption);
                }
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

        if (!skipGobble)
        {
            gobble(iter);
        }
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}