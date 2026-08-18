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
                int eqIdx = opt.indexOf('=');

                if (eqIdx != -1)
                {
                    String optName = opt.substring(0, eqIdx);
                    String optValue = opt.substring(eqIdx + 1);

                    if (options.hasOption(optName))
                    {
                        tokens.add(arg.substring(0, arg.indexOf('=')));
                        tokens.add(optValue);
                    }
                    else if (!arg.startsWith("--") && opt.length() > 1 && options.hasOption(opt.substring(0, 1)))
                    {
                        // handle special short option with attached property/value, e.g. -Dkey=value
                        tokens.add("-" + opt.substring(0, 1));
                        tokens.add(opt.substring(1));
                    }
                    else
                    {
                        eatTheRest = stopAtNonOption;
                        tokens.add(arg);
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