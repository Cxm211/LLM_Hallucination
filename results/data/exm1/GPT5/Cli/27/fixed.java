// ===== FIXED org.apache.commons.cli.OptionGroup :: setSelected(Option) [lines 86-106] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-27-fixed/src/java/org/apache/commons/cli/OptionGroup.java =====
    public void setSelected(Option option) throws AlreadySelectedException
    {
        if (option == null)
        {
            // reset the option previously selected
            selected = null;
            return;
        }
        
        // if no option has already been selected or the 
        // same option is being reselected then set the
        // selected member variable
        if (selected == null || selected.equals(option.getKey()))
        {
            selected = option.getKey();
        }
        else
        {
            throw new AlreadySelectedException(this, option);
        }
    }
