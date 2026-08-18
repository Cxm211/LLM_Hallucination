    private void processNonOptionToken(String value)
    {
            if (!eatTheRest) {
                eatTheRest = true;
                tokens.add("--");
            }

        tokens.add(value);
    }
