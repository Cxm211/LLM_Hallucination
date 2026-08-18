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
@Test
    public void testLANG_831() throws Exception {
        testSdfAndFdp("M E","3  Tue", true);
    }

@Test
    public void testLANG_831() throws Exception {
        testSdfAndFdp("M E","3  Tue", true);
    }
