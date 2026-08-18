// buggy code
        private static boolean looksLikeUtf8(byte[] input) {
            int i = 0;
            // BOM:
            if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
                && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
                i = 3;
            }

            int end;
            for (int j = input.length; i < j; ++i) {
                int o = input[i];
                if ((o & 0x80) == 0) {
                    continue; // ASCII
                }

                // UTF-8 leading:
                if ((o & 0xE0) == 0xC0) {
                    end = i + 1;
                } else if ((o & 0xF0) == 0xE0) {
                    end = i + 2;
                } else if ((o & 0xF8) == 0xF0) {
                    end = i + 3;
                } else {
                    return false;
                }


                while (i < end) {
                    i++;
                    o = input[i];
                    if ((o & 0xC0) != 0x80) {
                        return false;
                    }
                }
            }
            return true;
        }

// relevant test
// org.jsoup.helper.HttpConnectionTest::throwsExceptionOnParseWithoutExecute
    @Test(expected=IllegalArgumentException.class) public void throwsExceptionOnParseWithoutExecute() throws IOException {
        Connection con = HttpConnection.connect("http://example.com");
        con.response().parse();
    }

// org.jsoup.helper.HttpConnectionTest::throwsExceptionOnBodyWithoutExecute
    @Test(expected=IllegalArgumentException.class) public void throwsExceptionOnBodyWithoutExecute() throws IOException {
        Connection con = HttpConnection.connect("http://example.com");
        con.response().body();
    }

// org.jsoup.helper.HttpConnectionTest::throwsExceptionOnBodyAsBytesWithoutExecute
    @Test(expected=IllegalArgumentException.class) public void throwsExceptionOnBodyAsBytesWithoutExecute() throws IOException {
        Connection con = HttpConnection.connect("http://example.com");
        con.response().bodyAsBytes();
    }

// org.jsoup.helper.HttpConnectionTest::headers
    @Test public void headers() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/html");
        headers.put("Connection", "keep-alive");
        headers.put("Host", "http://example.com");
        con.headers(headers);
        assertEquals("text/html", con.request().header("content-type"));
        assertEquals("keep-alive", con.request().header("Connection"));
        assertEquals("http://example.com", con.request().header("Host"));
    }

// org.jsoup.helper.HttpConnectionTest::sameHeadersCombineWithComma
    @Test public void sameHeadersCombineWithComma() {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("no-cache");
        values.add("no-store");
        headers.put("Cache-Control", values);
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals("no-cache, no-store", res.header("Cache-Control"));
    }

// org.jsoup.helper.HttpConnectionTest::multipleHeaders
    @Test public void multipleHeaders() {
        Connection.Request req = new HttpConnection.Request();
        req.addHeader("Accept", "Something");
        req.addHeader("Accept", "Everything");
        req.addHeader("Foo", "Bar");

        assertTrue(req.hasHeader("Accept"));
        assertTrue(req.hasHeader("ACCEpt"));
        assertEquals("Something, Everything", req.header("accept"));
        assertTrue(req.hasHeader("fOO"));
        assertEquals("Bar", req.header("foo"));

        List<String> accept = req.headers("accept");
        assertEquals(2, accept.size());
        assertEquals("Something", accept.get(0));
        assertEquals("Everything", accept.get(1));

        Map<String, List<String>> headers = req.multiHeaders();
        assertEquals(accept, headers.get("Accept"));
        assertEquals("Bar", headers.get("Foo").get(0));

        assertTrue(req.hasHeader("Accept"));
        assertTrue(req.hasHeaderWithValue("accept", "Something"));
        assertTrue(req.hasHeaderWithValue("accept", "Everything"));
        assertFalse(req.hasHeaderWithValue("accept", "Something for nothing"));

        req.removeHeader("accept");
        headers = req.multiHeaders();
        assertEquals("Bar", headers.get("Foo").get(0));
        assertFalse(req.hasHeader("Accept"));
        assertTrue(headers.get("Accept") == null);
    }

// org.jsoup.helper.HttpConnectionTest::ignoresEmptySetCookies
    @Test public void ignoresEmptySetCookies() {
        
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Set-Cookie", Collections.<String>emptyList());
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals(0, res.cookies().size());
    }

