    private void processOptionToken(String token, boolean stopAtNonOption)
    {
        if (options.hasOption(token))
        {
            currentOption = options.getOption(token);
            tokens.add(token);
        }
        else if (stopAtNonOption)
        {
            eatTheRest = true;
            tokens.add(token);
        }
    }

// trigger testcase
public void testUnrecognizedOption2() throws Exception
    {
        String[] args = new String[] { "-z", "-abtoast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-z", e.getOption());
        }
    }
