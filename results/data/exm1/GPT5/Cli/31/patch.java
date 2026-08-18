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
            buff.append(longOptPrefix).append(option.getLongOpt());
        }
        
        // if the Option has a value, determine the displayed arg name
        if (option.hasArg())
        {
            String on = option.getArgName();
            // use the formatter-level default arg name unless an explicit non-empty arg name is provided
            String displayArgName = (on != null && on.length() != 0 && !"arg".equals(on)) ? on : argName;

            if (displayArgName != null && displayArgName.length() != 0)
            {
                buff.append(option.getOpt() == null ? longOptSeparator : " ");
                buff.append("<").append(displayArgName).append(">");
            }
        }
        
        // if the Option is not a required option
        if (!required)
        {
            buff.append("]");
        }
    }