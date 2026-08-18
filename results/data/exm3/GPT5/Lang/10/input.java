// buggy function
    private static StringBuilder escapeRegex(StringBuilder regex, String value, boolean unquote) {
        boolean wasWhite= false;
        for(int i= 0; i<value.length(); ++i) {
            char c= value.charAt(i);
            if(Character.isWhitespace(c)) {
                if(!wasWhite) {
                    wasWhite= true;
                    regex.append("\\s*+");
                }
                continue;
            }
            wasWhite= false;
            switch(c) {
            case '\'':
                if(unquote) {
                    if(++i==value.length()) {
                        return regex;
                    }
                    c= value.charAt(i);
                }
                break;
            case '?':
            case '[':
            case ']':
            case '(':
            case ')':
            case '{':
            case '}':
            case '\\':
            case '|':
            case '*':
            case '+':
            case '^':
            case '$':
            case '.':
                regex.append('\\');
            }
            regex.append(c);
        }
        return regex;
    }

// trigger testcase
// org/apache/commons/lang3/time/FastDateParserTest.java::testLANG_831
@Test
    public void testLANG_831() throws Exception {
        testSdfAndFdp("M E","3  Tue", true);
    }

// org/apache/commons/lang3/time/FastDateParserTest.java::testLANG_831
@Test
    public void testLANG_831() throws Exception {
        testSdfAndFdp("M E","3  Tue", true);
    }
