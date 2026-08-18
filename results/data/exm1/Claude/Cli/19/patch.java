private void processOptionToken(String token, boolean stopAtNonOption)
{
    if (token.startsWith("--") && options.hasOption(token.substring(2)))
    {
        currentOption = options.getOption(token.substring(2));
        tokens.add(token);
    }
    else if (token.startsWith("-") && token.length() > 1 && !token.startsWith("--") && options.hasOption(token.substring(1)))
    {
        currentOption = options.getOption(token.substring(1));
        tokens.add(token);
    }
    else if (stopAtNonOption)
    {
        eatTheRest = true;
        tokens.add(token);
    }
}