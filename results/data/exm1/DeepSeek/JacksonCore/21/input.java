// buggy code
    public JsonToken nextToken() throws IOException
    {
        // 23-May-2017, tatu: To be honest, code here is rather hairy and I don't like all
        //    conditionals; and it seems odd to return `null` but NOT considering input
        //    as closed... would love a rewrite to simplify/clear up logic here.
        
        // Check for _allowMultipleMatches - false and at least there is one token - which is _currToken
        // check for no buffered context _exposedContext - null
        // If all the conditions matches then check for scalar / non-scalar property
        if (!_allowMultipleMatches && (_currToken != null) && (_exposedContext == null)) {
            //if not scalar and ended successfully, and !includePath, then return null
                if (_currToken.isStructEnd()) {
                    if (_headContext.isStartHandled()) {
                        return (_currToken = null);
                    }
                } else if (_currToken.isScalarValue()) {
                    //else if scalar, and scalar not present in obj/array and !includePath and INCLUDE_ALL matched once
                    // then return null 
                    if (!_headContext.isStartHandled() && (_itemFilter == TokenFilter.INCLUDE_ALL)) {
                        return (_currToken = null);
                    }
            }
        }
        // Anything buffered?
        TokenFilterContext ctxt = _exposedContext;

        if (ctxt != null) {
            while (true) {
                JsonToken t = ctxt.nextTokenToRead();
                if (t != null) {
                    _currToken = t;
                    return t;
                }
                // all done with buffered stuff?
                if (ctxt == _headContext) {
                    _exposedContext = null;
                    if (ctxt.inArray()) {
                        t = delegate.getCurrentToken();
// Is this guaranteed to work without further checks?
//                        if (t != JsonToken.START_ARRAY) {
                        _currToken = t;
                        return t;
                    }

                    // Almost! Most likely still have the current token;
                    // with the sole exception of 
                    /*
                    t = delegate.getCurrentToken();
                    if (t != JsonToken.FIELD_NAME) {
                        _currToken = t;
                        return t;
                    }
                    */
                    break;
                }
                // If not, traverse down the context chain
                ctxt = _headContext.findChildOf(ctxt);
                _exposedContext = ctxt;
                if (ctxt == null) { // should never occur
                    throw _constructError("Unexpected problem: chain of filtered context broken");
                }
            }
        }

        // If not, need to read more. If we got any:
        JsonToken t = delegate.nextToken();
        if (t == null) {
            // no strict need to close, since we have no state here
            _currToken = t;
            return t;
        }

        // otherwise... to include or not?
        TokenFilter f;
        
        switch (t.id()) {
        case ID_START_ARRAY:
            f = _itemFilter;
            if (f == TokenFilter.INCLUDE_ALL) {
                _headContext = _headContext.createChildArrayContext(f, true);
                return (_currToken = t);
            }
            if (f == null) { // does this occur?
                delegate.skipChildren();
                break;
            }
            // Otherwise still iffy, need to check
            f = _headContext.checkValue(f);
            if (f == null) {
                delegate.skipChildren();
                break;
            }
            if (f != TokenFilter.INCLUDE_ALL) {
                f = f.filterStartArray();
            }
            _itemFilter = f;
            if (f == TokenFilter.INCLUDE_ALL) {
                _headContext = _headContext.createChildArrayContext(f, true);
                return (_currToken = t);
            }
            _headContext = _headContext.createChildArrayContext(f, false);
            
            // Also: only need buffering if parent path to be included
            if (_includePath) {
                t = _nextTokenWithBuffering(_headContext);
                if (t != null) {
                    _currToken = t;
                    return t;
                }
            }
            break;

        case ID_START_OBJECT:
            f = _itemFilter;
            if (f == TokenFilter.INCLUDE_ALL) {
                _headContext = _headContext.createChildObjectContext(f, true);
                return (_currToken = t);
            }
            if (f == null) { // does this occur?
                delegate.skipChildren();
                break;
            }
            // Otherwise still iffy, need to check
            f = _headContext.checkValue(f);
            if (f == null) {
                delegate.skipChildren();
                break;
            }
            if (f != TokenFilter.INCLUDE_ALL) {
                f = f.filterStartObject();
            }
            _itemFilter = f;
            if (f == TokenFilter.INCLUDE_ALL) {
                _headContext = _headContext.createChildObjectContext(f, true);
                return (_currToken = t);
            }
            _headContext = _headContext.createChildObjectContext(f, false);
            // Also: only need buffering if parent path to be included
            if (_includePath) {
                t = _nextTokenWithBuffering(_headContext);
                if (t != null) {
                    _currToken = t;
                    return t;
                }
            }
            // note: inclusion of surrounding Object handled separately via
            // FIELD_NAME
            break;

        case ID_END_ARRAY:
        case ID_END_OBJECT:
            {
                boolean returnEnd = _headContext.isStartHandled();
                f = _headContext.getFilter();
                if ((f != null) && (f != TokenFilter.INCLUDE_ALL)) {
                    f.filterFinishArray();
                }
                _headContext = _headContext.getParent();
                _itemFilter = _headContext.getFilter();
                if (returnEnd) {
                    return (_currToken = t);
                }
            }
            break;

        case ID_FIELD_NAME:
            {
                final String name = delegate.getCurrentName();
                // note: this will also set 'needToHandleName'
                f = _headContext.setFieldName(name);
                if (f == TokenFilter.INCLUDE_ALL) {
                    _itemFilter = f;
                    if (!_includePath) {
                        // Minor twist here: if parent NOT included, may need to induce output of
                        // surrounding START_OBJECT/END_OBJECT
                        if (_includeImmediateParent && !_headContext.isStartHandled()) {
                            t = _headContext.nextTokenToRead(); // returns START_OBJECT but also marks it handled
                            _exposedContext = _headContext;
                        }
                    }
                    return (_currToken = t);
                }
                if (f == null) {
                    delegate.nextToken();
                    delegate.skipChildren();
                    break;
                }
                f = f.includeProperty(name);
                if (f == null) {
                    delegate.nextToken();
                    delegate.skipChildren();
                    break;
                }
                _itemFilter = f;
                if (f == TokenFilter.INCLUDE_ALL) {
                    if (_includePath) {
                        return (_currToken = t);
                    }
                }
                if (_includePath) {
                    t = _nextTokenWithBuffering(_headContext);
                    if (t != null) {
                        _currToken = t;
                        return t;
                    }
                }
                break;
            }

        default: // scalar value
            f = _itemFilter;
            if (f == TokenFilter.INCLUDE_ALL) {
                return (_currToken = t);
            }
            if (f != null) {
                f = _headContext.checkValue(f);
                if ((f == TokenFilter.INCLUDE_ALL)
                        || ((f != null) && f.includeValue(delegate))) {
                    return (_currToken = t);
                }
            }
            // Otherwise not included (leaves must be explicitly included)
            break;
        }

        // We get here if token was not yet found; offlined handling
        return _nextToken2();
    }

