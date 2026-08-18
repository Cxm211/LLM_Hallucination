// buggy code
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
            return cs1.equals(cs2);
    }

// relevant test
// org.apache.commons.lang3.math.NumberUtilsTest::testIsNumber
    public void testIsNumber() {
        String val = "12345";
        assertTrue("isNumber(String) 1 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 failed", checkCreateNumber(val));
        val = "1234.5";
        assertTrue("isNumber(String) 2 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 failed", checkCreateNumber(val));
        val = ".12345";
        assertTrue("isNumber(String) 3 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 failed", checkCreateNumber(val));
        val = "1234E5";
        assertTrue("isNumber(String) 4 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 failed", checkCreateNumber(val));
        val = "1234E+5";
        assertTrue("isNumber(String) 5 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 failed", checkCreateNumber(val));
        val = "1234E-5";
        assertTrue("isNumber(String) 6 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 failed", checkCreateNumber(val));
        val = "123.4E5";
        assertTrue("isNumber(String) 7 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 failed", checkCreateNumber(val));
        val = "-1234";
        assertTrue("isNumber(String) 8 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 failed", checkCreateNumber(val));
        val = "-1234.5";
        assertTrue("isNumber(String) 9 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 failed", checkCreateNumber(val));
        val = "-.12345";
        assertTrue("isNumber(String) 10 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 failed", checkCreateNumber(val));
        val = "-1234E5";
        assertTrue("isNumber(String) 11 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 failed", checkCreateNumber(val));
        val = "0";
        assertTrue("isNumber(String) 12 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 failed", checkCreateNumber(val));
        val = "-0";
        assertTrue("isNumber(String) 13 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 failed", checkCreateNumber(val));
        val = "01234";
        assertTrue("isNumber(String) 14 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 failed", checkCreateNumber(val));
        val = "-01234";
        assertTrue("isNumber(String) 15 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 failed", checkCreateNumber(val));
        val = "0xABC123";
        assertTrue("isNumber(String) 16 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 failed", checkCreateNumber(val));
        val = "0x0";
        assertTrue("isNumber(String) 17 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 failed", checkCreateNumber(val));
        val = "123.4E21D";
        assertTrue("isNumber(String) 19 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 failed", checkCreateNumber(val));
        val = "-221.23F";
        assertTrue("isNumber(String) 20 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 failed", checkCreateNumber(val));
        val = "22338L";
        assertTrue("isNumber(String) 21 failed", NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 failed", checkCreateNumber(val));
        val = null;
        assertTrue("isNumber(String) 1 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 1 Neg failed", !checkCreateNumber(val));
        val = "";
        assertTrue("isNumber(String) 2 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 2 Neg failed", !checkCreateNumber(val));
        val = "--2.3";
        assertTrue("isNumber(String) 3 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 3 Neg failed", !checkCreateNumber(val));
        val = ".12.3";
        assertTrue("isNumber(String) 4 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 4 Neg failed", !checkCreateNumber(val));
        val = "-123E";
        assertTrue("isNumber(String) 5 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 5 Neg failed", !checkCreateNumber(val));
        val = "-123E+-212";
        assertTrue("isNumber(String) 6 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 6 Neg failed", !checkCreateNumber(val));
        val = "-123E2.12";
        assertTrue("isNumber(String) 7 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 7 Neg failed", !checkCreateNumber(val));
        val = "0xGF";
        assertTrue("isNumber(String) 8 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 8 Neg failed", !checkCreateNumber(val));
        val = "0xFAE-1";
        assertTrue("isNumber(String) 9 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 9 Neg failed", !checkCreateNumber(val));
        val = ".";
        assertTrue("isNumber(String) 10 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 10 Neg failed", !checkCreateNumber(val));
        val = "-0ABC123";
        assertTrue("isNumber(String) 11 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 11 Neg failed", !checkCreateNumber(val));
        val = "123.4E-D";
        assertTrue("isNumber(String) 12 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 12 Neg failed", !checkCreateNumber(val));
        val = "123.4ED";
        assertTrue("isNumber(String) 13 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 13 Neg failed", !checkCreateNumber(val));
        val = "1234E5l";
        assertTrue("isNumber(String) 14 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 14 Neg failed", !checkCreateNumber(val));
        val = "11a";
        assertTrue("isNumber(String) 15 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 15 Neg failed", !checkCreateNumber(val));
        val = "1a";
        assertTrue("isNumber(String) 16 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 16 Neg failed", !checkCreateNumber(val));
        val = "a";
        assertTrue("isNumber(String) 17 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 17 Neg failed", !checkCreateNumber(val));
        val = "11g";
        assertTrue("isNumber(String) 18 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 18 Neg failed", !checkCreateNumber(val));
        val = "11z";
        assertTrue("isNumber(String) 19 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 19 Neg failed", !checkCreateNumber(val));
        val = "11def";
        assertTrue("isNumber(String) 20 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 20 Neg failed", !checkCreateNumber(val));
        val = "11d11";
        assertTrue("isNumber(String) 21 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 21 Neg failed", !checkCreateNumber(val));
        val = "11 11";
        assertTrue("isNumber(String) 22 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 22 Neg failed", !checkCreateNumber(val));
        val = " 1111";
        assertTrue("isNumber(String) 23 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 23 Neg failed", !checkCreateNumber(val));
        val = "1111 ";
        assertTrue("isNumber(String) 24 Neg failed", !NumberUtils.isNumber(val));
        assertTrue("isNumber(String)/createNumber(String) 24 Neg failed", !checkCreateNumber(val));

        
        val = "2.";
        assertTrue("isNumber(String) LANG-521 failed", NumberUtils.isNumber(val));

        
        val = "1.1L";
        assertFalse("isNumber(String) LANG-664 failed", NumberUtils.isNumber(val));
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testConstants
    public void testConstants() {
        assertTrue(NumberUtils.LONG_ZERO instanceof Long);
        assertTrue(NumberUtils.LONG_ONE instanceof Long);
        assertTrue(NumberUtils.LONG_MINUS_ONE instanceof Long);
        assertTrue(NumberUtils.INTEGER_ZERO instanceof Integer);
        assertTrue(NumberUtils.INTEGER_ONE instanceof Integer);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE instanceof Integer);
        assertTrue(NumberUtils.SHORT_ZERO instanceof Short);
        assertTrue(NumberUtils.SHORT_ONE instanceof Short);
        assertTrue(NumberUtils.SHORT_MINUS_ONE instanceof Short);
        assertTrue(NumberUtils.BYTE_ZERO instanceof Byte);
        assertTrue(NumberUtils.BYTE_ONE instanceof Byte);
        assertTrue(NumberUtils.BYTE_MINUS_ONE instanceof Byte);
        assertTrue(NumberUtils.DOUBLE_ZERO instanceof Double);
        assertTrue(NumberUtils.DOUBLE_ONE instanceof Double);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE instanceof Double);
        assertTrue(NumberUtils.FLOAT_ZERO instanceof Float);
        assertTrue(NumberUtils.FLOAT_ONE instanceof Float);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE instanceof Float);

        assertTrue(NumberUtils.LONG_ZERO.longValue() == 0);
        assertTrue(NumberUtils.LONG_ONE.longValue() == 1);
        assertTrue(NumberUtils.LONG_MINUS_ONE.longValue() == -1);
        assertTrue(NumberUtils.INTEGER_ZERO.intValue() == 0);
        assertTrue(NumberUtils.INTEGER_ONE.intValue() == 1);
        assertTrue(NumberUtils.INTEGER_MINUS_ONE.intValue() == -1);
        assertTrue(NumberUtils.SHORT_ZERO.shortValue() == 0);
        assertTrue(NumberUtils.SHORT_ONE.shortValue() == 1);
        assertTrue(NumberUtils.SHORT_MINUS_ONE.shortValue() == -1);
        assertTrue(NumberUtils.BYTE_ZERO.byteValue() == 0);
        assertTrue(NumberUtils.BYTE_ONE.byteValue() == 1);
        assertTrue(NumberUtils.BYTE_MINUS_ONE.byteValue() == -1);
        assertTrue(NumberUtils.DOUBLE_ZERO.doubleValue() == 0.0d);
        assertTrue(NumberUtils.DOUBLE_ONE.doubleValue() == 1.0d);
        assertTrue(NumberUtils.DOUBLE_MINUS_ONE.doubleValue() == -1.0d);
        assertTrue(NumberUtils.FLOAT_ZERO.floatValue() == 0.0f);
        assertTrue(NumberUtils.FLOAT_ONE.floatValue() == 1.0f);
        assertTrue(NumberUtils.FLOAT_MINUS_ONE.floatValue() == -1.0f);
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testLang300
    public void testLang300() {
        NumberUtils.createNumber("-1l");
        NumberUtils.createNumber("01l");
        NumberUtils.createNumber("1l");
    }

