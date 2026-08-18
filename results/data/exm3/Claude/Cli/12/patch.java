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
                if (arg.contains("="))
                {
                    int equalsPos = arg.indexOf('=');
                    String optionPart = arg.substring(0, equalsPos);
                    String valuePart = arg.substring(equalsPos + 1);
                    
                    String strippedOption = Util.stripLeadingHyphens(optionPart);
                    if (options.hasOption(strippedOption))
                    {
                        tokens.add(optionPart);
                        tokens.add(valuePart);
                    }
                    else if (options.hasOption(optionPart.substring(0, 2)))
                    {
                        tokens.add(optionPart.substring(0, 2));
                        tokens.add(optionPart.substring(2) + "=" + valuePart);
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
                    }
                }
                else if (options.hasOption(arg.substring(0, 2)))
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