// relevant test
// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNonFiltering
    public void testNonFiltering() throws Exception
    {
        JsonParser p = JSON_F.createParser(SIMPLE);
        String result = readAndWrite(JSON_F, p);
        assertEquals(SIMPLE, result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testSingleMatchFilteringWithoutPath
    public void testSingleMatchFilteringWithoutPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   false 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testNotAllowMultipleMatches
    public void testNotAllowMultipleMatches() throws Exception
    {
    	String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'value':4,'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   false 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testAllowMultipleMatches
    public void testAllowMultipleMatches() throws Exception
    {
    	String jsonString = aposToQuotes("{'a':123,'array':[1,2],'ob':{'value0':2,'value':3,'value2':4},'value':4,'b':true}");
        JsonParser p0 = JSON_F.createParser(jsonString);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   false, 
                   true 
                );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("3 4"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath1
    public void testMultipleMatchFilteringWithPath1() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value0", "value2"),
                true,  true  );
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value0':2,'value2':4}}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath2
    public void testMultipleMatchFilteringWithPath2() throws Exception
    {
        String INPUT = aposToQuotes("{'a':123,'ob':{'value0':2,'value':3,'value2':4},'b':true}");
        JsonParser p0 = JSON_F.createParser(INPUT);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("b", "value"),
                true, true);

        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3},'b':true}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testMultipleMatchFilteringWithPath3
    public void testMultipleMatchFilteringWithPath3() throws Exception
    {
        final String JSON = aposToQuotes("{'root':{'a0':true,'a':{'value':3},'b':{'value':4}},'b0':false}");
        JsonParser p0 = JSON_F.createParser(JSON);
        JsonParser p = new FilteringParserDelegate(p0,
                new NameMatchFilter("value"),
                true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'root':{'a':{'value':3},'b':{'value':4}}}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath1
    public void testIndexMatchWithPath1() throws Exception
    {
        JsonParser p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(1), true, true);
        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[2]}"), result);

        p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0), true, true);
        result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'array':[1]}"), result);
    }

