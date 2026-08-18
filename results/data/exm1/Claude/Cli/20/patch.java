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
            else if (token.length() > 2 && options.hasOption(token.substring(0, 2)))
            {
                String opt = token.substring(0, 2);
                Option option = options.getOption(opt);
                if (option != null && option.getValueSeparator() > 0)
                {
                    String remaining = token.substring(2);
                    tokens.add(opt);
                    int sepIndex = remaining.indexOf(option.getValueSeparator());
                    while (sepIndex != -1)
                    {
                        tokens.add(remaining.substring(0, sepIndex));
                        remaining = remaining.substring(sepIndex + 1);
                        sepIndex = remaining.indexOf(option.getValueSeparator());
                    }
                    if (remaining.length() > 0)
                    {
                        tokens.add(remaining);
                    }
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

        gobble(iter);
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}