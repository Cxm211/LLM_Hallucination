    private void checkRequiredOptions()
        throws MissingOptionException
    {
        // if there are required options that have not been
        // processsed
        if (requiredOptions.size() > 0)
        {
            Iterator iter = requiredOptions.iterator();
            StringBuffer buff = new StringBuffer();


            // loop through the required options
            while (iter.hasNext())
            {
                buff.append(iter.next());
            }

            throw new MissingOptionException(buff.toString());
        }
    }

// trigger testcase
public void testMissingOptionException() throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        try {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required option: f", e.getMessage());
        }
    }

public void testMissingOptionsException() throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        options.addOption(OptionBuilder.isRequired().create("x"));
        try {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required options: fx", e.getMessage());
        }
    }
