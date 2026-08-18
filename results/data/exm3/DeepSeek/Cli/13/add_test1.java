// org/apache/commons/cli2/bug/BugLoopingOptionLookAlikeTest.java
public void testSubsequentSplitNoExtraTokens() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("test")
                                     .withSubsequentSeparator(',')
                                     .withMinimum(0)
                                     .withMaximum(3)
                                     .create();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group group = gbuilder.withOption(arg).create();
        final Parser parser = new Parser();
        parser.setGroup(group);
        parser.parse(new String[] { "a,b" });
        final WriteableCommandLine cmd = parser.getCommandLine();
        final List values = cmd.getValues(arg);
        assertEquals(2, values.size());
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
    }
