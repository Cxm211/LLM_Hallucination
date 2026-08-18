    protected void burstToken(String token, boolean stopAtNonOption)
    {
        // If there is a current option waiting for an argument, treat the entire token as the argument.
        if (currentOption != null && currentOption.hasArg())
        {
            tokens.add(token);
            currentOption = null;
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
                break;
            }
            else
            {
                tokens.add("-" + ch);
            }
        }
    }