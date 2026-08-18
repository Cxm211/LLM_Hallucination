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
            int equalsIndex = opt.indexOf('=');
            if (equalsIndex != -1)
            {
                String optName = opt.substring(0, equalsIndex);
                if (options.hasOption(optName))
                {
                    int argEqualsIndex = arg.indexOf('=');
                    String optionPart = arg.substring(0, argEqualsIndex);
                    tokens.add(optionPart);
                    tokens.add(opt.substring(equalsIndex + 1));
                }
                else
                {
                    if (options.hasOption(arg.substring(0, 2)))
                    {
                        tokens.add(arg.substring(0, 2));
                        tokens.add(arg.substring(2));
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
                if (options.hasOption(opt))
                {
                    tokens.add(arg);
                }
                else
                {
                    if (options.hasOption(arg.substring(0, 2)))
                    {
                        tokens.add(arg.substring(0, 2));
                        tokens.add(arg.substring(2));
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
                    }
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