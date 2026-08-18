    public WriteableCommandLineImpl(final Option rootOption,
                                    final List arguments) {
        this.prefixes = rootOption.getPrefixes();
        this.normalised = arguments;
    }

    public boolean looksLikeOption(final String trigger)
    {
            // this is a reentrant call

            for (final Iterator i = prefixes.iterator(); i.hasNext();)
            {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        return true;
                }
            }
            return false;
    }

    private boolean looksLikeOption(final WriteableCommandLine commandLine,
            final String trigger) {
            return commandLine.looksLikeOption(trigger);
    }

// trigger testcase
public void testNegativeNumber() throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Argument numArg = aBuilder.withValidator(
                NumberValidator.getNumberInstance()).withMinimum(1)
                .withMaximum(1).create();
        final Option numOpt = oBuilder.withLongName("num").withArgument(numArg)
                .create();
        final Group options = gBuilder.withOption(numOpt).create();

        final Parser parser = new Parser();
        parser.setGroup(options);

        CommandLine cl = parser.parse(new String[] {
                "--num", "-42"
        });
        Number num = (Number) cl.getValue(numOpt);
        assertEquals("Wrong option value", -42, num.intValue());
    }
