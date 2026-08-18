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
                    int eq = arg.indexOf('=');
                    if (eq != -1)
                    {
                        String optName = Util.stripLeadingHyphens(arg.substring(0, eq));
                        if (options.hasOption(optName))
                        {
                            tokens.add(arg.substring(0, eq));
                            tokens.add(arg.substring(eq + 1));
                        }
                        else if (arg.length() >= 2 && options.hasOption(arg.substring(0, 2)))
                        {
                            // special properties option like -Dproperty=value
                            tokens.add(arg.substring(0, 2)); // -D
                            tokens.add(arg.substring(2)); // property=value
                        }
                        else
                        {
                            eatTheRest = stopAtNonOption;
                            tokens.add(arg);
                        }
                    }
                    else if (arg.length() >= 2 && options.hasOption(arg.substring(0, 2)))
                    {
                        // special properties option like -Dproperty
                        tokens.add(arg.substring(0, 2)); // -D
                        tokens.add(arg.substring(2)); // property
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