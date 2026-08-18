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
            if (option.hasArgName())
            {
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
                buff.append("<").append(option.getArgName()).append(">");
            }
            else
            {
                // ensure a separator is appended even when arg name is not specified
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
            }
        }
        
        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }