public static Option create(String opt) throws IllegalArgumentException
    {
        try {
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

            // return the Option instance
            return option;
        } finally {
            // reset the OptionBuilder properties always, even on exception
            OptionBuilder.reset();
        }
    }