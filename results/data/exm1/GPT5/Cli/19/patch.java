private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
            tokens.add(token);
        }
        else if (token != null && token.startsWith("--"))
        {
            String t = token.substring(2);
            String opt = t;
            String val = null;
            int eq = t.indexOf('=');
            if (eq != -1)
            {
                opt = t.substring(0, eq);
                val = t.substring(eq + 1);
            }
            if (options.hasOption(opt))
            {
                currentOption = options.getOption(opt);
                tokens.add("--" + opt);
                if (val != null)
                {
                    currentOption.addValueForProcessing(val);
                }
            }
            else if (stopAtNonOption)
            {
                eatTheRest = true;
                tokens.add(token);
            }
        }
        else if (token != null && token.startsWith("-") && token.length() > 2)
        {
            String opt = token.substring(1);
            if (options.hasOption(opt))
            {
                currentOption = options.getOption(opt);
                tokens.add("-" + opt);
            }
            else if (stopAtNonOption)
            {
                eatTheRest = true;
                tokens.add(token);
            }
        }
        else if (stopAtNonOption)
        {
            eatTheRest = true;
            tokens.add(token);
        }
    }