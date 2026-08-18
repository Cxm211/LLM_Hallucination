public static Option create(String opt) throws IllegalArgumentException
    {
        if (opt == null || opt.length() == 0)
        {
            throw new IllegalArgumentException("opt is null or empty");
        }
        // validate that all characters in opt are legal (letters, digits, '?' or '@')
        for (int i = 0; i < opt.length(); i++)
        {
            char ch = opt.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '?' && ch != '@')
            {
                throw new IllegalArgumentException("Illegal character in option: " + ch);
            }
        }
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