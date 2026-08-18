    public JsonGenerator enable(Feature f) {
        super.enable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        }
        return this;
    }

// trigger testcase
public void testFieldNameQuotingEnabled() throws IOException
    {
        // // First, test with default factory, with quoting enabled by default
        
        // First, default, with quotes
        _testFieldNameQuotingEnabled(JSON_F, true, true, "{\"foo\":1}");
        _testFieldNameQuotingEnabled(JSON_F, false, true, "{\"foo\":1}");

        // then without quotes
        _testFieldNameQuotingEnabled(JSON_F, true, false, "{foo:1}");
        _testFieldNameQuotingEnabled(JSON_F, false, false, "{foo:1}");

        // // Then with alternatively configured factory

        JsonFactory JF2 = new JsonFactory();
        JF2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        _testFieldNameQuotingEnabled(JF2, true, true, "{\"foo\":1}");
        _testFieldNameQuotingEnabled(JF2, false, true, "{\"foo\":1}");

        // then without quotes
        _testFieldNameQuotingEnabled(JF2, true, false, "{foo:1}");
        _testFieldNameQuotingEnabled(JF2, false, false, "{foo:1}");
    }
