private void processOptionToken(String token, boolean stopAtNonOption)
{
    if (options.hasOption(token))
    {
        currentOption = options.getOption(token);
        tokens.add(token);
    }
    else
    {
        String key = null;
        if (token.startsWith("--"))
        {
            int eqIndex = token.indexOf('=');
            if (eqIndex != -1)
            {
                key = token.substring(2, eqIndex);
            }
            else
            {
                key = token.substring(2);
            }
        }
        else if (token.startsWith("-") && token.length() > 1)
        {
            // Try long option first
            String longKey = token.substring(1);
            if (options.hasOption(longKey))
            {
                key = longKey;
            }
            else
            {
                // short option: first character after '-'
                key = token.substring(1, 2);
            }
        }
        if (key != null && options.hasOption(key))
        {
            currentOption = options.getOption(key);
            tokens.add(token);
        }
        else if (stopAtNonOption)
        {
            eatTheRest = true;
            tokens.add(token);
        }
    }
}