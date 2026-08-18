// org/apache/commons/cli/OptionBuilderTest.java
public void testResetAfterCreate() {
        // Build first option with all non-default values
        Option first = OptionBuilder.withDescription("desc")
                                    .withArgName("myarg")
                                    .withLongOpt("long")
                                    .withType(Number.class)
                                    .hasArgs()
                                    .isRequired()
                                    .withOptionalArg()
                                    .withValueSeparator('=')
                                    .create('a');
        
        // Verify first option
        assertEquals("desc", first.getDescription());
        assertEquals("myarg", first.getArgName());
        assertEquals("long", first.getLongOpt());
        assertEquals(Number.class, first.getType());
        assertTrue(first.hasArgs());
        assertTrue(first.isRequired());
        assertTrue(first.hasOptionalArg());
        assertEquals('=', first.getValueSeparator());
        
        // Build second option with no builder methods (should use defaults)
        Option second = OptionBuilder.create('b');
        
        // Assert defaults
        assertNull(second.getDescription());
        assertEquals("arg", second.getArgName());
        assertNull(second.getLongOpt());
        assertEquals(String.class, second.getType());
        assertFalse(second.hasArg());
        assertFalse(second.isRequired());
        assertFalse(second.hasOptionalArg());
        assertEquals(0, second.getValueSeparator());
    }
