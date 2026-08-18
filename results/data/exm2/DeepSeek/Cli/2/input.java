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
            }
            else
            {
                tokens.add("-" + ch);
            }
        }
    }

// trigger testcase
public void test() throws Exception
    {
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new PosixParser();
        String[] args = new String[] {"-t", "-something" };
        CommandLine commandLine;
        commandLine = parser.parse( options, args );
        assertEquals("-something", commandLine.getOptionValue( 't'));
    }
