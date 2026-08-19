// ===== FIXED org.apache.commons.cli.PosixParser :: burstToken(String, boolean) [lines 282-311] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-17-fixed/src/java/org/apache/commons/cli/PosixParser.java =====
    protected void burstToken(String token, boolean stopAtNonOption)
    {
        for (int i = 1; i < token.length(); i++)
        {
            String ch = String.valueOf(token.charAt(i));

            if (options.hasOption(ch))
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
                tokens.add(token);
                break;
            }
        }
    }
