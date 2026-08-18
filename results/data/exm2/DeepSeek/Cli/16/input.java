    void appendUsage(
        final StringBuffer buffer,
        final Set helpSettings,
        final Comparator comp);

    public void addOption(Option option) {
        options.add(option);
        nameToOption.put(option.getPreferredName(), option);

        for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
            nameToOption.put(i.next(), option);
        }

        // ensure that all parent options are also added
    }

    public GroupImpl(final List options,
                     final String name,
                     final String description,
                     final int minimum,
                     final int maximum) {
        super(0, false);

        this.name = name;
        this.description = description;
        this.minimum = minimum;
        this.maximum = maximum;

        // store a copy of the options to be used by the
        // help methods
        this.options = Collections.unmodifiableList(options);

        // anonymous Argument temporary storage
        final List newAnonymous = new ArrayList();

        // map (key=trigger & value=Option) temporary storage
        final SortedMap newOptionMap = new TreeMap(ReverseStringComparator.getInstance());

        // prefixes temporary storage
        final Set newPrefixes = new HashSet();

        // process the options
        for (final Iterator i = options.iterator(); i.hasNext();) {
            final Option option = (Option) i.next();

            if (option instanceof Argument) {
                i.remove();
                newAnonymous.add(option);
            } else {
                final Set triggers = option.getTriggers();

                for (Iterator j = triggers.iterator(); j.hasNext();) {
                    newOptionMap.put(j.next(), option);
                }

                // store the prefixes
                newPrefixes.addAll(option.getPrefixes());
            }
        }

        this.anonymous = Collections.unmodifiableList(newAnonymous);
        this.optionMap = Collections.unmodifiableSortedMap(newOptionMap);
        this.prefixes = Collections.unmodifiableSet(newPrefixes);
    }

    public void defaults(final WriteableCommandLine commandLine) {
        // nothing to do normally
    }

// trigger testcase
public void testMultipleChildOptions() throws OptionException {
        CommandLine cl = parser.parse(new String[] { "--child", "test",
                "--sub", "anotherTest" });
        assertTrue("Child option not found", cl.hasOption(childOption1));
        assertEquals("Wrong value for option", "test", cl
                .getValue(childOption1));
        assertTrue("Sub option not found", cl.hasOption(childOption2));
        assertEquals("Wrong value for sub option", "anotherTest", cl
                .getValue(childOption2));
        assertTrue("Child group not found", cl.hasOption(childGroup));
    }

public void testParentOptionAndChildOption() throws OptionException {
        try {
            parser.parse(new String[] { "--parent", "error", "--child",
                    "exception" });
            fail("Maximum restriction for parent not verified!");
        } catch (OptionException oex) {
            // ok
        }
    }

public void testSingleChildOption() throws OptionException {
        CommandLine cl = parser.parse(new String[] { "--child", "test" });
        assertTrue("Child option not found", cl.hasOption(childOption1));
        assertEquals("Wrong value for option", "test", cl
                .getValue(childOption1));
        assertTrue("Child group not found", cl.hasOption(childGroup));
    }

public final void testGetOptions_Order()
        throws OptionException {
        final Option help = DefaultOptionTest.buildHelpOption();
        final Option login = CommandTest.buildLoginCommand();
        final Option targets = ArgumentTest.buildTargetsArgument();

        final Group group =
            new GroupBuilder().withOption(help).withOption(login).withOption(targets).create();

        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl =
            parser.parse(new String[] { "login", "rob", "--help", "target1", "target2" });

        final Iterator i = cl.getOptions().iterator();

        assertSame(login, i.next());
        assertSame(group, i.next());
        assertSame(help, i.next());
        assertSame(targets, i.next());
        assertSame(targets, i.next());
        assertFalse(i.hasNext());
    }

public final void testGetOptions_Order()
        throws OptionException {
        final Option help = DefaultOptionTest.buildHelpOption();
        final Option login = CommandTest.buildLoginCommand();
        final Option targets = ArgumentTest.buildTargetsArgument();

        final Group group =
            new GroupBuilder().withOption(help).withOption(login).withOption(targets).create();

        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl =
            parser.parse(new String[] { "login", "rob", "--help", "target1", "target2" });

        final Iterator i = cl.getOptions().iterator();

        assertSame(login, i.next());
        assertSame(group, i.next());
        assertSame(help, i.next());
        assertSame(targets, i.next());
        assertSame(targets, i.next());
        assertFalse(i.hasNext());
    }

public final void testGetOptions_Order()
        throws OptionException {
        final Option help = DefaultOptionTest.buildHelpOption();
        final Option login = CommandTest.buildLoginCommand();
        final Option targets = ArgumentTest.buildTargetsArgument();

        final Group group =
            new GroupBuilder().withOption(help).withOption(login).withOption(targets).create();

        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl =
            parser.parse(new String[] { "login", "rob", "--help", "target1", "target2" });

        final Iterator i = cl.getOptions().iterator();

        assertSame(login, i.next());
        assertSame(group, i.next());
        assertSame(help, i.next());
        assertSame(targets, i.next());
        assertSame(targets, i.next());
        assertFalse(i.hasNext());
    }

public final void testGetOptions_Order()
        throws OptionException {
        final Option help = DefaultOptionTest.buildHelpOption();
        final Option login = CommandTest.buildLoginCommand();
        final Option targets = ArgumentTest.buildTargetsArgument();

        final Group group =
            new GroupBuilder().withOption(help).withOption(login).withOption(targets).create();

        final Parser parser = new Parser();
        parser.setGroup(group);

        final CommandLine cl =
            parser.parse(new String[] { "login", "rob", "--help", "target1", "target2" });

        final Iterator i = cl.getOptions().iterator();

        assertSame(login, i.next());
        assertSame(group, i.next());
        assertSame(help, i.next());
        assertSame(targets, i.next());
        assertSame(targets, i.next());
        assertFalse(i.hasNext());
    }
