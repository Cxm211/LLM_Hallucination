public static Option create(String opt) throws IllegalArgumentException
{
        // create the option
    Option option = new Option(opt, OptionBuilder.description);

        // set the option properties
        option.setLongOpt(OptionBuilder.longopt);
        option.setRequired(OptionBuilder.required);
        option.setOptionalArg(OptionBuilder.optionalArg);
        option.setArgs(OptionBuilder.numberOfArgs);
        option.setType(OptionBuilder.type);
        option.setValueSeparator(OptionBuilder.valuesep);
        option.setArgName(OptionBuilder.argName);
        // reset the OptionBuilder properties
        OptionBuilder.reset();

    // return the Option instance
    return option;
}