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
        
        // if the Option has a value, append the appropriate arg name
        if (option.hasArg())
        {
            String sep = (option.getOpt() == null ? longOptSeparator : " ");
            String name = option.hasArgName() ? option.getArgName() : getArgName();
            if (name != null && name.length() > 0)
            {
                buff.append(sep);
                buff.append("<").append(name).append(">");
            }
            else
            {
                buff.append(sep);
            }
        }
        
        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }