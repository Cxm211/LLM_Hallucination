// buggy function
    public List<String> getMatchingOptions(String opt)
    {
        opt = Util.stripLeadingHyphens(opt);
        
        List<String> matchingOpts = new ArrayList<String>();

        // for a perfect match return the single option only

        for (String longOpt : longOpts.keySet())
        {
            if (longOpt.startsWith(opt))
            {
                matchingOpts.add(longOpt);
            }
        }
        
        return matchingOpts;
    }

// trigger testcase
// org/apache/commons/cli/bug/BugCLI252Test.java::testExactOptionNameMatch
@Test
    public void testExactOptionNameMatch() throws ParseException {
        new DefaultParser().parse(getOptions(), new String[]{"--prefix"});
    }
