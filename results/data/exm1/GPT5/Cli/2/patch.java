protected void burstToken(String token, boolean stopAtNonOption)
    {
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
                // treat the whole token as a single token (e.g. a long option like -attr)
                tokens.add(token);
                break;
            }
        }
    }