// org.apache.commons.cli.BugsTest::test12210
    public void test12210() throws Exception
    {
        
        Options mainOptions = new Options();
        

        

        String[] argv = new String[] { "-exec", "-exec_opt1", "-exec_opt2" };
        OptionGroup grp = new OptionGroup();

        grp.addOption(new Option("exec",false,"description for this option"));

        grp.addOption(new Option("rep",false,"description for this option"));

        mainOptions.addOptionGroup(grp);

        
        Options execOptions = new Options();
        execOptions.addOption("exec_opt1", false, " desc");
        execOptions.addOption("exec_opt2", false, " desc");

        
        Options repOptions = new Options();
        repOptions.addOption("repopto", false, "desc");
        repOptions.addOption("repoptt", false, "desc");

        
        GnuParser parser = new GnuParser();

        

        
        
        
        CommandLine cmd = parser.parse(mainOptions,argv,true);
        
        argv = cmd.getArgs();

        if(cmd.hasOption("exec"))
        {
            cmd = parser.parse(execOptions,argv,false);
            
            assertTrue( cmd.hasOption("exec_opt1") );
            assertTrue( cmd.hasOption("exec_opt2") );
        }
        else if(cmd.hasOption("rep"))
        {
            cmd = parser.parse(repOptions,argv,false);
            
        }
        else {
            fail( "exec option not found" );
        }
    }