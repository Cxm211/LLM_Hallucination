    private void processNonOptionToken(String value, boolean stopAtNonOption)
    {
        if (stopAtNonOption)
        {
            eatTheRest = true;
        }

        tokens.add(value);
    }