// buggy function
    public List getValues(final Option option,
                          final List defaultValues) {
        // First grab the command line values
        List valueList = (List) values.get(option);

        // Secondly try the defaults supplied to the method
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        // Thirdly try the option's default values
        if ((valueList == null) || valueList.isEmpty()) {
            valueList = (List) this.defaultValues.get(option);
        }

        // Finally use an empty list
        if (valueList == null) {
            valueList = Collections.EMPTY_LIST;
        }

        return valueList;
    }

    public void processValues(final WriteableCommandLine commandLine,
                              final ListIterator arguments,
                              final Option option)
        throws OptionException {
        // count of arguments processed for this option.
        int argumentCount = 0;

        while (arguments.hasNext() && (argumentCount < maximum)) {
            final String allValuesQuoted = (String) arguments.next();
            final String allValues = stripBoundaryQuotes(allValuesQuoted);

            // should we ignore things that look like options?
            if (allValuesQuoted.equals(consumeRemaining)) {
                while (arguments.hasNext() && (argumentCount < maximum)) {
                    ++argumentCount;
                    commandLine.addValue(option, arguments.next());
                }
            }
            // does it look like an option?
            else if (commandLine.looksLikeOption(allValuesQuoted)) {
                arguments.previous();

                break;
            }
            // should we split the string up?
            else if (subsequentSplit) {
                final StringTokenizer values =
                    new StringTokenizer(allValues, String.valueOf(subsequentSeparator));

                arguments.remove();

                while (values.hasMoreTokens() && (argumentCount < maximum)) {
                    ++argumentCount;

                    final String token = values.nextToken();
                    commandLine.addValue(option, token);
                    arguments.add(token);
                }

                if (values.hasMoreTokens()) {
                    throw new OptionException(option, ResourceConstants.ARGUMENT_UNEXPECTED_VALUE,
                                              values.nextToken());
                }
            }
            // it must be a value as it is
            else {
                ++argumentCount;
                commandLine.addValue(option, allValues);
            }
        }
    }

// trigger testcase
// org/apache/commons/cli2/bug/BugLoopingOptionLookAlikeTest.java::testLoopingOptionLookAlike2
public void testLoopingOptionLookAlike2() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Argument inputfile_opt = abuilder.withName("input").withMinimum(1).withMaximum(1).create();
        final Argument outputfile_opt = abuilder.withName("output").withMinimum(1).withMaximum(1).create();
        final Argument targets = new SourceDestArgument(inputfile_opt, outputfile_opt);
        final Group options = gbuilder.withOption(targets).create();
        final Parser parser = new Parser();
        parser.setGroup(options);
        try {
            parser.parse(new String[] { "testfile.txt", "testfile.txt", "testfile.txt", "testfile.txt" });
            fail("OptionException");
        } catch (OptionException e) {
            assertEquals("Unexpected testfile.txt while processing ", e.getMessage());
        }
    }
