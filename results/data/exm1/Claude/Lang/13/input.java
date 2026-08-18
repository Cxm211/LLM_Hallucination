// buggy code
    public static Object deserialize(byte[] objectData) {
        if (objectData == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
        return deserialize(bais);
    }

        public ClassLoaderAwareObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;

        }

        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String name = desc.getName();
            try {
                return Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException ex) {
                    return Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            }
        }

// relevant test
// org.apache.commons.lang3.CharRangeTest::testClass
    public void testClass() {
        
        assertEquals(false, Modifier.isPublic(CharRange.class.getModifiers()));
        assertEquals(true, Modifier.isFinal(CharRange.class.getModifiers()));
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_is
    public void testConstructorAccessors_is() {
        CharRange rangea = CharRange.is('a');
        assertEquals('a', rangea.getStart());
        assertEquals('a', rangea.getEnd());
        assertEquals(false, rangea.isNegated());
        assertEquals("a", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isNot
    public void testConstructorAccessors_isNot() {
        CharRange rangea = CharRange.isNot('a');
        assertEquals('a', rangea.getStart());
        assertEquals('a', rangea.getEnd());
        assertEquals(true, rangea.isNegated());
        assertEquals("^a", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isIn_Same
    public void testConstructorAccessors_isIn_Same() {
        CharRange rangea = CharRange.isIn('a', 'a');
        assertEquals('a', rangea.getStart());
        assertEquals('a', rangea.getEnd());
        assertEquals(false, rangea.isNegated());
        assertEquals("a", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isIn_Normal
    public void testConstructorAccessors_isIn_Normal() {
        CharRange rangea = CharRange.isIn('a', 'e');
        assertEquals('a', rangea.getStart());
        assertEquals('e', rangea.getEnd());
        assertEquals(false, rangea.isNegated());
        assertEquals("a-e", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isIn_Reversed
    public void testConstructorAccessors_isIn_Reversed() {
        CharRange rangea = CharRange.isIn('e', 'a');
        assertEquals('a', rangea.getStart());
        assertEquals('e', rangea.getEnd());
        assertEquals(false, rangea.isNegated());
        assertEquals("a-e", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isNotIn_Same
    public void testConstructorAccessors_isNotIn_Same() {
        CharRange rangea = CharRange.isNotIn('a', 'a');
        assertEquals('a', rangea.getStart());
        assertEquals('a', rangea.getEnd());
        assertEquals(true, rangea.isNegated());
        assertEquals("^a", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isNotIn_Normal
    public void testConstructorAccessors_isNotIn_Normal() {
        CharRange rangea = CharRange.isNotIn('a', 'e');
        assertEquals('a', rangea.getStart());
        assertEquals('e', rangea.getEnd());
        assertEquals(true, rangea.isNegated());
        assertEquals("^a-e", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testConstructorAccessors_isNotIn_Reversed
    public void testConstructorAccessors_isNotIn_Reversed() {
        CharRange rangea = CharRange.isNotIn('e', 'a');
        assertEquals('a', rangea.getStart());
        assertEquals('e', rangea.getEnd());
        assertEquals(true, rangea.isNegated());
        assertEquals("^a-e", rangea.toString());
    }

// org.apache.commons.lang3.CharRangeTest::testEquals_Object
    public void testEquals_Object() {
        CharRange rangea = CharRange.is('a');
        CharRange rangeae = CharRange.isIn('a', 'e');
        CharRange rangenotbf = CharRange.isIn('b', 'f');

        assertEquals(false, rangea.equals(null));

        assertEquals(true, rangea.equals(rangea));
        assertEquals(true, rangea.equals(CharRange.is('a')));
        assertEquals(true, rangeae.equals(rangeae));
        assertEquals(true, rangeae.equals(CharRange.isIn('a', 'e')));
        assertEquals(true, rangenotbf.equals(rangenotbf));
        assertEquals(true, rangenotbf.equals(CharRange.isIn('b', 'f')));

        assertEquals(false, rangea.equals(rangeae));
        assertEquals(false, rangea.equals(rangenotbf));
        assertEquals(false, rangeae.equals(rangea));
        assertEquals(false, rangeae.equals(rangenotbf));
        assertEquals(false, rangenotbf.equals(rangea));
        assertEquals(false, rangenotbf.equals(rangeae));
    }

// org.apache.commons.lang3.CharRangeTest::testHashCode
    public void testHashCode() {
        CharRange rangea = CharRange.is('a');
        CharRange rangeae = CharRange.isIn('a', 'e');
        CharRange rangenotbf = CharRange.isIn('b', 'f');

        assertEquals(true, rangea.hashCode() == rangea.hashCode());
        assertEquals(true, rangea.hashCode() == CharRange.is('a').hashCode());
        assertEquals(true, rangeae.hashCode() == rangeae.hashCode());
        assertEquals(true, rangeae.hashCode() == CharRange.isIn('a', 'e').hashCode());
        assertEquals(true, rangenotbf.hashCode() == rangenotbf.hashCode());
        assertEquals(true, rangenotbf.hashCode() == CharRange.isIn('b', 'f').hashCode());

        assertEquals(false, rangea.hashCode() == rangeae.hashCode());
        assertEquals(false, rangea.hashCode() == rangenotbf.hashCode());
        assertEquals(false, rangeae.hashCode() == rangea.hashCode());
        assertEquals(false, rangeae.hashCode() == rangenotbf.hashCode());
        assertEquals(false, rangenotbf.hashCode() == rangea.hashCode());
        assertEquals(false, rangenotbf.hashCode() == rangeae.hashCode());
    }

// org.apache.commons.lang3.CharRangeTest::testContains_Char
    public void testContains_Char() {
        CharRange range = CharRange.is('c');
        assertEquals(false, range.contains('b'));
        assertEquals(true, range.contains('c'));
        assertEquals(false, range.contains('d'));
        assertEquals(false, range.contains('e'));

        range = CharRange.isIn('c', 'd');
        assertEquals(false, range.contains('b'));
        assertEquals(true, range.contains('c'));
        assertEquals(true, range.contains('d'));
        assertEquals(false, range.contains('e'));

        range = CharRange.isIn('d', 'c');
        assertEquals(false, range.contains('b'));
        assertEquals(true, range.contains('c'));
        assertEquals(true, range.contains('d'));
        assertEquals(false, range.contains('e'));

        range = CharRange.isNotIn('c', 'd');
        assertEquals(true, range.contains('b'));
        assertEquals(false, range.contains('c'));
        assertEquals(false, range.contains('d'));
        assertEquals(true, range.contains('e'));
        assertEquals(true, range.contains((char) 0));
        assertEquals(true, range.contains(Character.MAX_VALUE));
    }

// org.apache.commons.lang3.CharRangeTest::testContains_Charrange
    public void testContains_Charrange() {
        CharRange a = CharRange.is('a');
        CharRange b = CharRange.is('b');
        CharRange c = CharRange.is('c');
        CharRange c2 = CharRange.is('c');
        CharRange d = CharRange.is('d');
        CharRange e = CharRange.is('e');
        CharRange cd = CharRange.isIn('c', 'd');
        CharRange bd = CharRange.isIn('b', 'd');
        CharRange bc = CharRange.isIn('b', 'c');
        CharRange ab = CharRange.isIn('a', 'b');
        CharRange de = CharRange.isIn('d', 'e');
        CharRange ef = CharRange.isIn('e', 'f');
        CharRange ae = CharRange.isIn('a', 'e');

        
        assertEquals(false, c.contains(b));
        assertEquals(true, c.contains(c));
        assertEquals(true, c.contains(c2));
        assertEquals(false, c.contains(d));

        assertEquals(false, c.contains(cd));
        assertEquals(false, c.contains(bd));
        assertEquals(false, c.contains(bc));
        assertEquals(false, c.contains(ab));
        assertEquals(false, c.contains(de));

        assertEquals(true, cd.contains(c));
        assertEquals(true, bd.contains(c));
        assertEquals(true, bc.contains(c));
        assertEquals(false, ab.contains(c));
        assertEquals(false, de.contains(c));

        assertEquals(true, ae.contains(b));
        assertEquals(true, ae.contains(ab));
        assertEquals(true, ae.contains(bc));
        assertEquals(true, ae.contains(cd));
        assertEquals(true, ae.contains(de));

        CharRange notb = CharRange.isNot('b');
        CharRange notc = CharRange.isNot('c');
        CharRange notd = CharRange.isNot('d');
        CharRange notab = CharRange.isNotIn('a', 'b');
        CharRange notbc = CharRange.isNotIn('b', 'c');
        CharRange notbd = CharRange.isNotIn('b', 'd');
        CharRange notcd = CharRange.isNotIn('c', 'd');
        CharRange notde = CharRange.isNotIn('d', 'e');
        CharRange notae = CharRange.isNotIn('a', 'e');
        CharRange all = CharRange.isIn((char) 0, Character.MAX_VALUE);
        CharRange allbutfirst = CharRange.isIn((char) 1, Character.MAX_VALUE);

        
        assertEquals(false, c.contains(notc));
        assertEquals(false, c.contains(notbd));
        assertEquals(true, all.contains(notc));
        assertEquals(true, all.contains(notbd));
        assertEquals(false, allbutfirst.contains(notc));
        assertEquals(false, allbutfirst.contains(notbd));

        
        assertEquals(true, notc.contains(a));
        assertEquals(true, notc.contains(b));
        assertEquals(false, notc.contains(c));
        assertEquals(true, notc.contains(d));
        assertEquals(true, notc.contains(e));

        assertEquals(true, notc.contains(ab));
        assertEquals(false, notc.contains(bc));
        assertEquals(false, notc.contains(bd));
        assertEquals(false, notc.contains(cd));
        assertEquals(true, notc.contains(de));
        assertEquals(false, notc.contains(ae));
        assertEquals(false, notc.contains(all));
        assertEquals(false, notc.contains(allbutfirst));

        assertEquals(true, notbd.contains(a));
        assertEquals(false, notbd.contains(b));
        assertEquals(false, notbd.contains(c));
        assertEquals(false, notbd.contains(d));
        assertEquals(true, notbd.contains(e));

        assertEquals(true, notcd.contains(ab));
        assertEquals(false, notcd.contains(bc));
        assertEquals(false, notcd.contains(bd));
        assertEquals(false, notcd.contains(cd));
        assertEquals(false, notcd.contains(de));
        assertEquals(false, notcd.contains(ae));
        assertEquals(true, notcd.contains(ef));
        assertEquals(false, notcd.contains(all));
        assertEquals(false, notcd.contains(allbutfirst));

        
        assertEquals(false, notc.contains(notb));
        assertEquals(true, notc.contains(notc));
        assertEquals(false, notc.contains(notd));

        assertEquals(false, notc.contains(notab));
        assertEquals(true, notc.contains(notbc));
        assertEquals(true, notc.contains(notbd));
        assertEquals(true, notc.contains(notcd));
        assertEquals(false, notc.contains(notde));

        assertEquals(false, notbd.contains(notb));
        assertEquals(false, notbd.contains(notc));
        assertEquals(false, notbd.contains(notd));

        assertEquals(false, notbd.contains(notab));
        assertEquals(false, notbd.contains(notbc));
        assertEquals(true, notbd.contains(notbd));
        assertEquals(false, notbd.contains(notcd));
        assertEquals(false, notbd.contains(notde));
        assertEquals(true, notbd.contains(notae));
    }

// org.apache.commons.lang3.CharRangeTest::testContainsNullArg
    public void testContainsNullArg() {
        CharRange range = CharRange.is('a');
        try {
            @SuppressWarnings("unused")
            boolean contains = range.contains(null);
        } catch(IllegalArgumentException e) {
            assertEquals("The Range must not be null", e.getMessage());
        }
    }

// org.apache.commons.lang3.CharRangeTest::testIterator
    public void testIterator() {
        CharRange a = CharRange.is('a');
        CharRange ad = CharRange.isIn('a', 'd');
        CharRange nota = CharRange.isNot('a');
        CharRange emptySet = CharRange.isNotIn((char) 0, Character.MAX_VALUE);
        CharRange notFirst = CharRange.isNotIn((char) 1, Character.MAX_VALUE);
        CharRange notLast = CharRange.isNotIn((char) 0, (char) (Character.MAX_VALUE - 1));

        Iterator<Character> aIt = a.iterator();
        assertNotNull(aIt);
        assertTrue(aIt.hasNext());
        assertEquals(Character.valueOf('a'), aIt.next());
        assertFalse(aIt.hasNext());

        Iterator<Character> adIt = ad.iterator();
        assertNotNull(adIt);
        assertTrue(adIt.hasNext());
        assertEquals(Character.valueOf('a'), adIt.next());
        assertEquals(Character.valueOf('b'), adIt.next());
        assertEquals(Character.valueOf('c'), adIt.next());
        assertEquals(Character.valueOf('d'), adIt.next());
        assertFalse(adIt.hasNext());

        Iterator<Character> notaIt = nota.iterator();
        assertNotNull(notaIt);
        assertTrue(notaIt.hasNext());
        while (notaIt.hasNext()) {
            Character c = notaIt.next();
            assertFalse('a' == c.charValue());
        }

        Iterator<Character> emptySetIt = emptySet.iterator();
        assertNotNull(emptySetIt);
        assertFalse(emptySetIt.hasNext());
        try {
            emptySetIt.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        Iterator<Character> notFirstIt = notFirst.iterator();
        assertNotNull(notFirstIt);
        assertTrue(notFirstIt.hasNext());
        assertEquals(Character.valueOf((char) 0), notFirstIt.next());
        assertFalse(notFirstIt.hasNext());
        try {
            notFirstIt.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        Iterator<Character> notLastIt = notLast.iterator();
        assertNotNull(notLastIt);
        assertTrue(notLastIt.hasNext());
        assertEquals(Character.valueOf(Character.MAX_VALUE), notLastIt.next());
        assertFalse(notLastIt.hasNext());
        try {
            notLastIt.next();
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
    }

// org.apache.commons.lang3.CharRangeTest::testSerialization
    public void testSerialization() {
        CharRange range = CharRange.is('a');
        assertEquals(range, SerializationUtils.clone(range)); 
        range = CharRange.isIn('a', 'e');
        assertEquals(range, SerializationUtils.clone(range)); 
        range = CharRange.isNotIn('a', 'e');
        assertEquals(range, SerializationUtils.clone(range)); 
    }

// org.apache.commons.lang3.CharSetTest::testClass
    public void testClass() {
        assertEquals(true, Modifier.isPublic(CharSet.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(CharSet.class.getModifiers()));
    }

// org.apache.commons.lang3.CharSetTest::testGetInstance
    public void testGetInstance() {
        assertSame(CharSet.EMPTY, CharSet.getInstance( (String) null));
        assertSame(CharSet.EMPTY, CharSet.getInstance(""));
        assertSame(CharSet.ASCII_ALPHA, CharSet.getInstance("a-zA-Z"));
        assertSame(CharSet.ASCII_ALPHA, CharSet.getInstance("A-Za-z"));
        assertSame(CharSet.ASCII_ALPHA_LOWER, CharSet.getInstance("a-z"));
        assertSame(CharSet.ASCII_ALPHA_UPPER, CharSet.getInstance("A-Z"));
        assertSame(CharSet.ASCII_NUMERIC, CharSet.getInstance("0-9"));
    }

// org.apache.commons.lang3.CharSetTest::testGetInstance_Stringarray
    public void testGetInstance_Stringarray() {
        assertEquals(null, CharSet.getInstance((String[]) null));
        assertEquals("[]", CharSet.getInstance(new String[0]).toString());
        assertEquals("[]", CharSet.getInstance(new String[] {null}).toString());
        assertEquals("[a-e]", CharSet.getInstance(new String[] {"a-e"}).toString());
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_simple
    public void testConstructor_String_simple() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance((String) null);
        array = set.getCharRanges();
        assertEquals("[]", set.toString());
        assertEquals(0, array.length);
        
        set = CharSet.getInstance("");
        array = set.getCharRanges();
        assertEquals("[]", set.toString());
        assertEquals(0, array.length);
        
        set = CharSet.getInstance("a");
        array = set.getCharRanges();
        assertEquals("[a]", set.toString());
        assertEquals(1, array.length);
        assertEquals("a", array[0].toString());
        
        set = CharSet.getInstance("^a");
        array = set.getCharRanges();
        assertEquals("[^a]", set.toString());
        assertEquals(1, array.length);
        assertEquals("^a", array[0].toString());
        
        set = CharSet.getInstance("a-e");
        array = set.getCharRanges();
        assertEquals("[a-e]", set.toString());
        assertEquals(1, array.length);
        assertEquals("a-e", array[0].toString());
        
        set = CharSet.getInstance("^a-e");
        array = set.getCharRanges();
        assertEquals("[^a-e]", set.toString());
        assertEquals(1, array.length);
        assertEquals("^a-e", array[0].toString());
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_combo
    public void testConstructor_String_combo() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("abc");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("a-ce-f");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'c')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("ae-f");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("e-fa");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        
        set = CharSet.getInstance("ae-fm-pz");
        array = set.getCharRanges();
        assertEquals(4, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('e', 'f')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('m', 'p')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('z')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_comboNegated
    public void testConstructor_String_comboNegated() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("^abc");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("b^ac");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("db^ac");
        array = set.getCharRanges();
        assertEquals(4, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('d')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c')));
        
        set = CharSet.getInstance("^b^a");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('b')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a')));
        
        set = CharSet.getInstance("b^a-c^z");
        array = set.getCharRanges();
        assertEquals(3, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('a', 'c')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('z')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('b')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddDash
    public void testConstructor_String_oddDash() {
        CharSet set;
        CharRange[] array;
        
        set = CharSet.getInstance("-");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("--");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("---");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("----");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("-a");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        
        set = CharSet.getInstance("a-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-')));
        
        set = CharSet.getInstance("a--");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', '-')));
        
        set = CharSet.getInstance("--a");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('-', 'a')));
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddNegate
    public void testConstructor_String_oddNegate() {
        CharSet set;
        CharRange[] array;
        set = CharSet.getInstance("^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        
        set = CharSet.getInstance("^^^");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^^^^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        
        set = CharSet.getInstance("a^");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('a'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('^'))); 
        
        set = CharSet.getInstance("^a-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('a'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-'))); 
        
        set = CharSet.getInstance("^^-c");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('^', 'c'))); 
        
        set = CharSet.getInstance("^c-^");
        array = set.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('c', '^'))); 
        
        set = CharSet.getInstance("^c-^d");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('c', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('d'))); 
        
        set = CharSet.getInstance("^^-");
        array = set.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNot('^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('-'))); 
    }

// org.apache.commons.lang3.CharSetTest::testConstructor_String_oddCombinations
    public void testConstructor_String_oddCombinations() {
        CharSet set;
        CharRange[] array = null;
        
        set = CharSet.getInstance("a-^c");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c'))); 
        assertEquals(false, set.contains('b'));
        assertEquals(true, set.contains('^'));  
        assertEquals(true, set.contains('_')); 
        assertEquals(true, set.contains('c'));  
        
        set = CharSet.getInstance("^a-^c");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('a', '^'))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.is('c'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(false, set.contains('^'));  
        assertEquals(false, set.contains('_')); 
        
        set = CharSet.getInstance("a- ^-- "); 
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', ' '))); 
        assertEquals(true, ArrayUtils.contains(array, CharRange.isNotIn('-', ' '))); 
        assertEquals(true, set.contains('#'));
        assertEquals(true, set.contains('^'));
        assertEquals(true, set.contains('a'));
        assertEquals(true, set.contains('*'));
        assertEquals(true, set.contains('A'));
        
        set = CharSet.getInstance("^-b");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('^','b'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(true, set.contains('_')); 
        assertEquals(false, set.contains('A'));
        assertEquals(true, set.contains('^')); 
        
        set = CharSet.getInstance("b-^");
        array = set.getCharRanges();
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('^','b'))); 
        assertEquals(true, set.contains('b'));
        assertEquals(true, set.contains('^'));
        assertEquals(true, set.contains('a')); 
        assertEquals(false, set.contains('c')); 
    }

// org.apache.commons.lang3.CharSetTest::testEquals_Object
    public void testEquals_Object() {
        CharSet abc = CharSet.getInstance("abc");
        CharSet abc2 = CharSet.getInstance("abc");
        CharSet atoc = CharSet.getInstance("a-c");
        CharSet atoc2 = CharSet.getInstance("a-c");
        CharSet notatoc = CharSet.getInstance("^a-c");
        CharSet notatoc2 = CharSet.getInstance("^a-c");
        
        assertEquals(false, abc.equals(null));
        
        assertEquals(true, abc.equals(abc));
        assertEquals(true, abc.equals(abc2));
        assertEquals(false, abc.equals(atoc));
        assertEquals(false, abc.equals(notatoc));
        
        assertEquals(false, atoc.equals(abc));
        assertEquals(true, atoc.equals(atoc));
        assertEquals(true, atoc.equals(atoc2));
        assertEquals(false, atoc.equals(notatoc));
        
        assertEquals(false, notatoc.equals(abc));
        assertEquals(false, notatoc.equals(atoc));
        assertEquals(true, notatoc.equals(notatoc));
        assertEquals(true, notatoc.equals(notatoc2));
    }

// org.apache.commons.lang3.CharSetTest::testHashCode
    public void testHashCode() {
        CharSet abc = CharSet.getInstance("abc");
        CharSet abc2 = CharSet.getInstance("abc");
        CharSet atoc = CharSet.getInstance("a-c");
        CharSet atoc2 = CharSet.getInstance("a-c");
        CharSet notatoc = CharSet.getInstance("^a-c");
        CharSet notatoc2 = CharSet.getInstance("^a-c");
        
        assertEquals(abc.hashCode(), abc.hashCode());
        assertEquals(abc.hashCode(), abc2.hashCode());
        assertEquals(atoc.hashCode(), atoc.hashCode());
        assertEquals(atoc.hashCode(), atoc2.hashCode());
        assertEquals(notatoc.hashCode(), notatoc.hashCode());
        assertEquals(notatoc.hashCode(), notatoc2.hashCode());
    }

// org.apache.commons.lang3.CharSetTest::testContains_Char
    public void testContains_Char() {
        CharSet btod = CharSet.getInstance("b-d");
        CharSet dtob = CharSet.getInstance("d-b");
        CharSet bcd = CharSet.getInstance("bcd");
        CharSet bd = CharSet.getInstance("bd");
        CharSet notbtod = CharSet.getInstance("^b-d");
        
        assertEquals(false, btod.contains('a'));
        assertEquals(true, btod.contains('b'));
        assertEquals(true, btod.contains('c'));
        assertEquals(true, btod.contains('d'));
        assertEquals(false, btod.contains('e'));
        
        assertEquals(false, bcd.contains('a'));
        assertEquals(true, bcd.contains('b'));
        assertEquals(true, bcd.contains('c'));
        assertEquals(true, bcd.contains('d'));
        assertEquals(false, bcd.contains('e'));
        
        assertEquals(false, bd.contains('a'));
        assertEquals(true, bd.contains('b'));
        assertEquals(false, bd.contains('c'));
        assertEquals(true, bd.contains('d'));
        assertEquals(false, bd.contains('e'));
        
        assertEquals(true, notbtod.contains('a'));
        assertEquals(false, notbtod.contains('b'));
        assertEquals(false, notbtod.contains('c'));
        assertEquals(false, notbtod.contains('d'));
        assertEquals(true, notbtod.contains('e'));
        
        assertEquals(false, dtob.contains('a'));
        assertEquals(true, dtob.contains('b'));
        assertEquals(true, dtob.contains('c'));
        assertEquals(true, dtob.contains('d'));
        assertEquals(false, dtob.contains('e'));
      
        CharRange[] array = dtob.getCharRanges();
        assertEquals("[b-d]", dtob.toString());
        assertEquals(1, array.length);
    }

// org.apache.commons.lang3.CharSetTest::testSerialization
    public void testSerialization() {
        CharSet set = CharSet.getInstance("a");
        assertEquals(set, SerializationUtils.clone(set)); 
        set = CharSet.getInstance("a-e");
        assertEquals(set, SerializationUtils.clone(set)); 
        set = CharSet.getInstance("be-f^a-z");
        assertEquals(set, SerializationUtils.clone(set)); 
    }

// org.apache.commons.lang3.CharSetTest::testStatics
    public void testStatics() {
        CharRange[] array;
        
        array = CharSet.EMPTY.getCharRanges();
        assertEquals(0, array.length);
        
        array = CharSet.ASCII_ALPHA.getCharRanges();
        assertEquals(2, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'z')));
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('A', 'Z')));
        
        array = CharSet.ASCII_ALPHA_LOWER.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('a', 'z')));
        
        array = CharSet.ASCII_ALPHA_UPPER.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('A', 'Z')));
        
        array = CharSet.ASCII_NUMERIC.getCharRanges();
        assertEquals(1, array.length);
        assertEquals(true, ArrayUtils.contains(array, CharRange.isIn('0', '9')));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ObjectUtils());
        Constructor<?>[] cons = ObjectUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(ObjectUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(ObjectUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testIsNull
    public void testIsNull() {
        Object o = FOO;
        Object dflt = BAR;
        assertSame("dflt was not returned when o was null", dflt, ObjectUtils.defaultIfNull(null, dflt));
        assertSame("dflt was returned when o was not null", o, ObjectUtils.defaultIfNull(o, dflt));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testFirstNonNull
    public void testFirstNonNull() {
        assertEquals(null, ObjectUtils.firstNonNull(null, null));
        assertEquals("", ObjectUtils.firstNonNull(null, ""));
        String firstNonNullGenerics = ObjectUtils.firstNonNull(null, null, "123", "456");
        assertEquals("123", firstNonNullGenerics);
        assertEquals("123", ObjectUtils.firstNonNull("123", null, "456", null));
        assertEquals(null, ObjectUtils.firstNonNull());
        assertSame(Boolean.TRUE, ObjectUtils.firstNonNull(Boolean.TRUE));
        assertNull(ObjectUtils.firstNonNull());
        assertNull(ObjectUtils.firstNonNull(null, null));

        assertNull(ObjectUtils.firstNonNull((Object) null));
        assertNull(ObjectUtils.firstNonNull((Object[]) null));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testEquals
    public void testEquals() {
        assertTrue("ObjectUtils.equals(null, null) returned false", ObjectUtils.equals(null, null));
        assertTrue("ObjectUtils.equals(\"foo\", null) returned true", !ObjectUtils.equals(FOO, null));
        assertTrue("ObjectUtils.equals(null, \"bar\") returned true", !ObjectUtils.equals(null, BAR));
        assertTrue("ObjectUtils.equals(\"foo\", \"bar\") returned true", !ObjectUtils.equals(FOO, BAR));
        assertTrue("ObjectUtils.equals(\"foo\", \"foo\") returned false", ObjectUtils.equals(FOO, FOO));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testNotEqual
    public void testNotEqual() {
        assertFalse("ObjectUtils.notEqual(null, null) returned false", ObjectUtils.notEqual(null, null));
        assertTrue("ObjectUtils.notEqual(\"foo\", null) returned true", ObjectUtils.notEqual(FOO, null));
        assertTrue("ObjectUtils.notEqual(null, \"bar\") returned true", ObjectUtils.notEqual(null, BAR));
        assertTrue("ObjectUtils.notEqual(\"foo\", \"bar\") returned true", ObjectUtils.notEqual(FOO, BAR));
        assertFalse("ObjectUtils.notEqual(\"foo\", \"foo\") returned false", ObjectUtils.notEqual(FOO, FOO));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testHashCode
    public void testHashCode() {
        assertEquals(0, ObjectUtils.hashCode(null));
        assertEquals("a".hashCode(), ObjectUtils.hashCode("a"));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testHashCodeMulti_multiple_emptyArray
    public void testHashCodeMulti_multiple_emptyArray() {
        Object[] array = new Object[0];
        assertEquals(1, ObjectUtils.hashCodeMulti(array));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testHashCodeMulti_multiple_nullArray
    public void testHashCodeMulti_multiple_nullArray() {
        Object[] array = null;
        assertEquals(1, ObjectUtils.hashCodeMulti(array));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testHashCodeMulti_multiple_likeList
    public void testHashCodeMulti_multiple_likeList() {
        List<Object> list0 = new ArrayList<Object>(Arrays.asList());
        assertEquals(list0.hashCode(), ObjectUtils.hashCodeMulti());
        
        List<Object> list1 = new ArrayList<Object>(Arrays.asList("a"));
        assertEquals(list1.hashCode(), ObjectUtils.hashCodeMulti("a"));
        
        List<Object> list2 = new ArrayList<Object>(Arrays.asList("a", "b"));
        assertEquals(list2.hashCode(), ObjectUtils.hashCodeMulti("a", "b"));
        
        List<Object> list3 = new ArrayList<Object>(Arrays.asList("a", "b", "c"));
        assertEquals(list3.hashCode(), ObjectUtils.hashCodeMulti("a", "b", "c"));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testIdentityToString
    public void testIdentityToString() {
        assertEquals(null, ObjectUtils.identityToString(null));
        assertEquals(
            "java.lang.String@" + Integer.toHexString(System.identityHashCode(FOO)),
            ObjectUtils.identityToString(FOO));
        Integer i = Integer.valueOf(90);
        String expected = "java.lang.Integer@" + Integer.toHexString(System.identityHashCode(i));
        assertEquals(expected, ObjectUtils.identityToString(i));
        StringBuffer buffer = new StringBuffer();
        ObjectUtils.identityToString(buffer, i);
        assertEquals(expected, buffer.toString());

        try {
            ObjectUtils.identityToString(null, "tmp");
            fail("NullPointerException expected");
        } catch(NullPointerException npe) {
        }
        try {
            ObjectUtils.identityToString(new StringBuffer(), null);
            fail("NullPointerException expected");
        } catch(NullPointerException npe) {
        }
    }

// org.apache.commons.lang3.ObjectUtilsTest::testToString_Object
    public void testToString_Object() {
        assertEquals("", ObjectUtils.toString((Object) null) );
        assertEquals(Boolean.TRUE.toString(), ObjectUtils.toString(Boolean.TRUE) );
    }

// org.apache.commons.lang3.ObjectUtilsTest::testToString_ObjectString
    public void testToString_ObjectString() {
        assertEquals(BAR, ObjectUtils.toString((Object) null, BAR) );
        assertEquals(Boolean.TRUE.toString(), ObjectUtils.toString(Boolean.TRUE, BAR) );
    }

// org.apache.commons.lang3.ObjectUtilsTest::testNull
    public void testNull() {
        assertNotNull(ObjectUtils.NULL);
        
        assertTrue(ObjectUtils.NULL instanceof ObjectUtils.Null);
        assertSame(ObjectUtils.NULL, SerializationUtils.clone(ObjectUtils.NULL));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMax
    public void testMax() {
        Calendar calendar = Calendar.getInstance();
        Date nonNullComparable1 = calendar.getTime();
        Date nonNullComparable2 = calendar.getTime();
        String[] nullAray = null;
        
        calendar.set( Calendar.YEAR, calendar.get( Calendar.YEAR ) -1 );
        Date minComparable = calendar.getTime();
        
        assertNotSame( nonNullComparable1, nonNullComparable2 );
        
        assertNull(ObjectUtils.max( (String) null ) );
        assertNull(ObjectUtils.max( nullAray ) );
        assertSame( nonNullComparable1, ObjectUtils.max( null, nonNullComparable1 ) );
        assertSame( nonNullComparable1, ObjectUtils.max( nonNullComparable1, null ) );
        assertSame( nonNullComparable1, ObjectUtils.max( null, nonNullComparable1, null ) );
        assertSame( nonNullComparable1, ObjectUtils.max( nonNullComparable1, nonNullComparable2 ) );
        assertSame( nonNullComparable2, ObjectUtils.max( nonNullComparable2, nonNullComparable1 ) );
        assertSame( nonNullComparable1, ObjectUtils.max( nonNullComparable1, minComparable ) );
        assertSame( nonNullComparable1, ObjectUtils.max( minComparable, nonNullComparable1 ) );
        assertSame( nonNullComparable1, ObjectUtils.max( null, minComparable, null, nonNullComparable1 ) );

        assertNull( ObjectUtils.max((String)null, (String)null) );
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMin
    public void testMin() {
        Calendar calendar = Calendar.getInstance();
        Date nonNullComparable1 = calendar.getTime();
        Date nonNullComparable2 = calendar.getTime();
        String[] nullAray = null;
        
        calendar.set( Calendar.YEAR, calendar.get( Calendar.YEAR ) -1 );
        Date minComparable = calendar.getTime();
        
        assertNotSame( nonNullComparable1, nonNullComparable2 );
        
        assertNull(ObjectUtils.min( (String) null ) );
        assertNull(ObjectUtils.min( nullAray ) );
        assertSame( nonNullComparable1, ObjectUtils.min( null, nonNullComparable1 ) );
        assertSame( nonNullComparable1, ObjectUtils.min( nonNullComparable1, null ) );
        assertSame( nonNullComparable1, ObjectUtils.min( null, nonNullComparable1, null ) );
        assertSame( nonNullComparable1, ObjectUtils.min( nonNullComparable1, nonNullComparable2 ) );
        assertSame( nonNullComparable2, ObjectUtils.min( nonNullComparable2, nonNullComparable1 ) );
        assertSame( minComparable, ObjectUtils.min( nonNullComparable1, minComparable ) );
        assertSame( minComparable, ObjectUtils.min( minComparable, nonNullComparable1 ) );
        assertSame( minComparable, ObjectUtils.min( null, nonNullComparable1, null, minComparable ) );

        assertNull( ObjectUtils.min((String)null, (String)null) );
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCompare
    public void testCompare() {
        Integer one = Integer.valueOf(1);
        Integer two = Integer.valueOf(2);
        Integer nullValue = null;

        assertEquals("Null Null false", 0, ObjectUtils.compare(nullValue, nullValue));
        assertEquals("Null Null true",  0, ObjectUtils.compare(nullValue, nullValue, true));

        assertEquals("Null one false", -1, ObjectUtils.compare(nullValue, one));
        assertEquals("Null one true",   1, ObjectUtils.compare(nullValue, one, true));
        
        assertEquals("one Null false", 1, ObjectUtils.compare(one, nullValue));
        assertEquals("one Null true", -1, ObjectUtils.compare(one, nullValue, true));

        assertEquals("one two false", -1, ObjectUtils.compare(one, two));
        assertEquals("one two true",  -1, ObjectUtils.compare(one, two, true));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMedian
    public void testMedian() {
        assertEquals("foo", ObjectUtils.median("foo"));
        assertEquals("bar", ObjectUtils.median("foo", "bar"));
        assertEquals("baz", ObjectUtils.median("foo", "bar", "baz"));
        assertEquals("baz", ObjectUtils.median("foo", "bar", "baz", "blah"));
        assertEquals("blah", ObjectUtils.median("foo", "bar", "baz", "blah", "wah"));
        assertEquals(Integer.valueOf(5),
            ObjectUtils.median(Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(10)));
        assertEquals(
            Integer.valueOf(7),
            ObjectUtils.median(Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8),
                Integer.valueOf(9)));
        assertEquals(Integer.valueOf(6),
            ObjectUtils.median(Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8)));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMedian_nullItems
    public void testMedian_nullItems() {
        ObjectUtils.median((String[]) null);
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMedian_emptyItems
    public void testMedian_emptyItems() {
        ObjectUtils.<String> median();
    }

// org.apache.commons.lang3.ObjectUtilsTest::testComparatorMedian
    public void testComparatorMedian() {
        CharSequenceComparator cmp = new CharSequenceComparator();
        NonComparableCharSequence foo = new NonComparableCharSequence("foo");
        NonComparableCharSequence bar = new NonComparableCharSequence("bar");
        NonComparableCharSequence baz = new NonComparableCharSequence("baz");
        NonComparableCharSequence blah = new NonComparableCharSequence("blah");
        NonComparableCharSequence wah = new NonComparableCharSequence("wah");
        assertSame(foo, ObjectUtils.median(cmp, foo));
        assertSame(bar, ObjectUtils.median(cmp, foo, bar));
        assertSame(baz, ObjectUtils.median(cmp, foo, bar, baz));
        assertSame(baz, ObjectUtils.median(cmp, foo, bar, baz, blah));
        assertSame(blah, ObjectUtils.median(cmp, foo, bar, baz, blah, wah));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testComparatorMedian_nullComparator
    public void testComparatorMedian_nullComparator() {
        ObjectUtils.median((Comparator<CharSequence>) null, new NonComparableCharSequence("foo"));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testComparatorMedian_nullItems
    public void testComparatorMedian_nullItems() {
        ObjectUtils.median(new CharSequenceComparator(), (CharSequence[]) null);
    }

// org.apache.commons.lang3.ObjectUtilsTest::testComparatorMedian_emptyItems
    public void testComparatorMedian_emptyItems() {
        ObjectUtils.median(new CharSequenceComparator());
    }

// org.apache.commons.lang3.ObjectUtilsTest::testMode
    public void testMode() {
        assertNull(ObjectUtils.mode((Object[]) null));
        assertNull(ObjectUtils.mode());
        assertNull(ObjectUtils.mode("foo", "bar", "baz"));
        assertNull(ObjectUtils.mode("foo", "bar", "baz", "foo", "bar"));
        assertEquals("foo", ObjectUtils.mode("foo", "bar", "baz", "foo"));
        assertEquals(Integer.valueOf(9),
            ObjectUtils.mode("foo", "bar", "baz", Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(9)));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCloneOfCloneable
    public void testCloneOfCloneable() {
        final CloneableString string = new CloneableString("apache");
        final CloneableString stringClone = ObjectUtils.clone(string);
        assertEquals("apache", stringClone.getValue());
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCloneOfNotCloneable
    public void testCloneOfNotCloneable() {
        final String string = new String("apache");
        assertNull(ObjectUtils.clone(string));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCloneOfUncloneable
    public void testCloneOfUncloneable() throws Throwable {
        final UncloneableString string = new UncloneableString("apache");
        try {
            ObjectUtils.clone(string);
            fail("Thrown " + CloneFailedException.class.getName() + " expected");
        } catch (final CloneFailedException e) {
            throw e.getCause();
        }
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCloneOfStringArray
    public void testCloneOfStringArray() {
        assertTrue(Arrays.deepEquals(
            new String[]{"string"}, ObjectUtils.clone(new String[]{"string"})));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testCloneOfPrimitiveArray
    public void testCloneOfPrimitiveArray() {
        assertTrue(Arrays.equals(new int[]{1}, ObjectUtils.clone(new int[]{1})));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testPossibleCloneOfCloneable
    public void testPossibleCloneOfCloneable() {
        final CloneableString string = new CloneableString("apache");
        final CloneableString stringClone = ObjectUtils.cloneIfPossible(string);
        assertEquals("apache", stringClone.getValue());
    }

// org.apache.commons.lang3.ObjectUtilsTest::testPossibleCloneOfNotCloneable
    public void testPossibleCloneOfNotCloneable() {
        final String string = new String("apache");
        assertSame(string, ObjectUtils.cloneIfPossible(string));
    }

// org.apache.commons.lang3.ObjectUtilsTest::testPossibleCloneOfUncloneable
    public void testPossibleCloneOfUncloneable() throws Throwable {
        final UncloneableString string = new UncloneableString("apache");
        try {
            ObjectUtils.cloneIfPossible(string);
            fail("Thrown " + CloneFailedException.class.getName() + " expected");
        } catch (final CloneFailedException e) {
            throw e.getCause();
        }
    }

// org.apache.commons.lang3.RangeTest::testComparableConstructors
    public void testComparableConstructors() {
        Comparable c = 
            new Comparable() { 
                public int compareTo(Object other) {
                    return 1;
                }
            };
        Range r1 = Range.is(c);
        Range r2 = Range.between(c, c);
        assertEquals(true, r1.isNaturalOrdering());
        assertEquals(true, r2.isNaturalOrdering());
    }

// org.apache.commons.lang3.RangeTest::testIsWithCompare
    public void testIsWithCompare(){
        Comparator<Integer> c = new Comparator<Integer>(){
            public int compare(Integer o1, Integer o2) {
                return 0; 
            }
        };
        Range<Integer> ri = Range.is(10);
        assertFalse("should not contain null",ri.contains(null));
        assertTrue("should contain 10",ri.contains(10));
        assertFalse("should not contain 11",ri.contains(11));
        ri = Range.is(10,c);
        assertFalse("should not contain null",ri.contains(null));
        assertTrue("should contain 10",ri.contains(10));
        assertTrue("should contain 11",ri.contains(11));
    }

// org.apache.commons.lang3.RangeTest::testBetweenWithCompare
    public void testBetweenWithCompare(){
        
        Comparator<Integer> c = new Comparator<Integer>(){
            public int compare(Integer o1, Integer o2) {
                return 0; 
            }
        };
        Range<Integer> rb = Range.between(-10,20);
        assertFalse("should not contain null",rb.contains(null));
        assertTrue("should contain 10",rb.contains(10));
        assertTrue("should contain -10",rb.contains(-10));
        assertFalse("should not contain 21",rb.contains(21));
        assertFalse("should not contain -11",rb.contains(-11));
        rb = Range.between(-10,20,c);
        assertFalse("should not contain null",rb.contains(null));
        assertTrue("should contain 10",rb.contains(10));
        assertTrue("should contain -10",rb.contains(-10));
        assertTrue("should contain 21",rb.contains(21));
        assertTrue("should contain -11",rb.contains(-11));
    }

// org.apache.commons.lang3.RangeTest::testRangeOfChars
    public void testRangeOfChars() {
        Range<Character> chars = Range.between('a', 'z');
        assertTrue(chars.contains('b'));
        assertFalse(chars.contains('B'));
    }

// org.apache.commons.lang3.RangeTest::testEqualsObject
    public void testEqualsObject() {
        assertEquals(byteRange, byteRange);
        assertEquals(byteRange, byteRange2);
        assertEquals(byteRange2, byteRange2);
        assertTrue(byteRange.equals(byteRange));
        assertTrue(byteRange2.equals(byteRange2));
        assertTrue(byteRange3.equals(byteRange3));
        assertFalse(byteRange2.equals(byteRange3));
        assertFalse(byteRange2.equals(null));
        assertFalse(byteRange2.equals("Ni!"));
    }

// org.apache.commons.lang3.RangeTest::testHashCode
    public void testHashCode() {
        assertEquals(byteRange.hashCode(), byteRange2.hashCode());
        assertFalse(byteRange.hashCode() == byteRange3.hashCode());
        
        assertEquals(intRange.hashCode(), intRange.hashCode());
        assertTrue(intRange.hashCode() != 0);
    }

// org.apache.commons.lang3.RangeTest::testToString
    public void testToString() {
        assertNotNull(byteRange.toString());
        
        String str = intRange.toString();
        assertEquals("[10..20]", str);
        assertEquals("[-20..-10]", Range.between(-20, -10).toString());
    }

// org.apache.commons.lang3.RangeTest::testToStringFormat
    public void testToStringFormat() {
        String str = intRange.toString("From %1$s to %2$s");
        assertEquals("From 10 to 20", str);
    }

// org.apache.commons.lang3.RangeTest::testGetMinimum
    public void testGetMinimum() {
        assertEquals(10, (int) intRange.getMinimum());
        assertEquals(10L, (long) longRange.getMinimum());
        assertEquals(10f, floatRange.getMinimum(), 0.00001f);
        assertEquals(10d, doubleRange.getMinimum(), 0.00001d);
    }

// org.apache.commons.lang3.RangeTest::testGetMaximum
    public void testGetMaximum() {
        assertEquals(20, (int) intRange.getMaximum());
        assertEquals(20L, (long) longRange.getMaximum());
        assertEquals(20f, floatRange.getMaximum(), 0.00001f);
        assertEquals(20d, doubleRange.getMaximum(), 0.00001d);
    }

// org.apache.commons.lang3.RangeTest::testContains
    public void testContains() {
        assertFalse(intRange.contains(null));
        
        assertFalse(intRange.contains(5));
        assertTrue(intRange.contains(10));
        assertTrue(intRange.contains(15));
        assertTrue(intRange.contains(20));
        assertFalse(intRange.contains(25));
    }

// org.apache.commons.lang3.RangeTest::testIsAfter
    public void testIsAfter() {
        assertFalse(intRange.isAfter(null));
        
        assertTrue(intRange.isAfter(5));
        assertFalse(intRange.isAfter(10));
        assertFalse(intRange.isAfter(15));
        assertFalse(intRange.isAfter(20));
        assertFalse(intRange.isAfter(25));
    }

// org.apache.commons.lang3.RangeTest::testIsStartedBy
    public void testIsStartedBy() {
        assertFalse(intRange.isStartedBy(null));
        
        assertFalse(intRange.isStartedBy(5));
        assertTrue(intRange.isStartedBy(10));
        assertFalse(intRange.isStartedBy(15));
        assertFalse(intRange.isStartedBy(20));
        assertFalse(intRange.isStartedBy(25));
    }

// org.apache.commons.lang3.RangeTest::testIsEndedBy
    public void testIsEndedBy() {
        assertFalse(intRange.isEndedBy(null));
        
        assertFalse(intRange.isEndedBy(5));
        assertFalse(intRange.isEndedBy(10));
        assertFalse(intRange.isEndedBy(15));
        assertTrue(intRange.isEndedBy(20));
        assertFalse(intRange.isEndedBy(25));
    }

// org.apache.commons.lang3.RangeTest::testIsBefore
    public void testIsBefore() {
        assertFalse(intRange.isBefore(null));
        
        assertFalse(intRange.isBefore(5));
        assertFalse(intRange.isBefore(10));
        assertFalse(intRange.isBefore(15));
        assertFalse(intRange.isBefore(20));
        assertTrue(intRange.isBefore(25));
    }

// org.apache.commons.lang3.RangeTest::testElementCompareTo
    public void testElementCompareTo() {
        try {
            intRange.elementCompareTo(null);
            fail("NullPointerException should have been thrown");
        } catch(NullPointerException npe) {
            
        }
        
        assertEquals(-1, intRange.elementCompareTo(5));
        assertEquals(0, intRange.elementCompareTo(10));
        assertEquals(0, intRange.elementCompareTo(15));
        assertEquals(0, intRange.elementCompareTo(20));
        assertEquals(1, intRange.elementCompareTo(25));
    }

// org.apache.commons.lang3.RangeTest::testContainsRange
    public void testContainsRange() {

        
        assertFalse(intRange.containsRange(null));

        
        assertTrue(intRange.containsRange(Range.between(12, 18)));

        
        assertFalse(intRange.containsRange(Range.between(32, 45)));
        assertFalse(intRange.containsRange(Range.between(2, 8)));

        
        assertTrue(intRange.containsRange(Range.between(10, 20)));

        
        assertFalse(intRange.containsRange(Range.between(9, 14)));
        assertFalse(intRange.containsRange(Range.between(16, 21)));

        
        assertTrue(intRange.containsRange(Range.between(10, 19)));
        assertFalse(intRange.containsRange(Range.between(10, 21)));

        
        assertTrue(intRange.containsRange(Range.between(11, 20)));
        assertFalse(intRange.containsRange(Range.between(9, 20)));
        
        
        assertFalse(intRange.containsRange(Range.between(-11, -18)));
    }

// org.apache.commons.lang3.RangeTest::testIsAfterRange
    public void testIsAfterRange() {
        assertFalse(intRange.isAfterRange(null));
        
        assertTrue(intRange.isAfterRange(Range.between(5, 9)));
        
        assertFalse(intRange.isAfterRange(Range.between(5, 10)));
        assertFalse(intRange.isAfterRange(Range.between(5, 20)));
        assertFalse(intRange.isAfterRange(Range.between(5, 25)));
        assertFalse(intRange.isAfterRange(Range.between(15, 25)));
        
        assertFalse(intRange.isAfterRange(Range.between(21, 25)));
        
        assertFalse(intRange.isAfterRange(Range.between(10, 20)));
    }

// org.apache.commons.lang3.RangeTest::testIsOverlappedBy
    public void testIsOverlappedBy() {

        
        assertFalse(intRange.isOverlappedBy(null));

        
        assertTrue(intRange.isOverlappedBy(Range.between(12, 18)));

        
        assertFalse(intRange.isOverlappedBy(Range.between(32, 45)));
        assertFalse(intRange.isOverlappedBy(Range.between(2, 8)));

        
        assertTrue(intRange.isOverlappedBy(Range.between(10, 20)));

        
        assertTrue(intRange.isOverlappedBy(Range.between(9, 14)));
        assertTrue(intRange.isOverlappedBy(Range.between(16, 21)));

        
        assertTrue(intRange.isOverlappedBy(Range.between(10, 19)));
        assertTrue(intRange.isOverlappedBy(Range.between(10, 21)));

        
        assertTrue(intRange.isOverlappedBy(Range.between(11, 20)));
        assertTrue(intRange.isOverlappedBy(Range.between(9, 20)));
        
        
        assertFalse(intRange.isOverlappedBy(Range.between(-11, -18)));
    }

// org.apache.commons.lang3.RangeTest::testIsBeforeRange
    public void testIsBeforeRange() {
        assertFalse(intRange.isBeforeRange(null));
        
        assertFalse(intRange.isBeforeRange(Range.between(5, 9)));
        
        assertFalse(intRange.isBeforeRange(Range.between(5, 10)));
        assertFalse(intRange.isBeforeRange(Range.between(5, 20)));
        assertFalse(intRange.isBeforeRange(Range.between(5, 25)));
        assertFalse(intRange.isBeforeRange(Range.between(15, 25)));
        
        assertTrue(intRange.isBeforeRange(Range.between(21, 25)));
        
        assertFalse(intRange.isBeforeRange(Range.between(10, 20)));
    }

// org.apache.commons.lang3.RangeTest::testIntersectionWith
    public void testIntersectionWith() {
        assertSame(intRange, intRange.intersectionWith(intRange));
        assertSame(byteRange, byteRange.intersectionWith(byteRange));
        assertSame(longRange, longRange.intersectionWith(longRange));
        assertSame(floatRange, floatRange.intersectionWith(floatRange));
        assertSame(doubleRange, doubleRange.intersectionWith(doubleRange));

        assertEquals(Range.between(10, 15), intRange.intersectionWith(Range.between(5, 15)));
    }

// org.apache.commons.lang3.RangeTest::testIntersectionWithNull
    public void testIntersectionWithNull() {
        intRange.intersectionWith(null);
    }

// org.apache.commons.lang3.RangeTest::testIntersectionWithNonOverlapping
    public void testIntersectionWithNonOverlapping() {
        intRange.intersectionWith(Range.between(0, 9));
    }

// org.apache.commons.lang3.RangeTest::testSerializing
    public void testSerializing() {
        SerializationUtils.clone(intRange);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new SerializationUtils());
        Constructor<?>[] cons = SerializationUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(SerializationUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(SerializationUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.SerializationUtilsTest::testException
    public void testException() {
        SerializationException serEx;
        Exception ex = new Exception();
        
        serEx = new SerializationException();
        assertSame(null, serEx.getMessage());
        assertSame(null, serEx.getCause());
        
        serEx = new SerializationException("Message");
        assertSame("Message", serEx.getMessage());
        assertSame(null, serEx.getCause());
        
        serEx = new SerializationException(ex);
        assertEquals("java.lang.Exception", serEx.getMessage());
        assertSame(ex, serEx.getCause());
        
        serEx = new SerializationException("Message", ex);
        assertSame("Message", serEx.getMessage());
        assertSame(ex, serEx.getCause());
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeStream
    public void testSerializeStream() throws Exception {
        ByteArrayOutputStream streamTest = new ByteArrayOutputStream();
        SerializationUtils.serialize(iMap, streamTest);

        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(iMap);
        oos.flush();
        oos.close();

        byte[] testBytes = streamTest.toByteArray();
        byte[] realBytes = streamReal.toByteArray();
        assertEquals(testBytes.length, realBytes.length);
        for (int i = 0; i < realBytes.length; i++) {
            assertEquals(realBytes[i], testBytes[i]);
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeStreamUnserializable
    public void testSerializeStreamUnserializable() throws Exception {
        ByteArrayOutputStream streamTest = new ByteArrayOutputStream();
        try {
            iMap.put(new Object(), new Object());
            SerializationUtils.serialize(iMap, streamTest);
        } catch (SerializationException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeStreamNullObj
    public void testSerializeStreamNullObj() throws Exception {
        ByteArrayOutputStream streamTest = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, streamTest);

        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(null);
        oos.flush();
        oos.close();

        byte[] testBytes = streamTest.toByteArray();
        byte[] realBytes = streamReal.toByteArray();
        assertEquals(testBytes.length, realBytes.length);
        for (int i = 0; i < realBytes.length; i++) {
            assertEquals(realBytes[i], testBytes[i]);
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeStreamObjNull
    public void testSerializeStreamObjNull() throws Exception {
        try {
            SerializationUtils.serialize(iMap, null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeStreamNullNull
    public void testSerializeStreamNullNull() throws Exception {
        try {
            SerializationUtils.serialize(null, null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeIOException
    public void testSerializeIOException() throws Exception {
        
        
        OutputStream streamTest = new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
                throw new IOException(SERIALIZE_IO_EXCEPTION_MESSAGE);
            }
        };
        try {
            SerializationUtils.serialize(iMap, streamTest);
        }
        catch(SerializationException e) {
            assertEquals("java.io.IOException: " + SERIALIZE_IO_EXCEPTION_MESSAGE, e.getMessage());
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeStream
    public void testDeserializeStream() throws Exception {
        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(iMap);
        oos.flush();
        oos.close();

        ByteArrayInputStream inTest = new ByteArrayInputStream(streamReal.toByteArray());
        Object test = SerializationUtils.deserialize(inTest);
        assertNotNull(test);
        assertTrue(test instanceof HashMap<?, ?>);
        assertTrue(test != iMap);
        HashMap<?, ?> testMap = (HashMap<?, ?>) test;
        assertEquals(iString, testMap.get("FOO"));
        assertTrue(iString != testMap.get("FOO"));
        assertEquals(iInteger, testMap.get("BAR"));
        assertTrue(iInteger != testMap.get("BAR"));
        assertEquals(iMap, testMap);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeStreamOfNull
    public void testDeserializeStreamOfNull() throws Exception {
        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(null);
        oos.flush();
        oos.close();

        ByteArrayInputStream inTest = new ByteArrayInputStream(streamReal.toByteArray());
        Object test = SerializationUtils.deserialize(inTest);
        assertNull(test);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeStreamNull
    public void testDeserializeStreamNull() throws Exception {
        try {
            SerializationUtils.deserialize((InputStream) null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeStreamBadStream
    public void testDeserializeStreamBadStream() throws Exception {
        try {
            SerializationUtils.deserialize(new ByteArrayInputStream(new byte[0]));
        } catch (SerializationException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeStreamClassNotFound
    public void testDeserializeStreamClassNotFound() throws Exception {
        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(new ClassNotFoundSerialization());
        oos.flush();
        oos.close();

        ByteArrayInputStream inTest = new ByteArrayInputStream(streamReal.toByteArray());
        try {
            @SuppressWarnings("unused")
            Object test = SerializationUtils.deserialize(inTest);
        } catch(SerializationException se) {
            assertEquals("java.lang.ClassNotFoundException: " + CLASS_NOT_FOUND_MESSAGE, se.getMessage());
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeBytes
    public void testSerializeBytes() throws Exception {
        byte[] testBytes = SerializationUtils.serialize(iMap);

        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(iMap);
        oos.flush();
        oos.close();

        byte[] realBytes = streamReal.toByteArray();
        assertEquals(testBytes.length, realBytes.length);
        for (int i = 0; i < realBytes.length; i++) {
            assertEquals(realBytes[i], testBytes[i]);
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeBytesUnserializable
    public void testSerializeBytesUnserializable() throws Exception {
        try {
            iMap.put(new Object(), new Object());
            SerializationUtils.serialize(iMap);
        } catch (SerializationException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testSerializeBytesNull
    public void testSerializeBytesNull() throws Exception {
        byte[] testBytes = SerializationUtils.serialize(null);

        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(null);
        oos.flush();
        oos.close();

        byte[] realBytes = streamReal.toByteArray();
        assertEquals(testBytes.length, realBytes.length);
        for (int i = 0; i < realBytes.length; i++) {
            assertEquals(realBytes[i], testBytes[i]);
        }
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeBytes
    public void testDeserializeBytes() throws Exception {
        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(iMap);
        oos.flush();
        oos.close();

        Object test = SerializationUtils.deserialize(streamReal.toByteArray());
        assertNotNull(test);
        assertTrue(test instanceof HashMap<?, ?>);
        assertTrue(test != iMap);
        HashMap<?, ?> testMap = (HashMap<?, ?>) test;
        assertEquals(iString, testMap.get("FOO"));
        assertTrue(iString != testMap.get("FOO"));
        assertEquals(iInteger, testMap.get("BAR"));
        assertTrue(iInteger != testMap.get("BAR"));
        assertEquals(iMap, testMap);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeBytesOfNull
    public void testDeserializeBytesOfNull() throws Exception {
        ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(null);
        oos.flush();
        oos.close();

        Object test = SerializationUtils.deserialize(streamReal.toByteArray());
        assertNull(test);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeBytesNull
    public void testDeserializeBytesNull() throws Exception {
        try {
            SerializationUtils.deserialize((byte[]) null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testDeserializeBytesBadStream
    public void testDeserializeBytesBadStream() throws Exception {
        try {
            SerializationUtils.deserialize(new byte[0]);
        } catch (SerializationException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testClone
    public void testClone() throws Exception {
        Object test = SerializationUtils.clone(iMap);
        assertNotNull(test);
        assertTrue(test instanceof HashMap<?,?>);
        assertTrue(test != iMap);
        HashMap<?, ?> testMap = (HashMap<?, ?>) test;
        assertEquals(iString, testMap.get("FOO"));
        assertTrue(iString != testMap.get("FOO"));
        assertEquals(iInteger, testMap.get("BAR"));
        assertTrue(iInteger != testMap.get("BAR"));
        assertEquals(iMap, testMap);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testCloneNull
    public void testCloneNull() throws Exception {
        Object test = SerializationUtils.clone(null);
        assertNull(test);
    }

// org.apache.commons.lang3.SerializationUtilsTest::testCloneUnserializable
    public void testCloneUnserializable() throws Exception {
        try {
            iMap.put(new Object(), new Object());
            SerializationUtils.clone(iMap);
        } catch (SerializationException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang3.SerializationUtilsTest::testPrimitiveTypeClassSerialization
    public void testPrimitiveTypeClassSerialization() {
        Class<?>[] primitiveTypes = { byte.class, short.class, int.class, long.class, float.class, double.class,
                boolean.class, char.class, void.class };

        for (Class<?> primitiveType : primitiveTypes) {
            Class<?> clone = SerializationUtils.clone(primitiveType);
            assertEquals(primitiveType, clone);
        }
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedException
    public void testContextedException() {
        exceptionContext = new ContextedException();
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(StringUtils.isEmpty(message));
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionString
    public void testContextedExceptionString() {
        exceptionContext = new ContextedException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, exceptionContext.getMessage());
        
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionThrowable
    public void testContextedExceptionThrowable() {
        exceptionContext = new ContextedException(new Exception(TEST_MESSAGE));
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionStringThrowable
    public void testContextedExceptionStringThrowable() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE));
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testContextedExceptionStringThrowableContext
    public void testContextedExceptionStringThrowableContext() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testNullExceptionPassing
    public void testNullExceptionPassing() {
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), null)
        .addContextValue("test1", null)
        .addContextValue("test2", "some value")
        .addContextValue("test Date", new Date())
        .addContextValue("test Nbr", Integer.valueOf(5))
        .addContextValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = exceptionContext.getMessage();
        assertTrue(message != null);
    }

// org.apache.commons.lang3.exception.ContextedExceptionTest::testRawMessage
    public void testRawMessage() {
        assertEquals(Exception.class.getName() + ": " + TEST_MESSAGE, exceptionContext.getRawMessage());
        exceptionContext = new ContextedException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertEquals(TEST_MESSAGE_2, exceptionContext.getRawMessage());
        exceptionContext = new ContextedException(null, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertNull(exceptionContext.getRawMessage());
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedException
    public void testContextedException() {
        exceptionContext = new ContextedRuntimeException();
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(StringUtils.isEmpty(message));
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionString
    public void testContextedExceptionString() {
        exceptionContext = new ContextedRuntimeException(TEST_MESSAGE);
        assertEquals(TEST_MESSAGE, exceptionContext.getMessage());
        
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionThrowable
    public void testContextedExceptionThrowable() {
        exceptionContext = new ContextedRuntimeException(new Exception(TEST_MESSAGE));
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionStringThrowable
    public void testContextedExceptionStringThrowable() {
        exceptionContext = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE));
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testContextedExceptionStringThrowableContext
    public void testContextedExceptionStringThrowableContext() {
        exceptionContext = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext() {});
        String message = exceptionContext.getMessage();
        String trace = ExceptionUtils.getStackTrace(exceptionContext);
        assertTrue(trace.indexOf("ContextedException")>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE)>=0);
        assertTrue(trace.indexOf(TEST_MESSAGE_2)>=0);
        assertTrue(message.indexOf(TEST_MESSAGE_2)>=0);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testNullExceptionPassing
    public void testNullExceptionPassing() {
        exceptionContext = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), null)
        .addContextValue("test1", null)
        .addContextValue("test2", "some value")
        .addContextValue("test Date", new Date())
        .addContextValue("test Nbr", Integer.valueOf(5))
        .addContextValue("test Poorly written obj", new ObjectWithFaultyToString());
        
        String message = exceptionContext.getMessage();
        assertTrue(message != null);
    }

// org.apache.commons.lang3.exception.ContextedRuntimeExceptionTest::testRawMessage
    public void testRawMessage() {
        assertEquals(Exception.class.getName() + ": " + TEST_MESSAGE, exceptionContext.getRawMessage());
        exceptionContext = new ContextedRuntimeException(TEST_MESSAGE_2, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertEquals(TEST_MESSAGE_2, exceptionContext.getRawMessage());
        exceptionContext = new ContextedRuntimeException(null, new Exception(TEST_MESSAGE), new DefaultExceptionContext());
        assertNull(exceptionContext.getRawMessage());
    }

// org.apache.commons.lang3.exception.DefaultExceptionContextTest::testFormattedExceptionMessageNull
    public void testFormattedExceptionMessageNull() {
        exceptionContext = new DefaultExceptionContext();
        exceptionContext.getFormattedExceptionMessage(null);
    }

// org.apache.commons.lang3.time.FastDateFormat_ParserTest::testParseZone
    public void testParseZone() {}

// org.apache.commons.lang3.time.FastDateParserTest::test_Equality_Hash
    public void test_Equality_Hash() {        
        DateParser[] parsers= {
            getInstance(yMdHmsSZ, NEW_YORK, Locale.US),
            getInstance(DMY_DOT, NEW_YORK, Locale.US),
            getInstance(YMD_SLASH, NEW_YORK, Locale.US),
            getInstance(MDY_DASH, NEW_YORK, Locale.US),
            getInstance(MDY_SLASH, NEW_YORK, Locale.US),
            getInstance(MDY_SLASH, REYKJAVIK, Locale.US),
            getInstance(MDY_SLASH, REYKJAVIK, SWEDEN)
        };
        
        Map<DateParser,Integer> map= new HashMap<DateParser,Integer>();
        int i= 0;
        for(DateParser parser:parsers) {
            map.put(parser, i++);            
        }

        i= 0;
        for(DateParser parser:parsers) {
            assertEquals(i++, (int)map.get(parser));
        }        
    }

// org.apache.commons.lang3.time.FastDateParserTest::testParseZone
    public void testParseZone() {}

// org.apache.commons.lang3.time.FastDateParserTest::testParseLongShort
    public void testParseLongShort() throws ParseException {
        Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);        
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);
        cal.setTimeZone(NEW_YORK);
        
        DateParser fdf = getInstance("yyyy GGGG MMMM dddd aaaa EEEE HHHH mmmm ssss SSSS ZZZZ", NEW_YORK, Locale.US);
        
        assertEquals(cal.getTime(), fdf.parse("2003 AD February 0010 PM Monday 0015 0033 0020 0989 GMT-05:00"));
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        
        Date parse = fdf.parse("2003 BC February 0010 PM Saturday 0015 0033 0020 0989 GMT-05:00");
                assertEquals(cal.getTime(), parse);
                
        fdf = getInstance("y G M d a E H m s S Z", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("03 BC 2 10 PM Sat 15 33 20 989 -0500"));
        
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        assertEquals(cal.getTime(), fdf.parse("03 AD 2 10 PM Saturday 15 33 20 989 -0500"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testAmPm
    public void testAmPm() throws ParseException {
        Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        
        DateParser h = getInstance("yyyy-MM-dd hh a mm:ss", NEW_YORK, Locale.US);        
        DateParser K = getInstance("yyyy-MM-dd KK a mm:ss", NEW_YORK, Locale.US);        
        DateParser k = getInstance("yyyy-MM-dd kk:mm:ss", NEW_YORK, Locale.US);        
        DateParser H = getInstance("yyyy-MM-dd HH:mm:ss", NEW_YORK, Locale.US);        

        cal.set(2010, 7, 1, 0, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 12 AM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 0 AM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 00:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 00:33:20"));
        
        cal.set(2010, 7, 1, 3, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 3 AM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 3 AM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 03:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 03:33:20"));

        cal.set(2010, 7, 1, 15, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 3 PM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 3 PM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 15:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 15:33:20"));

        cal.set(2010, 7, 1, 12, 33, 20);
        assertEquals(cal.getTime(), h.parse("2010-08-01 12 PM 33:20"));
        assertEquals(cal.getTime(), K.parse("2010-08-01 0 PM 33:20"));
        assertEquals(cal.getTime(), k.parse("2010-08-01 12:33:20"));
        assertEquals(cal.getTime(), H.parse("2010-08-01 12:33:20"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testLocales
    public void testLocales() throws ParseException {
                
        for(Locale locale : Locale.getAvailableLocales()) {
            Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
            cal.clear();
            cal.set(2003, 1, 10);

            try {
                String longFormat= "GGGG/yyyy/MMMM/dddd/aaaa/EEEE/ZZZZ";
                SimpleDateFormat sdf = new SimpleDateFormat(longFormat, locale);
                DateParser fdf = getInstance(longFormat, locale);
                
                                checkParse(cal, sdf, fdf);
                
                cal.set(Calendar.ERA, GregorianCalendar.BC);
                                checkParse(cal, sdf, fdf);
                        
                String shortFormat= "G/y/M/d/a/E/Z";
                sdf = new SimpleDateFormat(shortFormat, locale);
                fdf = getInstance(shortFormat, locale);
                                checkParse(cal, sdf, fdf);
                
                cal.set(Calendar.ERA, GregorianCalendar.AD);
                                checkParse(cal, sdf, fdf);
            }
            catch(ParseException ex) {
                
                System.out.println("Locale "+locale+ " failed");
            }
        }
    }

// org.apache.commons.lang3.time.FastDateParserTest::testParseNumerics
    public void testParseNumerics() throws ParseException {
        Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);
        
        DateParser fdf = getInstance("yyyyMMddHHmmssSSS", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("20030210153320989"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testQuotes
    public void testQuotes() throws ParseException {
        Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10, 15, 33, 20);
        cal.set(Calendar.MILLISECOND, 989);
        
        DateParser fdf = getInstance("''yyyyMMdd'A''B'HHmmssSSS''", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("'20030210A'B153320989'"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testDayOf
    public void testDayOf() throws ParseException {
        Calendar cal= Calendar.getInstance(NEW_YORK, Locale.US);
        cal.clear();
        cal.set(2003, 1, 10);
        
        DateParser fdf = getInstance("W w F D y", NEW_YORK, Locale.US);
        assertEquals(cal.getTime(), fdf.parse("3 7 2 41 03"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testShortDateStyleWithLocales
    public void testShortDateStyleWithLocales() throws ParseException {
        DateParser fdf = getDateInstance(FastDateFormat.SHORT, Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        
        cal.set(2004, 1, 3);
        assertEquals(cal.getTime(), fdf.parse("2/3/04"));

        fdf = getDateInstance(FastDateFormat.SHORT, SWEDEN);
        assertEquals(cal.getTime(), fdf.parse("2004-02-03"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testLowYearPadding
    public void testLowYearPadding() throws ParseException {
        DateParser parser = getInstance(YMD_SLASH);
        Calendar cal = Calendar.getInstance();
        cal.clear();

        cal.set(1,0,1);
        assertEquals(cal.getTime(), parser.parse("0001/01/01"));
        cal.set(10,0,1);
        assertEquals(cal.getTime(), parser.parse("0010/01/01"));
        cal.set(100,0,1);
        assertEquals(cal.getTime(), parser.parse("0100/01/01"));
        cal.set(999,0,1);
        assertEquals(cal.getTime(), parser.parse("0999/01/01"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testMilleniumBug
    public void testMilleniumBug() throws ParseException {
        DateParser parser = getInstance(DMY_DOT);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        
        cal.set(1000,0,1);
        assertEquals(cal.getTime(), parser.parse("01.01.1000"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testLang303
    public void testLang303() throws ParseException {
        DateParser parser = getInstance(YMD_SLASH);
        Calendar cal = Calendar.getInstance();
        cal.set(2004,11,31);

        Date date = parser.parse("2004/11/31");

        parser = (DateParser) SerializationUtils.deserialize( SerializationUtils.serialize( (Serializable)parser ) );
        assertEquals(date, parser.parse("2004/11/31"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testLang538
    public void testLang538() throws ParseException {
        DateParser parser = getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        assertEquals(cal.getTime(), parser.parse("2009-10-16T16:42:16.000Z"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testEquals
    public void testEquals() {
        DateParser parser1= getInstance(YMD_SLASH);
        DateParser parser2= getInstance(YMD_SLASH);

        assertEquals(parser1, parser2);        
        assertEquals(parser1.hashCode(), parser2.hashCode());
        
        assertFalse(parser1.equals(new Object()));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testToStringContainsName
    public void testToStringContainsName() {
        DateParser parser= getInstance(YMD_SLASH);
        assertTrue(parser.toString().startsWith("FastDate"));
    }

// org.apache.commons.lang3.time.FastDateParserTest::testPatternMatches
    public void testPatternMatches() {
        DateParser parser= getInstance(yMdHmsSZ);
        assertEquals(yMdHmsSZ, parser.getPattern());
    }

// org.apache.commons.lang3.time.FastDateParserTest::testLocaleMatches
    public void testLocaleMatches() {
        DateParser parser= getInstance(yMdHmsSZ, SWEDEN);
        assertEquals(SWEDEN, parser.getLocale());
    }

// org.apache.commons.lang3.time.FastDateParserTest::testTimeZoneMatches
    public void testTimeZoneMatches() {
        DateParser parser= getInstance(yMdHmsSZ, REYKJAVIK);
        assertEquals(REYKJAVIK, parser.getTimeZone());
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testFormat
    public void testFormat() {
        Locale realDefaultLocale = Locale.getDefault();
        TimeZone realDefaultZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.US);
            TimeZone.setDefault(NEW_YORK);

            GregorianCalendar cal1 = new GregorianCalendar(2003, 0, 10, 15, 33, 20);
            GregorianCalendar cal2 = new GregorianCalendar(2003, 6, 10, 9, 00, 00);
            Date date1 = cal1.getTime();
            Date date2 = cal2.getTime();
            long millis1 = date1.getTime();
            long millis2 = date2.getTime();

            DatePrinter fdf = getInstance("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            assertEquals(sdf.format(date1), fdf.format(date1));
            assertEquals("2003-01-10T15:33:20", fdf.format(date1));
            assertEquals("2003-01-10T15:33:20", fdf.format(cal1));
            assertEquals("2003-01-10T15:33:20", fdf.format(millis1));
            assertEquals("2003-07-10T09:00:00", fdf.format(date2));
            assertEquals("2003-07-10T09:00:00", fdf.format(cal2));
            assertEquals("2003-07-10T09:00:00", fdf.format(millis2));

            fdf = getInstance("Z");
            assertEquals("-0500", fdf.format(date1));
            assertEquals("-0500", fdf.format(cal1));
            assertEquals("-0500", fdf.format(millis1));

            assertEquals("-0400", fdf.format(date2));
            assertEquals("-0400", fdf.format(cal2));
            assertEquals("-0400", fdf.format(millis2));

            fdf = getInstance("ZZ");
            assertEquals("-05:00", fdf.format(date1));
            assertEquals("-05:00", fdf.format(cal1));
            assertEquals("-05:00", fdf.format(millis1));

            assertEquals("-04:00", fdf.format(date2));
            assertEquals("-04:00", fdf.format(cal2));
            assertEquals("-04:00", fdf.format(millis2));

            String pattern = "GGGG GGG GG G yyyy yyy yy y MMMM MMM MM M" +
                " dddd ddd dd d DDDD DDD DD D EEEE EEE EE E aaaa aaa aa a zzzz zzz zz z";
            fdf = getInstance(pattern);
            sdf = new SimpleDateFormat(pattern);
            
            assertEquals(sdf.format(date1).replaceAll("2003 03 03 03", "2003 2003 03 2003"), fdf.format(date1));
            assertEquals(sdf.format(date2).replaceAll("2003 03 03 03", "2003 2003 03 2003"), fdf.format(date2));
        } finally {
            Locale.setDefault(realDefaultLocale);
            TimeZone.setDefault(realDefaultZone);
        }
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testShortDateStyleWithLocales
    public void testShortDateStyleWithLocales() {
        Locale usLocale = Locale.US;
        Locale swedishLocale = new Locale("sv", "SE");
        Calendar cal = Calendar.getInstance();
        cal.set(2004, 1, 3);
        DatePrinter fdf = getDateInstance(FastDateFormat.SHORT, usLocale);
        assertEquals("2/3/04", fdf.format(cal));

        fdf = getDateInstance(FastDateFormat.SHORT, swedishLocale);
        assertEquals("2004-02-03", fdf.format(cal));

    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testLowYearPadding
    public void testLowYearPadding() {
        Calendar cal = Calendar.getInstance();
        DatePrinter format = getInstance(YYYY_MM_DD);

        cal.set(1,0,1);
        assertEquals("0001/01/01", format.format(cal));
        cal.set(10,0,1);
        assertEquals("0010/01/01", format.format(cal));
        cal.set(100,0,1);
        assertEquals("0100/01/01", format.format(cal));
        cal.set(999,0,1);
        assertEquals("0999/01/01", format.format(cal));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testMilleniumBug
    public void testMilleniumBug() {
        Calendar cal = Calendar.getInstance();
        DatePrinter format = getInstance("dd.MM.yyyy");

        cal.set(1000,0,1);
        assertEquals("01.01.1000", format.format(cal));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testSimpleDate
    public void testSimpleDate() {
        Calendar cal = Calendar.getInstance();
        DatePrinter format = getInstance(YYYY_MM_DD);

        cal.set(2004,11,31);
        assertEquals("2004/12/31", format.format(cal));
        cal.set(999,11,31);
        assertEquals("0999/12/31", format.format(cal));
        cal.set(1,2,2);
        assertEquals("0001/03/02", format.format(cal));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testLang303
    public void testLang303() {
        Calendar cal = Calendar.getInstance();
        cal.set(2004,11,31);

        DatePrinter format = getInstance(YYYY_MM_DD);
        String output = format.format(cal);

        format = (DatePrinter) SerializationUtils.deserialize( SerializationUtils.serialize( (Serializable)format ) );
        assertEquals(output, format.format(cal));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testLang538
    public void testLang538() {
        
        
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        DatePrinter format = getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals("dateTime", "2009-10-16T16:42:16.000Z", format.format(cal.getTime()));
        assertEquals("dateTime", "2009-10-16T08:42:16.000Z", format.format(cal));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testLang645
    public void testLang645() {
        Locale locale = new Locale("sv", "SE");

        Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1, 12, 0, 0);
        Date d = cal.getTime();

        DatePrinter fdf = getInstance("EEEE', week 'ww", locale);

        assertEquals("fredag, week 53", fdf.format(d));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testEquals
    public void testEquals() {
        DatePrinter printer1= getInstance(YYYY_MM_DD);
        DatePrinter printer2= getInstance(YYYY_MM_DD);

        assertEquals(printer1, printer2);
        assertEquals(printer1.hashCode(), printer2.hashCode());        

        assertFalse(printer1.equals(new Object()));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testToStringContainsName
    public void testToStringContainsName() {
        DatePrinter printer= getInstance(YYYY_MM_DD);
        assertTrue(printer.toString().startsWith("FastDate"));
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testPatternMatches
    public void testPatternMatches() {
        DatePrinter printer= getInstance(YYYY_MM_DD);
        assertEquals(YYYY_MM_DD, printer.getPattern());
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testLocaleMatches
    public void testLocaleMatches() {
        DatePrinter printer= getInstance(YYYY_MM_DD, SWEDEN);
        assertEquals(SWEDEN, printer.getLocale());
    }

// org.apache.commons.lang3.time.FastDatePrinterTest::testTimeZoneMatches
    public void testTimeZoneMatches() {
        DatePrinter printer= getInstance(YYYY_MM_DD, NEW_YORK);
        assertEquals(NEW_YORK, printer.getTimeZone());
    }
