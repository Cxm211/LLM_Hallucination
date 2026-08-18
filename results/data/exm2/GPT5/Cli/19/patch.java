private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
            tokens.add(token);
        }
        else if (stopAtNonOption && !token.startsWith("-"))
        {
            eatTheRest = true;
            tokens.add(token);
        }
        else
        {
            throw new UnrecognizedOptionException(token);
        }
    }