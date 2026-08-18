    public static Option create(String opt) throws IllegalArgumentException
    {
            // create the option
        Option option = new Option(opt, description);

            // set the option properties
            option.setLongOpt(longopt);
            option.setRequired(required);
            option.setOptionalArg(optionalArg);
            option.setArgs(numberOfArgs);
            option.setType(type);
            option.setValueSeparator(valuesep);
            option.setArgName(argName);
            // reset the OptionBuilder properties
            OptionBuilder.reset();

        // return the Option instance
        return option;
    }

// trigger testcase
public void testBuilderIsResettedAlways() {
        try
        {
            OptionBuilder.withDescription("JUnit").create('"');
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());

        try
        {
            OptionBuilder.withDescription("JUnit").create();
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        assertNull("we inherited a description", OptionBuilder.create('x').getDescription());
    }
