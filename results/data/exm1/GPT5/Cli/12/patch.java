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

                int eqIndex = opt.indexOf('=');

                if (eqIndex != -1)
                {
                    String optName = opt.substring(0, eqIndex);
                    String optValue = opt.substring(eqIndex + 1);

                    if (options.hasOption(optName))
                    {
                        int namePos = arg.indexOf(optName);
                        String prefix = (namePos >= 0) ? arg.substring(0, namePos) : "-";
                        tokens.add(prefix + optName);
                        tokens.add(optValue);
                    }
                    else if (opt.length() > 0 && options.hasOption(opt.substring(0,1)))
                    {
                        tokens.add("-" + opt.substring(0,1));
                        tokens.add(opt.substring(1));
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
                else if (opt.length() > 0 && options.hasOption(opt.substring(0,1)))
                {
                    tokens.add("-" + opt.substring(0,1));
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