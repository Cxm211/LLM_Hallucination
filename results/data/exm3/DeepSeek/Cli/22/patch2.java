    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        String opt = token.startsWith("-") ? token.substring(1) : token;
        if (stopAtNonOption && !options.hasOption(opt))
        {
            processNonOptionToken(token);
            return;
        }
        tokens.add(token);
    }