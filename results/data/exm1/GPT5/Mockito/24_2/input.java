// buggy code
    public Object answer(InvocationOnMock invocation) {
        if (methodsGuru.isToString(invocation.getMethod())) {
            Object mock = invocation.getMock();
            MockName name = mockUtil.getMockName(mock);
            if (name.isDefault()) {
                return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
            } else {
                return name.toString();
            }
        } else if (methodsGuru.isCompareToMethod(invocation.getMethod())) {
            //see issue 184.
            //mocks by default should return 0 if references are the same, otherwise some other value because they are not the same. Hence we return 1 (anything but 0 is good).
            //Only for compareTo() method by the Comparable interface
            return 1;
        }
        
        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnValueFor(returnType);
    }

// relevant test
// org.mockitousage.matchers.MatchersMixedWithRawArgumentsTest::shouldAllowMixingRawArgumentsWithMatchers
    public void shouldAllowMixingRawArgumentsWithMatchers() {
        mock.varargs("1", "2", "3");
        verify(mock).varargs("1", anyString(), "3");
        
        verify(mock).varargs(anyBoolean(), false);
    }

// org.mockitousage.matchers.MatchersTest::andOverloaded
    public void andOverloaded() {
        when(mock.oneArg(and(eq(false), eq(false)))).thenReturn("0");
        when(mock.oneArg(and(eq((byte) 1), eq((byte) 1)))).thenReturn("1");
        when(mock.oneArg(and(eq('a'), eq('a')))).thenReturn("2");
        when(mock.oneArg(and(eq((double) 1), eq((double) 1)))).thenReturn("3");
        when(mock.oneArg(and(eq((float) 1), eq((float) 1)))).thenReturn("4");
        when(mock.oneArg(and(eq((int) 1), eq((int) 1)))).thenReturn("5");
        when(mock.oneArg(and(eq((long) 1), eq((long) 1)))).thenReturn("6");
        when(mock.oneArg(and(eq((short) 1), eq((short) 1)))).thenReturn("7");
        when(mock.oneArg(and(Matchers.contains("a"), Matchers.contains("d")))).thenReturn("8");
        when(mock.oneArg(and(isA(Class.class), eq(Object.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(false));
        assertEquals(null, mock.oneArg(true));

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals("2", mock.oneArg('a'));
        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("4", mock.oneArg((float) 1));
        assertEquals("5", mock.oneArg((int) 1));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));

        assertEquals("8", mock.oneArg("abcde"));
        assertEquals(null, mock.oneArg("aaaaa"));

        assertEquals("9", mock.oneArg(Object.class));
    }

// org.mockitousage.matchers.MatchersTest::orOverloaded
    public void orOverloaded() {
        when(mock.oneArg(or(eq(false), eq(true)))).thenReturn("0");
        when(mock.oneArg(or(eq((byte) 1), eq((byte) 2)))).thenReturn("1");
        when(mock.oneArg(or(eq((char) 1), eq((char) 2)))).thenReturn("2");
        when(mock.oneArg(or(eq((double) 1), eq((double) 2)))).thenReturn("3");
        when(mock.oneArg(or(eq((float) 1), eq((float) 2)))).thenReturn("4");
        when(mock.oneArg(or(eq((int) 1), eq((int) 2)))).thenReturn("5");
        when(mock.oneArg(or(eq((long) 1), eq((long) 2)))).thenReturn("6");
        when(mock.oneArg(or(eq((short) 1), eq((short) 2)))).thenReturn("7");
        when(mock.oneArg(or(eq("asd"), eq("jkl")))).thenReturn("8");
        when(mock.oneArg(or(eq(this.getClass()), eq(Object.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(true));
        assertEquals("0", mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals("2", mock.oneArg((char) 1));
        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("4", mock.oneArg((float) 1));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));

        assertEquals("8", mock.oneArg("jkl"));
        assertEquals("8", mock.oneArg("asd"));
        assertEquals(null, mock.oneArg("asdjkl"));

        assertEquals("9", mock.oneArg(Object.class));
        assertEquals(null, mock.oneArg(String.class));
    }

// org.mockitousage.matchers.MatchersTest::notOverloaded
    public void notOverloaded() {
        when(mock.oneArg(not(eq(false)))).thenReturn("0");
        when(mock.oneArg(not(eq((byte) 1)))).thenReturn("1");
        when(mock.oneArg(not(eq('a')))).thenReturn("2");
        when(mock.oneArg(not(eq((double) 1)))).thenReturn("3");
        when(mock.oneArg(not(eq((float) 1)))).thenReturn("4");
        when(mock.oneArg(not(eq((int) 1)))).thenReturn("5");
        when(mock.oneArg(not(eq((long) 1)))).thenReturn("6");
        when(mock.oneArg(not(eq((short) 1)))).thenReturn("7");
        when(mock.oneArg(not(Matchers.contains("a")))).thenReturn("8");
        when(mock.oneArg(not(isA(Class.class)))).thenReturn("9");

        assertEquals("0", mock.oneArg(true));
        assertEquals(null, mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals("2", mock.oneArg('b'));
        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("4", mock.oneArg((float) 2));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 2));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("8", mock.oneArg("bcde"));

        assertEquals("9", mock.oneArg(new Object()));
        assertEquals(null, mock.oneArg(Class.class));
    }

// org.mockitousage.matchers.MatchersTest::lessOrEqualOverloaded
    public void lessOrEqualOverloaded() {
        when(mock.oneArg(leq((byte) 1))).thenReturn("1");
        when(mock.oneArg(leq((double) 1))).thenReturn("3");
        when(mock.oneArg(leq((float) 1))).thenReturn("4");
        when(mock.oneArg(leq((int) 1))).thenReturn("5");
        when(mock.oneArg(leq((long) 1))).thenReturn("6");
        when(mock.oneArg(leq((short) 1))).thenReturn("7");
        when(mock.oneArg(leq(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals(null, mock.oneArg((byte) 2));

        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("7", mock.oneArg((short) 0));
        assertEquals("4", mock.oneArg((float) -5));
        assertEquals("5", mock.oneArg((int) -2));
        assertEquals("6", mock.oneArg((long) -3));

        assertEquals("8", mock.oneArg(new BigDecimal("0.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("1.1")));
    }

// org.mockitousage.matchers.MatchersTest::lessThanOverloaded
    public void lessThanOverloaded() {
        when(mock.oneArg(lt((byte) 1))).thenReturn("1");
        when(mock.oneArg(lt((double) 1))).thenReturn("3");
        when(mock.oneArg(lt((float) 1))).thenReturn("4");
        when(mock.oneArg(lt((int) 1))).thenReturn("5");
        when(mock.oneArg(lt((long) 1))).thenReturn("6");
        when(mock.oneArg(lt((short) 1))).thenReturn("7");
        when(mock.oneArg(lt(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 0));
        assertEquals(null, mock.oneArg((byte) 1));

        assertEquals("3", mock.oneArg((double) 0));
        assertEquals("7", mock.oneArg((short) 0));
        assertEquals("4", mock.oneArg((float) -4));
        assertEquals("5", mock.oneArg((int) -34));
        assertEquals("6", mock.oneArg((long) -6));

        assertEquals("8", mock.oneArg(new BigDecimal("0.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("23")));
    }

// org.mockitousage.matchers.MatchersTest::greaterOrEqualMatcherOverloaded
    public void greaterOrEqualMatcherOverloaded() {
        when(mock.oneArg(geq((byte) 1))).thenReturn("1");
        when(mock.oneArg(geq((double) 1))).thenReturn("3");
        when(mock.oneArg(geq((float) 1))).thenReturn("4");
        when(mock.oneArg(geq((int) 1))).thenReturn("5");
        when(mock.oneArg(geq((long) 1))).thenReturn("6");
        when(mock.oneArg(geq((short) 1))).thenReturn("7");
        when(mock.oneArg(geq(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals(null, mock.oneArg((byte) 0));

        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("4", mock.oneArg((float) 3));
        assertEquals("5", mock.oneArg((int) 4));
        assertEquals("6", mock.oneArg((long) 5));

        assertEquals("8", mock.oneArg(new BigDecimal("1.00")));
        assertEquals(null, mock.oneArg(new BigDecimal("0.9")));
    }

// org.mockitousage.matchers.MatchersTest::greaterThanMatcherOverloaded
    public void greaterThanMatcherOverloaded() {
        when(mock.oneArg(gt((byte) 1))).thenReturn("1");
        when(mock.oneArg(gt((double) 1))).thenReturn("3");
        when(mock.oneArg(gt((float) 1))).thenReturn("4");
        when(mock.oneArg(gt((int) 1))).thenReturn("5");
        when(mock.oneArg(gt((long) 1))).thenReturn("6");
        when(mock.oneArg(gt((short) 1))).thenReturn("7");
        when(mock.oneArg(gt(new BigDecimal("1")))).thenReturn("8");

        assertEquals("1", mock.oneArg((byte) 2));
        assertEquals(null, mock.oneArg((byte) 1));

        assertEquals("3", mock.oneArg((double) 2));
        assertEquals("7", mock.oneArg((short) 2));
        assertEquals("4", mock.oneArg((float) 3));
        assertEquals("5", mock.oneArg((int) 2));
        assertEquals("6", mock.oneArg((long) 5));

        assertEquals("8", mock.oneArg(new BigDecimal("1.5")));
        assertEquals(null, mock.oneArg(new BigDecimal("0.9")));
    }

// org.mockitousage.matchers.MatchersTest::compareToMatcher
    public void compareToMatcher() {
        when(mock.oneArg(cmpEq(new BigDecimal("1.5")))).thenReturn("0");

        assertEquals("0", mock.oneArg(new BigDecimal("1.50")));
        assertEquals(null, mock.oneArg(new BigDecimal("1.51")));
    }

// org.mockitousage.matchers.MatchersTest::anyStringMatcher
    public void anyStringMatcher() {
        when(mock.oneArg(anyString())).thenReturn("1");
        
        assertEquals("1", mock.oneArg(""));
        assertEquals("1", mock.oneArg("any string"));
        assertEquals(null, mock.oneArg((Object) null));
    }

// org.mockitousage.matchers.MatchersTest::anyMatcher
    public void anyMatcher() {
        when(mock.oneArg(anyBoolean())).thenReturn("0");
        when(mock.oneArg(anyByte())).thenReturn("1");
        when(mock.oneArg(anyChar())).thenReturn("2");
        when(mock.oneArg(anyDouble())).thenReturn("3");
        when(mock.oneArg(anyFloat())).thenReturn("4");
        when(mock.oneArg(anyInt())).thenReturn("5");
        when(mock.oneArg(anyLong())).thenReturn("6");
        when(mock.oneArg(anyShort())).thenReturn("7");
        when(mock.oneArg((String) anyObject())).thenReturn("8");
        when(mock.oneArg(anyObject())).thenReturn("9");
        
        assertEquals("0", mock.oneArg(true));
        assertEquals("0", mock.oneArg(false));

        assertEquals("1", mock.oneArg((byte) 1));
        assertEquals("2", mock.oneArg((char) 1));
        assertEquals("3", mock.oneArg((double) 1));
        assertEquals("4", mock.oneArg((float) 889));
        assertEquals("5", mock.oneArg((int) 1));
        assertEquals("6", mock.oneArg((long) 1));
        assertEquals("7", mock.oneArg((short) 1));
        assertEquals("8", mock.oneArg("Test"));

        assertEquals("9", mock.oneArg(new Object()));
        assertEquals("9", mock.oneArg(new HashMap()));
    }

// org.mockitousage.matchers.MatchersTest::shouldArrayEqualsDealWithNullArray
    public void shouldArrayEqualsDealWithNullArray() throws Exception {
        Object[] nullArray = null;
        when(mock.oneArray(aryEq(nullArray))).thenReturn("null");

        assertEquals("null", mock.oneArray(nullArray));

        mock = mock(IMethods.class);

        try {
            verify(mock).oneArray(aryEq(nullArray));
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("oneArray(null)", e.getMessage());
        }
    }

// org.mockitousage.matchers.MatchersTest::shouldUseSmartEqualsForArrays
    public void shouldUseSmartEqualsForArrays() throws Exception {
        
        mock.arrayMethod(new String[] {"one"});
        verify(mock).arrayMethod(eq(new String[] {"one"}));
        verify(mock).arrayMethod(new String[] {"one"});
    }

// org.mockitousage.matchers.MatchersTest::shouldUseSmartEqualsForPrimitiveArrays
    public void shouldUseSmartEqualsForPrimitiveArrays() throws Exception {
        
        mock.objectArgMethod(new int[] {1, 2});
        verify(mock).objectArgMethod(eq(new int[] {1, 2}));
        verify(mock).objectArgMethod(new int[] {1, 2});
    }

// org.mockitousage.matchers.MatchersTest::arrayEqualsShouldThrowArgumentsAreDifferentExceptionForNonMatchingArguments
    public void arrayEqualsShouldThrowArgumentsAreDifferentExceptionForNonMatchingArguments() {        
        List list = Mockito.mock(List.class);
        
        list.add("test"); 
        list.contains(new Object[] {"1"});
        
        Mockito.verify(list).contains(new Object[] {"1", "2", "3"});    
    }

// org.mockitousage.matchers.MatchersTest::arrayEqualsMatcher
    public void arrayEqualsMatcher() {
        when(mock.oneArray(aryEq(new boolean[] { true, false, false }))).thenReturn("0");
        when(mock.oneArray(aryEq(new byte[] { 1 }))).thenReturn("1");
        when(mock.oneArray(aryEq(new char[] { 1 }))).thenReturn("2");
        when(mock.oneArray(aryEq(new double[] { 1 }))).thenReturn("3");
        when(mock.oneArray(aryEq(new float[] { 1 }))).thenReturn("4");
        when(mock.oneArray(aryEq(new int[] { 1 }))).thenReturn("5");
        when(mock.oneArray(aryEq(new long[] { 1 }))).thenReturn("6");
        when(mock.oneArray(aryEq(new short[] { 1 }))).thenReturn("7");
        when(mock.oneArray(aryEq(new String[] { "Test" }))).thenReturn("8");
        when(mock.oneArray(aryEq(new Object[] { "Test", new Integer(4) }))).thenReturn("9");

        assertEquals("0", mock.oneArray(new boolean[] { true, false, false }));
        assertEquals("1", mock.oneArray(new byte[] { 1 }));
        assertEquals("2", mock.oneArray(new char[] { 1 }));
        assertEquals("3", mock.oneArray(new double[] { 1 }));
        assertEquals("4", mock.oneArray(new float[] { 1 }));
        assertEquals("5", mock.oneArray(new int[] { 1 }));
        assertEquals("6", mock.oneArray(new long[] { 1 }));
        assertEquals("7", mock.oneArray(new short[] { 1 }));
        assertEquals("8", mock.oneArray(new String[] { "Test" }));
        assertEquals("9", mock.oneArray(new Object[] { "Test", new Integer(4) }));

        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(999) }));
        assertEquals(null, mock.oneArray(new Object[] { "Test", new Integer(4), "x" }));

        assertEquals(null, mock.oneArray(new boolean[] { true, false }));
        assertEquals(null, mock.oneArray(new boolean[] { true, true, false }));
    }

// org.mockitousage.matchers.MatchersTest::greaterOrEqualMatcher
    public void greaterOrEqualMatcher() {
        when(mock.oneArg(geq(7))).thenReturn(">= 7");
        when(mock.oneArg(lt(7))).thenReturn("< 7");

        assertEquals(">= 7", mock.oneArg(7));
        assertEquals(">= 7", mock.oneArg(8));
        assertEquals(">= 7", mock.oneArg(9));

        assertEquals("< 7", mock.oneArg(6));
        assertEquals("< 7", mock.oneArg(6));
    }

// org.mockitousage.matchers.MatchersTest::greaterThanMatcher
    public void greaterThanMatcher() {
        when(mock.oneArg(gt(7))).thenReturn("> 7");
        when(mock.oneArg(leq(7))).thenReturn("<= 7");

        assertEquals("> 7", mock.oneArg(8));
        assertEquals("> 7", mock.oneArg(9));
        assertEquals("> 7", mock.oneArg(10));

        assertEquals("<= 7", mock.oneArg(7));
        assertEquals("<= 7", mock.oneArg(6));
    }

// org.mockitousage.matchers.MatchersTest::lessOrEqualMatcher
    public void lessOrEqualMatcher() {
        when(mock.oneArg(leq(7))).thenReturn("<= 7");
        when(mock.oneArg(gt(7))).thenReturn("> 7");

        assertEquals("<= 7", mock.oneArg(7));
        assertEquals("<= 7", mock.oneArg(6));
        assertEquals("<= 7", mock.oneArg(5));

        assertEquals("> 7", mock.oneArg(8));
        assertEquals("> 7", mock.oneArg(9));
    }

// org.mockitousage.matchers.MatchersTest::lessThanMatcher
    public void lessThanMatcher() {
        when(mock.oneArg(lt(7))).thenReturn("< 7");
        when(mock.oneArg(geq(7))).thenReturn(">= 7");

        assertEquals("< 7", mock.oneArg(5));
        assertEquals("< 7", mock.oneArg(6));
        assertEquals("< 7", mock.oneArg(4));

        assertEquals(">= 7", mock.oneArg(7));
        assertEquals(">= 7", mock.oneArg(8));
    }

// org.mockitousage.matchers.MatchersTest::orMatcher
    public void orMatcher() {
        when(mock.oneArg(anyInt())).thenReturn("other");
        when(mock.oneArg(or(eq(7), eq(9)))).thenReturn("7 or 9");

        assertEquals("other", mock.oneArg(10));
        assertEquals("7 or 9", mock.oneArg(7));
        assertEquals("7 or 9", mock.oneArg(9));
    }

// org.mockitousage.matchers.MatchersTest::nullMatcher
    public void nullMatcher() {
        when(mock.threeArgumentMethod(eq(1), isNull(), eq(""))).thenReturn("1");
        when(mock.threeArgumentMethod(eq(1), not(isNull()), eq(""))).thenReturn("2");

        assertEquals("1", mock.threeArgumentMethod(1, null, ""));
        assertEquals("2", mock.threeArgumentMethod(1, new Object(), ""));
    }

// org.mockitousage.matchers.MatchersTest::notNullMatcher
    public void notNullMatcher() {
        when(mock.threeArgumentMethod(eq(1), notNull(), eq(""))).thenReturn("1");
        when(mock.threeArgumentMethod(eq(1), not(isNotNull()), eq(""))).thenReturn("2");

        assertEquals("1", mock.threeArgumentMethod(1, new Object(), ""));
        assertEquals("2", mock.threeArgumentMethod(1, null, ""));
    }

// org.mockitousage.matchers.MatchersTest::findMatcher
    public void findMatcher() {
        when(mock.oneArg(find("([a-z]+)\\d"))).thenReturn("1");

        assertEquals("1", mock.oneArg("ab12"));
        assertEquals(null, mock.oneArg("12345"));
        assertEquals(null, mock.oneArg((Object) null));
    }

// org.mockitousage.matchers.MatchersTest::matchesMatcher
    public void matchesMatcher() {
        when(mock.oneArg(matches("[a-z]+\\d\\d"))).thenReturn("1");
        when(mock.oneArg(matches("\\d\\d\\d"))).thenReturn("2");

        assertEquals("1", mock.oneArg("a12"));
        assertEquals("2", mock.oneArg("131"));
        assertEquals(null, mock.oneArg("blah"));
    }

// org.mockitousage.matchers.MatchersTest::containsMatcher
    public void containsMatcher() {
        when(mock.oneArg(Matchers.contains("ell"))).thenReturn("1");
        when(mock.oneArg(Matchers.contains("ld"))).thenReturn("2");

        assertEquals("1", mock.oneArg("hello"));
        assertEquals("2", mock.oneArg("world"));
        assertEquals(null, mock.oneArg("xlx"));
    }

// org.mockitousage.matchers.MatchersTest::startsWithMatcher
    public void startsWithMatcher() {
        when(mock.oneArg(startsWith("ab"))).thenReturn("1");
        when(mock.oneArg(startsWith("bc"))).thenReturn("2");

        assertEquals("1", mock.oneArg("ab quake"));
        assertEquals("2", mock.oneArg("bc quake"));
        assertEquals(null, mock.oneArg("ba quake"));
    }

// org.mockitousage.matchers.MatchersTest::endsWithMatcher
    public void endsWithMatcher() {
        when(mock.oneArg(Matchers.endsWith("ab"))).thenReturn("1");
        when(mock.oneArg(Matchers.endsWith("bc"))).thenReturn("2");

        assertEquals("1", mock.oneArg("xab"));
        assertEquals("2", mock.oneArg("xbc"));
        assertEquals(null, mock.oneArg("ac"));
    }

// org.mockitousage.matchers.MatchersTest::deltaMatcher
    public void deltaMatcher() {
        when(mock.oneArg(eq(1.0D, 0.1D))).thenReturn("1");
        when(mock.oneArg(eq(2.0D, 0.1D))).thenReturn("2");
        when(mock.oneArg(eq(1.0F, 0.1F))).thenReturn("3");
        when(mock.oneArg(eq(2.0F, 0.1F))).thenReturn("4");
        when(mock.oneArg(eq(2.0F, 0.1F))).thenReturn("4");

        assertEquals("1", mock.oneArg(1.0));
        assertEquals("1", mock.oneArg(0.91));
        assertEquals("1", mock.oneArg(1.09));
        assertEquals("2", mock.oneArg(2.0));

        assertEquals("3", mock.oneArg(1.0F));
        assertEquals("3", mock.oneArg(0.91F));
        assertEquals("3", mock.oneArg(1.09F));
        assertEquals("4", mock.oneArg(2.1F));

        assertEquals(null, mock.oneArg(2.2F));
    }

// org.mockitousage.matchers.MatchersTest::deltaMatcherPrintsItself
    public void deltaMatcherPrintsItself() {
        try {
            verify(mock).oneArg(eq(1.0D, 0.1D));
            fail();
        } catch (WantedButNotInvoked e) {
            assertContains("eq(1.0, 0.1)", e.getMessage());
        }
    }

// org.mockitousage.matchers.MatchersTest::sameMatcher
    public void sameMatcher() {
        Object one = new String("1243");
        Object two = new String("1243");
        Object three = new String("1243");

        assertNotSame(one, two);
        assertEquals(one, two);
        assertEquals(two, three);

        when(mock.oneArg(same(one))).thenReturn("1");
        when(mock.oneArg(same(two))).thenReturn("2");

        assertEquals("1", mock.oneArg(one));
        assertEquals("2", mock.oneArg(two));
        assertEquals(null, mock.oneArg(three));
    }

// org.mockitousage.matchers.MatchersTest::eqMatcherAndNulls
    public void eqMatcherAndNulls() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod((Object) eq(null));
    }

// org.mockitousage.matchers.MatchersTest::sameMatcherAndNulls
    public void sameMatcherAndNulls() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod(same(null));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCasting
    public void shouldHelpOutWithUnnecessaryCasting() {
        when(mock.objectArgMethod(any(String.class))).thenReturn("string");

        assertEquals("string", mock.objectArgMethod("foo"));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldAnyBeActualAliasToAnyObject
    public void shouldAnyBeActualAliasToAnyObject() {
        mock.simpleMethod((Object) null);

        verify(mock).simpleMethod(anyObject());
        verify(mock).simpleMethod(any(Object.class));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfLists
    public void shouldHelpOutWithUnnecessaryCastingOfLists() {
        
        
        when(mock.listArgMethod(anyListOf(String.class))).thenReturn("list");

        assertEquals("list", mock.listArgMethod(new LinkedList<String>()));
        assertEquals("list", mock.listArgMethod(Collections.<String>emptyList()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfSets
    public void shouldHelpOutWithUnnecessaryCastingOfSets() {
        
        
        when(mock.setArgMethod(anySetOf(String.class))).thenReturn("set");

        assertEquals("set", mock.setArgMethod(new HashSet<String>()));
        assertEquals("set", mock.setArgMethod(Collections.<String>emptySet()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfMaps
    public void shouldHelpOutWithUnnecessaryCastingOfMaps() {
        
        
        when(mock.forMap(anyMapOf(String.class, String.class))).thenReturn("map");

        assertEquals("map", mock.forMap(new HashMap<String, String>()));
        assertEquals("map", mock.forMap(Collections.<String, String>emptyMap()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfCollections
    public void shouldHelpOutWithUnnecessaryCastingOfCollections() {
        
        
        when(mock.collectionArgMethod(anyCollectionOf(String.class))).thenReturn("col");

        assertEquals("col", mock.collectionArgMethod(new ArrayList<String>()));
        assertEquals("col", mock.collectionArgMethod(Collections.<String>emptyList()));
    }

// org.mockitousage.matchers.MoreMatchersTest::shouldHelpOutWithUnnecessaryCastingOfNullityChecks
    public void shouldHelpOutWithUnnecessaryCastingOfNullityChecks() {
        when(mock.objectArgMethod(isNull(LinkedList.class))).thenReturn("string");
        when(mock.objectArgMethod(notNull(LinkedList.class))).thenReturn("string");
        when(mock.objectArgMethod(isNotNull(LinkedList.class))).thenReturn("string");

        assertEquals("string", mock.objectArgMethod(null));
        assertEquals("string", mock.objectArgMethod("foo"));
        assertEquals("string", mock.objectArgMethod("foo"));
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyList
    public void shouldAllowAnyList() {
        when(mock.forList(anyList())).thenReturn("x");
        
        assertEquals("x", mock.forList(null));
        assertEquals("x", mock.forList(Arrays.asList("x", "y")));
        
        verify(mock, times(2)).forList(anyList());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyCollection
    public void shouldAllowAnyCollection() {
        when(mock.forCollection(anyCollection())).thenReturn("x");
        
        assertEquals("x", mock.forCollection(null));
        assertEquals("x", mock.forCollection(Arrays.asList("x", "y")));
        
        verify(mock, times(2)).forCollection(anyCollection());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnyMap
    public void shouldAllowAnyMap() {
        when(mock.forMap(anyMap())).thenReturn("x");
        
        assertEquals("x", mock.forMap(null));
        assertEquals("x", mock.forMap(new HashMap<String, String>()));
        
        verify(mock, times(2)).forMap(anyMap());
    }

// org.mockitousage.matchers.NewMatchersTest::shouldAllowAnySet
    public void shouldAllowAnySet() {
        when(mock.forSet(anySet())).thenReturn("x");
        
        assertEquals("x", mock.forSet(null));
        assertEquals("x", mock.forSet(new HashSet<String>()));
        
        verify(mock, times(2)).forSet(anySet());
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqual
    public void shouldMatchWhenFieldValuesEqual() throws Exception {
        Child wanted = new Child(1, "foo", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchWhenFieldValuesDiffer
    public void shouldNotMatchWhenFieldValuesDiffer() throws Exception {
        Child wanted = new Child(1, "foo", 2, "bar XXX");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchAgain
    public void shouldNotMatchAgain() throws Exception {
        Child wanted = new Child(1, "foo", 999, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchYetAgain
    public void shouldNotMatchYetAgain() throws Exception {
        Child wanted = new Child(1, "XXXXX", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatch
    public void shouldNotMatch() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "bar");
        verify(mock).run(refEq(wanted));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqualWithOneFieldExcluded
    public void shouldMatchWhenFieldValuesEqualWithOneFieldExcluded() throws Exception {
        Child wanted = new Child(1, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo"));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldMatchWhenFieldValuesEqualWithTwoFieldsExcluded
    public void shouldMatchWhenFieldValuesEqualWithTwoFieldsExcluded() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo", "parentField"));
        verify(mock).run(refEq(wanted, "parentField", "childFieldTwo"));
    }

// org.mockitousage.matchers.ReflectionMatchersTest::shouldNotMatchWithFieldsExclusion
    public void shouldNotMatchWithFieldsExclusion() throws Exception {
        Child wanted = new Child(234234, "foo", 2, "excluded");
        verify(mock).run(refEq(wanted, "childFieldTwo"));
    }

// org.mockitousage.matchers.VerificationAndStubbingUsingMatchersTest::shouldStubUsingMatchers
    public void shouldStubUsingMatchers() {
        when(one.simpleMethod(2)).thenReturn("2");
        when(two.simpleMethod(anyString())).thenReturn("any");
        when(three.simpleMethod(startsWith("test"))).thenThrow(new RuntimeException());

        assertEquals(null, one.simpleMethod(1));
        assertEquals("2", one.simpleMethod(2));
        
        assertEquals("any", two.simpleMethod("two"));
        assertEquals("any", two.simpleMethod("two again"));
        
        assertEquals(null, three.simpleMethod("three"));
        assertEquals(null, three.simpleMethod("three again"));
       
        try {
            three.simpleMethod("test three again");
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.matchers.VerificationAndStubbingUsingMatchersTest::shouldVerifyUsingMatchers
    public void shouldVerifyUsingMatchers() {
        stubVoid(one).toThrow(new RuntimeException()).on().oneArg(true);
        when(three.varargsObject(5, "first arg", "second arg")).thenReturn("stubbed");

        try {
            one.oneArg(true);
            fail();
        } catch (RuntimeException e) {}

        one.simpleMethod(100);
        two.simpleMethod("test Mockito");
        three.varargsObject(10, "first arg", "second arg");
        
        assertEquals("stubbed", three.varargsObject(5, "first arg", "second arg"));

        verify(one).oneArg(eq(true));
        verify(one).simpleMethod(anyInt());
        verify(two).simpleMethod(startsWith("test"));
        verify(three).varargsObject(5, "first arg", "second arg");
        verify(three).varargsObject(eq(10), eq("first arg"), startsWith("second"));
        
        verifyNoMoreInteractions(one, two, three);
        
        try {
            verify(three).varargsObject(eq(10), eq("first arg"), startsWith("third"));
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnVerify
    public void shouldResetOngoingStubbingOnVerify() {
        
        mock.booleanReturningMethod();
        verify(mock).booleanReturningMethod();
        
        
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnInOrder
    public void shouldResetOngoingStubbingOnInOrder() {
        mock.booleanReturningMethod();
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).booleanReturningMethod();
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.CleaningUpPotentialStubbingTest::shouldResetOngoingStubbingOnDoReturn
    public void shouldResetOngoingStubbingOnDoReturn() {
        mock.booleanReturningMethod();
        doReturn(false).when(mock).booleanReturningMethod();
        assertOngoingStubbingIsReset();
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::tryDescriptiveMessagesOnMisuse
    public void tryDescriptiveMessagesOnMisuse() {
        Foo foo = mock(Foo.class);
        

        

        

    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenWholeMethodPassedToVerify
    public void shouldScreamWhenWholeMethodPassedToVerify() {
        verify(mock.booleanReturningMethod());
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenWholeMethodPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenWholeMethodPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions(mock.byteReturningMethod());
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenInOrderCreatedWithDodgyMock
    public void shouldScreamWhenInOrderCreatedWithDodgyMock() {
        inOrder("not a mock");
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenInOrderCreatedWithNulls
    public void shouldScreamWhenInOrderCreatedWithNulls() {
        inOrder(mock, null);
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamNullPassedToVerify
    public void shouldScreamNullPassedToVerify() {
        verify(null);
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenNotMockPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenNotMockPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions(null, "blah");
    }

// org.mockitousage.misuse.DescriptiveMessagesOnMisuseTest::shouldScreamWhenNullPassedToVerifyNoMoreInteractions
    public void shouldScreamWhenNullPassedToVerifyNoMoreInteractions() {
        verifyNoMoreInteractions(null);
    }

// org.mockitousage.misuse.DetectingFinalMethodsTest::shouldFailWithUnfinishedVerification
    public void shouldFailWithUnfinishedVerification() {
        withFinal = mock(WithFinal.class);
        verify(withFinal).foo();
        try {
            verify(withFinal).foo();
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.misuse.DetectingFinalMethodsTest::shouldFailWithUnfinishedStubbing
    public void shouldFailWithUnfinishedStubbing() {
        withFinal = mock(WithFinal.class);
        try {
            when(withFinal.foo()).thenReturn(null);
            fail();
        } catch (MissingMethodInvocationException e) {}
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::should_fail_fast_when_argument_matchers_are_abused
    public void should_fail_fast_when_argument_matchers_are_abused() {
        misplaced_anyObject_argument_matcher();
        try {
            mock(IMethods.class);
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("Misplaced argument matcher", e.getMessage());
        }
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::should_report_argument_locations_when_argument_matchers_misused
    public void should_report_argument_locations_when_argument_matchers_misused() {
        try {
        	Observer observer = mock(Observer.class);
        	
        	misplaced_anyInt_argument_matcher();
        	misplaced_anyObject_argument_matcher();
        	misplaced_anyBoolean_argument_matcher();
        	
        	observer.update(null, null);
        	
        	validateMockitoUsage();
        	fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("DetectingMisusedMatchersTest.misplaced_anyInt_argument_matcher", e.getMessage());
            assertContains("DetectingMisusedMatchersTest.misplaced_anyObject_argument_matcher", e.getMessage());
            assertContains("DetectingMisusedMatchersTest.misplaced_anyBoolean_argument_matcher", e.getMessage());
        }
    }

// org.mockitousage.misuse.DetectingMisusedMatchersTest::shouldSayUnfinishedVerificationButNotInvalidUseOfMatchers
    public void shouldSayUnfinishedVerificationButNotInvalidUseOfMatchers() {
        verify(withFinal).finalMethod(anyObject());
        try {
            verify(withFinal);
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldValidateExplicitly
    public void shouldValidateExplicitly() {
        verify(mock);
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (UnfinishedVerificationException e) {}
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldDetectUnfinishedStubbing
    public void shouldDetectUnfinishedStubbing() {
        when(mock.simpleMethod());
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (UnfinishedStubbingException e) {}
    }

// org.mockitousage.misuse.ExplicitFrameworkValidationTest::shouldDetectMisplacedArgumentMatcher
    public void shouldDetectMisplacedArgumentMatcher() {
        anyObject();
        try {
            Mockito.validateMockitoUsage();
            fail();
        } catch (InvalidUseOfMatchersException e) {}
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldRequireArgumentsWhenVerifyingNoMoreInteractions
    public void shouldRequireArgumentsWhenVerifyingNoMoreInteractions() {
        verifyNoMoreInteractions();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldRequireArgumentsWhenVerifyingZeroInteractions
    public void shouldRequireArgumentsWhenVerifyingZeroInteractions() {
        verifyZeroInteractions();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotCreateInOrderObjectWithoutMocks
    public void shouldNotCreateInOrderObjectWithoutMocks() {
        inOrder();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowVerifyingInOrderUnfamilarMocks
    public void shouldNotAllowVerifyingInOrderUnfamilarMocks() {
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mockTwo).simpleMethod();
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldReportMissingMethodInvocationWhenStubbing
    public void shouldReportMissingMethodInvocationWhenStubbing() {
        when(mock.simpleMethod()).thenReturn("this stubbing is required to make sure Stubbable is pulled");
        when("".toString()).thenReturn("x");
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.simpleMethod()).thenThrow(new Exception());
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.simpleMethod()).thenThrow(new Throwable[] {null});
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullThrowableVararg
    public void shouldNotAllowSettingNullThrowableVararg() throws Exception {
        when(mock.simpleMethod()).thenThrow((Throwable) null);
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowSettingNullConsecutiveThrowable
    public void shouldNotAllowSettingNullConsecutiveThrowable() throws Exception {
        when(mock.simpleMethod()).thenThrow(new RuntimeException(), null);
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowMockingFinalClasses
    public void shouldNotAllowMockingFinalClasses() throws Exception {
        mock(FinalClass.class); 
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotAllowMockingPrimitves
    public void shouldNotAllowMockingPrimitves() throws Exception {
        mock(Integer.TYPE); 
    }

// org.mockitousage.misuse.InvalidUsageTest::shouldNotMockObjectMethodsOnInterface
    public void shouldNotMockObjectMethodsOnInterface() throws Exception {
        ObjectLikeInterface inter = mock(ObjectLikeInterface.class);
        
        inter.equals(null);
        inter.toString();
        inter.hashCode();
        
        verifyZeroInteractions(inter);
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldScreamWhenVerifyToString
    public void shouldScreamWhenVerifyToString() {
        try {
            verify(mock).toString();
            fail();
        } catch (MockitoException e) {
            assertContains("cannot verify", e.getMessage());
        }
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyHashCode
    public void shouldBeSilentWhenVerifyHashCode() {
        
        
        
        verify(mock).hashCode();
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyEquals
    public void shouldBeSilentWhenVerifyEquals() {
        
        
        
        verify(mock).equals(null);
    }

// org.mockitousage.misuse.RestrictedObjectMethodsTest::shouldBeSilentWhenVerifyEqualsInOrder
    public void shouldBeSilentWhenVerifyEqualsInOrder() {
        
        
        
        InOrder inOrder = inOrder(mock);
        inOrder.verify(mock).equals(null);
    }

// org.mockitousage.misuse.SpyStubbingMisuseTest::nestedWhenTest
    public void nestedWhenTest() {
        Strategy mfoo = mock(Strategy.class);
        Sampler mpoo = mock(Sampler.class);
        Producer out = spy(new Producer(mfoo));

        try {
            when(out.produce()).thenReturn(mpoo);
            fail();
        } catch (WrongTypeOfReturnValue e) {
            assertThat(e.getMessage()).contains("spy").contains("syntax").contains("doReturn|Throw");
        }
    }

// org.mockitousage.packageprotected.MockingPackageProtectedTest::shouldMockPackageProtectedClasses
    public void shouldMockPackageProtectedClasses() {
        mock(PackageProtected.class);
        mock(Foo.class);
        mock(Bar.class);
    }

// org.mockitousage.performance.StubOnlyAvoidMemoryConsumptionTest::using_stub_only_wont_thrown_an_OutOfMemoryError
    public void using_stub_only_wont_thrown_an_OutOfMemoryError() {
        Object obj = mock(Object.class, withSettings().stubOnly());
        when(obj.toString()).thenReturn("asdf");

        for (int i = 0; i < 1000000; i++) {
            obj.toString();
        }
    }

// org.mockitousage.performance.StubOnlyAvoidMemoryConsumptionTest::without_stub_only_mocks_will_store_invocations_leading_to_an_OutOfMemoryError
    public void without_stub_only_mocks_will_store_invocations_leading_to_an_OutOfMemoryError() {
        Object obj = mock(Object.class, withSettings());
        when(obj.toString()).thenReturn("asdf");

        for (int i = 0; i < 1000000; i++) {
            obj.toString();
        }
    }

// org.mockitousage.puzzlers.BridgeMethodPuzzleTest::shouldHaveBridgeMethod
    public void shouldHaveBridgeMethod() throws Exception {
        Super s = new Sub();
        
        assertEquals("Dummy says: Hello", s.say("Hello"));
        
        assertThat(Sub.class, hasBridgeMethod("say"));
        assertThat(s, hasBridgeMethod("say"));
    }

// org.mockitousage.puzzlers.BridgeMethodPuzzleTest::shouldVerifyCorrectlyWhenBridgeMethodCalled
    public void shouldVerifyCorrectlyWhenBridgeMethodCalled() throws Exception {
        
        
        Sub s = mock(Sub.class);
        setMockWithDownCast(s);
        say("Hello");
        
        verify(s).say("Hello");
    }

// org.mockitousage.puzzlers.OverloadingPuzzleTest::shouldUseArgumentTypeWhenOverloadingPuzzleDetected
    public void shouldUseArgumentTypeWhenOverloadingPuzzleDetected() throws Exception {
        Sub sub = mock(Sub.class);
        setMockWithDowncast(sub);
        say("Hello");
        try {
            verify(sub).say("Hello");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.serialization.AcrossClassLoaderSerializationTest::check_that_mock_can_be_serialized_in_a_classloader_and_deserialized_in_another
    public void check_that_mock_can_be_serialized_in_a_classloader_and_deserialized_in_another() throws Exception {
        byte[] bytes = create_mock_and_serialize_it_in_class_loader_A();

        Object the_deserialized_mock = read_stream_and_deserialize_it_in_class_loader_B(bytes);
    }

// org.mockitousage.serialization.ParallelSerializationTest::single_mock_being_serialized_in_different_classloaders_by_multiple_threads
    public void single_mock_being_serialized_in_different_classloaders_by_multiple_threads() throws ExecutionException, InterruptedException {
        
        int iterations = 2;
        int threadingFactor = 200;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadingFactor);
        final IMethods iMethods_that_store_invocations = mock(IMethods.class, withSettings().serializable());

        
        for (int i = 0; i <= iterations; i++) {
            List<Future> futures = new ArrayList<Future>(threadingFactor);
            final CyclicBarrier barrier_that_will_wait_until_threads_are_ready = new CyclicBarrier(threadingFactor);

            
            
            
            for (int j = 0; j < threadingFactor; j++) {
                
                futures.add(executorService.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        barrier_that_will_wait_until_threads_are_ready.await();

                        randomCallOn(iMethods_that_store_invocations);

                        return SimpleSerializationUtil.serializeMock(iMethods_that_store_invocations).toByteArray();
                    }
                }));

                
                executorService.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        barrier_that_will_wait_until_threads_are_ready.await();
                        return iMethods_that_store_invocations.longObjectReturningMethod();
                    }
                });
            }

            
            for (Future future : futures) {
                future.get();
            }
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldCallRealMethdsEvenDelegatedToOtherSelfMethod
    public void shouldCallRealMethdsEvenDelegatedToOtherSelfMethod() {
        
        String name = spy.getName();

        
        assertEquals("Default name", name);
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldAllowStubbingOfMethodsThatDelegateToOtherMethods
    public void shouldAllowStubbingOfMethodsThatDelegateToOtherMethods() {
        
        when(spy.getName()).thenReturn("foo");
        
        
        assertEquals("foo", spy.getName());
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldAllowStubbingWithThrowablesMethodsThatDelegateToOtherMethods
    public void shouldAllowStubbingWithThrowablesMethodsThatDelegateToOtherMethods() {
        
        doThrow(new RuntimeException("appetite for destruction"))
            .when(spy).getNameButDelegateToMethodThatThrows();
        
        
        try {
            spy.getNameButDelegateToMethodThatThrows();
            fail();
        } catch(Exception e) {
            assertEquals("appetite for destruction", e.getMessage());
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldStackTraceGetFilteredOnUserExceptions
    public void shouldStackTraceGetFilteredOnUserExceptions() {
        try {
            
            spy.getNameButDelegateToMethodThatThrows();
            fail();
        } catch (Throwable t) {
            
            assertThat(t, ExtraMatchers.hasMethodsInStackTrace(
                    "throwSomeException",
                    "getNameButDelegateToMethodThatThrows",
                    "shouldStackTraceGetFilteredOnUserExceptions"
                    ));
        }
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldVerify
    public void shouldVerify() {
        
        spy.getName();

        
        verify(spy).guessName();
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldStub
    public void shouldStub() {
        
        when(spy.guessName()).thenReturn(new Name("John"));
        
        String name = spy.getName();
        
        assertEquals("John", name);
    }

// org.mockitousage.spies.PartialMockingWithSpiesTest::shouldDealWithPrivateFieldsOfSubclasses
    public void shouldDealWithPrivateFieldsOfSubclasses() {
        assertEquals("100$", spy.howMuchDidYouInherit());
    }

// org.mockitousage.spies.SpyingOnInterfacesTest::shouldFailFastWhenCallingRealMethodOnInterface
    public void shouldFailFastWhenCallingRealMethodOnInterface() throws Exception {
        List list = mock(List.class);
        try {
            
            when(list.get(0)).thenCallRealMethod();
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.spies.SpyingOnInterfacesTest::shouldFailInRuntimeWhenCallingRealMethodOnInterface
    public void shouldFailInRuntimeWhenCallingRealMethodOnInterface() throws Exception {
        
        List list = mock(List.class);
        when(list.get(0)).thenAnswer(
            new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.callRealMethod();
                }
            }
        );
        try {
            
            list.get(0);            
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerify
    public void shouldVerify() {
        spy.add("one");
        spy.add("two");

        assertEquals("one", spy.get(0));
        assertEquals("two", spy.get(1));

        verify(spy).add("one");
        verify(spy).add("two");
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldBeAbleToMockObjectBecauseWhyNot
    public void shouldBeAbleToMockObjectBecauseWhyNot() {
        spy(new Object());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStub
    public void shouldStub() {
        spy.add("one");
        when(spy.get(0))
            .thenReturn("1")
            .thenReturn("1 again");
               
        assertEquals("1", spy.get(0));
        assertEquals("1 again", spy.get(0));
        assertEquals("one", spy.iterator().next());
        
        assertEquals(1, spy.size());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldAllowOverridingStubs
    public void shouldAllowOverridingStubs() {
        when(spy.contains(anyObject())).thenReturn(true);
        when(spy.contains("foo")).thenReturn(false);
        
        assertTrue(spy.contains("bar"));
        assertFalse(spy.contains("foo"));
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStubVoid
    public void shouldStubVoid() {
        stubVoid(spy)
            .toReturn()
            .toThrow(new RuntimeException())
            .on().clear();

        spy.add("one");
        spy.clear();
        try {
            spy.clear();
            fail();
        } catch (RuntimeException e) {}
            
        assertEquals(1, spy.size());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldStubWithDoReturnAndVerify
    public void shouldStubWithDoReturnAndVerify() {
        doReturn("foo")
        .doReturn("bar")
        .when(spy).get(0);
        
        assertEquals("foo", spy.get(0));
        assertEquals("bar", spy.get(0));
        
        verify(spy, times(2)).get(0);
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        spy.add("one");
        spy.add("two");
        
        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).add("one");
        inOrder.verify(spy).add("two");
        
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyInOrderAndFail
    public void shouldVerifyInOrderAndFail() {
        spy.add("one");
        spy.add("two");
        
        InOrder inOrder = inOrder(spy);
        inOrder.verify(spy).add("two");
        try {
            inOrder.verify(spy).add("one");
            fail();
        } catch (VerificationInOrderFailure f) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNumberOfTimes
    public void shouldVerifyNumberOfTimes() {
        spy.add("one");
        spy.add("one");
        
        verify(spy, times(2)).add("one");
        verifyNoMoreInteractions(spy);
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNumberOfTimesAndFail
    public void shouldVerifyNumberOfTimesAndFail() {
        spy.add("one");
        spy.add("one");
        
        try {
            verify(spy, times(3)).add("one");
            fail();
        } catch (TooLittleActualInvocations e) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldVerifyNoMoreInteractionsAndFail
    public void shouldVerifyNoMoreInteractionsAndFail() {
        spy.add("one");
        spy.add("two");
        
        verify(spy).add("one");
        try {
            verifyNoMoreInteractions(spy);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldToString
    public void shouldToString() {
        spy.add("foo");
        assertEquals("[foo]" , spy.toString());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldAllowSpyingAnonymousClasses
    public void shouldAllowSpyingAnonymousClasses() {
        
        Foo spy = spy(new Foo() {
            public String print() {
                return "foo";
            }
        });

        
        assertEquals("foo", spy.print());
    }

// org.mockitousage.spies.SpyingOnRealObjectsTest::shouldSayNiceMessageWhenSpyingOnPrivateClass
    public void shouldSayNiceMessageWhenSpyingOnPrivateClass() throws Exception {
        List real = Arrays.asList("first", "second");
        try {
            spy(real);
            fail();
        } catch (MockitoException e) {
            assertContains("Most likely it is a private class that is not visible by Mockito", e.getMessage());
        }
    }

// org.mockitousage.spies.StubbingSpiesDoesNotYieldNPETest::shouldNotThrowNPE
    public void shouldNotThrowNPE() throws Exception {
        Foo foo = new Foo();
        Foo spy = spy(foo);
        
        spy.len(anyString());
        spy.size(anyMap());
        spy.size(anyList());
        spy.size(anyCollection());
        spy.size(anySet());
    }

// org.mockitousage.stacktrace.ClickableStackTracesTest::shouldShowActualAndExpectedWhenArgumentsAreDifferent
    public void shouldShowActualAndExpectedWhenArgumentsAreDifferent() {
        callMethodOnMock("foo");
        try {
            verifyTheMock(1, "not foo");
            fail();
        } catch (ArgumentsAreDifferent e) {
            assertContains("callMethodOnMock(", e.getMessage());
            assertContains("verifyTheMock(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldPointOutMisplacedMatcher
    public void shouldPointOutMisplacedMatcher() {
        misplacedArgumentMatcherHere();
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertContains("-> at ", e.getMessage());
            assertContains("misplacedArgumentMatcherHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldPointOutUnfinishedStubbing
    public void shouldPointOutUnfinishedStubbing() {
        unfinishedStubbingHere();
        
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (UnfinishedStubbingException e) {
            assertContains("-> at ", e.getMessage());
            assertContains("unfinishedStubbingHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.ClickableStackTracesWhenFrameworkMisusedTest::shouldShowWhereIsUnfinishedVerification
    public void shouldShowWhereIsUnfinishedVerification() throws Exception {
        unfinishedVerificationHere();
        try {
            mock(IMethods.class);
            fail();
        } catch (UnfinishedVerificationException e) {
            assertContains("unfinishedVerificationHere(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointStackTraceToPreviousInvocation
    public void shouldPointStackTraceToPreviousInvocation() {
        inOrder.verify(mock, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo, times(2)).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mock).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("secondChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToThirdInteractionBecauseAtLeastOnceUsed
    public void shouldPointToThirdInteractionBecauseAtLeastOnceUsed() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("thirdChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToThirdChunkWhenTooLittleActualInvocations
    public void shouldPointToThirdChunkWhenTooLittleActualInvocations() {
        inOrder.verify(mock, times(2)).simpleMethod(anyInt());
        inOrder.verify(mockTwo, times(2)).simpleMethod(anyInt());
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("thirdChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationChunkInOrderTest::shouldPointToFourthChunkBecauseTooManyActualInvocations
    public void shouldPointToFourthChunkBecauseTooManyActualInvocations() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourthChunk(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointStackTraceToPreviousVerified
    public void shouldPointStackTraceToPreviousVerified() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mock).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourth(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToThirdMethod
    public void shouldPointToThirdMethod() {
        inOrder.verify(mock, atLeastOnce()).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("third(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToSecondMethod
    public void shouldPointToSecondMethod() {
        inOrder.verify(mock).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("second(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToFirstMethodBecauseOfTooManyActualInvocations
    public void shouldPointToFirstMethodBecauseOfTooManyActualInvocations() {
        try {
            inOrder.verify(mock, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("first(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToSecondMethodBecauseOfTooManyActualInvocations
    public void shouldPointToSecondMethodBecauseOfTooManyActualInvocations() {
        inOrder.verify(mock).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("second(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationInOrderTest::shouldPointToFourthMethodBecauseOfTooLittleActualInvocations
    public void shouldPointToFourthMethodBecauseOfTooLittleActualInvocations() {
        inOrder.verify(mock).simpleMethod(anyInt());
        inOrder.verify(mockTwo).simpleMethod(anyInt());
        inOrder.verify(mock).simpleMethod(anyInt());
        
        try {
            inOrder.verify(mockTwo, times(3)).simpleMethod(anyInt());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("fourth(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationTest::shouldPointToTooManyInvocationsChunkOnError
    public void shouldPointToTooManyInvocationsChunkOnError() {
        try {
            verify(mock, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertContains("first(", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.PointingStackTraceToActualInvocationTest::shouldNotPointStackTracesToRunnersCode
    public void shouldNotPointStackTracesToRunnersCode() {
        try {
            verify(mock, times(0)).simpleMethod(1);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertNotContains(".runners.", e.getMessage());
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerify
    public void shouldFilterStackTraceOnVerify() {
        try {
            verify(mock).simpleMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerify"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerifyNoMoreInteractions
    public void shouldFilterStackTraceOnVerifyNoMoreInteractions() {
        mock.oneArg(true);
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyNoMoreInteractions"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceOnVerifyZeroInteractions
    public void shouldFilterStackTraceOnVerifyZeroInteractions() {
        mock.oneArg(true);
        try {
            verifyZeroInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStackTraceOnVerifyZeroInteractions"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceOnMockitoException
    public void shouldFilterStacktraceOnMockitoException() {
        verify(mock);
        try {
            verify(mock).oneArg(true); 
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceOnMockitoException"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenVerifyingInOrder
    public void shouldFilterStacktraceWhenVerifyingInOrder() {
        InOrder inOrder = inOrder(mock);
        mock.oneArg(true);
        mock.oneArg(false);
        
        inOrder.verify(mock).oneArg(false);
        try {
            inOrder.verify(mock).oneArg(true);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenVerifyingInOrder"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenInOrderThrowsMockitoException
    public void shouldFilterStacktraceWhenInOrderThrowsMockitoException() {
        try {
            inOrder();
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenInOrderThrowsMockitoException"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStacktraceWhenInOrderVerifies
    public void shouldFilterStacktraceWhenInOrderVerifies() {
        try {
            InOrder inOrder = inOrder(mock);
            inOrder.verify(null);
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStacktraceWhenInOrderVerifies"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldFilterStackTraceWhenThrowingExceptionFromMockHandler
    public void shouldFilterStackTraceWhenThrowingExceptionFromMockHandler() {
        try {
            when(mock.oneArg(true)).thenThrow(new Exception());
            fail();
        } catch (MockitoException expected) {
            assertThat(expected, hasFirstMethodInStackTrace("shouldFilterStackTraceWhenThrowingExceptionFromMockHandler"));
        }
    }

// org.mockitousage.stacktrace.StackTraceFilteringTest::shouldShowProperExceptionStackTrace
    public void shouldShowProperExceptionStackTrace() throws Exception {
        when(mock.simpleMethod()).thenThrow(new RuntimeException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {
            assertThat(e, hasFirstMethodInStackTrace("shouldShowProperExceptionStackTrace"));
        }
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_evaluate_latest_stubbing_first
    public void should_evaluate_latest_stubbing_first() throws Exception {
        when(mock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(mock.objectReturningMethod(200)).thenReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_stubbing_be_treated_as_interaction
    public void should_stubbing_be_treated_as_interaction() throws Exception {
        when(mock.booleanReturningMethod()).thenReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_allow_stubbing_to_string
    public void should_allow_stubbing_to_string() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        when(mockTwo.toString()).thenReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_stubbing_not_be_treated_as_interaction
    public void should_stubbing_not_be_treated_as_interaction() {
        when(mock.simpleMethod("one")).thenThrow(new RuntimeException());
        doThrow(new RuntimeException()).when(mock).simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::unfinished_stubbing_cleans_up_the_state
    public void unfinished_stubbing_cleans_up_the_state() {
        reset(mock);
        try {
            when("").thenReturn("");
            fail(); 
        } catch (MissingMethodInvocationException e) {}

        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_to_string_mock_name
    public void should_to_string_mock_name() {
        IMethods mock = mock(IMethods.class, "mockie");
        IMethods mockTwo = mock(IMethods.class);
        
        assertContains("Mock for IMethods", "" + mockTwo);
        assertEquals("mockie", "" + mock);
    }

// org.mockitousage.stubbing.BasicStubbingTest::should_allow_mocking_when_to_string_is_final
    public void should_allow_mocking_when_to_string_is_final() throws Exception {
        mock(Foo.class);
    }

// org.mockitousage.stubbing.BasicStubbingTest::test_stub_only_not_verifiable
    public void test_stub_only_not_verifiable() throws Exception {
        IMethods localMock = mock(IMethods.class, withSettings().stubOnly());

        when(localMock.objectReturningMethod(isA(Integer.class))).thenReturn(100);
        when(localMock.objectReturningMethod(200)).thenReturn(200);

        assertEquals(200, localMock.objectReturningMethod(200));
        assertEquals(100, localMock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, localMock.objectReturningMethod("blah"));

        try {
            verify(localMock, atLeastOnce()).objectReturningMethod(eq(200));
            fail();
        } catch (CannotVerifyStubOnlyMock e) {}
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldAllowCallingInternalMethod
    public void shouldAllowCallingInternalMethod() {
        when(mock.getValue()).thenReturn("foo");
        when(mock.callInternalMethod()).thenCallRealMethod();
        
        assertEquals("foo", mock.callInternalMethod());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldReturnRealValue
    public void shouldReturnRealValue() {
        when(mock.getValue()).thenCallRealMethod();

        Assert.assertEquals("HARD_CODED_RETURN_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldExecuteRealMethod
    public void shouldExecuteRealMethod() {
        doCallRealMethod().when(mock).setValue(anyString());

        mock.setValue("REAL_VALUE");

        Assert.assertEquals("REAL_VALUE", mock.value);
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldCallRealMethodByDefault
    public void shouldCallRealMethodByDefault() {
        TestedObject mock = mock(TestedObject.class, CALLS_REAL_METHODS);

        Assert.assertEquals("HARD_CODED_RETURN_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CallingRealMethodTest::shouldNotCallRealMethodWhenStubbedLater
    public void shouldNotCallRealMethodWhenStubbedLater() {
        TestedObject mock = mock(TestedObject.class);

        when(mock.getValue()).thenCallRealMethod();
        when(mock.getValue()).thenReturn("FAKE_VALUE");

        Assert.assertEquals("FAKE_VALUE", mock.getValue());
    }

// org.mockitousage.stubbing.CloningParameterTest::shouldVerifyEvenIfArgumentsWereMutated
    public void shouldVerifyEvenIfArgumentsWereMutated() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());

        
        businessLogic(emailSender);

        
        verify(emailSender).sendEmail(1, new Person("Wes"));
    }

// org.mockitousage.stubbing.CloningParameterTest::shouldReturnDefaultValueWithCloningAnswer
    public void shouldReturnDefaultValueWithCloningAnswer() throws Exception {

        
        EmailSender emailSender = mock(EmailSender.class, new ClonesArguments());
        when(emailSender.getAllEmails(new Person("Wes"))).thenAnswer(new ClonesArguments());

        
        List<?> emails = emailSender.getAllEmails(new Person("Wes"));

        
        assertNotNull(emails);
    }

// org.mockitousage.stubbing.DeepStubbingTest::myTest
    public void myTest() throws Exception {
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80))).thenReturn(null);
        sf.createSocket("what", 80);
    }

// org.mockitousage.stubbing.DeepStubbingTest::simpleCase
    public void simpleCase() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        assertSame(out, socket.getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::oneLevelDeep
    public void oneLevelDeep() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        SocketFactory socketFactory = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(socketFactory.createSocket().getOutputStream()).thenReturn(out);

        assertSame(out, socketFactory.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::interactions
    public void interactions() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf1 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf1.createSocket().getOutputStream()).thenReturn(out1);

        SocketFactory sf2 = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf2.createSocket().getOutputStream()).thenReturn(out2);

        assertSame(out1, sf1.createSocket().getOutputStream());
        assertSame(out2, sf2.createSocket().getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withArguments
    public void withArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();
        OutputStream out3 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getOutputStream()).thenReturn(out1);
        when(sf.createSocket("google.com", 80).getOutputStream()).thenReturn(out2);
        when(sf.createSocket("stackoverflow.com", 80).getOutputStream()).thenReturn(out3);

        assertSame(out1, sf.createSocket().getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out3, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withAnyPatternArguments
    public void withAnyPatternArguments() throws Exception {
        OutputStream out = new ByteArrayOutputStream();

        
        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), anyInt()).getOutputStream()).thenReturn(out);

        assertSame(out, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withComplexPatternArguments
    public void withComplexPatternArguments() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        OutputStream out2 = new ByteArrayOutputStream();

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(anyString(), eq(80)).getOutputStream()).thenReturn(out1);
        when(sf.createSocket(anyString(), eq(8080)).getOutputStream()).thenReturn(out2);

        assertSame(out2, sf.createSocket("stackoverflow.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("google.com", 80).getOutputStream());
        assertSame(out2, sf.createSocket("google.com", 8080).getOutputStream());
        assertSame(out1, sf.createSocket("stackoverflow.com", 80).getOutputStream());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withSimplePrimitive
    public void withSimplePrimitive() throws Exception {
        int a = 32;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket().getPort()).thenReturn(a);

        assertEquals(a, sf.createSocket().getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::withPatternPrimitive
    public void withPatternPrimitive() throws Exception {
        int a = 12, b = 23, c = 34;

        SocketFactory sf = mock(SocketFactory.class, RETURNS_DEEP_STUBS);
        when(sf.createSocket(eq("stackoverflow.com"), eq(80)).getPort()).thenReturn(a);
        when(sf.createSocket(eq("google.com"), anyInt()).getPort()).thenReturn(b);
        when(sf.createSocket(eq("stackoverflow.com"), eq(8080)).getPort()).thenReturn(c);

        assertEquals(b, sf.createSocket("google.com", 80).getPort());
        assertEquals(c, sf.createSocket("stackoverflow.com", 8080).getPort());
        assertEquals(a, sf.createSocket("stackoverflow.com", 80).getPort());
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldStubbingBasicallyWorkFine
    public void shouldStubbingBasicallyWorkFine() throws Exception {
        
        given(person.getAddress().getStreet().getName()).willReturn("Norymberska");
        
        
        String street = person.getAddress().getStreet().getName();
        
        
        assertEquals("Norymberska", street);
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldVerificationBasicallyWorkFine
    public void shouldVerificationBasicallyWorkFine() throws Exception {
        
        person.getAddress().getStreet().getName();
        
        
        verify(person.getAddress().getStreet()).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verification_work_with_argument_Matchers_in_nested_calls
	public void verification_work_with_argument_Matchers_in_nested_calls() throws Exception {
		
    	person.getAddress("111 Mock Lane").getStreet();
    	person.getAddress("111 Mock Lane").getStreet(Locale.ITALIAN).getName();

		
    	verify(person.getAddress(anyString())).getStreet();
    	verify(person.getAddress(anyString()).getStreet(Locale.CHINESE), never()).getName();
    	verify(person.getAddress(anyString()).getStreet(eq(Locale.ITALIAN))).getName();
	}

// org.mockitousage.stubbing.DeepStubbingTest::deep_stub_return_same_mock_instance_if_invocation_matchers_matches
    public void deep_stub_return_same_mock_instance_if_invocation_matchers_matches() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();

        assertSame(person.getAddress("the docks").getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress(anyString()).getStreet());
        assertSame(person.getAddress("the docks").getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress(anyString()).getStreet(), person.getAddress("the docks").getStreet());
        assertSame(person.getAddress("111 Mock Lane").getStreet(), person.getAddress("the docks").getStreet());
    }

// org.mockitousage.stubbing.DeepStubbingTest::times_never_atLeast_atMost_verificationModes_should_work
    public void times_never_atLeast_atMost_verificationModes_should_work() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();

        verify(person.getAddress("the docks").getStreet(), times(3)).getName();
        verify(person.getAddress("the docks").getStreet(Locale.CHINESE), never()).getName();
        verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atMost(1)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::inOrder_only_work_on_the_very_last_mock_but_it_works
    public void inOrder_only_work_on_the_very_last_mock_but_it_works() throws Exception {
        when(person.getAddress(anyString()).getStreet().getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.ITALIAN).getName()).thenReturn("deep");
        when(person.getAddress(anyString()).getStreet(Locale.CHINESE).getName()).thenReturn("deep");

        person.getAddress("the docks").getStreet().getName();
        person.getAddress("the docks").getStreet().getLongName();
        person.getAddress("the docks").getStreet(Locale.ITALIAN).getName();
        person.getAddress("the docks").getStreet(Locale.CHINESE).getName();

        InOrder inOrder = inOrder(
                person.getAddress("the docks").getStreet(),
                person.getAddress("the docks").getStreet(Locale.CHINESE),
                person.getAddress("the docks").getStreet(Locale.ITALIAN)
        );
        inOrder.verify(person.getAddress("the docks").getStreet(), times(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet()).getLongName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.ITALIAN), atLeast(1)).getName();
        inOrder.verify(person.getAddress("the docks").getStreet(Locale.CHINESE)).getName();
    }

// org.mockitousage.stubbing.DeepStubbingTest::verificationMode_only_work_on_the_last_returned_mock
    public void verificationMode_only_work_on_the_last_returned_mock() throws Exception {
        
        when(person.getAddress("the docks").getStreet().getName()).thenReturn("deep");

        
        person.getAddress("the docks").getStreet().getName();
        
        
        verify(person.getAddress("the docks").getStreet()).getName();

        try {
            verify(person.getAddress("the docks"), times(1)).getStreet();
            fail();
        } catch (TooManyActualInvocations e) {
            Assertions.assertThat(e.getMessage())
                    .contains("Wanted 1 time")
                    .contains("But was 3 times");
        }
    }

// org.mockitousage.stubbing.DeepStubbingTest::shouldFailGracefullyWhenClassIsFinal
    public void shouldFailGracefullyWhenClassIsFinal() throws Exception {
        
        FinalClass value = new FinalClass();
        given(person.getFinalClass()).willReturn(value);
        
        
        assertEquals(value, person.getFinalClass());
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldEvaluateLatestStubbingFirst
    public void shouldEvaluateLatestStubbingFirst() throws Exception {
        stub(mock.objectReturningMethod(isA(Integer.class))).toReturn(100);
        stub(mock.objectReturningMethod(200)).toReturn(200);
        
        assertEquals(200, mock.objectReturningMethod(200));
        assertEquals(100, mock.objectReturningMethod(666));
        assertEquals("default behavior should return null", null, mock.objectReturningMethod("blah"));
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        stub(mock.booleanReturningMethod()).toReturn(true);
        
        mock.booleanReturningMethod();
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        IMethods mockTwo = mock(IMethods.class);
        stub(mockTwo.toString()).toReturn("test");
        
        assertContains("Mock for IMethods", mock.toString());
        assertEquals("test", mockTwo.toString());
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldStubbingNotBeTreatedAsInteraction
    public void shouldStubbingNotBeTreatedAsInteraction() {
        stub(mock.simpleMethod("one")).toThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().simpleMethod("two");
        
        verifyZeroInteractions(mock);
    }

// org.mockitousage.stubbing.DeprecatedStubbingTest::shouldAllowConsecutiveStubbing
    public void shouldAllowConsecutiveStubbing() throws Exception {
        
        stub(mock.simpleMethod())
            .toReturn("100")
            .toReturn("200");
        
        
        assertEquals("100", mock.simpleMethod());
        assertEquals("200", mock.simpleMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnAllKindsOfPrimitives
    public void shouldReturnAllKindsOfPrimitives() throws Exception {
        assertEquals((byte) 0, mock.byteReturningMethod());
        assertEquals((short) 0, mock.shortReturningMethod());
        assertEquals(0, mock.intReturningMethod());
        assertEquals(0L, mock.longReturningMethod());
        assertEquals(0.0F, mock.floatReturningMethod(), 0.0F);
        assertEquals(0.0D, mock.doubleReturningMethod(), 0.0D);
        assertEquals((char) 0, mock.charReturningMethod());
        assertEquals(false, mock.booleanReturningMethod());
        assertEquals(null, mock.objectReturningMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnTheSameValuesForWrapperClasses
    public void shouldReturnTheSameValuesForWrapperClasses() throws Exception {
        assertEquals(new Byte((byte) 0), mock.byteObjectReturningMethod());
        assertEquals(new Short((short) 0), mock.shortObjectReturningMethod());
        assertEquals(new Integer(0), mock.integerReturningMethod());
        assertEquals(new Long(0L), mock.longObjectReturningMethod());
        assertEquals(new Float(0.0F), mock.floatObjectReturningMethod(), 0.0F);
        assertEquals(new Double(0.0D), mock.doubleObjectReturningMethod(), 0.0D);
        assertEquals(new Character((char) 0), mock.charObjectReturningMethod());
        assertEquals(new Boolean(false), mock.booleanObjectReturningMethod());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnEmptyCollections
    public void shouldReturnEmptyCollections() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        assertTrue(mock.list().isEmpty());
        assertTrue(mock.linkedList().isEmpty());
        assertTrue(mock.map().isEmpty());
        assertTrue(mock.hashSet().isEmpty());
    }

// org.mockitousage.stubbing.ReturningDefaultValuesTest::shouldReturnMutableEmptyCollection
    public void shouldReturnMutableEmptyCollection() {
        CollectionsServer mock = Mockito.mock(CollectionsServer.class);
        
        List list = mock.list();
        list.add("test");
       
        assertTrue(mock.list().isEmpty());
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldSmartNPEPointToUnstubbedCall
    public void shouldSmartNPEPointToUnstubbedCall() throws Exception {
        IMethods methods = unstubbedMethodInvokedHere(mock); 
        try {
            methods.simpleMethod();
            fail();
        } catch (SmartNullPointerException e) {
            assertContains("unstubbedMethodInvokedHere(", e.getMessage());
        }
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsClass
    public void shouldThrowSmartNPEWhenMethodReturnsClass() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Foo foo = mock.getSomeClass();
        try {
            foo.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldThrowSmartNPEWhenMethodReturnsInterface
    public void shouldThrowSmartNPEWhenMethodReturnsInterface() throws Exception {
        Foo mock = mock(Foo.class, RETURNS_SMART_NULLS);
        Bar bar = mock.getSomeInterface();
        try {
            bar.boo();
            fail();
        } catch (SmartNullPointerException e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldReturnOrdinaryEmptyValuesForOrdinaryTypes
    public void shouldReturnOrdinaryEmptyValuesForOrdinaryTypes() throws Exception {
        IMethods mock = mock(IMethods.class, RETURNS_SMART_NULLS);

        assertEquals("", mock.stringReturningMethod());
        assertEquals(0, mock.intReturningMethod());
        assertEquals(true, mock.listReturningMethod().isEmpty());
        assertEquals(0, mock.arrayReturningMethod().length);
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnToString
    public void shouldNotThrowSmartNullPointerOnToString() {
        Object smartNull = mock.objectReturningMethod();
        try {
            verify(mock).simpleMethod(smartNull);
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldNotThrowSmartNullPointerOnObjectMethods
    public void shouldNotThrowSmartNullPointerOnObjectMethods() {
        Object smartNull = mock.objectReturningMethod();
        smartNull.toString();
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldShowParameters
    public void shouldShowParameters() {
        Foo foo = mock(Foo.class, RETURNS_SMART_NULLS);
        Bar smartNull = foo.getBarWithParams(10, "yes sir");

        try {
            smartNull.boo();
            fail();
        } catch (Exception e) {
            assertContains("yes sir", e.getMessage());
        }
    }

// org.mockitousage.stubbing.SmartNullsStubbingTest::shouldShowParametersWhenParamsAreHuge
    public void shouldShowParametersWhenParamsAreHuge() {
        Foo foo = mock(Foo.class, RETURNS_SMART_NULLS);
        String longStr = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
        Bar smartNull = foo.getBarWithParams(10, longStr);

        try {
            smartNull.boo();
            fail();
        } catch (Exception e) {
            assertContains("Lorem Ipsum", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValues
    public void shouldReturnConsecutiveValues() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenReturn("two")
            .thenReturn("three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesForTwoNulls
    public void shouldReturnConsecutiveValuesForTwoNulls() throws Exception {
        when(mock.simpleMethod()).thenReturn(null, null);
        
        assertNull(mock.simpleMethod());        
        assertNull(mock.simpleMethod());        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValuesSetByShortenThenReturnMethod
    public void shouldReturnConsecutiveValuesSetByShortenThenReturnMethod() throws Exception {        
        when(mock.simpleMethod())
            .thenReturn("one", "two", "three");
        
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
        assertEquals("three", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods
    public void shouldReturnConsecutiveValueAndThrowExceptionssSetByShortenReturnMethods()
            throws Exception {
        when(mock.simpleMethod())
            .thenReturn("zero")
            .thenReturn("one", "two")
            .thenThrow(new NullPointerException(), new RuntimeException())
            .thenReturn("three")
            .thenThrow(new IllegalArgumentException());

        assertEquals("zero", mock.simpleMethod());
        assertEquals("one", mock.simpleMethod());
        assertEquals("two", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        assertEquals("three", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutively
    public void shouldThrowConsecutively() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException())
            .thenThrow(new IllegalArgumentException())
            .thenThrow(new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldThrowConsecutivelySetByShortenThenThrowMethod
    public void shouldThrowConsecutivelySetByShortenThenThrowMethod() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new RuntimeException(), new IllegalArgumentException(), new NullPointerException());

        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldMixConsecutiveReturnsWithExcepions
    public void shouldMixConsecutiveReturnsWithExcepions() throws Exception {
        when(mock.simpleMethod())
            .thenThrow(new IllegalArgumentException())
            .thenReturn("one")
            .thenThrow(new NullPointerException())
            .thenReturn(null);
        
        try {
            mock.simpleMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        assertEquals("one", mock.simpleMethod());
        
        try {
            mock.simpleMethod();
            fail();
        } catch (NullPointerException e) {}
        
        assertEquals(null, mock.simpleMethod());
        assertEquals(null, mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveException
    public void shouldValidateConsecutiveException() throws Exception {
        when(mock.simpleMethod())
            .thenReturn("one")
            .thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethodAndContinueThrowing
    public void shouldStubVoidMethodAndContinueThrowing() throws Exception {
        stubVoid(mock)
            .toThrow(new IllegalArgumentException())
            .toReturn()
            .toThrow(new NullPointerException())
            .on().voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (IllegalArgumentException e) {}
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}        
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldStubVoidMethod
    public void shouldStubVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new NullPointerException())
            .toReturn()
            .on().voidMethod();
        
        mock.voidMethod();
        
        try {
            mock.voidMethod();
            fail();
        } catch (NullPointerException e) {}
        
        mock.voidMethod();
        mock.voidMethod();
    }

// org.mockitousage.stubbing.StubbingConsecutiveAnswersTest::shouldValidateConsecutiveExceptionForVoidMethod
    public void shouldValidateConsecutiveExceptionForVoidMethod() throws Exception {
        stubVoid(mock)
            .toReturn()
            .toThrow(new Exception())
            .on().voidMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStub
    public void shouldStub() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        doReturn("bar").when(mock).simpleMethod();
        
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithArgs
    public void shouldStubWithArgs() throws Exception {
        doReturn("foo").when(mock).simpleMethod("foo");
        doReturn("bar").when(mock).simpleMethod(eq("one"), anyInt());
        
        assertEquals("foo", mock.simpleMethod("foo"));
        assertEquals("bar", mock.simpleMethod("one", 234));
        assertEquals(null, mock.simpleMethod("xxx", 234));
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        doThrow(new FooRuntimeException()).when(mock).voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (FooRuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowSettingValidCheckedException
    public void shouldAllowSettingValidCheckedException() throws Exception {
        doThrow(new IOException()).when(mock).throwsIOException(0);
        
        try {
            mock.throwsIOException(0);
            fail();
        } catch (IOException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidCheckedException
    public void shouldDetectInvalidCheckedException() throws Exception {
        try {
            doThrow(new FooCheckedException()).when(mock).throwsIOException(0);
            fail();
        } catch (Exception e) {
            assertContains("Checked exception is invalid", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenReturnSetForVoid
    public void shouldScreamWhenReturnSetForVoid() throws Exception {
        try {
            doReturn("foo").when(mock).voidMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("void method", e.getMessage());
            assertContains("cannot", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNotAMockPassed
    public void shouldScreamWhenNotAMockPassed() throws Exception {
        try {
            doReturn("foo").when("foo").toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is not a mock", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            doReturn("foo").when((Object) null).toString();
            fail();
        } catch (Exception e) {
            assertContains("Argument passed to when() is null", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbing
    public void shouldAllowChainedStubbing() {
        doReturn("foo").
        doThrow(new RuntimeException()).
        doReturn("bar")
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
        try {
            mock.simpleMethod();
            fail();
        } catch (RuntimeException e) {}
        
        assertEquals("bar", mock.simpleMethod());
        assertEquals("bar", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowDoCallRealMethodInChainedStubbing
    public void shouldAllowDoCallRealMethodInChainedStubbing() throws Exception {
        MethodsImpl methods = mock(MethodsImpl.class);
        doReturn("A").doCallRealMethod()
                .when(methods).simpleMethod();

        assertEquals("A", methods.simpleMethod());
        assertEquals(null, methods.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingWithExceptionClass
    public void shouldAllowChainedStubbingWithExceptionClass() throws Exception {
        doReturn("whatever").doThrow(IllegalArgumentException.class).when(mock).simpleMethod();

        assertEquals("whatever", mock.simpleMethod());
        mock.simpleMethod();
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowChainedStubbingOnVoidMethods
    public void shouldAllowChainedStubbingOnVoidMethods() {
        doNothing().
        doNothing().
        doThrow(new RuntimeException())
        .when(mock).voidMethod();
        
        mock.voidMethod();
        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
        try {
            mock.voidMethod();
            fail();
        } catch (RuntimeException e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubWithGenericAnswer
    public void shouldStubWithGenericAnswer() {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return "foo";
            }
        })
        .when(mock).simpleMethod();
        
        assertEquals("foo", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldNotAllowDoNothingOnNonVoids
    public void shouldNotAllowDoNothingOnNonVoids() {
        try {
            doNothing().when(mock).simpleMethod();
            fail();
        } catch (MockitoException e) {
            assertContains("Only void methods can doNothing()", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldStubbingBeTreatedAsInteraction
    public void shouldStubbingBeTreatedAsInteraction() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldVerifyStubbedCall
    public void shouldVerifyStubbedCall() throws Exception {
        doReturn("foo").when(mock).simpleMethod();
        mock.simpleMethod();
        mock.simpleMethod();
        
        verify(mock, times(2)).simpleMethod();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingToString
    public void shouldAllowStubbingToString() throws Exception {
        doReturn("test").when(mock).toString();
        assertEquals("test", mock.toString());
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectInvalidReturnType
    public void shouldDetectInvalidReturnType() throws Exception {
        try {
            doReturn("foo").when(mock).booleanObjectReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("String cannot be returned by booleanObjectReturningMethod()" +
                    "\n" +
                    "booleanObjectReturningMethod() should return Boolean",
                    e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldDetectWhenNullAssignedToBoolean
    public void shouldDetectWhenNullAssignedToBoolean() throws Exception {
        try {
            doReturn(null).when(mock).intReturningMethod();
            fail();
        } catch (Exception e) {
            assertContains("null cannot be returned by intReturningMethod", e.getMessage());
        }
    }

// org.mockitousage.stubbing.StubbingUsingDoReturnTest::shouldAllowStubbingWhenTypesMatchSignature
    public void shouldAllowStubbingWhenTypesMatchSignature() throws Exception {
        doReturn("foo").when(mock).objectReturningMethodNoArgs();
        doReturn("foo").when(mock).simpleMethod();
        doReturn(1).when(mock).intReturningMethod();
        doReturn(new Integer(2)).when(mock).intReturningMethod();
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswer
    public void shouldAnswer() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                String arg = (String) invocation.getArguments()[0];

                return invocation.getMethod().getName() + "-" + arg;
            }
        });

        assertEquals("simpleMethod-test", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerWithThenAnswerAlias
    public void shouldAnswerWithThenAnswerAlias() throws Exception {
        RecordCall recordCall = new RecordCall();
        Set mockedSet = when(mock(Set.class).isEmpty()).then(recordCall).getMock();

        mockedSet.isEmpty();

        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerConsecutively
    public void shouldAnswerConsecutively() throws Exception {
        when(mock.simpleMethod())
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName();
                    }
                })
                .thenReturn("Hello")
                .thenAnswer(new Answer<String>() {
                    public String answer(InvocationOnMock invocation) throws Throwable {
                        return invocation.getMethod().getName() + "-1";
                    }
                });

        assertEquals("simpleMethod", mock.simpleMethod());
        assertEquals("Hello", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
        assertEquals("simpleMethod-1", mock.simpleMethod());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shoudAnswerVoidMethod
    public void shoudAnswerVoidMethod() throws Exception {
        RecordCall recordCall = new RecordCall();

        stubVoid(mock).toAnswer(recordCall).on().voidMethod();

        mock.voidMethod();
        assertTrue(recordCall.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldAnswerVoidMethodConsecutively
    public void shouldAnswerVoidMethodConsecutively() throws Exception {
        RecordCall call1 = new RecordCall();
        RecordCall call2 = new RecordCall();

        stubVoid(mock).toAnswer(call1)
                .toThrow(new UnsupportedOperationException())
                .toAnswer(call2)
                .on().voidMethod();

        mock.voidMethod();
        assertTrue(call1.isCalled());
        assertFalse(call2.isCalled());

        try {
            mock.voidMethod();
            fail();
        } catch (UnsupportedOperationException e) {
        }

        mock.voidMethod();
        assertTrue(call2.isCalled());
    }

// org.mockitousage.stubbing.StubbingWithCustomAnswerTest::shouldMakeSureTheInterfaceDoesNotChange
    public void shouldMakeSureTheInterfaceDoesNotChange() throws Exception {
        when(mock.simpleMethod(anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArguments().getClass().isArray());
                assertEquals(Method.class, invocation.getMethod().getClass());

                return "assertions passed";
            }
        });

        assertEquals("assertions passed", mock.simpleMethod("test"));
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldWorkAsStandardMockito
    public void shouldWorkAsStandardMockito() throws Exception {
        
        List<Integer> list = asList(1, 2, 3);
        when(mock.objectReturningMethodNoArgs()).thenAnswer(new ReturnsElementsOf(list));
        
        
        assertEquals(1, mock.objectReturningMethodNoArgs());
        assertEquals(2, mock.objectReturningMethodNoArgs());
        assertEquals(3, mock.objectReturningMethodNoArgs());
        
        assertEquals(3, mock.objectReturningMethodNoArgs());
        assertEquals(3, mock.objectReturningMethodNoArgs());
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldReturnNullIfNecessary
    public void shouldReturnNullIfNecessary() throws Exception {
        
        List<Integer> list = asList(1, null);
        when(mock.objectReturningMethodNoArgs()).thenAnswer(new ReturnsElementsOf(list));
        
        
        assertEquals(1, mock.objectReturningMethodNoArgs());
        assertEquals(null, mock.objectReturningMethodNoArgs());
        assertEquals(null, mock.objectReturningMethodNoArgs());
    }

// org.mockitousage.stubbing.StubbingWithExtraAnswersTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() throws Exception {
        try {
            
            new ReturnsElementsOf(null);
            
            fail();
        } catch (MockitoException e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubWithThrowable
    public void shouldStubWithThrowable() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        when(mock.add("throw")).thenThrow(expected);
        
        try {
            mock.add("throw");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldSetThrowableToVoidMethod
    public void shouldSetThrowableToVoidMethod() throws Exception {
        IllegalArgumentException expected = new IllegalArgumentException("thrown by mock");
        
        stubVoid(mock).toThrow(expected).on().clear();
        try {
            mock.clear();
            fail();
        } catch (Exception e) {
            assertEquals(expected, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldLastStubbingVoidBeImportant
    public void shouldLastStubbingVoidBeImportant() throws Exception {
        stubVoid(mock).toThrow(new ExceptionOne()).on().clear();
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation
    public void shouldFailStubbingThrowableOnTheSameInvocationDueToAcceptableLimitation() throws Exception {
        when(mock.get(1)).thenThrow(new ExceptionOne());
        
        try {
            when(mock.get(1)).thenThrow(new ExceptionTwo());
            fail();
        } catch (ExceptionOne e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingCheckedException
    public void shouldAllowSettingCheckedException() throws Exception {
        Reader reader = mock(Reader.class);
        IOException ioException = new IOException();
        
        when(reader.read()).thenThrow(ioException);
        
        try {
            reader.read();
            fail();
        } catch (Exception e) {
            assertEquals(ioException, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldAllowSettingError
    public void shouldAllowSettingError() throws Exception {
        Error error = new Error();
        
        when(mock.add("quake")).thenThrow(error);
        
        try {
            mock.add("quake");
            fail();
        } catch (Error e) {
            assertEquals(error, e);
        }
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldInstantiateExceptionClassOnInteraction
    public void shouldInstantiateExceptionClassOnInteraction() {
        when(mock.add(null)).thenThrow(IllegalArgumentException.class);

        mock.add(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldInstantiateExceptionClassWithOngoingStubbingOnInteraction
    public void shouldInstantiateExceptionClassWithOngoingStubbingOnInteraction() {
        Mockito.doThrow(IllegalArgumentException.class).when(mock).add(null);

        mock.add(null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingInvalidCheckedException
    public void shouldNotAllowSettingInvalidCheckedException() throws Exception {
        when(mock.add("monkey island")).thenThrow(new Exception());
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowable
    public void shouldNotAllowSettingNullThrowable() throws Exception {
        when(mock.add("monkey island")).thenThrow((Throwable) null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldNotAllowSettingNullThrowableArray
    public void shouldNotAllowSettingNullThrowableArray() throws Exception {
        when(mock.add("monkey island")).thenThrow((Throwable[]) null);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldMixThrowablesAndReturnsOnDifferentMocks
    public void shouldMixThrowablesAndReturnsOnDifferentMocks() throws Exception {
        when(mock.add("ExceptionOne")).thenThrow(new ExceptionOne());
        when(mock.getLast()).thenReturn("last");
        stubVoid(mock).toThrow(new ExceptionTwo()).on().clear();
        
        stubVoid(mockTwo).toThrow(new ExceptionThree()).on().clear();
        when(mockTwo.containsValue("ExceptionFour")).thenThrow(new ExceptionFour());
        when(mockTwo.get("Are you there?")).thenReturn("Yes!");

        assertNull(mockTwo.get("foo"));
        assertTrue(mockTwo.keySet().isEmpty());
        assertEquals("Yes!", mockTwo.get("Are you there?"));
        try {
            mockTwo.clear();
            fail();
        } catch (ExceptionThree e) {}
        try {
            mockTwo.containsValue("ExceptionFour");
            fail();
        } catch (ExceptionFour e) {}
        
        assertNull(mock.getFirst());
        assertEquals("last", mock.getLast());
        try {
            mock.add("ExceptionOne");
            fail();
        } catch (ExceptionOne e) {}
        try {
            mock.clear();
            fail();
        } catch (ExceptionTwo e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableBeVerifiable
    public void shouldStubbingWithThrowableBeVerifiable() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        try {
            mock.size();
            fail();
        } catch (RuntimeException e) {}
        
        try {
            mock.clone();
            fail();
        } catch (RuntimeException e) {}
        
        verify(mock).size();
        verify(mock).clone();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldStubbingWithThrowableFailVerification
    public void shouldStubbingWithThrowableFailVerification() {
        when(mock.size()).thenThrow(new RuntimeException());
        stubVoid(mock).toThrow(new RuntimeException()).on().clone();
        
        verifyZeroInteractions(mock);
        
        mock.add("test");
        
        try {
            verify(mock).size();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verify(mock).clone();
            fail();
        } catch (WantedButNotInvoked e) {}
        
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.stubbing.StubbingWithThrowablesTest::shouldShowDecentMessageWhenExcepionIsNaughty
    public void shouldShowDecentMessageWhenExcepionIsNaughty() throws Exception {
        when(mock.add("")).thenThrow(NaughtyException.class);
        mock.add("");
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldVerifyAtLeastXTimes
    public void shouldVerifyAtLeastXTimes() throws Exception {
        
        mock.clear();
        mock.clear();
        mock.clear();

        
        verify(mock, atLeast(2)).clear();
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldFailVerifiationAtLeastXTimes
    public void shouldFailVerifiationAtLeastXTimes() throws Exception {
        mock.add("one");
        verify(mock, atLeast(1)).add(anyString());

        try {
            verify(mock, atLeast(2)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtLeastXVerificationTest::shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes
    public void shouldAllowAtLeastZeroForTheSakeOfVerifyNoMoreInteractionsSometimes() throws Exception {
        
        mock.add("one");
        mock.clear();

        
        verify(mock, atLeast(0)).add("one");
        verify(mock, atLeast(0)).clear();

        verifyNoMoreInteractions(mock);        
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldVerifyAtMostXTimes
    public void shouldVerifyAtMostXTimes() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(2)).clear();
        verify(mock, atMost(3)).clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldWorkWithArgumentMatchers
    public void shouldWorkWithArgumentMatchers() throws Exception {
        mock.add("one");
        verify(mock, atMost(5)).add(anyString());
        
        try {
            verify(mock, atMost(0)).add(anyString());
            fail();
        } catch (MockitoAssertionError e) {}
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowNegativeNumber
    public void shouldNotAllowNegativeNumber() throws Exception {
        try {
            verify(mock, atMost(-1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("Negative value is not allowed here", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldPrintDecentMessage
    public void shouldPrintDecentMessage() throws Exception {
        mock.clear();
        mock.clear();
        
        try {
            verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoAssertionError e) {
            assertEquals("\nWanted at most 1 time but was 2", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldNotAllowInOrderMode
    public void shouldNotAllowInOrderMode() throws Exception {
        mock.clear();
        InOrder inOrder = inOrder(mock);
        
        try {
            inOrder.verify(mock, atMost(1)).clear();
            fail();
        } catch (MockitoException e) {
            assertEquals("AtMost is not implemented to work with InOrder", e.getMessage());
        }
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldMarkInteractionsAsVerified
    public void shouldMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        
        verify(mock, atMost(3)).clear();
        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.AtMostXVerificationTest::shouldDetectUnverifiedInMarkInteractionsAsVerified
    public void shouldDetectUnverifiedInMarkInteractionsAsVerified() throws Exception {
        mock.clear();
        mock.clear();
        undesiredInteraction();
        
        verify(mock, atMost(3)).clear();
        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch(NoInteractionsWanted e) {
            assertContains("undesiredInteraction(", e.getMessage());
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrder
    public void shouldVerifyInOrder() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderUsingAtLeastOnce
    public void shouldVerifyInOrderUsingAtLeastOnce() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(4);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes
    public void shouldVerifyInOrderWhenExpectingSomeInvocationsToBeCalledZeroTimes() {
        inOrder.verify(mockOne, times(0)).oneArg(false);
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockTwo, times(0)).simpleMethod(22);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        inOrder.verify(mockThree, times(0)).oneArg(false);
        verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenFirstMockCalledTwice
    public void shouldFailWhenFirstMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMockCalledTwice
    public void shouldFailWhenLastMockCalledTwice() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWanted
    public void shouldFailOnFirstMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, times(0)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnFirstMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, times(2)).simpleMethod(1);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseFourInvocationsWanted
    public void shouldFailOnSecondMethodBecauseFourInvocationsWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(4)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain
    public void shouldFailOnSecondMethodBecauseTwoInvocationsWantedAgain() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(0)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWanted
    public void shouldFailOnLastMethodBecauseOneInvocationWanted() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(0)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseOneInvocationWantedAgain
    public void shouldFailOnLastMethodBecauseOneInvocationWantedAgain() {
        inOrder.verify(mockOne, atLeastOnce()).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree, atLeastOnce()).simpleMethod(3);
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne, times(2)).simpleMethod(4);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentArgsWanted
    public void shouldFailOnFirstMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(100);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnFirstMethodBecauseDifferentMethodWanted
    public void shouldFailOnFirstMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).oneArg(true);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentArgsWanted
    public void shouldFailOnSecondMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).simpleMethod(-999);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnSecondMethodBecauseDifferentMethodWanted
    public void shouldFailOnSecondMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne, times(1)).simpleMethod(1);
        try {
            inOrder.verify(mockTwo, times(2)).oneArg(true);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentArgsWanted
    public void shouldFailOnLastMethodBecauseDifferentArgsWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(-666);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnLastMethodBecauseDifferentMethodWanted
    public void shouldFailOnLastMethodBecauseDifferentMethodWanted() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);
        try {
            inOrder.verify(mockOne).oneArg(false);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenLastMethodVerifiedFirst
    public void shouldFailWhenLastMethodVerifiedFirst() {
        inOrder.verify(mockOne).simpleMethod(4);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirst
    public void shouldFailWhenMiddleMethodVerifiedFirst() {
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode
    public void shouldFailWhenMiddleMethodVerifiedFirstInAtLeastOnceMode() {
        inOrder.verify(mockTwo, atLeastOnce()).simpleMethod(2);
        try {
            inOrder.verify(mockOne).simpleMethod(1);
            fail();
        } catch (VerificationInOrderFailure e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyNoMoreInteractions
    public void shouldFailOnVerifyNoMoreInteractions() {
        inOrder.verify(mockOne).simpleMethod(1);
        inOrder.verify(mockTwo, times(2)).simpleMethod(2);
        inOrder.verify(mockThree).simpleMethod(3);
        inOrder.verify(mockTwo).simpleMethod(2);

        try {
            verifyNoMoreInteractions(mockOne, mockTwo, mockThree);
            fail();
        } catch (NoInteractionsWanted e) {
        }
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldFailOnVerifyZeroInteractions
    public void shouldFailOnVerifyZeroInteractions() {
        verifyZeroInteractions(mockOne);
    }

// org.mockitousage.verification.BasicVerificationInOrderTest::shouldScreamWhenNullPassed
    public void shouldScreamWhenNullPassed() {
        inOrder(null);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerify
    public void shouldVerify() throws Exception {
        mock.clear();
        verify(mock).clear();

        mock.add("test");
        verify(mock).add("test");

        verifyNoMoreInteractions(mock);
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerification
    public void shouldFailVerification() throws Exception {
        verify(mock).clear();
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailVerificationOnMethodArgument
    public void shouldFailVerificationOnMethodArgument() throws Exception {
        mock.clear();
        mock.add("foo");

        verify(mock).clear();
        try {
            verify(mock).add("bar");
            fail();
        } catch (AssertionError expected) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldFailOnWrongMethod
    public void shouldFailOnWrongMethod() throws Exception {
        mock.clear();
        mock.clear();
        
        mockTwo.add("add");

        verify(mock, atLeastOnce()).clear();
        verify(mockTwo, atLeastOnce()).add("add");
        try {
            verify(mockTwo, atLeastOnce()).add("foo");
            fail();
        } catch (WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectRedundantInvocation
    public void shouldDetectRedundantInvocation() throws Exception {
        mock.clear();
        mock.add("foo");
        mock.add("bar");

        verify(mock).clear();
        verify(mock).add("foo");

        try {
            verifyNoMoreInteractions(mock);
            fail();
        } catch (NoInteractionsWanted e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenInvokedMoreThanOnce
    public void shouldDetectWhenInvokedMoreThanOnce() throws Exception {
        mock.add("foo");
        mock.clear();
        mock.clear();
        
        verify(mock).add("foo");

        try {
            verify(mock).clear();
            fail();
        } catch (TooManyActualInvocations e) {}
    }

// org.mockitousage.verification.BasicVerificationTest::shouldVerifyStubbedMethods
    public void shouldVerifyStubbedMethods() throws Exception {
        when(mock.add("test")).thenReturn(Boolean.FALSE);
        
        mock.add("test");
        
        verify(mock).add("test");
    }

// org.mockitousage.verification.BasicVerificationTest::shouldDetectWhenOverloadedMethodCalled
    public void shouldDetectWhenOverloadedMethodCalled() throws Exception {
        IMethods mockThree = mock(IMethods.class);
        
        mockThree.varargs((Object[]) new Object[] {});
        try {
            verify(mockThree).varargs((String[]) new String[] {});
            fail();
        } catch(WantedButNotInvoked e) {}
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious
    public void shouldPrintVerificationInOrderErrorAndShowBothWantedAndPrevious() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(two, atLeastOnce()).simpleMethod(2);
        
        try {
            inOrder.verify(one, atLeastOnce()).simpleMethod(11);
            fail();
        } catch (VerificationInOrderFailure e) {
            String expected = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(11);" +
                    "\n" +
                    "-> at "; 
            
            assertContains(expected, e.getMessage());
            
            String expectedCause = 
                "\n" +
                "Wanted anywhere AFTER following interaction:" +
                "\n" +
                "iMethods.simpleMethod(2);" +
                "\n" +
                "-> at ";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedOnly
    public void shouldPrintVerificationInOrderErrorAndShowWantedOnly() {
        try {
            inOrder.verify(one).differentMethod();
            fail();
        } catch (WantedButNotInvoked e) {
            String expected = 
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.differentMethod();" +
                    "\n" +
                    "-> at"; 
            
            assertContains(expected, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintVerificationInOrderErrorAndShowWantedAndActual
    public void shouldPrintVerificationInOrderErrorAndShowWantedAndActual() {
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (org.mockito.exceptions.verification.junit.ArgumentsAreDifferent e) {           
            assertContains("has different arguments", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldNotSayArgumentsAreDifferent
    public void shouldNotSayArgumentsAreDifferent() {
        
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(one).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertContains("Wanted but not invoked", e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintMethodThatWasNotInvoked
    public void shouldPrintMethodThatWasNotInvoked() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three).simpleMethod(3);
        try {
            inOrder.verify(three).simpleMethod(999);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure" +
                    "\n" +
                    "Wanted but not invoked:" +
                    "\n" +
                    "iMethods.simpleMethod(999);"; 
            assertContains(expectedMessage, actualMessage);     
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooManyInvocations
    public void shouldPrintTooManyInvocations() {
        inOrder.verify(one).simpleMethod(1);
        inOrder.verify(one).simpleMethod(11);
        try {
            inOrder.verify(two, times(1)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 1 time:" +
                    "\n" +
                    "-> at"; 
            assertContains(expectedMessage, actualMessage);      

            String expectedCause =
                "\n" +
                "But was 2 times. Undesired invocation:" +
                "\n" +
                "-> at";
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesOnVerificationInOrderErrorsTest::shouldPrintTooLittleInvocations
    public void shouldPrintTooLittleInvocations() {
        two.simpleMethod(2);
        
        inOrder.verify(one, atLeastOnce()).simpleMethod(anyInt());
        inOrder.verify(two, times(2)).simpleMethod(2);
        inOrder.verify(three, atLeastOnce()).simpleMethod(3);
        
        try {
            inOrder.verify(two, times(2)).simpleMethod(2);
            fail();
        } catch (VerificationInOrderFailure e) {
            String actualMessage = e.getMessage();
            String expectedMessage = 
                    "\n" +
                    "Verification in order failure:" +
                    "\n" +
                    "iMethods.simpleMethod(2);" +
                    "\n" +
                    "Wanted 2 times:" +
                    "\n" +
                    "-> at";
            assertContains(expectedMessage, actualMessage);
            
            String expectedCause = 
                "\n" +
                "But was 1 time:" +
                "\n" +
                "-> at";
            
            assertContains(expectedCause, e.getMessage());
        }
    }

// org.mockitousage.verification.DescriptiveMessagesWhenTimesXVerificationFailsTest::shouldVerifyActualNumberOfInvocationsSmallerThanWanted
    public void shouldVerifyActualNumberOfInvocationsSmallerThanWanted() throws Exception {
        mock.clear();
        mock.clear();
        mock.clear();

        Mockito.verify(mock, times(3)).clear();
        try {
            Mockito.verify(mock, times(100)).clear();
            fail();
        } catch (TooLittleActualInvocations e) {
            assertContains("mock.clear();", e.getMessage());
            assertContains("Wanted 100 times", e.getMessage());
            assertContains("was 3", e.getMessage());
        }
    }
