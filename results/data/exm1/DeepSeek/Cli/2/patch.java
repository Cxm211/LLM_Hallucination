protected void burstToken(String token, boolean stopAtNonOption)
{
    // check if the token is a long option
    String opt = token.substring(1);
    if (options.hasLongOption(opt))
    {
        tokens.add("--" + opt);
        currentOption = options.getOption(opt);
        return;
    }

    int tokenLength = token.length();

    for (int i = 1; i < tokenLength; i++)
    {
        String ch = String.valueOf(token.charAt(i));
        boolean hasOption = options.hasOption(ch);

        if (hasOption)
        {
            tokens.add("-" + ch);
            currentOption = options.getOption(ch);

            if (currentOption.hasArg() && (token.length() != (i + 1)))
            {
                tokens.add(token.substring(i + 1));

                break;
            }
        }
        else if (stopAtNonOption)
        {
            process(token.substring(i));
        }
        else
        {
            tokens.add("-" + ch);
        }
    }
}