// org.jsoup.helper.HttpConnectionTest::ignoresEmptyCookieNameAndVals
    @Test public void ignoresEmptyCookieNameAndVals() {
        
        Map<String, List<String>> headers = new HashMap<>();
        List<String> cookieStrings = new ArrayList<>();
        cookieStrings.add(null);
        cookieStrings.add("");
        cookieStrings.add("one");
        cookieStrings.add("two=");
        cookieStrings.add("three=;");
        cookieStrings.add("four=data; Domain=.example.com; Path=/");

        headers.put("Set-Cookie", cookieStrings);
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals(4, res.cookies().size());
        assertEquals("", res.cookie("one"));
        assertEquals("", res.cookie("two"));
        assertEquals("", res.cookie("three"));
        assertEquals("data", res.cookie("four"));
    }

// org.jsoup.helper.HttpConnectionTest::connectWithUrl
    @Test public void connectWithUrl() throws MalformedURLException {
        Connection con = HttpConnection.connect(new URL("http://example.com"));
        assertEquals("http://example.com", con.request().url().toExternalForm());
    }

// org.jsoup.helper.HttpConnectionTest::throwsOnMalformedUrl
    @Test(expected=IllegalArgumentException.class) public void throwsOnMalformedUrl() {
        Connection con = HttpConnection.connect("bzzt");
    }

// org.jsoup.helper.HttpConnectionTest::userAgent
    @Test public void userAgent() {
        Connection con = HttpConnection.connect("http://example.com/");
        assertEquals(HttpConnection.DEFAULT_UA, con.request().header("User-Agent"));
        con.userAgent("Mozilla");
        assertEquals("Mozilla", con.request().header("User-Agent"));
    }

// org.jsoup.helper.HttpConnectionTest::timeout
    @Test public void timeout() {
        Connection con = HttpConnection.connect("http://example.com/");
        assertEquals(30 * 1000, con.request().timeout());
        con.timeout(1000);
        assertEquals(1000, con.request().timeout());
    }

// org.jsoup.helper.HttpConnectionTest::referrer
    @Test public void referrer() {
        Connection con = HttpConnection.connect("http://example.com/");
        con.referrer("http://foo.com");
        assertEquals("http://foo.com", con.request().header("Referer"));
    }

// org.jsoup.helper.HttpConnectionTest::method
    @Test public void method() {
        Connection con = HttpConnection.connect("http://example.com/");
        assertEquals(Connection.Method.GET, con.request().method());
        con.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, con.request().method());
    }

// org.jsoup.helper.HttpConnectionTest::throwsOnOddData
    @Test(expected=IllegalArgumentException.class) public void throwsOnOddData() {
        Connection con = HttpConnection.connect("http://example.com/");
        con.data("Name", "val", "what");
    }

// org.jsoup.helper.HttpConnectionTest::data
    @Test public void data() {
        Connection con = HttpConnection.connect("http://example.com/");
        con.data("Name", "Val", "Foo", "bar");
        Collection<Connection.KeyVal> values = con.request().data();
        Object[] data =  values.toArray();
        Connection.KeyVal one = (Connection.KeyVal) data[0];
        Connection.KeyVal two = (Connection.KeyVal) data[1];
        assertEquals("Name", one.key());
        assertEquals("Val", one.value());
        assertEquals("Foo", two.key());
        assertEquals("bar", two.value());
    }

// org.jsoup.helper.HttpConnectionTest::cookie
    @Test public void cookie() {
        Connection con = HttpConnection.connect("http://example.com/");
        con.cookie("Name", "Val");
        assertEquals("Val", con.request().cookie("Name"));
    }

// org.jsoup.helper.HttpConnectionTest::inputStream
    @Test public void inputStream() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "thumb.jpg", ParseTest.inputStreamFrom("Check"));
        assertEquals("file", kv.key());
        assertEquals("thumb.jpg", kv.value());
        assertTrue(kv.hasInputStream());

        kv = HttpConnection.KeyVal.create("one", "two");
        assertEquals("one", kv.key());
        assertEquals("two", kv.value());
        assertFalse(kv.hasInputStream());
    }

