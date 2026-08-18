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
            }
            else
            {
                tokens.add(token);
                break;
            }
        }
    }

// trigger testcase
public void testStopBursting() throws Exception
    {
        String[] args = new String[] { "-azc" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue( "Confirm -a is set", cl.hasOption("a") );
        assertFalse( "Confirm -c is not set", cl.hasOption("c") );

        assertTrue( "Confirm  1 extra arg: " + cl.getArgList().size(), cl.getArgList().size() == 1);
        assertTrue(cl.getArgList().contains("zc"));
    }
