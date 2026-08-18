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

            // handle SPECIAL TOKEN
            if (token.startsWith("--"))
            {
                if (token.indexOf('=') != -1)
                {
                    tokens.add(token.substring(0, token.indexOf('=')));
                    tokens.add(token.substring(token.indexOf('=') + 1, token.length()));
                }
                else
                {
                    tokens.add(token);
                }
            }

            // single hyphen
            else if ("-".equals(token))
            {
                processSingleHyphen(token);
            }
            else if (token.startsWith("-"))
            {
                // Check if the token (without the dash) is a recognized option (could be short or long)
                String withoutDash = token.substring(1);
                int eqIndex = withoutDash.indexOf('=');
                String optKey = (eqIndex != -1) ? withoutDash.substring(0, eqIndex) : withoutDash;
                if (options.hasOption(optKey))
                {
                    // Recognized as an option; treat it accordingly.
                    if (eqIndex != -1)
                    {
                        tokens.add("-" + optKey);
                        tokens.add(withoutDash.substring(eqIndex + 1));
                    }
                    else
                    {
                        tokens.add(token);
                    }
                }
                else if (token.length() == 2)
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