// org.jsoup.helper.HttpConnectionTest::requestBody
    @Test public void requestBody() {
        Connection con = HttpConnection.connect("http://example.com/");
        con.requestBody("foo");
        assertEquals("foo", con.request().requestBody());
    }

// org.jsoup.helper.HttpConnectionTest::encodeUrl
    @Test public void encodeUrl() throws MalformedURLException {
        URL url1 = new URL("http://test.com/?q=white space");
        URL url2 = HttpConnection.encodeUrl(url1);
        assertEquals("http://test.com/?q=white%20space", url2.toExternalForm());
    }

// org.jsoup.helper.HttpConnectionTest::noUrlThrowsValidationError
    @Test public void noUrlThrowsValidationError() throws IOException {
        HttpConnection con = new HttpConnection();
        boolean threw = false;
        try {
            con.execute();
        } catch (IllegalArgumentException e) {
            threw = true;
            assertEquals("URL must be specified to connect", e.getMessage());
        }
        assertTrue(threw);
    }

// org.jsoup.helper.HttpConnectionTest::handlesHeaderEncodingOnRequest
    @Test public void handlesHeaderEncodingOnRequest() {
        Connection.Request req = new HttpConnection.Request();
        req.addHeader("xxx", "é");
    }

// org.jsoup.integration.ConnectTest::canConnectToLocalServer
    public void canConnectToLocalServer() throws IOException {
        String url = HelloServlet.Url;
        Document doc = Jsoup.connect(url).get();
        Element p = doc.selectFirst("p");
        assertEquals("Hello, World!", p.text());
    }

// org.jsoup.integration.ConnectTest::fetchURl
    public void fetchURl() throws IOException {
        Document doc = Jsoup.parse(new URL(echoUrl), 10 * 1000);
        assertTrue(doc.title().contains("Environment Variables"));
    }

// org.jsoup.integration.ConnectTest::fetchURIWithWihtespace
    public void fetchURIWithWihtespace() throws IOException {
        Connection con = Jsoup.connect(echoUrl + "#with whitespaces");
        Document doc = con.get();
        assertTrue(doc.title().contains("Environment Variables"));
    }

// org.jsoup.integration.ConnectTest::exceptOnUnsupportedProtocol
    public void exceptOnUnsupportedProtocol() {
        String url = "file://etc/passwd";
        boolean threw = false;
        try {
            Document doc = Jsoup.connect(url).get();
        } catch (MalformedURLException e) {
            threw = true;
            assertEquals("java.net.MalformedURLException: Only http & https protocols supported", e.toString());
        } catch (IOException e) {
        }
        assertTrue(threw);
    }

// org.jsoup.integration.ConnectTest::doesPost
    public void doesPost() throws IOException {
        Document doc = Jsoup.connect(echoUrl)
            .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
            .cookie("auth", "token")
            .post();

        assertEquals("POST", ihVal("Method", doc));
        assertEquals("gzip", ihVal("Accept-Encoding", doc));
        assertEquals("auth=token", ihVal("Cookie", doc));
        assertEquals("度一下", ihVal("百", doc));
        assertEquals("Jsoup, Jonathan", ihVal("uname", doc));
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", ihVal("Content-Type", doc));
    }

// org.jsoup.integration.ConnectTest::doesPostMultipartWithoutInputstream
    public void doesPostMultipartWithoutInputstream() throws IOException {
        Document doc = Jsoup.connect(echoUrl)
                .header(CONTENT_TYPE, MULTIPART_FORM_DATA)
                .userAgent(browserUa)
                .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
                .post();

        assertTrue(ihVal("Content-Type", doc).contains(MULTIPART_FORM_DATA));

        assertTrue(ihVal("Content-Type", doc).contains("boundary")); 
        assertEquals("Jsoup, Jonathan", ihVal("uname", doc));
        assertEquals("度一下", ihVal("百", doc));
    }

