    private void appendOption(final StringBuffer buff, final Option option, final boolean required)
    {
        if (!required)
        {
            buff.append("[");
        }

        if (option.getOpt() != null)
        {
            buff.append("-").append(option.getOpt());
        }
        else
        {
            buff.append("--").append(option.getLongOpt());
        }
        
        // if the Option has a value
        if (option.hasArg())
        {
            String argName = option.getArgName();
            if (argName != null && argName.length() == 0)
            {
                // if the option has a blank argname
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
                buff.append("<>");
            }
            else if (argName != null)
            {
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
                buff.append("<").append(argName).append(">");
            }
            else
            {
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
                buff.append("<").append(defaultArgName).append(">");
            }
        }
        
        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }