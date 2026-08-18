// buggy function
    public UncheckedIOException(IOException cause) {
        super(cause);
    }

    public CharacterReader(Reader input, int sz) {
        Validate.notNull(input);
        Validate.isTrue(input.markSupported());
        reader = input;
        charBuf = new char[sz > maxBufferLen ? maxBufferLen : sz];
        bufferUp();

    }

    boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent case.
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    }

// trigger testcase
// org/jsoup/integration/ConnectTest.java::testBinaryResultThrows
@Test
    public void testBinaryResultThrows() {
        Connection con = Jsoup.connect(FileServlet.Url);
        con.data(FileServlet.LocationParam, "/htmltests/thumb.jpg");
        con.data(FileServlet.ContentTypeParam, "text/html");

        boolean threw = false;
        try {
            con.execute();
            Document doc = con.response().parse();
        } catch (IOException e) {
            threw = true;
            assertEquals("Input is binary and unsupported", e.getMessage());
        }
        assertTrue(threw);
    }

// org/jsoup/integration/ConnectTest.java::testBinaryThrowsExceptionWhenTypeIgnored
@Test
    public void testBinaryThrowsExceptionWhenTypeIgnored() {
        Connection con = Jsoup.connect(FileServlet.Url);
        con.data(FileServlet.LocationParam, "/htmltests/thumb.jpg");
        con.data(FileServlet.ContentTypeParam, "image/jpeg");
        con.ignoreContentType(true);

        boolean threw = false;
        try {
            con.execute();
            Document doc = con.response().parse();
        } catch (IOException e) {
            threw = true;
            assertEquals("Input is binary and unsupported", e.getMessage());
        }
        assertTrue(threw);
    }

// org/jsoup/integration/ParseTest.java::testBinaryThrowsException
@Test
    public void testBinaryThrowsException() throws IOException {
        File in = getFile("/htmltests/thumb.jpg");

        boolean threw = false;
        try {
            Document doc = Jsoup.parse(in, "UTF-8");
        } catch (IOException e) {
            threw = true;
            assertEquals("Input is binary and unsupported", e.getMessage());
        }
        assertTrue(threw);
    }
