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
        if (selected == null || selected.equals(option.getOpt()))
        {
            selected = option.getOpt();
        }
        else
        {
            throw new AlreadySelectedException(this, option);
        }
    }

// trigger testcase
public void testOptionGroupLong() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withLongOpt("foo").create());
        group.addOption(OptionBuilder.withLongOpt("bar").create());
        
        Options options = new Options();
        options.addOptionGroup(group);
        
        CommandLine cl = parser.parse(options, new String[] { "--bar" });
        
        assertTrue(cl.hasOption("bar"));
        assertEquals("selected option", "bar", group.getSelected());
    }

public void testOptionGroupLong() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withLongOpt("foo").create());
        group.addOption(OptionBuilder.withLongOpt("bar").create());
        
        Options options = new Options();
        options.addOptionGroup(group);
        
        CommandLine cl = parser.parse(options, new String[] { "--bar" });
        
        assertTrue(cl.hasOption("bar"));
        assertEquals("selected option", "bar", group.getSelected());
    }

public void testOptionGroupLong() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withLongOpt("foo").create());
        group.addOption(OptionBuilder.withLongOpt("bar").create());
        
        Options options = new Options();
        options.addOptionGroup(group);
        
        CommandLine cl = parser.parse(options, new String[] { "--bar" });
        
        assertTrue(cl.hasOption("bar"));
        assertEquals("selected option", "bar", group.getSelected());
    }
