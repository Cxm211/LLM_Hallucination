// buggy code
        void processResponseHeaders(Map<String, List<String>> resHeaders) {
            for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null)
                    continue; // http/1.1 line

                List<String> values = entry.getValue();
                if (name.equalsIgnoreCase("Set-Cookie")) {
                    for (String value : values) {
                        if (value == null)
                            continue;
                        TokenQueue cd = new TokenQueue(value);
                        String cookieName = cd.chompTo("=").trim();
                        String cookieVal = cd.consumeTo(";").trim();
                        // ignores path, date, domain, validateTLSCertificates et al. req'd?
                        // name not blank, value not null
                        if (cookieName.length() > 0)
                            cookie(cookieName, cookieVal);
                    }
                } else { // combine same header names with comma: http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
                    if (!values.isEmpty())
                        header(name, values.get(0));
                }
            }
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

// org.jsoup.helper.HttpConnectionTest::caseInsensitiveHeaders
    @Test public void caseInsensitiveHeaders() {
        Connection.Response res = new HttpConnection.Response();
        Map<String, String> headers = res.headers();
        headers.put("Accept-Encoding", "gzip");
        headers.put("content-type", "text/html");
        headers.put("refErrer", "http://example.com");

        assertTrue(res.hasHeader("Accept-Encoding"));
        assertTrue(res.hasHeader("accept-encoding"));
        assertTrue(res.hasHeader("accept-Encoding"));

        assertEquals("gzip", res.header("accept-Encoding"));
        assertEquals("text/html", res.header("Content-Type"));
        assertEquals("http://example.com", res.header("Referrer"));

        res.removeHeader("Content-Type");
        assertFalse(res.hasHeader("content-type"));

        res.header("accept-encoding", "deflate");
        assertEquals("deflate", res.header("Accept-Encoding"));
        assertEquals("deflate", res.header("accept-Encoding"));
    }

// org.jsoup.helper.HttpConnectionTest::sameHeadersCombineWithComma
    @Test public void sameHeadersCombineWithComma() {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("no-cache");
        values.add("no-store");
        headers.put("Cache-Control", values);
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals("no-cache, no-store", res.header("Cache-Control"));
    }

// org.jsoup.helper.HttpConnectionTest::ignoresEmptySetCookies
    @Test public void ignoresEmptySetCookies() {
        
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Set-Cookie", Collections.<String>emptyList());
        HttpConnection.Response res = new HttpConnection.Response();
        res.processResponseHeaders(headers);
        assertEquals(0, res.cookies().size());
    }

// org.jsoup.helper.HttpConnectionTest::ignoresEmptyCookieNameAndVals
    @Test public void ignoresEmptyCookieNameAndVals() {
        
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> cookieStrings = new ArrayList<String>();
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
        con.userAgent("Mozilla");
        assertEquals("Mozilla", con.request().header("User-Agent"));
    }

// org.jsoup.helper.HttpConnectionTest::timeout
    @Test public void timeout() {
        Connection con = HttpConnection.connect("http://example.com/");
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

// org.jsoup.helper.HttpConnectionTest::throwsOnOdddData
    @Test(expected=IllegalArgumentException.class) public void throwsOnOdddData() {
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
