    public boolean contains(char ch) {
        char[] thisBuf = buffer;
        for (int i = 0; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(char ch, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (startIndex >= size) {
            return -1;
        }
        char[] thisBuf = buffer;
        for (int i = startIndex; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }

// trigger testcase
public void testLang295() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertFalse( "The contains(char) method is looking beyond the end of the string", sb.contains('h'));
        assertEquals( "The indexOf(char) method is looking beyond the end of the string", -1, sb.indexOf('h'));
    }
