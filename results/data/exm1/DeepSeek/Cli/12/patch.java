protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption)
{
    List tokens = new ArrayList();

    boolean eatTheRest = false;

    for (int i = 0; i < arguments.length; i++)
    {
        String arg = arguments[i];

        if ("--".equals(arg))
        {
            eatTheRest = true;
            tokens.add("--");
        }
        else if ("-".equals(arg))
        {
            tokens.add("-");
        }
        else if (arg.startsWith("-"))
        {
            String opt = Util.stripLeadingHyphens(arg);

            if (options.hasOption(opt))
            {
                tokens.add(arg);
            }
            else
            {
                // check for short option with attached value (e.g., -Dproperty=value, -fbar)
                if (arg.length() > 1 && options.hasOption(Util.stripLeadingHyphens(arg.substring(0, 2))))
                {
                    tokens.add(arg.substring(0, 2));
                    String value = arg.substring(2);
                    if (value.startsWith("="))
                    {
                        value = value.substring(1);
                    }
                    tokens.add(value);
                }
                // check for long option with '=' or short option with '=' not already handled
                else if (arg.indexOf('=') != -1)
                {
                    int pos = arg.indexOf('=');
                    String optPart = arg.substring(0, pos);
                    if (options.hasOption(Util.stripLeadingHyphens(optPart)))
                    {
                        tokens.add(optPart);
                        tokens.add(arg.substring(pos + 1));
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
                    }
                }
                else
                {
                    eatTheRest = stopAtNonOption;
                    tokens.add(arg);
                }
            }
        }
        else
        {
            tokens.add(arg);
        }

        if (eatTheRest)
        {
            for (i++; i < arguments.length; i++)
            {
                tokens.add(arguments[i]);
            }
        }
    }

    return (String[]) tokens.toArray(new String[tokens.size()]);
}