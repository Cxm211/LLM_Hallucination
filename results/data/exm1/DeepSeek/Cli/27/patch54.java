// org.apache.commons.cli.HelpFormatterTest::testPrintSortedUsage
    public void testPrintSortedUsage()
    {
        Options opts = new Options();
        opts.addOption(new Option("a", "first"));
        opts.addOption(new Option("b", "second"));
        opts.addOption(new Option("c", "third"));

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        StringWriter out = new StringWriter();
        helpFormatter.printUsage(new PrintWriter(out), 80, "app", opts);

        assertEquals("usage: app [-c] [-b] [-a]" + EOL, out.toString());
    }