// com.fasterxml.jackson.core.filter.BasicParserFilteringTest::testIndexMatchWithPath2
    public void testIndexMatchWithPath2() throws Exception
    {
        JsonParser p = new FilteringParserDelegate(JSON_F.createParser(SIMPLE),
                new IndexMatchFilter(0, 1), true, true);
        assertEquals(aposToQuotes("{'array':[1,2]}"), readAndWrite(JSON_F, p));
    
        String JSON = aposToQuotes("{'a':123,'array':[1,2,3,4,5],'b':[1,2,3]}");
        p = new FilteringParserDelegate(JSON_F.createParser(JSON),
                new IndexMatchFilter(1, 3), true, true);
        assertEquals(aposToQuotes("{'array':[2,4],'b':[2]}"), readAndWrite(JSON_F, p));
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimplestWithPath
    public void testSimplestWithPath() throws Exception
    {
        _assert(SIMPLEST_INPUT, "/a", true, "{'a':1}");
        _assert(SIMPLEST_INPUT, "/b", true, "{'b':2}");
        _assert(SIMPLEST_INPUT, "/c", true, "{'c':3}");
        _assert(SIMPLEST_INPUT, "/c/0", true, "");
        _assert(SIMPLEST_INPUT, "/d", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimplestNoPath
    public void testSimplestNoPath() throws Exception
    {
        _assert(SIMPLEST_INPUT, "/a", false, "1");
        _assert(SIMPLEST_INPUT, "/b", false, "2");
        _assert(SIMPLEST_INPUT, "/b/2", false, "");
        _assert(SIMPLEST_INPUT, "/c", false, "3");
        _assert(SIMPLEST_INPUT, "/d", false, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimpleWithPath
    public void testSimpleWithPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/c/d", true, "{'c':{'d':{'a':true}}}");
        _assert(SIMPLE_INPUT, "/a", true, "{'a':1}");
        _assert(SIMPLE_INPUT, "/b", true, "{'b':[1,2,3]}");
        _assert(SIMPLE_INPUT, "/b/0", true, "{'b':[1]}");
        _assert(SIMPLE_INPUT, "/b/1", true, "{'b':[2]}");
        _assert(SIMPLE_INPUT, "/b/2", true, "{'b':[3]}");
        _assert(SIMPLE_INPUT, "/b/3", true, "");
    }

// com.fasterxml.jackson.core.filter.JsonPointerParserFilteringTest::testSimpleNoPath
    public void testSimpleNoPath() throws Exception
    {
        _assert(SIMPLE_INPUT, "/c", false, "{'d':{'a':true}}");

        _assert(SIMPLE_INPUT, "/c/d", false, "{'a':true}");
        _assert(SIMPLE_INPUT, "/a", false, "1");
        _assert(SIMPLE_INPUT, "/b", false, "[1,2,3]");
        _assert(SIMPLE_INPUT, "/b/0", false, "1");
        _assert(SIMPLE_INPUT, "/b/1", false, "2");
        _assert(SIMPLE_INPUT, "/b/2", false, "3");
        _assert(SIMPLE_INPUT, "/b/3", false, "");
    }

// com.fasterxml.jackson.core.filter.TokenVerifyingParserFiltering330Test::testBasicSingleMatchFilteringWithPath
    public void testBasicSingleMatchFilteringWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   true, 
                   false 
                );

        String result = readAndWrite(JSON_F, p);
        assertEquals(aposToQuotes("{'ob':{'value':3}}"), result);
    }

// com.fasterxml.jackson.core.filter.TokenVerifyingParserFiltering330Test::testTokensSingleMatchWithPath
    public void testTokensSingleMatchWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   true, 
                   false 
                );

        assertFalse(p.hasCurrentToken());
        assertNull(p.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, p.getCurrentTokenId());
        assertFalse(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());
        

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.getCurrentToken());
        assertTrue(p.isExpectedStartObjectToken());
        assertFalse(p.isExpectedStartArrayToken());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        assertEquals("ob", p.getCurrentName());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("ob", p.getCurrentName());

        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("value", p.getCurrentName());
        assertEquals("value", p.getText());

        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
        assertEquals(NumberType.INT, p.getNumberType());
        assertEquals(3, p.getIntValue());
        assertEquals("value", p.getCurrentName());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());

        p.clearCurrentToken();
        assertNull(p.getCurrentToken());
        
        p.close();
    }

// com.fasterxml.jackson.core.filter.TokenVerifyingParserFiltering330Test::testSkippingForSingleWithPath
    public void testSkippingForSingleWithPath() throws Exception
    {
        JsonParser p0 = JSON_F.createParser(SIMPLE);
        JsonParser p = new FilteringParserDelegate(p0,
               new NameMatchFilter("value"),
                   true, 
                   false 
                );

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        p.skipChildren();
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        assertNull(p.nextToken());
    }
