// org/apache/commons/cli2/bug/BugLoopingOptionLookAlikeTest.java
public void testSwitchOptionEmptyList() {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final List defaults = Arrays.asList("default");
        final Switch sw = sbuilder.withName("test").withDefaultValues(defaults).create();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group group = gbuilder.withOption(sw).create();
        final Parser parser = new Parser();
        parser.setGroup(group);
        parser.parse(new String[] { "-test" });
        final WriteableCommandLine cmd = parser.getCommandLine();
        final List values = cmd.getValues(sw);
        assertTrue(values.isEmpty());
    }
