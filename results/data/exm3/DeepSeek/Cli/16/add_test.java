// org/apache/commons/cli2/CommandLineTestCase.java
public final void testConstructorPreservesInputList() {
        final Option help = DefaultOptionTest.buildHelpOption();
        final Option login = CommandTest.buildLoginCommand();
        final Option targets = ArgumentTest.buildTargetsArgument();
        List options = new ArrayList();
        options.add(help);
        options.add(login);
        options.add(targets);
        List copy = new ArrayList(options);
        GroupImpl group = new GroupImpl(options, "test", "test", 0, 3);
        // options should be unchanged
        assertEquals(copy, options);
    }
