    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        String optName = token.startsWith("--") ? token.substring(2) : (token.startsWith("-") ? token.substring(1) : token);
        if (stopAtNonOption && !options.hasOption(optName))
        {
            eatTheRest = true;
        }


        tokens.add(token);
    }
