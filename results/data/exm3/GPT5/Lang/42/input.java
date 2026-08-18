// buggy function
    public void escape(Writer writer, String str) throws IOException {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = this.entityName(c);
            if (entityName == null) {
                if (c > 0x7F) {
                    writer.write("&#");
                    writer.write(Integer.toString(c, 10));
                    writer.write(';');
                } else {
                    writer.write(c);
                }
            } else {
                writer.write('&');
                writer.write(entityName);
                writer.write(';');
            }
        }
    }

// trigger testcase
// org/apache/commons/lang/StringEscapeUtilsTest.java::testEscapeHtmlHighUnicode
public void testEscapeHtmlHighUnicode() throws java.io.UnsupportedEncodingException {
        // this is the utf8 representation of the character:
        // COUNTING ROD UNIT DIGIT THREE
        // in unicode
        // codepoint: U+1D362
        byte[] data = new byte[] { (byte)0xF0, (byte)0x9D, (byte)0x8D, (byte)0xA2 };

        String escaped = StringEscapeUtils.escapeHtml( new String(data, "UTF8") );
        String unescaped = StringEscapeUtils.unescapeHtml( escaped );

        assertEquals( "High unicode was not escaped correctly", "&#119650;", escaped);
    }
