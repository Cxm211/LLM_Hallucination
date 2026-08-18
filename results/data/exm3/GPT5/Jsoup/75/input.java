// buggy function
    final void html(final Appendable accum, final Document.OutputSettings out) throws IOException {
        final int sz = size;
        for (int i = 0; i < sz; i++) {
            // inlined from Attribute.html()
            final String key = keys[i];
            final String val = vals[i];
            accum.append(' ').append(key);

            // collapse checked=null, checked="", checked=checked; write out others
            if (!(out.syntax() == Document.OutputSettings.Syntax.html
                && (val == null || val.equals(key) && Attribute.isBooleanAttribute(key)))) {
                accum.append("=\"");
                Entities.escape(accum, val == null ? EmptyString : val, out, true, false, false);
                accum.append('"');
            }
        }
    }

// trigger testcase
// org/jsoup/nodes/ElementTest.java::booleanAttributeOutput
@Test
    public void booleanAttributeOutput() {
        Document doc = Jsoup.parse("<img src=foo noshade='' nohref async=async autofocus=false>");
        Element img = doc.selectFirst("img");

        assertEquals("<img src=\"foo\" noshade nohref async autofocus=\"false\">", img.outerHtml());
    }
