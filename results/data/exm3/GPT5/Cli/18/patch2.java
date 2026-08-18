private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
            tokens.add(token);
        }
        else if (stopAtNonOption)
        {
            eatTheRest = true;
            // when stopping at non option, the triggering token itself must be preserved
            tokens.add(token);
        }
    }