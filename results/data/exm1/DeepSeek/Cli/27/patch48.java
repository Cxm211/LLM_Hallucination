// org.apache.commons.cli.HelpFormatterTest::testFindWrapPos
    public void testFindWrapPos() throws Exception
    {
        HelpFormatter hf = new HelpFormatter();

        String text = "This is a test.";
        
        assertEquals("wrap position", 7, hf.findWrapPos(text, 8, 0));
        
        assertEquals("wrap position 2", -1, hf.findWrapPos(text, 8, 8));
        
        text = "aaaa aa";
        assertEquals("wrap position 3", 4, hf.findWrapPos(text, 3, 0));
    }