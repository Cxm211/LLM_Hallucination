protected void processProperties(Properties properties) throws ParseException
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
                Option opt = getOptions().getOption(option);
                
                if (opt == null)
                {
                    throw new UnrecognizedOptionException("Unrecognized option: " + option, option);
                }
            
                // get the value from the properties instance
                String value = properties.getProperty(option);

                if (opt.hasArg())
                {
                    if (opt.getValues() == null || opt.getValues().length == 0)
                    {
                        try
                        {
                            opt.addValueForProcessing(value);
                        }
                        catch (RuntimeException exp)
                        {
                            // if we cannot add the value don't worry about it
                        }
                    }
                }
                else if (!("yes".equalsIgnoreCase(value)
                        || "true".equalsIgnoreCase(value)
                        || "1".equalsIgnoreCase(value)))
                {
                    // if the value is not yes, true or 1 then don't add the
                    // option to the CommandLine
                    continue;
                }

                // ensure group handling and other invariants are enforced consistently
                handleOption(opt);
                currentOption = null;
            }
        }
    }