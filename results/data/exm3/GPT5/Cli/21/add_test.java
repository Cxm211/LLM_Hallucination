// org/apache/commons/cli2/bug/BugCLI150Test.java::testNegativeNumberWithShortOption
public void testNegativeNumberWithShortOption() throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Argument numArg = aBuilder.withValidator(
                NumberValidator.getNumberInstance()).withMinimum(1)
                .withMaximum(1).create();
        final Option numOpt = oBuilder.withShortName("n").withLongName("num").withArgument(numArg)
                .create();
        final Group options = gBuilder.withOption(numOpt).create();

        final Parser parser = new Parser();
        parser.setGroup(options);

        CommandLine cl = parser.parse(new String[] {
                "-n", "-42"
        });
        Number num = (Number) cl.getValue(numOpt);
        assertEquals("Wrong option value", -42, num.intValue());
    }