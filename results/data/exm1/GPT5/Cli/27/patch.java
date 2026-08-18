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
        String key = option.getKey();
        if (selected == null || selected.equals(key))
        {
            selected = key;
        }
        else
        {
            throw new AlreadySelectedException(this, option);
        }
    }