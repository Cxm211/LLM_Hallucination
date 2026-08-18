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
    String optionKey = option.getOpt() != null ? option.getOpt() : option.getLongOpt();
    if (selected == null || selected.equals(optionKey))
    {
        selected = optionKey;
    }
    else
    {
        throw new AlreadySelectedException(this, option);
    }
}