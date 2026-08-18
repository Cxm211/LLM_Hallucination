// org.apache.commons.cli.HelpFormatterTest::testPrintOptions
    public void testPrintOptions() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        HelpFormatter hf = new HelpFormatter();
        final int leftPad = 1;
        final int descPad = 3;
        final String lpad = hf.createPadding(leftPad);
        final String dpad = hf.createPadding(descPad);
        Options options = null;
        String expected = null;

        options = new Options().addOption("a", false, "aaaa aaaa aaaa aaaa aaaa");
        expected = lpad + "-a" + dpad + "aaaa aaaa aaaa aaaa aaaa";
        hf.renderOptions(sb, 60, options, leftPad, descPad);
        assertEquals("simple non-wrapped option", expected, sb.toString());

        int nextLineTabStop = leftPad + descPad + "-a".length();
        expected = lpad + "-a" + dpad + "aaaa aaaa aaaa" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "aaaa aaaa";
        sb.setLength(0);
        hf.renderOptions(sb, nextLineTabStop + 17, options, leftPad, descPad);
        assertEquals("simple wrapped option", expected, sb.toString());

        options = new Options().addOption("a", "aaa", false, "dddd dddd dddd dddd");
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd dddd dddd";
        sb.setLength(0);
        hf.renderOptions(sb, 60, options, leftPad, descPad);
        assertEquals("long non-wrapped option", expected, sb.toString());

        nextLineTabStop = leftPad + descPad + "-a,--aaa".length();
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "dddd dddd";
        sb.setLength(0);
        hf.renderOptions(sb, 25, options, leftPad, descPad);
        assertEquals("long wrapped option", expected, sb.toString());

        options = new Options().
                addOption("a", "aaa", false, "dddd dddd dddd dddd").
                addOption("b", false, "feeee eeee eeee eeee");
        expected = lpad + "-a,--aaa" + dpad + "dddd dddd" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "dddd dddd" + hf.getNewLine() +
                   lpad + "-b      " + dpad + "feeee eeee" + hf.getNewLine() +
                   hf.createPadding(nextLineTabStop) + "eeee eeee";
        sb.setLength(0);
        hf.renderOptions(sb, 25, options, leftPad, descPad);
        assertEquals("multiple wrapped options", expected, sb.toString());
    }