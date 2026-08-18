    private void processNonOptionToken(String value)
    {
        eatTheRest = true;
        if (tokens.isEmpty() || !"--".equals(tokens.get(tokens.size()-1)))
        {
            tokens.add("--");
        }
        tokens.add(value);
    }