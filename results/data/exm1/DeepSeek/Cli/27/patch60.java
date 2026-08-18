// org.apache.commons.cli.HelpFormatterTest::testAccessors
    public void testAccessors()
    {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setArgName("argname");
        assertEquals("arg name", "argname", formatter.getArgName());

        formatter.setDescPadding(3);
        assertEquals("desc padding", 3, formatter.getDescPadding());

        formatter.setLeftPadding(7);
        assertEquals("left padding", 7, formatter.getLeftPadding());

        formatter.setLongOptPrefix("~~");
        assertEquals("long opt prefix", "~~", formatter.getLongOptPrefix());

        formatter.setNewLine("\n");
        assertEquals("new line", "\n", formatter.getNewLine());

        formatter.setOptPrefix("~");
        assertEquals("opt prefix", "~", formatter.getOptPrefix());

        formatter.setSyntaxPrefix("-> ");
        assertEquals("syntax prefix", "-> ", formatter.getSyntaxPrefix());

        formatter.setWidth(80);
        assertEquals("width", 80, formatter.getWidth());
    }