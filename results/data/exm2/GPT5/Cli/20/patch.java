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
                String optToCheck = (eq != -1) ? token.substring(0, eq) : token;
                boolean hasLong = (this.options != null) && (this.options.hasOption(optToCheck) || this.options.hasOption(token));
                if (eq != -1)
                {
                    if (hasLong)
                    {
                        tokens.add(optToCheck);
                        tokens.add(token.substring(eq + 1, token.length()));
                    }
                    else if (stopAtNonOption)
                    {
                        process(token);
                    }
                    else
                    {
                        tokens.add(token);
                    }
                }
                else
                {
                    if (hasLong)
                    {
                        tokens.add(token);
                    }
                    else if (stopAtNonOption)
                    {
                        process(token);
                    }
                    else
                    {
                        tokens.add(token);
                    }
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