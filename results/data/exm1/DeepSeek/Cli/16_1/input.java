// buggy code
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

// relevant test
// org.apache.commons.cli2.util.HelpFormatterTest::testPad_TooLong
    public void testPad_TooLong()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad("hello world", 10, new PrintWriter(writer));
        assertEquals("hello world", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testPad_TooShort
    public void testPad_TooShort()
        throws IOException {
        final StringWriter writer = new StringWriter();
        HelpFormatter.pad("hello world", -5, new PrintWriter(writer));
        assertEquals("hello world", writer.toString());
    }

// org.apache.commons.cli2.util.HelpFormatterTest::testGutters
    public void testGutters()
        throws IOException {
        helpFormatter = new HelpFormatter(null, null, null, 80);
        helpFormatter.setShellCommand("ant");

        final Set lusage = new HashSet();
        lusage.add(DisplaySetting.DISPLAY_ALIASES);
        lusage.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpFormatter.setLineUsageSettings(lusage);

        
        assertEquals("incorrect line usage", lusage, helpFormatter.getLineUsageSettings());

        final Set fusage = new HashSet();
        fusage.add(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        fusage.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        fusage.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        fusage.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        fusage.add(DisplaySetting.DISPLAY_ARGUMENT_BRACKETED);
        fusage.add(DisplaySetting.DISPLAY_ARGUMENT_NUMBERED);
        fusage.add(DisplaySetting.DISPLAY_SWITCH_ENABLED);
        fusage.add(DisplaySetting.DISPLAY_SWITCH_DISABLED);
        fusage.add(DisplaySetting.DISPLAY_PROPERTY_OPTION);
        fusage.add(DisplaySetting.DISPLAY_PARENT_CHILDREN);
        fusage.add(DisplaySetting.DISPLAY_PARENT_ARGUMENT);
        fusage.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpFormatter.setFullUsageSettings(fusage);

        
        assertEquals("incorrect full usage", fusage, helpFormatter.getFullUsageSettings());

        final Set dsettings = new HashSet();
        dsettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        dsettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        dsettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        helpFormatter.setDisplaySettings(dsettings);

        verbose =
            new DefaultOptionBuilder().withLongName("verbose")
                                      .withDescription("print the version information and exit")
                                      .create();

        options =
            new GroupBuilder().withName("options").withOption(DefaultOptionTest.buildHelpOption())
                              .withOption(ArgumentTest.buildTargetsArgument())
                              .withOption(new DefaultOptionBuilder().withLongName("diagnostics")
                                                                    .withDescription("print information that might be helpful to diagnose or report problems.")
                                                                    .create())
                              .withOption(new DefaultOptionBuilder().withLongName("projecthelp")
                                                                    .withDescription("print project help information")
                                                                    .create()).withOption(verbose)
                              .create();

        helpFormatter.setGroup(options);

        
        assertEquals("incorrect left gutter", HelpFormatter.DEFAULT_GUTTER_LEFT,
                     helpFormatter.getGutterLeft());
        assertEquals("incorrect right gutter", HelpFormatter.DEFAULT_GUTTER_RIGHT,
                     helpFormatter.getGutterRight());
        assertEquals("incorrect center gutter", HelpFormatter.DEFAULT_GUTTER_CENTER,
                     helpFormatter.getGutterCenter());

        final StringWriter writer = new StringWriter();
        helpFormatter.setPrintWriter(new PrintWriter(writer));
        helpFormatter.print();

        final BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
        assertEquals("Usage:                                                                          ",
                     reader.readLine());
        assertEquals("ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2> ...]]  ",
                     reader.readLine());
        assertEquals("options                                                                         ",
                     reader.readLine());
        assertEquals("  --help (-?,-h)         Displays the help                                      ",
                     reader.readLine());
        assertEquals("  --diagnostics          print information that might be helpful to diagnose or ",
                     reader.readLine());
        assertEquals("                         report problems.                                       ",
                     reader.readLine());
        assertEquals("  --projecthelp          print project help information                         ",
                     reader.readLine());
        assertEquals("  --verbose              print the version information and exit                 ",
                     reader.readLine());
        assertEquals("  target [target ...]    The targets ant should build                           ",
                     reader.readLine());
        assertNull(reader.readLine());
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testValidName
    public void testValidName() throws InvalidArgumentException {
        final Object[] array = new Object[] { "MyApp", "org.apache.ant.Main" };
        final List list = Arrays.asList(array);

        validator.validate(list);

        assertEquals("Name is incorrect", "MyApp", list.get(0));
        assertEquals("Name is incorrect", "org.apache.ant.Main", list.get(1));
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testNameBadStart
    public void testNameBadStart() {
        final String className = "1stClass";
        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        try {
            validator.validate(list);
            fail("Class name cannot start with a number.");
        } catch (InvalidArgumentException ive) {
            assertEquals(
                resources.getMessage(
                    "ClassValidator.bad.classname",
                    className),
                ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testNameBadEnd
    public void testNameBadEnd() {
        final String className = "My.Class.";

        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        try {
            validator.validate(list);
            fail("Trailing period not permitted.");
        } catch (InvalidArgumentException ive) {
            assertEquals(
                resources.getMessage(
                    "ClassValidator.bad.classname",
                    className),
                ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testNameBadMiddle
    public void testNameBadMiddle() {
        final String className = "My..Class";

        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        try {
            validator.validate(list);
            fail("Two consecutive periods is not permitted.");
        } catch (InvalidArgumentException ive) {
            assertEquals(
                resources.getMessage(
                    "ClassValidator.bad.classname",
                    className),
                ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testIllegalNameChar
    public void testIllegalNameChar() {
        final String className = "My?Class";

        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        try {
            validator.validate(list);
            fail("Illegal character not allowed in Class name.");
        } catch (InvalidArgumentException ive) {
            assertEquals(
                resources.getMessage(
                    "ClassValidator.bad.classname",
                    className),
                ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testLoadable
    public void testLoadable() {
        assertFalse("Validator is loadable", validator.isLoadable());
        validator.setLoadable(true);
        assertTrue("Validator is NOT loadable", validator.isLoadable());
        validator.setLoadable(false);
        assertFalse("Validator is loadable", validator.isLoadable());
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testLoadValid
    public void testLoadValid() throws InvalidArgumentException {
        final Object[] array =
            new Object[] {
                "org.apache.commons.cli2.Option",
                "java.util.Vector" };
        final List list = Arrays.asList(array);

        validator.setLoadable(true);
        validator.validate(list);

        final Iterator i = list.iterator();
        assertEquals(
            "org.apache.commons.cli2.Option",
            ((Class) i.next()).getName());
        assertEquals("java.util.Vector", ((Class) i.next()).getName());
        assertFalse(i.hasNext());
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testLoadInvalid
    public void testLoadInvalid() {
        final String className = "org.apache.commons.cli2.NonOption";

        final Object[] array = new Object[] { className, "java.util.Vectors" };
        final List list = Arrays.asList(array);

        validator.setLoadable(true);

        try {
            validator.validate(list);
            fail("Class Not Found");
        } catch (InvalidArgumentException ive) {
            assertEquals(
                resources.getMessage(
                    "ClassValidator.class.notfound",
                    className),
                ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testInstantiate
    public void testInstantiate() {
        assertFalse("Validator creates instances", validator.isInstance());
        validator.setInstance(true);
        assertTrue(
            "Validator does NOT create instances",
            validator.isInstance());
        validator.setInstance(false);
        assertFalse("Validator creates instances", validator.isInstance());
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testCreateClassInstance
    public void testCreateClassInstance() throws InvalidArgumentException {
        final Object[] array = new Object[] { "java.util.Vector" };
        final List list = Arrays.asList(array);

        validator.setInstance(true);

        validator.validate(list);
        assertTrue(
            "Vector instance NOT found",
            list.get(0) instanceof java.util.Vector);
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testCreateInterfaceInstance
    public void testCreateInterfaceInstance() {
        final String className = "java.util.Map";
        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        validator.setInstance(true);

        try {
            validator.validate(list);
            fail("It's not possible to create a '" + className + "'");
        }
        catch (final InvalidArgumentException ive) {
            assertEquals(
                    resources.getMessage(
                            "ClassValidator.class.create",
                            className),
                            ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testCreateProtectedInstance
    public void testCreateProtectedInstance() {
        final String className = "org.apache.commons.cli2.validation.protect.ProtectedClass";
        final Object[] array = new Object[] { className };
        final List list = Arrays.asList(array);

        validator.setInstance(true);

        try {
            validator.validate(list);
            fail("It's not possible to create a '" + className + "'");
        }
        catch (final InvalidArgumentException ive) {
            assertEquals(
                    resources.getMessage(
                            "ClassValidator.class.access",
                            className,
                            "class org.apache.commons.cli2.validation.ClassValidator " +
                            "cannot access a member of class " +
                            "org.apache.commons.cli2.validation.protect.ProtectedClass " +
                            "with modifiers \"protected\""),
                            ive.getMessage());
        }
    }

// org.apache.commons.cli2.validation.ClassValidatorTest::testClassloader
    public void testClassloader() {
        assertEquals(
            "Wrong classloader found",
            validator.getClass().getClassLoader(),
            validator.getClassLoader());

        URLClassLoader classloader = new URLClassLoader(new URL[] {
        });
        validator.setClassLoader(classloader);

        assertEquals(
            "Wrong classloader found",
            classloader,
            validator.getClassLoader());
    }
