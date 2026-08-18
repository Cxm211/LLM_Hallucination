    private void handleProperties(Properties properties) throws ParseException
    {
        if (properties == null)
        {
            return;
        }
        
        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();)
        {
            String option = e.nextElement().toString();
            
            if (!cmd.hasOption(option))
            {
                Option opt = options.getOption(option);
                
                // if option is not recognized, throw UnrecognizedOptionException
                if (opt == null)
                {
                    throw new UnrecognizedOptionException("Unrecognized option: " + option);
                }
            
                // if the option is part of a group, check if another option of the group has been selected
                OptionGroup group = options.getOptionGroup(opt);
                if (group != null)
                {
                    boolean alreadySelected = false;
                    for (Option groupOption : group.getOptions())
                    {
                        if (cmd.hasOption(groupOption.getOpt()) || (groupOption.getLongOpt() != null && cmd.hasOption(groupOption.getLongOpt())))
                        {
                            alreadySelected = true;
                            break;
                        }
                    }
                    if (alreadySelected)
                    {
                        continue;
                    }
                }
            
                // get the value from the properties
                String value = properties.getProperty(option);
                
                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        opt.addValueForProcessing(value);
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the option to the CommandLine
                    continue;
                }
                
                handleOption(opt);
                currentOption = null;
            }
        }
    }