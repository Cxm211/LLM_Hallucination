    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (stopAtNonOption && !options.hasOption(token))
        {
            // If not recognized as-is (e.g., "-e"), check without leading '-'
            String stripped = token.startsWith("-") && token.length() > 1 ? token.substring(1) : token;
            if (!options.hasOption(stripped))
            {
                eatTheRest = true;
            }
        }

        tokens.add(token);
    }
