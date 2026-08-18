// buggy code
    public static String random(int count, int start, int end, boolean letters, boolean numbers,
                                char[] chars, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }

        if (start == 0 && end == 0) {
                if (!letters && !numbers) {
                    end = Integer.MAX_VALUE;
                } else {
                    end = 'z' + 1;
                    start = ' ';                
                }
        }

        char[] buffer = new char[count];
        int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (random.nextInt(gap) + start);
            } else {
                ch = chars[random.nextInt(gap) + start];
            }
            if (letters && Character.isLetter(ch)
                    || numbers && Character.isDigit(ch)
                    || !letters && !numbers) {
                if(ch >= 56320 && ch <= 57343) {
                    if(count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + random.nextInt(128));
                    }
                } else if(ch >= 55296 && ch <= 56191) {
                    if(count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting it in
                        buffer[count] = (char) (56320 + random.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if(ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }

// relevant test
// org.apache.commons.lang3.RandomStringUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new RandomStringUtils());
        Constructor<?>[] cons = RandomStringUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(RandomStringUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(RandomStringUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomStringUtils
    public void testRandomStringUtils() {
        String r1 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r1.length());
        String r2 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r2.length());
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        r1 = RandomStringUtils.randomAscii(50);
        assertEquals("randomAscii(50) length", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertTrue("char between 32 and 127", r1.charAt(i) >= 32 && r1.charAt(i) <= 127);
        }        
        r2 = RandomStringUtils.randomAscii(50);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));

        r1 = RandomStringUtils.randomAlphabetic(50);
        assertEquals("randomAlphabetic(50)", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertEquals("r1 contains alphabetic", true, Character.isLetter(r1.charAt(i)) && !Character.isDigit(r1.charAt(i)));
        }
        r2 = RandomStringUtils.randomAlphabetic(50);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        r1 = RandomStringUtils.randomAlphanumeric(50);
        assertEquals("randomAlphanumeric(50)", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertEquals("r1 contains alphanumeric", true, Character.isLetterOrDigit(r1.charAt(i)));
        }
        r2 = RandomStringUtils.randomAlphabetic(50);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        r1 = RandomStringUtils.randomNumeric(50);
        assertEquals("randomNumeric(50)", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertEquals("r1 contains numeric", true, Character.isDigit(r1.charAt(i)) && !Character.isLetter(r1.charAt(i)));
        }
        r2 = RandomStringUtils.randomNumeric(50);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        String set = "abcdefg";
        r1 = RandomStringUtils.random(50, set);
        assertEquals("random(50, \"abcdefg\")", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertTrue("random char in set", set.indexOf(r1.charAt(i)) > -1);
        }
        r2 = RandomStringUtils.random(50, set);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        r1 = RandomStringUtils.random(50, (String) null);
        assertEquals("random(50) length", 50, r1.length());
        r2 = RandomStringUtils.random(50, (String) null);
        assertEquals("random(50) length", 50, r2.length());
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        set = "stuvwxyz";
        r1 = RandomStringUtils.random(50, set.toCharArray());
        assertEquals("random(50, \"stuvwxyz\")", 50, r1.length());
        for(int i = 0; i < r1.length(); i++) {
            assertTrue("random char in set", set.indexOf(r1.charAt(i)) > -1);
        }
        r2 = RandomStringUtils.random(50, set);
        assertTrue("!r1.equals(r2)", !r1.equals(r2));
        
        r1 = RandomStringUtils.random(50, (char[]) null);
        assertEquals("random(50) length", 50, r1.length());
        r2 = RandomStringUtils.random(50, (char[]) null);
        assertEquals("random(50) length", 50, r2.length());
        assertTrue("!r1.equals(r2)", !r1.equals(r2));

        long seed = System.currentTimeMillis();
        r1 = RandomStringUtils.random(50,0,0,true,true,null,new Random(seed));
        r2 = RandomStringUtils.random(50,0,0,true,true,null,new Random(seed));
        assertEquals("r1.equals(r2)", r1, r2);

        r1 = RandomStringUtils.random(0);
        assertEquals("random(0).equals(\"\")", "", r1);
    }

// org.apache.commons.lang3.RandomStringUtilsTest::testLANG805
    public void testLANG805() {
        long seed = System.currentTimeMillis();
        assertEquals("aaa", RandomStringUtils.random(3,0,0,false,false,new char[]{'a'},new Random(seed)));
    }

// org.apache.commons.lang3.RandomStringUtilsTest::testExceptions
    public void testExceptions() {
        final char[] DUMMY = new char[]{'a'}; 
        try {
            RandomStringUtils.random(-1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, true, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, DUMMY);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(1, new char[0]); 
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, "");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, (String)null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, 'a', 'z', false, false);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, 'a', 'z', false, false, DUMMY);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            RandomStringUtils.random(-1, 'a', 'z', false, false, DUMMY, new Random());
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomAlphaNumeric
    public void testRandomAlphaNumeric() {}

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomNumeric
    public void testRandomNumeric() {}

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomAlphabetic
    public void testRandomAlphabetic() {}

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomAscii
    public void testRandomAscii() {}

// org.apache.commons.lang3.RandomStringUtilsTest::testRandomStringUtilsHomog
    public void testRandomStringUtilsHomog() {}

// org.apache.commons.lang3.RandomStringUtilsTest::testLang100
    public void testLang100() throws Exception {
        int size = 5000;
        String encoding = "UTF-8";
        String orig = RandomStringUtils.random(size);
        byte[] bytes = orig.getBytes(encoding);
        String copy = new String(bytes, encoding);

        
        for (int i=0; i < orig.length() && i < copy.length(); i++) {
            char o = orig.charAt(i);
            char c = copy.charAt(i);
            assertEquals("differs at " + i + "(" + Integer.toHexString(new Character(o).hashCode()) + "," +
            Integer.toHexString(new Character(c).hashCode()) + ")", o, c);
        }
        
        assertEquals(orig.length(), copy.length());
        
        assertEquals(orig, copy);
    }
