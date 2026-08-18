private void processOptionToken(String token, boolean stopAtNonOption)
{
    if (stopAtNonOption && !options.hasOption(token.substring(1)))
    {
        eatTheRest = true;
    }

    tokens.add(token);
}