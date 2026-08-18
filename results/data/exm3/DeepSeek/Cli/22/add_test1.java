// org/apache/commons/cli/ApplicationTest.java
public void testClusterWithUnknownAndStop() throws Exception {
        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("test").create('a'));
        
        Parser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-ab" }, true);
        
        assertTrue("Option a should be set", line.hasOption('a'));
        assertFalse("Option b should not be set", line.hasOption('b'));
        assertEquals(1, line.getArgList().size());
        assertEquals("b", line.getArgList().get(0));
    }
