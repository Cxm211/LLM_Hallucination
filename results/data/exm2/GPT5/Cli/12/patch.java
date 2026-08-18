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
                int eq = opt.indexOf('=');

                if (eq != -1)
                {
                    String optName = opt.substring(0, eq);
                    if (options.hasOption(optName))
                    {
                        String prefix = arg.substring(0, arg.indexOf('='));
                        String value = arg.substring(arg.indexOf('=') + 1);
                        tokens.add(prefix);
                        tokens.add(value);
                    }
                    else if (arg.length() >= 2 && arg.startsWith("-D") && options.hasOption(arg.substring(0, 2)))
                    {
                        tokens.add(arg.substring(0, 2)); // -D
                        tokens.add(arg.substring(2)); // property=value
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
                    }
                }
                else if (options.hasOption(opt))
                {
                    tokens.add(arg);
                }
                else if (arg.length() >= 2 && arg.startsWith("-D") && options.hasOption(arg.substring(0, 2)))
                {
                    tokens.add(arg.substring(0, 2)); // -D
                    tokens.add(arg.substring(2)); // property=value
                }
                else
                {
                    eatTheRest = stopAtNonOption;
                    tokens.add(arg);
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