// org.jsoup.integration.ConnectTest::sendsRequestBodyJsonWithData
    public void sendsRequestBodyJsonWithData() throws IOException {
        final String body = "{key:value}";
        Document doc = Jsoup.connect(echoUrl)
            .requestBody(body)
            .header("Content-Type", "application/json")
            .userAgent(browserUa)
            .data("foo", "true")
            .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("application/json", ihVal("Content-Type", doc));
        assertEquals("foo=true", ihVal("Query String", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

// org.jsoup.integration.ConnectTest::sendsRequestBodyJsonWithoutData
    public void sendsRequestBodyJsonWithoutData() throws IOException {
        final String body = "{key:value}";
        Document doc = Jsoup.connect(echoUrl)
            .requestBody(body)
            .header("Content-Type", "application/json")
            .userAgent(browserUa)
            .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("application/json", ihVal("Content-Type", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

// org.jsoup.integration.ConnectTest::sendsRequestBody
    public void sendsRequestBody() throws IOException {
        final String body = "{key:value}";
        Document doc = Jsoup.connect(echoUrl)
            .requestBody(body)
            .header("Content-Type", "text/plain")
            .userAgent(browserUa)
            .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("text/plain", ihVal("Content-Type", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

// org.jsoup.integration.ConnectTest::sendsRequestBodyWithUrlParams
    public void sendsRequestBodyWithUrlParams() throws IOException {
        final String body = "{key:value}";
        Document doc = Jsoup.connect(echoUrl)
            .requestBody(body)
            .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
            .header("Content-Type", "text/plain") 
            .userAgent(browserUa)
            .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("uname=Jsoup&uname=Jonathan&%E7%99%BE=%E5%BA%A6%E4%B8%80%E4%B8%8B", ihVal("Query String", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

// org.jsoup.integration.ConnectTest::doesGet
    public void doesGet() throws IOException {
        Connection con = Jsoup.connect(echoUrl + "?what=the")
            .userAgent("Mozilla")
            .referrer("http://example.com")
            .data("what", "about & me?");

        Document doc = con.get();
        assertEquals("what=the&what=about+%26+me%3F", ihVal("Query String", doc));
        assertEquals("the, about & me?", ihVal("what", doc));
        assertEquals("Mozilla", ihVal("User-Agent", doc));
        assertEquals("http://example.com", ihVal("Referer", doc));
    }

// org.jsoup.integration.ConnectTest::doesPut
    public void doesPut() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl)
            .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
            .cookie("auth", "token")
            .method(Connection.Method.PUT)
            .execute();

        Document doc = res.parse();
        assertEquals("PUT", ihVal("Method", doc));
        assertEquals("gzip", ihVal("Accept-Encoding", doc));
        assertEquals("auth=token", ihVal("Cookie", doc));
    }

// org.jsoup.integration.ConnectTest::canInterruptBodyStringRead
    @Test public void canInterruptBodyStringRead() throws IOException, InterruptedException {
        
        final String[] body = new String[1];
        Thread runner = new Thread(new Runnable() {
            public void run() {
                try {
                    Connection.Response res = Jsoup.connect(SlowRider.Url)
                        .timeout(15 * 1000)
                        .execute();
                    body[0] = res.body();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        runner.start();
        Thread.sleep(1000 * 3);
        runner.interrupt();
        assertTrue(runner.isInterrupted());
        runner.join();

        assertTrue(body[0].length() > 0);
        assertTrue(body[0].contains("<p>Are you still there?"));
    }

// org.jsoup.integration.ConnectTest::canInterruptDocumentRead
    @Test public void canInterruptDocumentRead() throws IOException, InterruptedException {
        
        final String[] body = new String[1];
        Thread runner = new Thread(new Runnable() {
            public void run() {
                try {
                    Connection.Response res = Jsoup.connect(SlowRider.Url)
                        .timeout(15 * 1000)
                        .execute();
                    body[0] = res.parse().text();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        runner.start();
        Thread.sleep(1000 * 3);
        runner.interrupt();
        assertTrue(runner.isInterrupted());
        runner.join();

        assertTrue(body[0].length() == 0); 
    }

// org.jsoup.integration.ConnectTest::totalTimeout
    @Test public void totalTimeout() throws IOException {
        int timeout = 3 * 1000;
        long start = System.currentTimeMillis();
        boolean threw = false;
        try {
            Jsoup.connect(SlowRider.Url).timeout(timeout).get();
        } catch (SocketTimeoutException e) {
            long end = System.currentTimeMillis();
            long took = end - start;
            assertTrue(("Time taken was " + took), took > timeout);
            assertTrue(("Time taken was " + took), took < timeout * 1.2);
            threw = true;
        }

        assertTrue(threw);
    }

// org.jsoup.integration.ConnectTest::slowReadOk
    @Test public void slowReadOk() throws IOException {
        
        Document doc = Jsoup.connect(SlowRider.Url)
            .data(SlowRider.MaxTimeParam, "2000") 
            .get();

        Element h1 = doc.selectFirst("h1");
        assertEquals("outatime", h1.text());
    }

// org.jsoup.integration.ConnectTest::infiniteReadSupported
    @Test public void infiniteReadSupported() throws IOException {
        Document doc = Jsoup.connect(SlowRider.Url)
            .timeout(0)
            .data(SlowRider.MaxTimeParam, "2000")
            .get();

        Element h1 = doc.selectFirst("h1");
        assertEquals("outatime", h1.text());
    }

// org.jsoup.integration.ConnectTest::postFiles
    public void postFiles() throws IOException {
        File thumb = ParseTest.getFile("/htmltests/thumb.jpg");
        File html = ParseTest.getFile("/htmltests/google-ipod.html");

        Document res = Jsoup
            .connect(EchoServlet.Url)
            .data("firstname", "Jay")
            .data("firstPart", thumb.getName(), new FileInputStream(thumb), "image/jpeg")
            .data("secondPart", html.getName(), new FileInputStream(html)) 
            .data("surname", "Soup")
            .post();

        assertEquals("4", ihVal("Parts", res));

        assertEquals("application/octet-stream", ihVal("Part secondPart ContentType", res));
        assertEquals("secondPart", ihVal("Part secondPart Name", res));
        assertEquals("google-ipod.html", ihVal("Part secondPart Filename", res));
        assertEquals("43963", ihVal("Part secondPart Size", res));

        assertEquals("image/jpeg", ihVal("Part firstPart ContentType", res));
        assertEquals("firstPart", ihVal("Part firstPart Name", res));
        assertEquals("thumb.jpg", ihVal("Part firstPart Filename", res));
        assertEquals("1052", ihVal("Part firstPart Size", res));

        assertEquals("Jay", ihVal("firstname", res));
        assertEquals("Soup", ihVal("surname", res));

        
    }

// org.jsoup.integration.ConnectTest::multipleParsesOkAfterBufferUp
    @Test public void multipleParsesOkAfterBufferUp() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute().bufferUp();

        Document doc = res.parse();
        assertTrue(doc.title().contains("Environment"));

        Document doc2 = res.parse();
        assertTrue(doc2.title().contains("Environment"));
    }

// org.jsoup.integration.ConnectTest::bodyAfterParseThrowsValidationError
    @Test(expected=IllegalArgumentException.class) public void bodyAfterParseThrowsValidationError() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        Document doc = res.parse();
        String body = res.body();
    }

// org.jsoup.integration.ConnectTest::bodyAndBytesAvailableBeforeParse
    @Test public void bodyAndBytesAvailableBeforeParse() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        String body = res.body();
        assertTrue(body.contains("Environment"));
        byte[] bytes = res.bodyAsBytes();
        assertTrue(bytes.length > 100);

        Document doc = res.parse();
        assertTrue(doc.title().contains("Environment"));
    }

// org.jsoup.integration.ConnectTest::parseParseThrowsValidates
    @Test(expected=IllegalArgumentException.class) public void parseParseThrowsValidates() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        Document doc = res.parse();
        assertTrue(doc.title().contains("Environment"));
        Document doc2 = res.parse(); 
    }

// org.jsoup.integration.ConnectTest::multiCookieSet
    public void multiCookieSet() {}

// org.jsoup.integration.ConnectTest::supportsDeflate
    public void supportsDeflate() throws IOException {
        Connection.Response res = Jsoup.connect(Deflateservlet.Url).execute();
        assertEquals("deflate", res.header("Content-Encoding"));

        Document doc = res.parse();
        assertEquals("Hello, World!", doc.selectFirst("p").text());
    }

// org.jsoup.integration.ConnectTest::handlesEmptyStreamDuringParseRead
    public void handlesEmptyStreamDuringParseRead() throws IOException {
        

        Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
            .timeout(200)
            .execute();

        boolean threw = false;
        try {
            Document document = res.parse();
            assertEquals("Something", document.title());
        } catch (IOException e) {
            threw = true;
        }
        assertTrue(threw);
    }

// org.jsoup.integration.ConnectTest::handlesEmtpyStreamDuringBufferdRead
    public void handlesEmtpyStreamDuringBufferdRead() throws IOException {
        Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
            .timeout(200)
            .execute();

        boolean threw = false;
        try {
            res.bufferUp();
        } catch (UncheckedIOException e) {
            threw = true;
        }
        assertTrue(threw);
    }

// org.jsoup.integration.ConnectTest::handlesRedirect
    @Test public void handlesRedirect() throws IOException {
        Document doc = Jsoup.connect(RedirectServlet.Url)
            .data(RedirectServlet.LocationParam, HelloServlet.Url)
            .get();

        Element p = doc.selectFirst("p");
        assertEquals("Hello, World!", p.text());

        assertEquals(HelloServlet.Url, doc.location());
    }

// org.jsoup.integration.ConnectTest::handlesEmptyRedirect
    @Test public void handlesEmptyRedirect() throws IOException {
        boolean threw = false;
        try {
            Connection.Response res = Jsoup.connect(RedirectServlet.Url)
                .execute();
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Too many redirects"));
            threw = true;
        }
        assertTrue(threw);
    }

// org.jsoup.integration.ConnectTest::doesNotPostFor302
    @Test public void doesNotPostFor302() throws IOException {
        final Document doc = Jsoup.connect(RedirectServlet.Url)
            .data("Hello", "there")
            .data(RedirectServlet.LocationParam, EchoServlet.Url)
            .post();

        assertEquals(EchoServlet.Url, doc.location());
        assertEquals("GET", ihVal("Method", doc));
        assertNull(ihVal("Hello", doc)); 
    }

// org.jsoup.integration.ConnectTest::doesPostFor307
    @Test public void doesPostFor307() throws IOException {
        final Document doc = Jsoup.connect(RedirectServlet.Url)
            .data("Hello", "there")
            .data(RedirectServlet.LocationParam, EchoServlet.Url)
            .data(RedirectServlet.CodeParam, "307")
            .post();

        assertEquals(EchoServlet.Url, doc.location());
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("there", ihVal("Hello", doc));
    }

// org.jsoup.nodes.FormElementTest::hasAssociatedControls
    @Test public void hasAssociatedControls() {
        
        String html = "<form id=1><button id=1><fieldset id=2 /><input id=3><keygen id=4><object id=5><output id=6>" +
                "<select id=7><option></select><textarea id=8><p id=9>";
        Document doc = Jsoup.parse(html);

        FormElement form = (FormElement) doc.select("form").first();
        assertEquals(8, form.elements().size());
    }

// org.jsoup.nodes.FormElementTest::createsFormData
    @Test public void createsFormData() {
        String html = "<form><input name='one' value='two'><select name='three'><option value='not'>" +
                "<option value='four' selected><option value='five' selected><textarea name=six>seven</textarea>" +
                "<input name='seven' type='radio' value='on' checked><input name='seven' type='radio' value='off'>" +
                "<input name='eight' type='checkbox' checked><input name='nine' type='checkbox' value='unset'>" +
                "<input name='ten' value='text' disabled>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        assertEquals(6, data.size());
        assertEquals("one=two", data.get(0).toString());
        assertEquals("three=four", data.get(1).toString());
        assertEquals("three=five", data.get(2).toString());
        assertEquals("six=seven", data.get(3).toString());
        assertEquals("seven=on", data.get(4).toString()); 
        assertEquals("eight=on", data.get(5).toString()); 
        
        
    }

// org.jsoup.nodes.FormElementTest::createsSubmitableConnection
    @Test public void createsSubmitableConnection() {
        String html = "<form action='/search'><input name='q'></form>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        doc.select("[name=q]").attr("value", "jsoup");

        FormElement form = ((FormElement) doc.select("form").first());
        Connection con = form.submit();

        assertEquals(Connection.Method.GET, con.request().method());
        assertEquals("http://example.com/search", con.request().url().toExternalForm());
        List<Connection.KeyVal> dataList = (List<Connection.KeyVal>) con.request().data();
        assertEquals("q=jsoup", dataList.get(0).toString());

        doc.select("form").attr("method", "post");
        Connection con2 = form.submit();
        assertEquals(Connection.Method.POST, con2.request().method());
    }

// org.jsoup.nodes.FormElementTest::actionWithNoValue
    @Test public void actionWithNoValue() {
        String html = "<form><input name='q'></form>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        FormElement form = ((FormElement) doc.select("form").first());
        Connection con = form.submit();

        assertEquals("http://example.com/", con.request().url().toExternalForm());
    }

// org.jsoup.nodes.FormElementTest::actionWithNoBaseUri
    @Test public void actionWithNoBaseUri() {
        String html = "<form><input name='q'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = ((FormElement) doc.select("form").first());

        boolean threw = false;
        try {
            Connection con = form.submit();
        } catch (IllegalArgumentException e) {
            threw = true;
            assertEquals("Could not determine a form action URL for submit. Ensure you set a base URI when parsing.",
                    e.getMessage());
        }
        assertTrue(threw);
    }

// org.jsoup.nodes.FormElementTest::formsAddedAfterParseAreFormElements
    @Test public void formsAddedAfterParseAreFormElements() {
        Document doc = Jsoup.parse("<body />");
        doc.body().html("<form action='http://example.com/search'><input name='q' value='search'>");
        Element formEl = doc.select("form").first();
        assertTrue(formEl instanceof FormElement);

        FormElement form = (FormElement) formEl;
        assertEquals(1, form.elements().size());
    }

// org.jsoup.nodes.FormElementTest::controlsAddedAfterParseAreLinkedWithForms
    @Test public void controlsAddedAfterParseAreLinkedWithForms() {
        Document doc = Jsoup.parse("<body />");
        doc.body().html("<form />");

        Element formEl = doc.select("form").first();
        formEl.append("<input name=foo value=bar>");

        assertTrue(formEl instanceof FormElement);
        FormElement form = (FormElement) formEl;
        assertEquals(1, form.elements().size());

        List<Connection.KeyVal> data = form.formData();
        assertEquals("foo=bar", data.get(0).toString());
    }

// org.jsoup.nodes.FormElementTest::usesOnForCheckboxValueIfNoValueSet
    @Test public void usesOnForCheckboxValueIfNoValueSet() {
        Document doc = Jsoup.parse("<form><input type=checkbox checked name=foo></form>");
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals("on", data.get(0).value());
        assertEquals("foo", data.get(0).key());
    }

// org.jsoup.nodes.FormElementTest::adoptedFormsRetainInputs
    @Test public void adoptedFormsRetainInputs() {
        
        String html = "<html>\n" +
                "<body>  \n" +
                "  <table>\n" +
                "      <form action=\"/hello.php\" method=\"post\">\n" +
                "      <tr><td>User:</td><td> <input type=\"text\" name=\"user\" /></td></tr>\n" +
                "      <tr><td>Password:</td><td> <input type=\"password\" name=\"pass\" /></td></tr>\n" +
                "      <tr><td><input type=\"submit\" name=\"login\" value=\"login\" /></td></tr>\n" +
                "   </form>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(3, data.size());
        assertEquals("user", data.get(0).key());
        assertEquals("pass", data.get(1).key());
        assertEquals("login", data.get(2).key());
    }

// org.jsoup.nodes.FormElementTest::removeFormElement
    @Test public void removeFormElement() {
        String html = "<html>\n" +
                "  <body> \n" +
                "      <form action=\"/hello.php\" method=\"post\">\n" +
                "      User:<input type=\"text\" name=\"user\" />\n" +
                "      Password:<input type=\"password\" name=\"pass\" />\n" +
                "      <input type=\"submit\" name=\"login\" value=\"login\" />\n" +
                "   </form>\n" +
                "  </body>\n" +
                "</html>  ";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.selectFirst("form");
        Element pass = form.selectFirst("input[name=pass]");
        pass.remove();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("user", data.get(0).key());
        assertEquals("login", data.get(1).key());
        assertEquals(null, doc.selectFirst("input[name=pass]"));
    }
