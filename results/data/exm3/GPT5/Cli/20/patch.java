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

            // handle long option --foo or --foo=bar
            if (token.startsWith("--"))
            {
                int eq = token.indexOf('=');
                if (eq != -1)
                {
                    String opt = token.substring(0, eq);
                    String val = token.substring(eq + 1, token.length());
                    if (options.hasOption(opt))
                    {
                        tokens.add(opt);
                        tokens.add(val);
                    }
                    else
                    {
                        if (stopAtNonOption)
                        {
                            process(token);
                        }
                        else
                        {
                            tokens.add(token);
                        }
                    }
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
                // requires bursting
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