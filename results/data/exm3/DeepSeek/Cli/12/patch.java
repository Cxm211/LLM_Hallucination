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
                // Check for embedded equals sign for options that take an argument
                int eqIdx = arg.indexOf('=');
                if (eqIdx > 0)
                {
                    String prefix = arg.substring(0, eqIdx);
                    String optWithoutHyphens = Util.stripLeadingHyphens(prefix);
                    if (options.hasOption(optWithoutHyphens))
                    {
                        Option option = options.getOption(optWithoutHyphens);
                        if (option.hasArg())
                        {
                            tokens.add(prefix);
                            tokens.add(arg.substring(eqIdx + 1));
                            continue;
                        }
                    }
                }

                String opt = Util.stripLeadingHyphens(arg);

                if (options.hasOption(opt))
                {
                    tokens.add(arg);
                }
                else
                {
                    if (options.hasOption(arg.substring(0, 2)))
                    {
                        // the format is --foo=value or -foo=value
                        // the format is a special properties option (-Dproperty=value)
                        tokens.add(arg.substring(0, 2)); // -D
                        tokens.add(arg.substring(2)); // property=value
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