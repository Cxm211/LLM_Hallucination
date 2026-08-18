public static Option create(String opt) throws IllegalArgumentException
{
    try
    {
        Option option = new Option(opt, description);

        option.setLongOpt(longopt);
        option.setRequired(required);
        option.setOptionalArg(optionalArg);
        option.setArgs(numberOfArgs);
        option.setType(type);
        option.setValueSeparator(valuesep);
        option.setArgName(argName);

        return option;
    }
    finally
    {
        OptionBuilder.reset();
    }
}