// buggy function
    static String unescape(String string) {
        if (!string.contains("&"))
            return string;

        Matcher m = unescapePattern.matcher(string); // &(#(x|X)?([0-9a-fA-F]+)|[a-zA-Z]+);?
        StringBuffer accum = new StringBuffer(string.length()); // pity matcher can't use stringbuilder, avoid syncs

        while (m.find()) {
            int charval = -1;
            String num = m.group(3);
            if (num != null) {
                try {
                    int base = m.group(2) != null ? 16 : 10; // 2 is hex indicator
                    charval = Integer.valueOf(num, base);
                } catch (NumberFormatException e) {
                } // skip
            } else {
                String name = m.group(1).toLowerCase();
                if (full.containsKey(name))
                    charval = full.get(name);
            }

            if (charval != -1 || charval > 0xFFFF) { // out of range
                String c = Character.toString((char) charval);
                m.appendReplacement(accum, c);
            } else {
                m.appendReplacement(accum, m.group(0)); // replace with original string
            }
        }
        m.appendTail(accum);
        return accum.toString();
    }

// trigger testcase
// org/jsoup/nodes/EntitiesTest.java::caseSensitive
@Test public void caseSensitive() {
        String unescaped = "Ü ü & &";
        assertEquals("&Uuml; &uuml; &amp; &amp;", Entities.escape(unescaped, Charset.forName("ascii").newEncoder(), Entities.EscapeMode.extended));
        
        String escaped = "&Uuml; &uuml; &amp; &AMP";
        assertEquals("Ü ü & &", Entities.unescape(escaped));
    }

// org/jsoup/nodes/EntitiesTest.java::escape
@Test public void escape() {
        String text = "Hello &<> Å å π 新 there";
        String escapedAscii = Entities.escape(text, Charset.forName("ascii").newEncoder(), Entities.EscapeMode.base);
        String escapedAsciiFull = Entities.escape(text, Charset.forName("ascii").newEncoder(), Entities.EscapeMode.extended);
        String escapedUtf = Entities.escape(text, Charset.forName("UTF-8").newEncoder(), Entities.EscapeMode.base);

        assertEquals("Hello &amp;&lt;&gt; &Aring; &aring; &#960; &#26032; there", escapedAscii);
        assertEquals("Hello &amp;&lt;&gt; &angst; &aring; &pi; &#26032; there", escapedAsciiFull);
        assertEquals("Hello &amp;&lt;&gt; &Aring; &aring; π 新 there", escapedUtf);
        // odd that it's defined as aring in base but angst in full
    }