// org.apache.commons.lang3.math.NumberUtilsTest::testLang381
    public void testLang381() {
        assertTrue(Double.isNaN(NumberUtils.min(1.2, 2.5, Double.NaN)));
        assertTrue(Double.isNaN(NumberUtils.max(1.2, 2.5, Double.NaN)));
        assertTrue(Float.isNaN(NumberUtils.min(1.2f, 2.5f, Float.NaN)));
        assertTrue(Float.isNaN(NumberUtils.max(1.2f, 2.5f, Float.NaN)));

        double[] a = new double[] { 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(a)));
        assertTrue(Double.isNaN(NumberUtils.min(a)));

        double[] b = new double[] { Double.NaN, 1.2, Double.NaN, 3.7, 27.0, 42.0, Double.NaN };
        assertTrue(Double.isNaN(NumberUtils.max(b)));
        assertTrue(Double.isNaN(NumberUtils.min(b)));

        float[] aF = new float[] { 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(aF)));

        float[] bF = new float[] { Float.NaN, 1.2f, Float.NaN, 3.7f, 27.0f, 42.0f, Float.NaN };
        assertTrue(Float.isNaN(NumberUtils.max(bF)));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testConstructors
    public void testConstructors() {
        StrBuilder sb0 = new StrBuilder();
        assertEquals(32, sb0.capacity());
        assertEquals(0, sb0.length());
        assertEquals(0, sb0.size());

        StrBuilder sb1 = new StrBuilder(32);
        assertEquals(32, sb1.capacity());
        assertEquals(0, sb1.length());
        assertEquals(0, sb1.size());

        StrBuilder sb2 = new StrBuilder(0);
        assertEquals(32, sb2.capacity());
        assertEquals(0, sb2.length());
        assertEquals(0, sb2.size());

        StrBuilder sb3 = new StrBuilder(-1);
        assertEquals(32, sb3.capacity());
        assertEquals(0, sb3.length());
        assertEquals(0, sb3.size());

        StrBuilder sb4 = new StrBuilder(1);
        assertEquals(1, sb4.capacity());
        assertEquals(0, sb4.length());
        assertEquals(0, sb4.size());

        StrBuilder sb5 = new StrBuilder((String) null);
        assertEquals(32, sb5.capacity());
        assertEquals(0, sb5.length());
        assertEquals(0, sb5.size());

        StrBuilder sb6 = new StrBuilder("");
        assertEquals(32, sb6.capacity());
        assertEquals(0, sb6.length());
        assertEquals(0, sb6.size());

        StrBuilder sb7 = new StrBuilder("foo");
        assertEquals(35, sb7.capacity());
        assertEquals(3, sb7.length());
        assertEquals(3, sb7.size());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testChaining
    public void testChaining() {
        StrBuilder sb = new StrBuilder();
        assertSame(sb, sb.setNewLineText(null));
        assertSame(sb, sb.setNullText(null));
        assertSame(sb, sb.setLength(1));
        assertSame(sb, sb.setCharAt(0, 'a'));
        assertSame(sb, sb.ensureCapacity(0));
        assertSame(sb, sb.minimizeCapacity());
        assertSame(sb, sb.clear());
        assertSame(sb, sb.reverse());
        assertSame(sb, sb.trim());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetSetNewLineText
    public void testGetSetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertEquals(null, sb.getNewLineText());

        sb.setNewLineText("#");
        assertEquals("#", sb.getNewLineText());

        sb.setNewLineText("");
        assertEquals("", sb.getNewLineText());

        sb.setNewLineText((String) null);
        assertEquals(null, sb.getNewLineText());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetSetNullText
    public void testGetSetNullText() {
        StrBuilder sb = new StrBuilder();
        assertEquals(null, sb.getNullText());

        sb.setNullText("null");
        assertEquals("null", sb.getNullText());

        sb.setNullText("");
        assertEquals(null, sb.getNullText());

        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());

        sb.setNullText((String) null);
        assertEquals(null, sb.getNullText());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCapacityAndLength
    public void testCapacityAndLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.ensureCapacity(32);
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.append("foo");
        assertTrue(sb.capacity() >= 32);
        assertEquals(3, sb.length());
        assertEquals(3, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.clear();
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.append("123456789012345678901234567890123");
        assertTrue(sb.capacity() > 32);
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.ensureCapacity(16);
        assertTrue(sb.capacity() > 16);
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.minimizeCapacity();
        assertEquals(33, sb.capacity());
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        try {
            sb.setLength(-1);
            fail("setLength(-1) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.setLength(33);
        assertEquals(33, sb.capacity());
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(16);
        assertTrue(sb.capacity() >= 16);
        assertEquals(16, sb.length());
        assertEquals(16, sb.size());
        assertEquals("1234567890123456", sb.toString());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(32);
        assertTrue(sb.capacity() >= 32);
        assertEquals(32, sb.length());
        assertEquals(32, sb.size());
        assertEquals("1234567890123456\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0", sb.toString());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(0);
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLength
    public void testLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        
        sb.append("Hello");
        assertEquals(5, sb.length());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSetLength
    public void testSetLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        sb.setLength(2);  
        assertEquals("He", sb.toString());
        sb.setLength(2);  
        assertEquals("He", sb.toString());
        sb.setLength(3);  
        assertEquals("He\0", sb.toString());

        try {
            sb.setLength(-1);
            fail("setLength(-1) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCapacity
    public void testCapacity() {
        StrBuilder sb = new StrBuilder();
        assertEquals(sb.buffer.length, sb.capacity());
        
        sb.append("HelloWorldHelloWorldHelloWorldHelloWorld");
        assertEquals(sb.buffer.length, sb.capacity());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEnsureCapacity
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder();
        sb.ensureCapacity(2);
        assertEquals(true, sb.capacity() >= 2);
        
        sb.ensureCapacity(-1);
        assertEquals(true, sb.capacity() >= 0);
        
        sb.append("HelloWorld");
        sb.ensureCapacity(40);
        assertEquals(true, sb.capacity() >= 40);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testMinimizeCapacity
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder();
        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
        
        sb.append("HelloWorld");
        sb.minimizeCapacity();
        assertEquals(10, sb.capacity());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSize
    public void testSize() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.size());
        
        sb.append("Hello");
        assertEquals(5, sb.size());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIsEmpty
    public void testIsEmpty() {
        StrBuilder sb = new StrBuilder();
        assertEquals(true, sb.isEmpty());
        
        sb.append("Hello");
        assertEquals(false, sb.isEmpty());
        
        sb.clear();
        assertEquals(true, sb.isEmpty());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testClear
    public void testClear() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals(true, sb.buffer.length >= 5);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCharAt
    public void testCharAt() {
        StrBuilder sb = new StrBuilder();
        try {
            sb.charAt(0);
            fail("charAt(0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.charAt(-1);
            fail("charAt(-1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        sb.append("foo");
        assertEquals('f', sb.charAt(0));
        assertEquals('o', sb.charAt(1));
        assertEquals('o', sb.charAt(2));
        try {
            sb.charAt(-1);
            fail("charAt(-1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.charAt(3);
            fail("charAt(3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSetCharAt
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder();
        try {
            sb.setCharAt(0, 'f');
            fail("setCharAt(0,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.setCharAt(-1, 'f');
            fail("setCharAt(-1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        sb.append("foo");
        sb.setCharAt(0, 'b');
        sb.setCharAt(1, 'a');
        sb.setCharAt(2, 'r');
        try {
            sb.setCharAt(3, '!');
            fail("setCharAt(3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        assertEquals("bar", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteCharAt
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(0);
        assertEquals("bc", sb.toString()); 
        
        try {
            sb.deleteCharAt(1000);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToCharArray
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder();
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray());

        char[] a = sb.toCharArray();
        assertNotNull("toCharArray() result is null", a);
        assertEquals("toCharArray() result is too large", 0, a.length);

        sb.append("junit");
        a = sb.toCharArray();
        assertEquals("toCharArray() result incorrect length", 5, a.length);
        assertTrue("toCharArray() result does not match", Arrays.equals("junit".toCharArray(), a));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToCharArrayIntInt
    public void testToCharArrayIntInt() {
        StrBuilder sb = new StrBuilder();
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray(0, 0));

        sb.append("junit");
        char[] a = sb.toCharArray(0, 20); 
        assertEquals("toCharArray(int,int) result incorrect length", 5, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("junit".toCharArray(), a));

        a = sb.toCharArray(0, 4);
        assertEquals("toCharArray(int,int) result incorrect length", 4, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("juni".toCharArray(), a));

        a = sb.toCharArray(0, 4);
        assertEquals("toCharArray(int,int) result incorrect length", 4, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("juni".toCharArray(), a));

        a = sb.toCharArray(0, 1);
        assertNotNull("toCharArray(int,int) result is null", a);

        try {
            sb.toCharArray(-1, 5);
            fail("no string index out of bound on -1");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            sb.toCharArray(6, 5);
            fail("no string index out of bound on -1");
        } catch (IndexOutOfBoundsException e) {
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetChars
    public void testGetChars ( ) {
        StrBuilder sb = new StrBuilder();
        
        char[] input = new char[10];
        char[] a = sb.getChars(input);
        assertSame (input, a);
        assertTrue(Arrays.equals(new char[10], a));
        
        sb.append("junit");
        a = sb.getChars(input);
        assertSame(input, a);
        assertTrue(Arrays.equals(new char[] {'j','u','n','i','t',0,0,0,0,0},a));
        
        a = sb.getChars(null);
        assertNotSame(input,a);
        assertEquals(5,a.length);
        assertTrue(Arrays.equals("junit".toCharArray(),a));
        
        input = new char[5];
        a = sb.getChars(input);
        assertSame(input, a);
        
        input = new char[4];
        a = sb.getChars(input);
        assertNotSame(input, a);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetCharsIntIntCharArrayInt
    public void testGetCharsIntIntCharArrayInt( ) {
        StrBuilder sb = new StrBuilder();
               
        sb.append("junit");
        char[] a = new char[5];
        sb.getChars(0,5,a,0);
        assertTrue(Arrays.equals(new char[] {'j','u','n','i','t'},a));
        
        a = new char[5];
        sb.getChars(0,2,a,3);
        assertTrue(Arrays.equals(new char[] {0,0,0,'j','u'},a));
        
        try {
            sb.getChars(-1,0,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(0,-1,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(0,20,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(4,2,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteIntInt
    public void testDeleteIntInt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(0, 1);
        assertEquals("bc", sb.toString()); 
        sb.delete(1, 2);
        assertEquals("b", sb.toString());
        sb.delete(0, 1);
        assertEquals("", sb.toString()); 
        sb.delete(0, 1000);
        assertEquals("", sb.toString()); 
        
        try {
            sb.delete(1, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        try {
            sb.delete(-1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        
        sb = new StrBuilder("anything");
        try {
            sb.delete(2, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_char
    public void testDeleteAll_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteAll('X');
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll('a');
        assertEquals("bcbccb", sb.toString());
        sb.deleteAll('c');
        assertEquals("bbb", sb.toString());
        sb.deleteAll('b');
        assertEquals("", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll('b');
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_char
    public void testDeleteFirst_char() {
        StrBuilder sb = new StrBuilder("abcba");
        sb.deleteFirst('X');
        assertEquals("abcba", sb.toString());
        sb.deleteFirst('a');
        assertEquals("bcba", sb.toString());
        sb.deleteFirst('c');
        assertEquals("bba", sb.toString());
        sb.deleteFirst('b');
        assertEquals("ba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst('b');
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_String
    public void testDeleteAll_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteAll((String) null);
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll("");
        assertEquals("abcbccba", sb.toString());
        
        sb.deleteAll("X");
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll("a");
        assertEquals("bcbccb", sb.toString());
        sb.deleteAll("c");
        assertEquals("bbb", sb.toString());
        sb.deleteAll("b");
        assertEquals("", sb.toString());

        sb = new StrBuilder("abcbccba");
        sb.deleteAll("bc");
        assertEquals("acba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll("bc");
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_String
    public void testDeleteFirst_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteFirst((String) null);
        assertEquals("abcbccba", sb.toString());
        sb.deleteFirst("");
        assertEquals("abcbccba", sb.toString());

        sb.deleteFirst("X");
        assertEquals("abcbccba", sb.toString());
        sb.deleteFirst("a");
        assertEquals("bcbccba", sb.toString());
        sb.deleteFirst("c");
        assertEquals("bbccba", sb.toString());
        sb.deleteFirst("b");
        assertEquals("bccba", sb.toString());

        sb = new StrBuilder("abcbccba");
        sb.deleteFirst("bc");
        assertEquals("abccba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst("bc");
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_StrMatcher
    public void testDeleteAll_StrMatcher() {
        StrBuilder sb = new StrBuilder("A0xA1A2yA3");
        sb.deleteAll((StrMatcher) null);
        assertEquals("A0xA1A2yA3", sb.toString());
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("xy", sb.toString());

        sb = new StrBuilder("Ax1");
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("Ax1", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_StrMatcher
    public void testDeleteFirst_StrMatcher() {
        StrBuilder sb = new StrBuilder("A0xA1A2yA3");
        sb.deleteFirst((StrMatcher) null);
        assertEquals("A0xA1A2yA3", sb.toString());
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("xA1A2yA3", sb.toString());

        sb = new StrBuilder("Ax1");
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("Ax1", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_int_int_String
    public void testReplace_int_int_String() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replace(0, 1, "d");
        assertEquals("dbc", sb.toString());
        sb.replace(0, 1, "aaa");
        assertEquals("aaabc", sb.toString());
        sb.replace(0, 3, "");
        assertEquals("bc", sb.toString());
        sb.replace(1, 2, (String) null);
        assertEquals("b", sb.toString());
        sb.replace(1, 1000, "text");
        assertEquals("btext", sb.toString());
        sb.replace(0, 1000, "text");
        assertEquals("text", sb.toString());
        
        sb = new StrBuilder("atext");
        sb.replace(1, 1, "ny");
        assertEquals("anytext", sb.toString());
        try {
            sb.replace(2, 1, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        
        sb = new StrBuilder();
        try {
            sb.replace(1, 2, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        try {
            sb.replace(-1, 1, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_char_char
    public void testReplaceAll_char_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll('x', 'y');
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll('a', 'd');
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll('b', 'e');
        assertEquals("dececced", sb.toString());
        sb.replaceAll('c', 'f');
        assertEquals("defeffed", sb.toString());
        sb.replaceAll('d', 'd');
        assertEquals("defeffed", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_char_char
    public void testReplaceFirst_char_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst('x', 'y');
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst('a', 'd');
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst('b', 'e');
        assertEquals("decbccba", sb.toString());
        sb.replaceFirst('c', 'f');
        assertEquals("defbccba", sb.toString());
        sb.replaceFirst('d', 'd');
        assertEquals("defbccba", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_String_String
    public void testReplaceAll_String_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll((String) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll((String) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("", null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("", "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceAll("x", "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("a", "d");
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll("d", null);
        assertEquals("bcbccb", sb.toString());
        sb.replaceAll("cb", "-");
        assertEquals("b-c-", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceAll("b", "xbx");
        assertEquals("axbxcxbxa", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceAll("b", "xbx");
        assertEquals("xbxxbx", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_String_String
    public void testReplaceFirst_String_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst((String) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst((String) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("", null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("", "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceFirst("x", "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("a", "d");
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst("d", null);
        assertEquals("bcbccba", sb.toString());
        sb.replaceFirst("cb", "-");
        assertEquals("b-ccba", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceFirst("b", "xbx");
        assertEquals("axbxcba", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceFirst("b", "xbx");
        assertEquals("xbxb", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_StrMatcher_String
    public void testReplaceAll_StrMatcher_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll((StrMatcher) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll((StrMatcher) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.noneMatcher(), null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.noneMatcher(), "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceAll(StrMatcher.charMatcher('x'), "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.charMatcher('a'), "d");
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll(StrMatcher.charMatcher('d'), null);
        assertEquals("bcbccb", sb.toString());
        sb.replaceAll(StrMatcher.stringMatcher("cb"), "-");
        assertEquals("b-c-", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceAll(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("axbxcxbxa", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceAll(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("xbxxbx", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replaceAll(A_NUMBER_MATCHER, "***");
        assertEquals("***-******-***", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_StrMatcher_String
    public void testReplaceFirst_StrMatcher_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst((StrMatcher) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst((StrMatcher) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.noneMatcher(), null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.noneMatcher(), "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceFirst(StrMatcher.charMatcher('x'), "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.charMatcher('a'), "d");
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.charMatcher('d'), null);
        assertEquals("bcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.stringMatcher("cb"), "-");
        assertEquals("b-ccba", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceFirst(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("axbxcba", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceFirst(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("xbxb", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replaceFirst(A_NUMBER_MATCHER, "***");
        assertEquals("***-A2A3-A4", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryMatcher
    public void testReplace_StrMatcher_String_int_int_int_VaryMatcher() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replace((StrMatcher) null, "x", 0, sb.length(), -1);
        assertEquals("abcbccba", sb.toString());
        
        sb.replace(StrMatcher.charMatcher('a'), "x", 0, sb.length(), -1);
        assertEquals("xbcbccbx", sb.toString());
        
        sb.replace(StrMatcher.stringMatcher("cb"), "x", 0, sb.length(), -1);
        assertEquals("xbxcxx", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replace(A_NUMBER_MATCHER, "***", 0, sb.length(), -1);
        assertEquals("***-******-***", sb.toString());
        
        sb = new StrBuilder();
        sb.replace(A_NUMBER_MATCHER, "***", 0, sb.length(), -1);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryReplace
    public void testReplace_StrMatcher_String_int_int_int_VaryReplace() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "cb", 0, sb.length(), -1);
        assertEquals("abcbccba", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "-", 0, sb.length(), -1);
        assertEquals("ab-c-a", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "+++", 0, sb.length(), -1);
        assertEquals("ab+++c+++a", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "", 0, sb.length(), -1);
        assertEquals("abca", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), null, 0, sb.length(), -1);
        assertEquals("abca", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryStartIndex
    public void testReplace_StrMatcher_String_int_int_int_VaryStartIndex() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, sb.length(), -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 1, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 2, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 3, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 4, sb.length(), -1);
        assertEquals("aaxa-ay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 5, sb.length(), -1);
        assertEquals("aaxaa-y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 6, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 7, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 8, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 9, sb.length(), -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 10, sb.length(), -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", 11, sb.length(), -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", -1, sb.length(), -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryEndIndex
    public void testReplace_StrMatcher_String_int_int_int_VaryEndIndex() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 0, -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 2, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 3, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 4, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 5, -1);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 6, -1);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 7, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 8, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 9, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 1000, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", 2, 1, -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryCount
    public void testReplace_StrMatcher_String_int_int_int_VaryCount() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 0);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 2);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 3);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 4);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 5);
        assertEquals("-x--y-", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReverse
    public void testReverse() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.reverse().toString());
        
        sb.clear().append(true);
        assertEquals("eurt", sb.reverse().toString());
        assertEquals("true", sb.reverse().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testTrim
    public void testTrim() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.reverse().toString());
        
        sb.clear().append(" \u0000 ");
        assertEquals("", sb.trim().toString());
        
        sb.clear().append(" \u0000 a b c");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append("a b c \u0000 ");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append(" \u0000 a b c \u0000 ");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append("a b c");
        assertEquals("a b c", sb.trim().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testStartsWith
    public void testStartsWith() {
        StrBuilder sb = new StrBuilder();
        assertFalse(sb.startsWith("a"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        sb.append("abc");
        assertTrue(sb.startsWith("a"));
        assertTrue(sb.startsWith("ab"));
        assertTrue(sb.startsWith("abc"));
        assertFalse(sb.startsWith("cba"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEndsWith
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder();
        assertFalse(sb.endsWith("a"));
        assertFalse(sb.endsWith("c"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith(null));
        sb.append("abc");
        assertTrue(sb.endsWith("c"));
        assertTrue(sb.endsWith("bc"));
        assertTrue(sb.endsWith("abc"));
        assertFalse(sb.endsWith("cba"));
        assertFalse(sb.endsWith("abcd"));
        assertFalse(sb.endsWith(" abc"));
        assertFalse(sb.endsWith("abc "));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubSequenceIntInt
    public void testSubSequenceIntInt() {
       StrBuilder sb = new StrBuilder ("hello goodbye");
       
       try {
            sb.subSequence(-1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
       try {
            sb.subSequence(2, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        try {
            sb.subSequence(2, sb.length() + 1);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        try {
            sb.subSequence(3, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        assertEquals ("hello", sb.subSequence(0, 5));
        assertEquals ("hello goodbye".subSequence(0, 6), sb.subSequence(0, 6));
        assertEquals ("goodbye", sb.subSequence(6, 13));
        assertEquals ("hello goodbye".subSequence(6,13), sb.subSequence(6, 13));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubstringInt
    public void testSubstringInt() {
        StrBuilder sb = new StrBuilder ("hello goodbye");
        assertEquals ("goodbye", sb.substring(6));
        assertEquals ("hello goodbye".substring(6), sb.substring(6));
        assertEquals ("hello goodbye", sb.substring(0));
        assertEquals ("hello goodbye".substring(0), sb.substring(0));
        try {
            sb.substring(-1);
            fail ();
        } catch (IndexOutOfBoundsException e) {}
        
        try {
            sb.substring(15);
            fail ();
        } catch (IndexOutOfBoundsException e) {}
    
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubstringIntInt
    public void testSubstringIntInt() {
        StrBuilder sb = new StrBuilder ("hello goodbye");
        assertEquals ("hello", sb.substring(0, 5));
        assertEquals ("hello goodbye".substring(0, 6), sb.substring(0, 6));
        
        assertEquals ("goodbye", sb.substring(6, 13));
        assertEquals ("hello goodbye".substring(6,13), sb.substring(6, 13));
        
        assertEquals ("goodbye", sb.substring(6, 20));
        
        try {
            sb.substring(-1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        try {
            sb.substring(15, 20);
            fail();
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testMidString
    public void testMidString() {
        StrBuilder sb = new StrBuilder("hello goodbye hello");
        assertEquals("goodbye", sb.midString(6, 7));
        assertEquals("hello", sb.midString(0, 5));
        assertEquals("hello", sb.midString(-5, 5));
        assertEquals("", sb.midString(0, -1));
        assertEquals("", sb.midString(20, 2));
        assertEquals("hello", sb.midString(14, 22));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testRightString
    public void testRightString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("right", sb.rightString(5));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-5));
        assertEquals("left right", sb.rightString(15));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLeftString
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("left", sb.leftString(4));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-5));
        assertEquals("left right", sb.leftString(15));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_char
    public void testContains_char() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains('a'));
        assertEquals(true, sb.contains('o'));
        assertEquals(true, sb.contains('z'));
        assertEquals(false, sb.contains('1'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_String
    public void testContains_String() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains("a"));
        assertEquals(true, sb.contains("pq"));
        assertEquals(true, sb.contains("z"));
        assertEquals(false, sb.contains("zyx"));
        assertEquals(false, sb.contains((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_StrMatcher
    public void testContains_StrMatcher() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains(StrMatcher.charMatcher('a')));
        assertEquals(true, sb.contains(StrMatcher.stringMatcher("pq")));
        assertEquals(true, sb.contains(StrMatcher.charMatcher('z')));
        assertEquals(false, sb.contains(StrMatcher.stringMatcher("zy")));
        assertEquals(false, sb.contains((StrMatcher) null));

        sb = new StrBuilder();
        assertEquals(false, sb.contains(A_NUMBER_MATCHER));
        sb.append("B A1 C");
        assertEquals(true, sb.contains(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_char
    public void testIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf('a'));
        
        
        assertEquals("abab".indexOf('a'), sb.indexOf('a'));

        assertEquals(1, sb.indexOf('b'));
        assertEquals("abab".indexOf('b'), sb.indexOf('b'));

        assertEquals(-1, sb.indexOf('z'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_char_int
    public void testIndexOf_char_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf('a', -1));
        assertEquals(0, sb.indexOf('a', 0));
        assertEquals(2, sb.indexOf('a', 1));
        assertEquals(-1, sb.indexOf('a', 4));
        assertEquals(-1, sb.indexOf('a', 5));

        
        assertEquals("abab".indexOf('a', 1), sb.indexOf('a', 1));

        assertEquals(3, sb.indexOf('b', 2));
        assertEquals("abab".indexOf('b', 2), sb.indexOf('b', 2));

        assertEquals(-1, sb.indexOf('z', 2));

        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.indexOf('z', 0));
        assertEquals(-1, sb.indexOf('z', 3));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_char
    public void testLastIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals (2, sb.lastIndexOf('a'));
        
        assertEquals ("abab".lastIndexOf('a'), sb.lastIndexOf('a'));
        
        assertEquals(3, sb.lastIndexOf('b'));
        assertEquals ("abab".lastIndexOf('b'), sb.lastIndexOf('b'));
        
        assertEquals (-1, sb.lastIndexOf('z'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_char_int
    public void testLastIndexOf_char_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(-1, sb.lastIndexOf('a', -1));
        assertEquals(0, sb.lastIndexOf('a', 0));
        assertEquals(0, sb.lastIndexOf('a', 1));

        
        assertEquals("abab".lastIndexOf('a', 1), sb.lastIndexOf('a', 1));

        assertEquals(1, sb.lastIndexOf('b', 2));
        assertEquals("abab".lastIndexOf('b', 2), sb.lastIndexOf('b', 2));

        assertEquals(-1, sb.lastIndexOf('z', 2));

        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.lastIndexOf('z', sb.length()));
        assertEquals(-1, sb.lastIndexOf('z', 1));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_String
    public void testIndexOf_String() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals(0, sb.indexOf("a"));
        
        assertEquals("abab".indexOf("a"), sb.indexOf("a"));
        
        assertEquals(0, sb.indexOf("ab"));
        
        assertEquals("abab".indexOf("ab"), sb.indexOf("ab"));
        
        assertEquals(1, sb.indexOf("b"));
        assertEquals("abab".indexOf("b"), sb.indexOf("b"));
        
        assertEquals(1, sb.indexOf("ba"));
        assertEquals("abab".indexOf("ba"), sb.indexOf("ba"));
        
        assertEquals(-1, sb.indexOf("z"));
        
        assertEquals(-1, sb.indexOf((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_String_int
    public void testIndexOf_String_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf("a", -1));
        assertEquals(0, sb.indexOf("a", 0));
        assertEquals(2, sb.indexOf("a", 1));
        assertEquals(2, sb.indexOf("a", 2));
        assertEquals(-1, sb.indexOf("a", 3));
        assertEquals(-1, sb.indexOf("a", 4));
        assertEquals(-1, sb.indexOf("a", 5));
        
        assertEquals(-1, sb.indexOf("abcdef", 0));
        assertEquals(0, sb.indexOf("", 0));
        assertEquals(1, sb.indexOf("", 1));
        
        
        assertEquals ("abab".indexOf("a", 1), sb.indexOf("a", 1));
        
        assertEquals(2, sb.indexOf("ab", 1));
        
        assertEquals("abab".indexOf("ab", 1), sb.indexOf("ab", 1));
        
        assertEquals(3, sb.indexOf("b", 2));
        assertEquals("abab".indexOf("b", 2), sb.indexOf("b", 2));
        
        assertEquals(1, sb.indexOf("ba", 1));
        assertEquals("abab".indexOf("ba", 2), sb.indexOf("ba", 2));
        
        assertEquals(-1, sb.indexOf("z", 2));
        
        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.indexOf("za", 0));
        assertEquals(-1, sb.indexOf("za", 3));
        
        assertEquals(-1, sb.indexOf((String) null, 2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_String
    public void testLastIndexOf_String() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals(2, sb.lastIndexOf("a"));
        
        assertEquals("abab".lastIndexOf("a"), sb.lastIndexOf("a"));
        
        assertEquals(2, sb.lastIndexOf("ab"));
        
        assertEquals("abab".lastIndexOf("ab"), sb.lastIndexOf("ab"));
        
        assertEquals(3, sb.lastIndexOf("b"));
        assertEquals("abab".lastIndexOf("b"), sb.lastIndexOf("b"));
        
        assertEquals(1, sb.lastIndexOf("ba"));
        assertEquals("abab".lastIndexOf("ba"), sb.lastIndexOf("ba"));
        
        assertEquals(-1, sb.lastIndexOf("z"));
        
        assertEquals(-1, sb.lastIndexOf((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_String_int
    public void testLastIndexOf_String_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(-1, sb.lastIndexOf("a", -1));
        assertEquals(0, sb.lastIndexOf("a", 0));
        assertEquals(0, sb.lastIndexOf("a", 1));
        assertEquals(2, sb.lastIndexOf("a", 2));
        assertEquals(2, sb.lastIndexOf("a", 3));
        assertEquals(2, sb.lastIndexOf("a", 4));
        assertEquals(2, sb.lastIndexOf("a", 5));
        
        assertEquals(-1, sb.lastIndexOf("abcdef", 3));
        assertEquals("abab".lastIndexOf("", 3), sb.lastIndexOf("", 3));
        assertEquals("abab".lastIndexOf("", 1), sb.lastIndexOf("", 1));
        
        
        assertEquals("abab".lastIndexOf("a", 1), sb.lastIndexOf("a", 1));
        
        assertEquals(0, sb.lastIndexOf("ab", 1));
        
        assertEquals("abab".lastIndexOf("ab", 1), sb.lastIndexOf("ab", 1));
        
        assertEquals(1, sb.lastIndexOf("b", 2));
        assertEquals("abab".lastIndexOf("b", 2), sb.lastIndexOf("b", 2));
        
        assertEquals(1, sb.lastIndexOf("ba", 2));
        assertEquals("abab".lastIndexOf("ba", 2), sb.lastIndexOf("ba", 2));
        
        assertEquals(-1, sb.lastIndexOf("z", 2));
        
        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.lastIndexOf("za", sb.length()));
        assertEquals(-1, sb.lastIndexOf("za", 1));
        
        assertEquals(-1, sb.lastIndexOf((String) null, 2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_StrMatcher
    public void testIndexOf_StrMatcher() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a')));
        
        sb.append("ab bd");
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a')));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b')));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher()));
        assertEquals(4, sb.indexOf(StrMatcher.charMatcher('d')));
        assertEquals(-1, sb.indexOf(StrMatcher.noneMatcher()));
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        
        sb.append(" A1 junction");
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_StrMatcher_int
    public void testIndexOf_StrMatcher_int() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.indexOf((StrMatcher) null, 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 0));
        
        sb.append("ab bd");
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a'), -2));
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 20));
        
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), -1));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), 0));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), 1));
        assertEquals(3, sb.indexOf(StrMatcher.charMatcher('b'), 2));
        assertEquals(3, sb.indexOf(StrMatcher.charMatcher('b'), 3));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 4));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 5));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 6));
        
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), -2));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), 0));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.spaceMatcher(), 4));
        assertEquals(-1, sb.indexOf(StrMatcher.spaceMatcher(), 20));
        
        assertEquals(-1, sb.indexOf(StrMatcher.noneMatcher(), 0));
        assertEquals(-1, sb.indexOf((StrMatcher) null, 0));
        
        sb.append(" A1 junction with A2");
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER, 5));
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER, 6));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 7));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 22));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 23));
        assertEquals(-1, sb.indexOf(A_NUMBER_MATCHER, 24));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_StrMatcher
    public void testLastIndexOf_StrMatcher() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        
        sb.append("ab bd");
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b')));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher()));
        assertEquals(4, sb.lastIndexOf(StrMatcher.charMatcher('d')));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.noneMatcher()));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        
        sb.append(" A1 junction");
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_StrMatcher_int
    public void testLastIndexOf_StrMatcher_int() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null, 2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -1));
        
        sb.append("ab bd");
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -2));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 20));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('b'), -1));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 0));
        assertEquals(1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 1));
        assertEquals(1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 2));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 3));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 4));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 5));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 6));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.spaceMatcher(), -2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.spaceMatcher(), 0));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 2));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 4));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 20));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.noneMatcher(), 0));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null, 0));
        
        sb.append(" A1 junction with A2");
        assertEquals(-1, sb.lastIndexOf(A_NUMBER_MATCHER, 5));
        assertEquals(-1, sb.lastIndexOf(A_NUMBER_MATCHER, 6)); 
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 7));
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 22));
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 23)); 
        assertEquals(23, sb.lastIndexOf(A_NUMBER_MATCHER, 24));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsTokenizer
    public void testAsTokenizer() throws Exception {
        
        StrBuilder b = new StrBuilder();
        b.append("a b ");
        StrTokenizer t = b.asTokenizer();
        
        String[] tokens1 = t.getTokenArray();
        assertEquals(2, tokens1.length);
        assertEquals("a", tokens1[0]);
        assertEquals("b", tokens1[1]);
        assertEquals(2, t.size());
        
        b.append("c d ");
        String[] tokens2 = t.getTokenArray();
        assertEquals(2, tokens2.length);
        assertEquals("a", tokens2[0]);
        assertEquals("b", tokens2[1]);
        assertEquals(2, t.size());
        assertEquals("a", t.next());
        assertEquals("b", t.next());
        
        t.reset();
        String[] tokens3 = t.getTokenArray();
        assertEquals(4, tokens3.length);
        assertEquals("a", tokens3[0]);
        assertEquals("b", tokens3[1]);
        assertEquals("c", tokens3[2]);
        assertEquals("d", tokens3[3]);
        assertEquals(4, t.size());
        assertEquals("a", t.next());
        assertEquals("b", t.next());
        assertEquals("c", t.next());
        assertEquals("d", t.next());
        
        assertEquals("a b c d ", t.getContent());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsReader
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("some text");
        Reader reader = sb.asReader();
        assertEquals(true, reader.ready());
        char[] buf = new char[40];
        assertEquals(9, reader.read(buf));
        assertEquals("some text", new String(buf, 0, 9));
        
        assertEquals(-1, reader.read());
        assertEquals(false, reader.ready());
        assertEquals(0, reader.skip(2));
        assertEquals(0, reader.skip(-1));
        
        assertEquals(true, reader.markSupported());
        reader = sb.asReader();
        assertEquals('s', reader.read());
        reader.mark(-1);
        char[] array = new char[3];
        assertEquals(3, reader.read(array, 0, 3));
        assertEquals('o', array[0]);
        assertEquals('m', array[1]);
        assertEquals('e', array[2]);
        reader.reset();
        assertEquals(1, reader.read(array, 1, 1));
        assertEquals('o', array[0]);
        assertEquals('o', array[1]);
        assertEquals('e', array[2]);
        assertEquals(2, reader.skip(2));
        assertEquals(' ', reader.read());
        
        assertEquals(true, reader.ready());
        reader.close();
        assertEquals(true, reader.ready());
        
        reader = sb.asReader();
        array = new char[3];
        try {
            reader.read(array, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 100, 1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 0, 100);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        
        assertEquals(0, reader.read(array, 0, 0));
        assertEquals(0, array[0]);
        assertEquals(0, array[1]);
        assertEquals(0, array[2]);
        
        reader.skip(9);
        assertEquals(-1, reader.read(array, 0, 1));
        
        reader.reset();
        array = new char[30];
        assertEquals(9, reader.read(array, 0, 30));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsWriter
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder("base");
        Writer writer = sb.asWriter();
        
        writer.write('l');
        assertEquals("basel", sb.toString());
        
        writer.write(new char[] {'i', 'n'});
        assertEquals("baselin", sb.toString());
        
        writer.write(new char[] {'n', 'e', 'r'}, 1, 2);
        assertEquals("baseliner", sb.toString());
        
        writer.write(" rout");
        assertEquals("baseliner rout", sb.toString());
        
        writer.write("ping that server", 1, 3);
        assertEquals("baseliner routing", sb.toString());
        
        writer.flush();  
        assertEquals("baseliner routing", sb.toString());
        
        writer.close();  
        assertEquals("baseliner routing", sb.toString());
        
        writer.write(" hi");  
        assertEquals("baseliner routing hi", sb.toString());
        
        sb.setLength(4);  
        writer.write('d');
        assertEquals("based", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEqualsIgnoreCase
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder();
        StrBuilder sb2 = new StrBuilder();
        assertEquals(true, sb1.equalsIgnoreCase(sb1));
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        assertEquals(true, sb2.equalsIgnoreCase(sb2));
        
        sb1.append("abc");
        assertEquals(false, sb1.equalsIgnoreCase(sb2));
        
        sb2.append("ABC");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        
        sb2.clear().append("abc");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        assertEquals(true, sb1.equalsIgnoreCase(sb1));
        assertEquals(true, sb2.equalsIgnoreCase(sb2));
        
        sb2.clear().append("aBc");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEquals
    public void testEquals() {
        StrBuilder sb1 = new StrBuilder();
        StrBuilder sb2 = new StrBuilder();
        assertEquals(true, sb1.equals(sb2));
        assertEquals(true, sb1.equals(sb1));
        assertEquals(true, sb2.equals(sb2));
        assertEquals(true, sb1.equals((Object) sb2));
        
        sb1.append("abc");
        assertEquals(false, sb1.equals(sb2));
        assertEquals(false, sb1.equals((Object) sb2));
        
        sb2.append("ABC");
        assertEquals(false, sb1.equals(sb2));
        assertEquals(false, sb1.equals((Object) sb2));
        
        sb2.clear().append("abc");
        assertEquals(true, sb1.equals(sb2));
        assertEquals(true, sb1.equals((Object) sb2));
        
        assertEquals(false, sb1.equals(Integer.valueOf(1)));
        assertEquals(false, sb1.equals("abc"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testHashCode
    public void testHashCode() {
        StrBuilder sb = new StrBuilder();
        int hc1a = sb.hashCode();
        int hc1b = sb.hashCode();
        assertEquals(0, hc1a);
        assertEquals(hc1a, hc1b);
        
        sb.append("abc");
        int hc2a = sb.hashCode();
        int hc2b = sb.hashCode();
        assertEquals(true, hc2a != 0);
        assertEquals(hc2a, hc2b);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToString
    public void testToString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToStringBuffer
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder();
        assertEquals(new StringBuffer().toString(), sb.toStringBuffer().toString());
        
        sb.append("junit");
        assertEquals(new StringBuffer("junit").toString(), sb.toStringBuffer().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang294
    public void testLang294() {
        StrBuilder sb = new StrBuilder("\n%BLAH%\nDo more stuff\neven more stuff\n%BLAH%\n");
        sb.deleteAll("\n%BLAH%");
        assertEquals("\nDo more stuff\neven more stuff\n", sb.toString()); 
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOfLang294
    public void testIndexOfLang294() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertEquals(-1, sb.indexOf("three"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang295
    public void testLang295() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertFalse( "The contains(char) method is looking beyond the end of the string", sb.contains('h'));
        assertEquals( "The indexOf(char) method is looking beyond the end of the string", -1, sb.indexOf('h'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang412Right
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadRight correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang412Left
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadLeft correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrMatcherTest::testCommaMatcher
    public void testCommaMatcher() {
        StrMatcher matcher = StrMatcher.commaMatcher();
        assertSame(matcher, StrMatcher.commaMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 0));
        assertEquals(1, matcher.isMatch(BUFFER1, 1));
        assertEquals(0, matcher.isMatch(BUFFER1, 2));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testTabMatcher
    public void testTabMatcher() {
        StrMatcher matcher = StrMatcher.tabMatcher();
        assertSame(matcher, StrMatcher.tabMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 2));
        assertEquals(1, matcher.isMatch(BUFFER1, 3));
        assertEquals(0, matcher.isMatch(BUFFER1, 4));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testSpaceMatcher
    public void testSpaceMatcher() {
        StrMatcher matcher = StrMatcher.spaceMatcher();
        assertSame(matcher, StrMatcher.spaceMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 4));
        assertEquals(1, matcher.isMatch(BUFFER1, 5));
        assertEquals(0, matcher.isMatch(BUFFER1, 6));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testSplitMatcher
    public void testSplitMatcher() {
        StrMatcher matcher = StrMatcher.splitMatcher();
        assertSame(matcher, StrMatcher.splitMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 2));
        assertEquals(1, matcher.isMatch(BUFFER1, 3));
        assertEquals(0, matcher.isMatch(BUFFER1, 4));
        assertEquals(1, matcher.isMatch(BUFFER1, 5));
        assertEquals(0, matcher.isMatch(BUFFER1, 6));
        assertEquals(1, matcher.isMatch(BUFFER1, 7));
        assertEquals(1, matcher.isMatch(BUFFER1, 8));
        assertEquals(1, matcher.isMatch(BUFFER1, 9));
        assertEquals(0, matcher.isMatch(BUFFER1, 10));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testTrimMatcher
    public void testTrimMatcher() {
        StrMatcher matcher = StrMatcher.trimMatcher();
        assertSame(matcher, StrMatcher.trimMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 2));
        assertEquals(1, matcher.isMatch(BUFFER1, 3));
        assertEquals(0, matcher.isMatch(BUFFER1, 4));
        assertEquals(1, matcher.isMatch(BUFFER1, 5));
        assertEquals(0, matcher.isMatch(BUFFER1, 6));
        assertEquals(1, matcher.isMatch(BUFFER1, 7));
        assertEquals(1, matcher.isMatch(BUFFER1, 8));
        assertEquals(1, matcher.isMatch(BUFFER1, 9));
        assertEquals(1, matcher.isMatch(BUFFER1, 10));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testSingleQuoteMatcher
    public void testSingleQuoteMatcher() {
        StrMatcher matcher = StrMatcher.singleQuoteMatcher();
        assertSame(matcher, StrMatcher.singleQuoteMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 10));
        assertEquals(1, matcher.isMatch(BUFFER1, 11));
        assertEquals(0, matcher.isMatch(BUFFER1, 12));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testDoubleQuoteMatcher
    public void testDoubleQuoteMatcher() {
        StrMatcher matcher = StrMatcher.doubleQuoteMatcher();
        assertSame(matcher, StrMatcher.doubleQuoteMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 11));
        assertEquals(1, matcher.isMatch(BUFFER1, 12));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testQuoteMatcher
    public void testQuoteMatcher() {
        StrMatcher matcher = StrMatcher.quoteMatcher();
        assertSame(matcher, StrMatcher.quoteMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 10));
        assertEquals(1, matcher.isMatch(BUFFER1, 11));
        assertEquals(1, matcher.isMatch(BUFFER1, 12));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testNoneMatcher
    public void testNoneMatcher() {
        StrMatcher matcher = StrMatcher.noneMatcher();
        assertSame(matcher, StrMatcher.noneMatcher());
        assertEquals(0, matcher.isMatch(BUFFER1, 0));
        assertEquals(0, matcher.isMatch(BUFFER1, 1));
        assertEquals(0, matcher.isMatch(BUFFER1, 2));
        assertEquals(0, matcher.isMatch(BUFFER1, 3));
        assertEquals(0, matcher.isMatch(BUFFER1, 4));
        assertEquals(0, matcher.isMatch(BUFFER1, 5));
        assertEquals(0, matcher.isMatch(BUFFER1, 6));
        assertEquals(0, matcher.isMatch(BUFFER1, 7));
        assertEquals(0, matcher.isMatch(BUFFER1, 8));
        assertEquals(0, matcher.isMatch(BUFFER1, 9));
        assertEquals(0, matcher.isMatch(BUFFER1, 10));
        assertEquals(0, matcher.isMatch(BUFFER1, 11));
        assertEquals(0, matcher.isMatch(BUFFER1, 12));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testCharMatcher_char
    public void testCharMatcher_char() {
        StrMatcher matcher = StrMatcher.charMatcher('c');
        assertEquals(0, matcher.isMatch(BUFFER2, 0));
        assertEquals(0, matcher.isMatch(BUFFER2, 1));
        assertEquals(1, matcher.isMatch(BUFFER2, 2));
        assertEquals(0, matcher.isMatch(BUFFER2, 3));
        assertEquals(0, matcher.isMatch(BUFFER2, 4));
        assertEquals(0, matcher.isMatch(BUFFER2, 5));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testCharSetMatcher_String
    public void testCharSetMatcher_String() {
        StrMatcher matcher = StrMatcher.charSetMatcher("ace");
        assertEquals(1, matcher.isMatch(BUFFER2, 0));
        assertEquals(0, matcher.isMatch(BUFFER2, 1));
        assertEquals(1, matcher.isMatch(BUFFER2, 2));
        assertEquals(0, matcher.isMatch(BUFFER2, 3));
        assertEquals(1, matcher.isMatch(BUFFER2, 4));
        assertEquals(0, matcher.isMatch(BUFFER2, 5));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.charSetMatcher(""));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.charSetMatcher((String) null));
        assertTrue(StrMatcher.charSetMatcher("a") instanceof StrMatcher.CharMatcher);
    }

// org.apache.commons.lang3.text.StrMatcherTest::testCharSetMatcher_charArray
    public void testCharSetMatcher_charArray() {
        StrMatcher matcher = StrMatcher.charSetMatcher("ace".toCharArray());
        assertEquals(1, matcher.isMatch(BUFFER2, 0));
        assertEquals(0, matcher.isMatch(BUFFER2, 1));
        assertEquals(1, matcher.isMatch(BUFFER2, 2));
        assertEquals(0, matcher.isMatch(BUFFER2, 3));
        assertEquals(1, matcher.isMatch(BUFFER2, 4));
        assertEquals(0, matcher.isMatch(BUFFER2, 5));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.charSetMatcher(new char[0]));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.charSetMatcher((char[]) null));
        assertTrue(StrMatcher.charSetMatcher("a".toCharArray()) instanceof StrMatcher.CharMatcher);
    }

// org.apache.commons.lang3.text.StrMatcherTest::testStringMatcher_String
    public void testStringMatcher_String() {
        StrMatcher matcher = StrMatcher.stringMatcher("bc");
        assertEquals(0, matcher.isMatch(BUFFER2, 0));
        assertEquals(2, matcher.isMatch(BUFFER2, 1));
        assertEquals(0, matcher.isMatch(BUFFER2, 2));
        assertEquals(0, matcher.isMatch(BUFFER2, 3));
        assertEquals(0, matcher.isMatch(BUFFER2, 4));
        assertEquals(0, matcher.isMatch(BUFFER2, 5));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.stringMatcher(""));
        assertSame(StrMatcher.noneMatcher(), StrMatcher.stringMatcher((String) null));
    }

// org.apache.commons.lang3.text.StrMatcherTest::testMatcherIndices
    public void testMatcherIndices() {
        
        
        
        StrMatcher matcher = StrMatcher.stringMatcher("bc");
        assertEquals(2, matcher.isMatch(BUFFER2, 1, 1, BUFFER2.length));
        assertEquals(2, matcher.isMatch(BUFFER2, 1, 0, 3));
        assertEquals(0, matcher.isMatch(BUFFER2, 1, 0, 2));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceSimple
    public void testReplaceSimple() {
        doTestReplace("The quick brown fox jumps over the lazy dog.", "The ${animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceSolo
    public void testReplaceSolo() {
        doTestReplace("quick brown fox", "${animal}", false);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceNoVariables
    public void testReplaceNoVariables() {
        doTestNoReplace("The balloon arrived.");
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceNull
    public void testReplaceNull() {
        doTestNoReplace(null);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceEmpty
    public void testReplaceEmpty() {
        doTestNoReplace("");
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceChangedMap
    public void testReplaceChangedMap() {
        StrSubstitutor sub = new StrSubstitutor(values);
        values.put("target", "moon");
        assertEquals("The quick brown fox jumps over the moon.", sub.replace("The ${animal} jumps over the ${target}."));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceUnknownKey
    public void testReplaceUnknownKey() {
        doTestReplace("The ${person} jumps over the lazy dog.", "The ${person} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceAdjacentAtStart
    public void testReplaceAdjacentAtStart() {
        values.put("code", "GBP");
        values.put("amount", "12.50");
        StrSubstitutor sub = new StrSubstitutor(values);
        assertEquals("GBP12.50 charged", sub.replace("${code}${amount} charged"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceAdjacentAtEnd
    public void testReplaceAdjacentAtEnd() {
        values.put("code", "GBP");
        values.put("amount", "12.50");
        StrSubstitutor sub = new StrSubstitutor(values);
        assertEquals("Amount is GBP12.50", sub.replace("Amount is ${code}${amount}"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceRecursive
    public void testReplaceRecursive() {
        values.put("animal", "${critter}");
        values.put("target", "${pet}");
        values.put("pet", "${petCharacteristic} dog");
        values.put("petCharacteristic", "lazy");
        values.put("critter", "${critterSpeed} ${critterColor} ${critterType}");
        values.put("critterSpeed", "quick");
        values.put("critterColor", "brown");
        values.put("critterType", "fox");
        doTestReplace("The quick brown fox jumps over the lazy dog.", "The ${animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceEscaping
    public void testReplaceEscaping() {
        doTestReplace("The ${animal} jumps over the lazy dog.", "The $${animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceSoloEscaping
    public void testReplaceSoloEscaping() {
        doTestReplace("${animal}", "$${animal}", false);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceComplexEscaping
    public void testReplaceComplexEscaping() {
        doTestReplace("The ${quick brown fox} jumps over the lazy dog.", "The $${${animal}} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceNoPefixNoSuffix
    public void testReplaceNoPefixNoSuffix() {
        doTestReplace("The animal jumps over the lazy dog.", "The animal jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceIncompletePefix
    public void testReplaceIncompletePefix() {
        doTestReplace("The {animal} jumps over the lazy dog.", "The {animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplacePrefixNoSuffix
    public void testReplacePrefixNoSuffix() {
        doTestReplace("The ${animal jumps over the ${target} lazy dog.", "The ${animal jumps over the ${target} ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceNoPrefixSuffix
    public void testReplaceNoPrefixSuffix() {
        doTestReplace("The animal} jumps over the lazy dog.", "The animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceEmptyKeys
    public void testReplaceEmptyKeys() {
        doTestReplace("The ${} jumps over the lazy dog.", "The ${} jumps over the ${target}.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceToIdentical
    public void testReplaceToIdentical() {
        values.put("animal", "$${${thing}}");
        values.put("thing", "animal");
        doTestReplace("The ${animal} jumps.", "The ${animal} jumps.", true);
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testCyclicReplacement
    public void testCyclicReplacement() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("animal", "${critter}");
        map.put("target", "${pet}");
        map.put("pet", "${petCharacteristic} dog");
        map.put("petCharacteristic", "lazy");
        map.put("critter", "${critterSpeed} ${critterColor} ${critterType}");
        map.put("critterSpeed", "quick");
        map.put("critterColor", "brown");
        map.put("critterType", "${animal}");
        StrSubstitutor sub = new StrSubstitutor(map);
        try {
            sub.replace("The ${animal} jumps over the ${target}.");
            fail("Cyclic replacement was not detected!");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceWeirdPattens
    public void testReplaceWeirdPattens() {
        doTestNoReplace("");
        doTestNoReplace("${}");
        doTestNoReplace("${ }");
        doTestNoReplace("${\t}");
        doTestNoReplace("${\n}");
        doTestNoReplace("${\b}");
        doTestNoReplace("${");
        doTestNoReplace("$}");
        doTestNoReplace("}");
        doTestNoReplace("${}$");
        doTestNoReplace("${${");
        doTestNoReplace("${${}}");
        doTestNoReplace("${$${}}");
        doTestNoReplace("${$$${}}");
        doTestNoReplace("${$$${$}}");
        doTestNoReplace("${${}}");
        doTestNoReplace("${${ }}");
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplacePartialString_noReplace
    public void testReplacePartialString_noReplace() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals("${animal} jumps", sub.replace("The ${animal} jumps over the ${target}.", 4, 15));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceInVariable
    public void testReplaceInVariable() {
        values.put("animal.1", "fox");
        values.put("animal.2", "mouse");
        values.put("species", "2");
        StrSubstitutor sub = new StrSubstitutor(values);
        sub.setEnableSubstitutionInVariables(true);
        assertEquals(
                "Wrong result (1)",
                "The mouse jumps over the lazy dog.",
                sub.replace("The ${animal.${species}} jumps over the ${target}."));
        values.put("species", "1");
        assertEquals(
                "Wrong result (2)",
                "The fox jumps over the lazy dog.",
                sub.replace("The ${animal.${species}} jumps over the ${target}."));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceInVariableDisabled
    public void testReplaceInVariableDisabled() {
        values.put("animal.1", "fox");
        values.put("animal.2", "mouse");
        values.put("species", "2");
        StrSubstitutor sub = new StrSubstitutor(values);
        assertEquals(
                "Wrong result",
                "The ${animal.${species}} jumps over the lazy dog.",
                sub.replace("The ${animal.${species}} jumps over the ${target}."));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testReplaceInVariableRecursive
    public void testReplaceInVariableRecursive() {
        values.put("animal.2", "brown fox");
        values.put("animal.1", "white mouse");
        values.put("color", "white");
        values.put("species.white", "1");
        values.put("species.brown", "2");
        StrSubstitutor sub = new StrSubstitutor(values);
        sub.setEnableSubstitutionInVariables(true);
        assertEquals(
                "Wrong result",
                "The white mouse jumps over the lazy dog.",
                sub.replace("The ${animal.${species.${color}}} jumps over the ${target}."));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testResolveVariable
    public void testResolveVariable() {
        final StrBuilder builder = new StrBuilder("Hi ${name}!");
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map) {
            @Override
            protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos) {
                assertEquals("name", variableName);
                assertSame(builder, buf);
                assertEquals(3, startPos);
                assertEquals(10, endPos);
                return "jakarta";
            }
        };
        sub.replaceIn(builder);
        assertEquals("Hi jakarta!", builder.toString());
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testConstructorNoArgs
    public void testConstructorNoArgs() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals("Hi ${name}", sub.replace("Hi ${name}"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testConstructorMapPrefixSuffix
    public void testConstructorMapPrefixSuffix() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map, "<", ">");
        assertEquals("Hi < commons", sub.replace("Hi $< <name>"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testConstructorMapFull
    public void testConstructorMapFull() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map, "<", ">", '!');
        assertEquals("Hi < commons", sub.replace("Hi !< <name>"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testGetSetEscape
    public void testGetSetEscape() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals('$', sub.getEscapeChar());
        sub.setEscapeChar('<');
        assertEquals('<', sub.getEscapeChar());
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testGetSetPrefix
    public void testGetSetPrefix() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals(true, sub.getVariablePrefixMatcher() instanceof StrMatcher.StringMatcher);
        sub.setVariablePrefix('<');
        assertEquals(true, sub.getVariablePrefixMatcher() instanceof StrMatcher.CharMatcher);

        sub.setVariablePrefix("<<");
        assertEquals(true, sub.getVariablePrefixMatcher() instanceof StrMatcher.StringMatcher);
        try {
            sub.setVariablePrefix((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        assertEquals(true, sub.getVariablePrefixMatcher() instanceof StrMatcher.StringMatcher);

        StrMatcher matcher = StrMatcher.commaMatcher();
        sub.setVariablePrefixMatcher(matcher);
        assertSame(matcher, sub.getVariablePrefixMatcher());
        try {
            sub.setVariablePrefixMatcher((StrMatcher) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        assertSame(matcher, sub.getVariablePrefixMatcher());
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testGetSetSuffix
    public void testGetSetSuffix() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals(true, sub.getVariableSuffixMatcher() instanceof StrMatcher.StringMatcher);
        sub.setVariableSuffix('<');
        assertEquals(true, sub.getVariableSuffixMatcher() instanceof StrMatcher.CharMatcher);

        sub.setVariableSuffix("<<");
        assertEquals(true, sub.getVariableSuffixMatcher() instanceof StrMatcher.StringMatcher);
        try {
            sub.setVariableSuffix((String) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        assertEquals(true, sub.getVariableSuffixMatcher() instanceof StrMatcher.StringMatcher);

        StrMatcher matcher = StrMatcher.commaMatcher();
        sub.setVariableSuffixMatcher(matcher);
        assertSame(matcher, sub.getVariableSuffixMatcher());
        try {
            sub.setVariableSuffixMatcher((StrMatcher) null);
            fail();
        } catch (IllegalArgumentException ex) {
            
        }
        assertSame(matcher, sub.getVariableSuffixMatcher());
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testStaticReplace
    public void testStaticReplace() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "commons");
        assertEquals("Hi commons!", StrSubstitutor.replace("Hi ${name}!", map));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testStaticReplacePrefixSuffix
    public void testStaticReplacePrefixSuffix() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "commons");
        assertEquals("Hi commons!", StrSubstitutor.replace("Hi <name>!", map, "<", ">"));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testStaticReplaceSystemProperties
    public void testStaticReplaceSystemProperties() {
        StrBuilder buf = new StrBuilder();
        buf.append("Hi ").append(System.getProperty("user.name"));
        buf.append(", you are working with ");
        buf.append(System.getProperty("os.name"));
        buf.append(", your home directory is ");
        buf.append(System.getProperty("user.home")).append('.');
        assertEquals(buf.toString(), StrSubstitutor.replaceSystemProperties("Hi ${user.name}, you are "
            + "working with ${os.name}, your home "
            + "directory is ${user.home}."));
    }

// org.apache.commons.lang3.text.StrSubstitutorTest::testSubstitutetDefaultProperties
    public void testSubstitutetDefaultProperties(){
        String org = "${doesnotwork}";
        System.setProperty("doesnotwork", "It work's!");

        
        Properties props = new Properties(System.getProperties());

        assertEquals("It work's!",StrSubstitutor.replace(org, props));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::test1
    public void test1() {

        String input = "a;b;c;\"d;\"\"e\";f; ; ;  ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", "", "", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test2
    public void test2() {

        String input = "a;b;c ;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c ", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test3
    public void test3() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test4
    public void test4() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test5
    public void test5() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", null, null, null,};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test6
    public void test6() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", null, null, null,};

        int nextCount = 0;
        while (tok.hasNext()) {
            tok.next();
            nextCount++;
        }

        int prevCount = 0;
        while (tok.hasPrevious()) {
            tok.previous();
            prevCount++;
        }

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasNext' and 'next' methods",
                nextCount == expected.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasPrevious' and 'previous' methods",
                prevCount == expected.length);

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test7
    public void test7() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "", "", "b", "c", "d e", "f", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test8
    public void test8() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic1
    public void testBasic1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic2
    public void testBasic2() {
        String input = "a \nb\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic3
    public void testBasic3() {
        String input = "a \nb\u0001\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b\u0001", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic4
    public void testBasic4() {
        String input = "a \"b\" c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("\"b\"", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic5
    public void testBasic5() {
        String input = "a:b':c";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        assertEquals("a", tok.next());
        assertEquals("b'", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim1
    public void testBasicDelim1() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ':');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim2
    public void testBasicDelim2() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ',');
        assertEquals("a:b:c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty1
    public void testBasicEmpty1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        assertEquals("a", tok.next());
        assertEquals("", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty2
    public void testBasicEmpty2() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals(null, tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted1
    public void testBasicQuoted1() {
        String input = "a 'b' c";
        StrTokenizer tok = new StrTokenizer(input, ' ', '\'');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted2
    public void testBasicQuoted2() {
        String input = "a:'b':";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted3
    public void testBasicQuoted3() {
        String input = "a:'b''c'";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b'c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted4
    public void testBasicQuoted4() {
        String input = "a: 'b' 'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b c", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted5
    public void testBasicQuoted5() {
        String input = "a: 'b'x'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bxc", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted6
    public void testBasicQuoted6() {
        String input = "a:'b'\"c':d";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("b\"c:d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted7
    public void testBasicQuoted7() {
        String input = "a:\"There's a reason here\":b";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("There's a reason here", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuotedTrimmed1
    public void testBasicQuotedTrimmed1() {
        String input = "a: 'b' :";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed1
    public void testBasicTrimmed1() {
        String input = "a: b :  ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed2
    public void testBasicTrimmed2() {
        String input = "a:  b  :";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.stringMatcher("  "));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed1
    public void testBasicIgnoreTrimmed1() {
        String input = "a: bIGNOREc : ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed2
    public void testBasicIgnoreTrimmed2() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed3
    public void testBasicIgnoreTrimmed3() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("  bc  ", tok.next());
        assertEquals("  ", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed4
    public void testBasicIgnoreTrimmed4() {
        String input = "IGNOREaIGNORE: IGNORE 'bIGNOREc'IGNORE'd' IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bIGNOREcd", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testListArray
    public void testListArray() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        String[] array = tok.getTokenArray();
        List<?> list = tok.getTokenList();
        
        assertEquals(Arrays.asList(array), list);
        assertEquals(3, list.size());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSV
    public void testCSV(String data) {
        this.testXSVAbc(StrTokenizer.getCSVInstance(data));
        this.testXSVAbc(StrTokenizer.getCSVInstance(data.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVEmpty
    public void testCSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimple
    public void testCSVSimple() {
        this.testCSV(CSV_SIMPLE_FIXTURE);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimpleNeedsTrim
    public void testCSVSimpleNeedsTrim() {
        this.testCSV("   " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n\t  " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n  " + CSV_SIMPLE_FIXTURE + "\n\n\r");
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testGetContent
    public void testGetContent() {
        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals(input, tok.getContent());

        tok = new StrTokenizer(input.toCharArray());
        assertEquals(input, tok.getContent());
        
        tok = new StrTokenizer();
        assertEquals(null, tok.getContent());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testChaining
    public void testChaining() {
        StrTokenizer tok = new StrTokenizer();
        assertEquals(tok, tok.reset());
        assertEquals(tok, tok.reset(""));
        assertEquals(tok, tok.reset(new char[0]));
        assertEquals(tok, tok.setDelimiterChar(' '));
        assertEquals(tok, tok.setDelimiterString(" "));
        assertEquals(tok, tok.setDelimiterMatcher(null));
        assertEquals(tok, tok.setQuoteChar(' '));
        assertEquals(tok, tok.setQuoteMatcher(null));
        assertEquals(tok, tok.setIgnoredChar(' '));
        assertEquals(tok, tok.setIgnoredMatcher(null));
        assertEquals(tok, tok.setTrimmerMatcher(null));
        assertEquals(tok, tok.setEmptyTokenAsNull(false));
        assertEquals(tok, tok.setIgnoreEmptyTokens(false));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNotSupportedException
    public void testCloneNotSupportedException() {
        Object notCloned = new StrTokenizer() {
            @Override
            Object cloneReset() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("test");
            }
        }.clone();
        assertNull(notCloned);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNull
    public void testCloneNull() {
        StrTokenizer tokenizer = new StrTokenizer((char[]) null);
        
        assertEquals(null, tokenizer.nextToken());
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        assertEquals(null, clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneReset
    public void testCloneReset() {
        char[] input = new char[]{'a'};
        StrTokenizer tokenizer = new StrTokenizer(input);
        
        assertEquals("a", tokenizer.nextToken());
        tokenizer.reset(input);
        assertEquals("a", tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        input[0] = 'b';
        tokenizer.reset(input);
        assertEquals("b", tokenizer.nextToken());
        assertEquals("a", clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String
    public void testConstructor_String() {
        StrTokenizer tok = new StrTokenizer("a b");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("");
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char
    public void testConstructor_String_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char_char
    public void testConstructor_String_char_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray
    public void testConstructor_charArray() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray());
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0]);
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char
    public void testConstructor_charArray_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char_char
    public void testConstructor_charArray_char_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset
    public void testReset() {
        StrTokenizer tok = new StrTokenizer("a b c");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset();
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_String
    public void testReset_String() {
        StrTokenizer tok = new StrTokenizer("x x x");
        tok.reset("d e");
        assertEquals("d", tok.next());
        assertEquals("e", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_charArray
    public void testReset_charArray() {
        StrTokenizer tok = new StrTokenizer("x x x");
        
        char[] array = new char[] {'a', 'b', 'c'};
        tok.reset(array);
        assertEquals("abc", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSV
    public void testTSV() {
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE));
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSVEmpty
    public void testTSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testIteration
    public void testIteration() {
        StrTokenizer tkn = new StrTokenizer("a b c");
        assertEquals(false, tkn.hasPrevious());
        try {
            tkn.previous();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasNext());
        
        assertEquals("a", tkn.next());
        try {
            tkn.remove();
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.set("x");
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.add("y");
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("b", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("c", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
        
        try {
            tkn.next();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassInputChange
    public void testTokenizeSubclassInputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c d e") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                return super.tokenize("w x y z".toCharArray(), 2, 5);
            }
        };
        assertEquals("x", tkn.next());
        assertEquals("y", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassOutputChange
    public void testTokenizeSubclassOutputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                List<String> list = super.tokenize(chars, offset, count);
                Collections.reverse(list);
                return list;
            }
        };
        assertEquals("c", tkn.next());
        assertEquals("b", tkn.next());
        assertEquals("a", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testToString
    public void testToString() {
        StrTokenizer tkn = new StrTokenizer("a b c d e");
        assertEquals("StrTokenizer[not tokenized yet]", tkn.toString());
        tkn.next();
        assertEquals("StrTokenizer[a, b, c, d, e]", tkn.toString());
    }

// org.apache.commons.lang3.text.WordUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new WordUtils());
        Constructor<?>[] cons = WordUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(WordUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(WordUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testWrap_StringInt
    public void testWrap_StringInt() {
        assertEquals(null, WordUtils.wrap(null, 20));
        assertEquals(null, WordUtils.wrap(null, -1));
        
        assertEquals("", WordUtils.wrap("", 20));
        assertEquals("", WordUtils.wrap("", -1));
        
        
        String systemNewLine = System.getProperty("line.separator");
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of" + systemNewLine + "text that is going" 
            + systemNewLine + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20));
        
        
        input = "Click here to jump to the jakarta website - http://jakarta.apache.org";
        expected = "Click here to jump" + systemNewLine + "to the jakarta" + systemNewLine 
            + "website -" + systemNewLine + "http://jakarta.apache.org";
        assertEquals(expected, WordUtils.wrap(input, 20));
        
        
        input = "Click here, http://jakarta.apache.org, to jump to the jakarta website";
        expected = "Click here," + systemNewLine + "http://jakarta.apache.org," + systemNewLine 
            + "to jump to the" + systemNewLine + "jakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testWrap_StringIntStringBoolean
    public void testWrap_StringIntStringBoolean() {
        assertEquals(null, WordUtils.wrap(null, 20, "\n", false));
        assertEquals(null, WordUtils.wrap(null, 20, "\n", true));
        assertEquals(null, WordUtils.wrap(null, 20, null, true));
        assertEquals(null, WordUtils.wrap(null, 20, null, false));
        assertEquals(null, WordUtils.wrap(null, -1, null, true));
        assertEquals(null, WordUtils.wrap(null, -1, null, false));
        
        assertEquals("", WordUtils.wrap("", 20, "\n", false));
        assertEquals("", WordUtils.wrap("", 20, "\n", true));
        assertEquals("", WordUtils.wrap("", 20, null, false));
        assertEquals("", WordUtils.wrap("", 20, null, true));
        assertEquals("", WordUtils.wrap("", -1, null, false));
        assertEquals("", WordUtils.wrap("", -1, null, true));
        
        
        String input = "Here is one line of text that is going to be wrapped after 20 columns.";
        String expected = "Here is one line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));

        
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of<br />text that is going<br />to be wrapped after<br />20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "<br />", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "<br />", true));

        
        input = "Here is one line";
        expected = "Here\nis one\nline";
        assertEquals(expected, WordUtils.wrap(input, 6, "\n", false));
        expected = "Here\nis\none\nline";
        assertEquals(expected, WordUtils.wrap(input, 2, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, -1, "\n", false));

        
        String systemNewLine = System.getProperty("line.separator");
        input = "Here is one line of text that is going to be wrapped after 20 columns.";
        expected = "Here is one line of" + systemNewLine + "text that is going" + systemNewLine 
            + "to be wrapped after" + systemNewLine + "20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, null, false));
        assertEquals(expected, WordUtils.wrap(input, 20, null, true));

        
        input = " Here:  is  one  line  of  text  that  is  going  to  be  wrapped  after  20  columns.";
        expected = "Here:  is  one  line\nof  text  that  is \ngoing  to  be \nwrapped  after  20 \ncolumns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Here is\tone line of text that is going to be wrapped after 20 columns.";
        expected = "Here is\tone line of\ntext that is going\nto be wrapped after\n20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Here is one line of\ttext that is going to be wrapped after 20 columns.";
        expected = "Here is one line\nof\ttext that is\ngoing to be wrapped\nafter 20 columns.";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Click here to jump to the jakarta website - http://jakarta.apache.org";
        expected = "Click here to jump\nto the jakarta\nwebsite -\nhttp://jakarta.apache.org";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        expected = "Click here to jump\nto the jakarta\nwebsite -\nhttp://jakarta.apach\ne.org";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));
        
        
        input = "Click here, http://jakarta.apache.org, to jump to the jakarta website";
        expected = "Click here,\nhttp://jakarta.apache.org,\nto jump to the\njakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", false));
        expected = "Click here,\nhttp://jakarta.apach\ne.org, to jump to\nthe jakarta website";
        assertEquals(expected, WordUtils.wrap(input, 20, "\n", true));

    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalize_String
    public void testCapitalize_String() {
        assertEquals(null, WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("  ", WordUtils.capitalize("  "));
        
        assertEquals("I", WordUtils.capitalize("I") );
        assertEquals("I", WordUtils.capitalize("i") );
        assertEquals("I Am Here 123", WordUtils.capitalize("i am here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalize("I Am Here 123") );
        assertEquals("I Am HERE 123", WordUtils.capitalize("i am HERE 123") );
        assertEquals("I AM HERE 123", WordUtils.capitalize("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeWithDelimiters_String
    public void testCapitalizeWithDelimiters_String() {
        assertEquals(null, WordUtils.capitalize(null, null));
        assertEquals("", WordUtils.capitalize("", new char[0]));
        assertEquals("  ", WordUtils.capitalize("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("I", WordUtils.capitalize("I", chars) );
        assertEquals("I", WordUtils.capitalize("i", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalize("i-am here+123", chars) );
        assertEquals("I Am+Here-123", WordUtils.capitalize("I Am+Here-123", chars) );
        assertEquals("I+Am-HERE 123", WordUtils.capitalize("i+am-HERE 123", chars) );
        assertEquals("I-AM HERE+123", WordUtils.capitalize("I-AM HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", chars) );
        assertEquals("I Am.fine", WordUtils.capitalize("i am.fine", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeFully_String
    public void testCapitalizeFully_String() {
        assertEquals(null, WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("  ", WordUtils.capitalizeFully("  "));
        
        assertEquals("I", WordUtils.capitalizeFully("I") );
        assertEquals("I", WordUtils.capitalizeFully("i") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("i am here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("I Am Here 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("i am HERE 123") );
        assertEquals("I Am Here 123", WordUtils.capitalizeFully("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testCapitalizeFullyWithDelimiters_String
    public void testCapitalizeFullyWithDelimiters_String() {
        assertEquals(null, WordUtils.capitalizeFully(null, null));
        assertEquals("", WordUtils.capitalizeFully("", new char[0]));
        assertEquals("  ", WordUtils.capitalizeFully("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("I", WordUtils.capitalizeFully("I", chars) );
        assertEquals("I", WordUtils.capitalizeFully("i", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalizeFully("i-am here+123", chars) );
        assertEquals("I Am+Here-123", WordUtils.capitalizeFully("I Am+Here-123", chars) );
        assertEquals("I+Am-Here 123", WordUtils.capitalizeFully("i+am-HERE 123", chars) );
        assertEquals("I-Am Here+123", WordUtils.capitalizeFully("I-AM HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", chars) );
        assertEquals("I Am.fine", WordUtils.capitalizeFully("i am.fine", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testUncapitalize_String
    public void testUncapitalize_String() {
        assertEquals(null, WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("  ", WordUtils.uncapitalize("  "));
        
        assertEquals("i", WordUtils.uncapitalize("I") );
        assertEquals("i", WordUtils.uncapitalize("i") );
        assertEquals("i am here 123", WordUtils.uncapitalize("i am here 123") );
        assertEquals("i am here 123", WordUtils.uncapitalize("I Am Here 123") );
        assertEquals("i am hERE 123", WordUtils.uncapitalize("i am HERE 123") );
        assertEquals("i aM hERE 123", WordUtils.uncapitalize("I AM HERE 123") );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testUncapitalizeWithDelimiters_String
    public void testUncapitalizeWithDelimiters_String() {
        assertEquals(null, WordUtils.uncapitalize(null, null));
        assertEquals("", WordUtils.uncapitalize("", new char[0]));
        assertEquals("  ", WordUtils.uncapitalize("  ", new char[0]));
        
        char[] chars = new char[] { '-', '+', ' ', '@' };
        assertEquals("i", WordUtils.uncapitalize("I", chars) );
        assertEquals("i", WordUtils.uncapitalize("i", chars) );
        assertEquals("i am-here+123", WordUtils.uncapitalize("i am-here+123", chars) );
        assertEquals("i+am here-123", WordUtils.uncapitalize("I+Am Here-123", chars) );
        assertEquals("i-am+hERE 123", WordUtils.uncapitalize("i-am+HERE 123", chars) );
        assertEquals("i aM-hERE+123", WordUtils.uncapitalize("I AM-HERE+123", chars) );
        chars = new char[] {'.'};
        assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", chars) );
        assertEquals("i aM.FINE", WordUtils.uncapitalize("I AM.FINE", null) );
    }

// org.apache.commons.lang3.text.WordUtilsTest::testInitials_String
    public void testInitials_String() {
        assertEquals(null, WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("", WordUtils.initials("  "));

        assertEquals("I", WordUtils.initials("I"));
        assertEquals("i", WordUtils.initials("i"));
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee"));
        assertEquals("iah1", WordUtils.initials("i am here 123"));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testInitials_String_charArray
    public void testInitials_String_charArray() {
        char[] array = null;
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = new char[0];
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("", WordUtils.initials("I", array));
        assertEquals("", WordUtils.initials("i", array));
        assertEquals("", WordUtils.initials("SJC", array));
        assertEquals("", WordUtils.initials("Ben John Lee", array));
        assertEquals("", WordUtils.initials("Ben J.Lee", array));
        assertEquals("", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("", WordUtils.initials("i am here 123", array));
        
        array = " ".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJ.L", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = " .".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJL", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KO", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = " .'".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals("", WordUtils.initials("  ", array));
        assertEquals("I", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("S", WordUtils.initials("SJC", array));
        assertEquals("BJL", WordUtils.initials("Ben John Lee", array));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", array));
        assertEquals("BJL", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("KOM", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("iah1", WordUtils.initials("i am here 123", array));
        
        array = "SIJo1".toCharArray();
        assertEquals(null, WordUtils.initials(null, array));
        assertEquals("", WordUtils.initials("", array));
        assertEquals(" ", WordUtils.initials("  ", array));
        assertEquals("", WordUtils.initials("I", array));
        assertEquals("i", WordUtils.initials("i", array));
        assertEquals("C", WordUtils.initials("SJC", array));
        assertEquals("Bh", WordUtils.initials("Ben John Lee", array));
        assertEquals("B.", WordUtils.initials("Ben J.Lee", array));
        assertEquals(" h", WordUtils.initials(" Ben   John  . Lee", array));
        assertEquals("K", WordUtils.initials("Kay O'Murphy", array));
        assertEquals("i2", WordUtils.initials("i am here 123", array));
    }

// org.apache.commons.lang3.text.WordUtilsTest::testSwapCase_String
    public void testSwapCase_String() {
        assertEquals(null, WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));
        assertEquals("  ", WordUtils.swapCase("  "));
        
        assertEquals("i", WordUtils.swapCase("I") );
        assertEquals("I", WordUtils.swapCase("i") );
        assertEquals("I AM HERE 123", WordUtils.swapCase("i am here 123") );
        assertEquals("i aM hERE 123", WordUtils.swapCase("I Am Here 123") );
        assertEquals("I AM here 123", WordUtils.swapCase("i am HERE 123") );
        assertEquals("i am here 123", WordUtils.swapCase("I AM HERE 123") );

        String test = "This String contains a TitleCase character: \u01C8";
        String expect = "tHIS sTRING CONTAINS A tITLEcASE CHARACTER: \u01C9";
        assertEquals(expect, WordUtils.swapCase(test));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new DurationFormatUtils());
        Constructor<?>[] cons = DurationFormatUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(DurationFormatUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(DurationFormatUtils.class.getModifiers()));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationWords
    public void testFormatDurationWords() {
        String text = null;

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, false);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, false);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, false);
        assertEquals("2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, false);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, false);
        assertEquals("1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, true, true);
        assertEquals("50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, true, true);
        assertEquals("1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, true, true);
        assertEquals("2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, true, true);
        assertEquals("2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, true, true);
        assertEquals("1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, true, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, true);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, true);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, true);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, true);
        assertEquals("0 days 1 hour 12 minutes", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000, false, true);
        assertEquals("1 day", text);

        text = DurationFormatUtils.formatDurationWords(50 * 1000, false, false);
        assertEquals("0 days 0 hours 0 minutes 50 seconds", text);
        text = DurationFormatUtils.formatDurationWords(65 * 1000, false, false);
        assertEquals("0 days 0 hours 1 minute 5 seconds", text);
        text = DurationFormatUtils.formatDurationWords(120 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(121 * 1000, false, false);
        assertEquals("0 days 0 hours 2 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(72 * 60 * 1000, false, false);
        assertEquals("0 days 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("1 day 1 hour 12 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(2 * 24 * 60 * 60 * 1000 + 72 * 60 * 1000, false, false);
        assertEquals("2 days 1 hour 12 minutes 0 seconds", text);
        for (int i = 2; i < 31; i++) {
            text = DurationFormatUtils.formatDurationWords(i * 24 * 60 * 60 * 1000L, false, false);
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationPluralWords
    public void testFormatDurationPluralWords() {
        long oneSecond = 1000;
        long oneMinute = oneSecond * 60;
        long oneHour = oneMinute * 60;
        long oneDay = oneHour * 24;
        String text = null;

        text = DurationFormatUtils.formatDurationWords(oneSecond, false, false);
        assertEquals("0 days 0 hours 0 minutes 1 second", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 2, false, false);
        assertEquals("0 days 0 hours 0 minutes 2 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneSecond * 11, false, false);
        assertEquals("0 days 0 hours 0 minutes 11 seconds", text);

        text = DurationFormatUtils.formatDurationWords(oneMinute, false, false);
        assertEquals("0 days 0 hours 1 minute 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 2, false, false);
        assertEquals("0 days 0 hours 2 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute * 11, false, false);
        assertEquals("0 days 0 hours 11 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneMinute + oneSecond, false, false);
        assertEquals("0 days 0 hours 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneHour, false, false);
        assertEquals("0 days 1 hour 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 2, false, false);
        assertEquals("0 days 2 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour * 11, false, false);
        assertEquals("0 days 11 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneHour + oneMinute + oneSecond, false, false);
        assertEquals("0 days 1 hour 1 minute 1 second", text);

        text = DurationFormatUtils.formatDurationWords(oneDay, false, false);
        assertEquals("1 day 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 2, false, false);
        assertEquals("2 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay * 11, false, false);
        assertEquals("11 days 0 hours 0 minutes 0 seconds", text);
        text = DurationFormatUtils.formatDurationWords(oneDay + oneHour + oneMinute + oneSecond, false, false);
        assertEquals("1 day 1 hour 1 minute 1 second", text);
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationHMS
    public void testFormatDurationHMS() {
        long time = 0;
        assertEquals("0:00:00.000", DurationFormatUtils.formatDurationHMS(time));

        time = 1;
        assertEquals("0:00:00.001", DurationFormatUtils.formatDurationHMS(time));

        time = 15;
        assertEquals("0:00:00.015", DurationFormatUtils.formatDurationHMS(time));

        time = 165;
        assertEquals("0:00:00.165", DurationFormatUtils.formatDurationHMS(time));

        time = 1675;
        assertEquals("0:00:01.675", DurationFormatUtils.formatDurationHMS(time));

        time = 13465;
        assertEquals("0:00:13.465", DurationFormatUtils.formatDurationHMS(time));

        time = 72789;
        assertEquals("0:01:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 32 * 60000;
        assertEquals("0:32:12.789", DurationFormatUtils.formatDurationHMS(time));

        time = 12789 + 62 * 60000;
        assertEquals("1:02:12.789", DurationFormatUtils.formatDurationHMS(time));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDurationISO
    public void testFormatDurationISO() {
        assertEquals("P0Y0M0DT0H0M0.000S", DurationFormatUtils.formatDurationISO(0L));
        assertEquals("P0Y0M0DT0H0M0.001S", DurationFormatUtils.formatDurationISO(1L));
        assertEquals("P0Y0M0DT0H0M0.010S", DurationFormatUtils.formatDurationISO(10L));
        assertEquals("P0Y0M0DT0H0M0.100S", DurationFormatUtils.formatDurationISO(100L));
        assertEquals("P0Y0M0DT0H1M15.321S", DurationFormatUtils.formatDurationISO(75321L));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatDuration
    public void testFormatDuration() {
        long duration = 0;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatDuration(duration, "yyMM"));

        duration = 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "y"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "M"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "d"));
        assertEquals("0", DurationFormatUtils.formatDuration(duration, "H"));
        assertEquals("1", DurationFormatUtils.formatDuration(duration, "m"));
        assertEquals("60", DurationFormatUtils.formatDuration(duration, "s"));
        assertEquals("60000", DurationFormatUtils.formatDuration(duration, "S"));
        assertEquals("01:00", DurationFormatUtils.formatDuration(duration, "mm:ss"));

        Calendar base = Calendar.getInstance();
        base.set(2000, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance();
        cal.set(2003, 1, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        duration = cal.getTime().getTime() - base.getTime().getTime(); 
        
        
        int days = 366 + 365 + 365 + 31;
        assertEquals("0 0 " + days, DurationFormatUtils.formatDuration(duration, "y M d"));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriodISO
    public void testFormatPeriodISO() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar base = Calendar.getInstance(timeZone);
        base.set(1970, 0, 1, 0, 0, 0);
        base.set(Calendar.MILLISECOND, 0);

        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(2002, 1, 23, 9, 11, 12);
        cal.set(Calendar.MILLISECOND, 1);
        String text;
        
        text = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(cal);
        assertEquals("2002-02-23T09:11:12-03:00", text);
        
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P32Y1M22DT9H11M12.001S", text);
        
        cal.set(1971, 1, 3, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        text = DurationFormatUtils.formatPeriod(base.getTime().getTime(), cal.getTime().getTime(),
                DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN, false, timeZone);
        assertEquals("P1Y1M2DT10H30M0.000S", text);
        
        
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testFormatPeriod
    public void testFormatPeriod() {
        Calendar cal1970 = Calendar.getInstance();
        cal1970.set(1970, 0, 1, 0, 0, 0);
        cal1970.set(Calendar.MILLISECOND, 0);
        long time1970 = cal1970.getTime().getTime();

        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "H"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "m"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "s"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time1970, "S"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "SSSS"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyyy"));
        assertEquals("0000", DurationFormatUtils.formatPeriod(time1970, time1970, "yyMM"));

        long time = time1970 + 60 * 1000;
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "y"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "d"));
        assertEquals("0", DurationFormatUtils.formatPeriod(time1970, time, "H"));
        assertEquals("1", DurationFormatUtils.formatPeriod(time1970, time, "m"));
        assertEquals("60", DurationFormatUtils.formatPeriod(time1970, time, "s"));
        assertEquals("60000", DurationFormatUtils.formatPeriod(time1970, time, "S"));
        assertEquals("01:00", DurationFormatUtils.formatPeriod(time1970, time, "mm:ss"));

        Calendar cal = Calendar.getInstance();
        cal.set(1973, 6, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("36", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 6 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/06", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1973, 10, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("310", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("3 years 10 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("03/10", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));

        cal.set(1974, 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        time = cal.getTime().getTime();
        assertEquals("40", DurationFormatUtils.formatPeriod(time1970, time, "yM"));
        assertEquals("4 years 0 months", DurationFormatUtils.formatPeriod(time1970, time, "y' years 'M' months'"));
        assertEquals("04/00", DurationFormatUtils.formatPeriod(time1970, time, "yy/MM"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "M"));
        assertEquals("48", DurationFormatUtils.formatPeriod(time1970, time, "MM"));
        assertEquals("048", DurationFormatUtils.formatPeriod(time1970, time, "MMM"));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLexx
    public void testLexx() {
        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.y, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1)}, DurationFormatUtils.lexx("yMdHmsS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 2),
            new DurationFormatUtils.Token(new StringBuffer(":"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 2),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 3)}, DurationFormatUtils.lexx("H:mm:ss.SSS"));

        
        assertArrayEquals(new DurationFormatUtils.Token[]{
            new DurationFormatUtils.Token(new StringBuffer("P"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.y, 4),
            new DurationFormatUtils.Token(new StringBuffer("Y"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.M, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.d, 1),
            new DurationFormatUtils.Token(new StringBuffer("DT"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.H, 1),
            new DurationFormatUtils.Token(new StringBuffer("H"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.m, 1),
            new DurationFormatUtils.Token(new StringBuffer("M"), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.s, 1),
            new DurationFormatUtils.Token(new StringBuffer("."), 1),
            new DurationFormatUtils.Token(DurationFormatUtils.S, 1),
            new DurationFormatUtils.Token(new StringBuffer("S"), 1)}, DurationFormatUtils
                .lexx(DurationFormatUtils.ISO_EXTENDED_FORMAT_PATTERN));

        
        DurationFormatUtils.Token token = new DurationFormatUtils.Token(DurationFormatUtils.y, 4);
        assertFalse("Token equal to non-Token class. ", token.equals(new Object()));
        assertFalse("Token equal to Token with wrong value class. ", token.equals(new DurationFormatUtils.Token(
                new Object())));
        assertFalse("Token equal to Token with different count. ", token.equals(new DurationFormatUtils.Token(
                DurationFormatUtils.y, 1)));
        DurationFormatUtils.Token numToken = new DurationFormatUtils.Token(Integer.valueOf(1), 4);
        assertTrue("Token with Number value not equal to itself. ", numToken.equals(numToken));
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testBugzilla38401
    public void testBugzilla38401() {
        assertEqualDuration( "0000/00/30 16:00:00 000", new int[] { 2006, 0, 26, 18, 47, 34 }, 
                             new int[] { 2006, 1, 26, 10, 47, 34 }, "yyyy/MM/dd HH:mm:ss SSS");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testJiraLang281
    public void testJiraLang281() {
        assertEqualDuration( "09", new int[] { 2005, 11, 31, 0, 0, 0 }, 
                             new int[] { 2006, 9, 6, 0, 0, 0 }, "MM");
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testLowDurations
    public void testLowDurations() {
        for(int hr=0; hr < 24; hr++) {
            for(int min=0; min < 60; min++) {
                for(int sec=0; sec < 60; sec++) {
                    assertEqualDuration( hr + ":" + min + ":" + sec, 
                                         new int[] { 2000, 0, 1, 0, 0, 0, 0 },
                                         new int[] { 2000, 0, 1, hr, min, sec },
                                         "H:m:s"
                                       );
                }
            }
        }
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testEdgeDurations
    public void testEdgeDurations() {
        assertEqualDuration( "01", new int[] { 2006, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 15, 0, 0, 0 }, "MM");
        assertEqualDuration( "12", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 16, 0, 0, 0 }, "MM");
        assertEqualDuration( "11", new int[] { 2005, 0, 15, 0, 0, 0 }, 
                             new int[] { 2006, 0, 14, 0, 0, 0 }, "MM");
        
        assertEqualDuration( "01 26", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "54", new int[] { 2006, 0, 15, 0, 0, 0 },
                             new int[] { 2006, 2, 10, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "09 12", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "MM dd");
        assertEqualDuration( "287", new int[] { 2006, 1, 20, 0, 0, 0 },
                             new int[] { 2006, 11, 4, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "11 30", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "364", new int[] { 2006, 0, 2, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "12 00", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "MM dd"); 
        assertEqualDuration( "365", new int[] { 2006, 0, 1, 0, 0, 0 },
                             new int[] { 2007, 0, 1, 0, 0, 0 }, "dd"); 
    
        assertEqualDuration( "31", new int[] { 2006, 0, 1, 0, 0, 0 },
                new int[] { 2006, 1, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "92", new int[] { 2005, 9, 1, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "77", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 0, 1, 0, 0, 0 }, "dd"); 

        
        assertEqualDuration( "136", new int[] { 2005, 9, 16, 0, 0, 0 },
                new int[] { 2006, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "136", new int[] { 2004, 9, 16, 0, 0, 0 },
                new int[] { 2005, 2, 1, 0, 0, 0 }, "dd"); 
        
        assertEqualDuration( "137", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 2, 1, 0, 0, 0 }, "dd");         
        
        assertEqualDuration( "135", new int[] { 2003, 9, 16, 0, 0, 0 },
                new int[] { 2004, 1, 28, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "364", new int[] { 2007, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "729", new int[] { 2006, 0, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "365", new int[] { 2007, 2, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "333", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 0, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2008, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 
        assertEqualDuration( "393", new int[] { 2007, 1, 2, 0, 0, 0 },
                new int[] { 2008, 2, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "369", new int[] { 2004, 0, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "338", new int[] { 2004, 1, 29, 0, 0, 0 },
                new int[] { 2005, 1, 1, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "28", new int[] { 2004, 2, 8, 0, 0, 0 },
                new int[] { 2004, 3, 5, 0, 0, 0 }, "dd"); 

        assertEqualDuration( "48", new int[] { 1992, 1, 29, 0, 0, 0 },
                new int[] { 1996, 1, 29, 0, 0, 0 }, "M"); 
        
        
        
        
        assertEqualDuration( "11", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M"); 
        
        assertEqualDuration( "11 28", new int[] { 1996, 1, 29, 0, 0, 0 },
                new int[] { 1997, 1, 28, 0, 0, 0 }, "M d"); 
        
    }

// org.apache.commons.lang3.time.DurationFormatUtilsTest::testDurationsByBruteForce
    public void testDurationsByBruteForce() {
        bruteForce(2006, 0, 1, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2006, 0, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2007, 1, 2, "d", Calendar.DAY_OF_MONTH);
        bruteForce(2004, 1, 29, "d", Calendar.DAY_OF_MONTH);
        bruteForce(1996, 1, 29, "d", Calendar.DAY_OF_MONTH);

        bruteForce(1969, 1, 28, "M", Calendar.MONTH);  
        
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSimple
    public void testStopWatchSimple(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long time = watch.getTime();
        assertEquals(time, watch.getTime());
        
        assertTrue(time >= 500);
        assertTrue(time < 700);
        
        watch.reset();
        assertEquals(0, watch.getTime());
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSimpleGet
    public void testStopWatchSimpleGet(){
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("0:00:00.000", watch.toString());
        
        watch.start();
            try {Thread.sleep(500);} catch (InterruptedException ex) {}
        assertTrue(watch.getTime() < 2000);
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSplit
    public void testStopWatchSplit(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.split();
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.unsplit();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();

        assertEquals("Formatted split string not the correct length", 
                     splitStr.length(), 11);
        assertTrue(splitTime >= 500);
        assertTrue(splitTime < 700);
        assertTrue(totalTime >= 1500);
        assertTrue(totalTime < 1900);
    }

// org.apache.commons.lang3.time.StopWatchTest::testStopWatchSuspend
    public void testStopWatchSuspend(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.resume();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        
        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

// org.apache.commons.lang3.time.StopWatchTest::testLang315
    public void testLang315() {
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue( suspendTime == totalTime );
    }

// org.apache.commons.lang3.time.StopWatchTest::testBadStates
    public void testBadStates() {
        StopWatch watch = new StopWatch();
        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.suspend();
            fail("Calling suspend on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.split();
            fail("Calling split on a non-running StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.start();

        try {
            watch.start();
            fail("Calling start on a started StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.stop();

        try {
            watch.start();
            fail("Calling start on a stopped StopWatch should throw an exception as it needs to be reset. ");
        } catch(IllegalStateException ise) {
            
        }
    }

// org.apache.commons.lang3.time.StopWatchTest::testGetStartTime
    public void testGetStartTime() {
        long beforeStopWatch = System.currentTimeMillis();
        StopWatch watch = new StopWatch();
        try {
            watch.getStartTime();
            fail("Calling getStartTime on an unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
            
        }
        watch.start();
        try {
            watch.getStartTime();
            Assert.assertTrue(watch.getStartTime() >= beforeStopWatch);
        } catch (IllegalStateException ex) {
            fail("Start time should be available: " + ex.getMessage());
        }
        watch.reset();
        try {
            watch.getStartTime();
            fail("Calling getStartTime on a reset, but unstarted StopWatch should throw an exception");
        } catch (IllegalStateException expected) {
            
        }
    }
