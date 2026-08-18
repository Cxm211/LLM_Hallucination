// buggy code
    public StrBuilder appendFixedWidthPadLeft(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = (obj == null ? getNullText() : obj.toString());
            int strLen = str.length();
            if (strLen >= width) {
                str.getChars(strLen - width, strLen, buffer, size);
            } else {
                int padLen = width - strLen;
                for (int i = 0; i < padLen; i++) {
                    buffer[size + i] = padChar;
                }
                str.getChars(0, strLen, buffer, size + padLen);
            }
            size += width;
        }
        return this;
    }

    public StrBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
        if (width > 0) {
            ensureCapacity(size + width);
            String str = (obj == null ? getNullText() : obj.toString());
            int strLen = str.length();
            if (strLen >= width) {
                str.getChars(0, width, buffer, size);
            } else {
                int padLen = width - strLen;
                str.getChars(0, strLen, buffer, size);
                for (int i = 0; i < padLen; i++) {
                    buffer[size + strLen + i] = padChar;
                }
            }
            size += width;
        }
        return this;
    }

// relevant test
// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendNewLine
    public void testAppendNewLine() {
        StrBuilder sb = new StrBuilder("---");
        sb.appendNewLine().append("+++");
        assertEquals("---" + SEP + "+++", sb.toString());
        
        sb = new StrBuilder("---");
        sb.setNewLineText("#").appendNewLine().setNewLineText(null).appendNewLine();
        assertEquals("---#" + SEP, sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendWithNullText
    public void testAppendWithNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        assertEquals("", sb.toString());

        sb.appendNull();
        assertEquals("NULL", sb.toString());

        sb.append((Object) null);
        assertEquals("NULLNULL", sb.toString());

        sb.append(FOO);
        assertEquals("NULLNULLfoo", sb.toString());

        sb.append((String) null);
        assertEquals("NULLNULLfooNULL", sb.toString());

        sb.append("");
        assertEquals("NULLNULLfooNULL", sb.toString());

        sb.append("bar");
        assertEquals("NULLNULLfooNULLbar", sb.toString());

        sb.append((StringBuffer) null);
        assertEquals("NULLNULLfooNULLbarNULL", sb.toString());

        sb.append(new StringBuffer("baz"));
        assertEquals("NULLNULLfooNULLbarNULLbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_Object
    public void testAppend_Object() {
        StrBuilder sb = new StrBuilder();
        sb.appendNull();
        assertEquals("", sb.toString());

        sb.append((Object) null);
        assertEquals("", sb.toString());

        sb.append(FOO);
        assertEquals("foo", sb.toString());

        sb.append((StringBuffer) null);
        assertEquals("foo", sb.toString());

        sb.append(new StringBuffer("baz"));
        assertEquals("foobaz", sb.toString());

        sb.append(new StrBuilder("yes"));
        assertEquals("foobazyes", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_String
    public void testAppend_String() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((String) null);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append("foo");
        assertEquals("foo", sb.toString());

        sb.append("");
        assertEquals("foo", sb.toString());

        sb.append("bar");
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_String_int_int
    public void testAppend_String_int_int() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((String) null, 0, 1);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append("foo", 0, 3);
        assertEquals("foo", sb.toString());

        try {
            sb.append("bar", -1, 1);
            fail("append(char[], -1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append("bar", 3, 1);
            fail("append(char[], 3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append("bar", 1, -1);
            fail("append(char[],, -1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append("bar", 1, 3);
            fail("append(char[], 1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append("bar", -1, 3);
            fail("append(char[], -1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append("bar", 4, 0);
            fail("append(char[], 4, 0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.append("bar", 3, 0);
        assertEquals("foo", sb.toString());

        sb.append("abcbardef", 3, 3);
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_StringBuffer
    public void testAppend_StringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((StringBuffer) null);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new StringBuffer("foo"));
        assertEquals("foo", sb.toString());

        sb.append(new StringBuffer(""));
        assertEquals("foo", sb.toString());

        sb.append(new StringBuffer("bar"));
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_StringBuffer_int_int
    public void testAppend_StringBuffer_int_int() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((StringBuffer) null, 0, 1);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new StringBuffer("foo"), 0, 3);
        assertEquals("foo", sb.toString());

        try {
            sb.append(new StringBuffer("bar"), -1, 1);
            fail("append(char[], -1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StringBuffer("bar"), 3, 1);
            fail("append(char[], 3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StringBuffer("bar"), 1, -1);
            fail("append(char[],, -1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StringBuffer("bar"), 1, 3);
            fail("append(char[], 1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StringBuffer("bar"), -1, 3);
            fail("append(char[], -1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StringBuffer("bar"), 4, 0);
            fail("append(char[], 4, 0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.append(new StringBuffer("bar"), 3, 0);
        assertEquals("foo", sb.toString());

        sb.append(new StringBuffer("abcbardef"), 3, 3);
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_StrBuilder
    public void testAppend_StrBuilder() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((StrBuilder) null);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new StrBuilder("foo"));
        assertEquals("foo", sb.toString());

        sb.append(new StrBuilder(""));
        assertEquals("foo", sb.toString());

        sb.append(new StrBuilder("bar"));
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_StrBuilder_int_int
    public void testAppend_StrBuilder_int_int() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((StrBuilder) null, 0, 1);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new StrBuilder("foo"), 0, 3);
        assertEquals("foo", sb.toString());

        try {
            sb.append(new StrBuilder("bar"), -1, 1);
            fail("append(char[], -1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StrBuilder("bar"), 3, 1);
            fail("append(char[], 3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StrBuilder("bar"), 1, -1);
            fail("append(char[],, -1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StrBuilder("bar"), 1, 3);
            fail("append(char[], 1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StrBuilder("bar"), -1, 3);
            fail("append(char[], -1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new StrBuilder("bar"), 4, 0);
            fail("append(char[], 4, 0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.append(new StrBuilder("bar"), 3, 0);
        assertEquals("foo", sb.toString());

        sb.append(new StrBuilder("abcbardef"), 3, 3);
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_CharArray
    public void testAppend_CharArray() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((char[]) null);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new char[0]);
        assertEquals("", sb.toString());

        sb.append(new char[]{'f', 'o', 'o'});
        assertEquals("foo", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_CharArray_int_int
    public void testAppend_CharArray_int_int() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL").append((char[]) null, 0, 1);
        assertEquals("NULL", sb.toString());

        sb = new StrBuilder();
        sb.append(new char[]{'f', 'o', 'o'}, 0, 3);
        assertEquals("foo", sb.toString());

        try {
            sb.append(new char[]{'b', 'a', 'r'}, -1, 1);
            fail("append(char[], -1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new char[]{'b', 'a', 'r'}, 3, 1);
            fail("append(char[], 3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new char[]{'b', 'a', 'r'}, 1, -1);
            fail("append(char[],, -1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new char[]{'b', 'a', 'r'}, 1, 3);
            fail("append(char[], 1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new char[]{'b', 'a', 'r'}, -1, 3);
            fail("append(char[], -1, 3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.append(new char[]{'b', 'a', 'r'}, 4, 0);
            fail("append(char[], 4, 0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.append(new char[]{'b', 'a', 'r'}, 3, 0);
        assertEquals("foo", sb.toString());

        sb.append(new char[]{'a', 'b', 'c', 'b', 'a', 'r', 'd', 'e', 'f'}, 3, 3);
        assertEquals("foobar", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_Boolean
    public void testAppend_Boolean() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());

        sb.append(false);
        assertEquals("truefalse", sb.toString());

        sb.append('!');
        assertEquals("truefalse!", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppend_PrimitiveNumber
    public void testAppend_PrimitiveNumber() {
        StrBuilder sb = new StrBuilder();
        sb.append(0);
        assertEquals("0", sb.toString());

        sb.append(1L);
        assertEquals("01", sb.toString());

        sb.append(2.3f);
        assertEquals("012.3", sb.toString());

        sb.append(4.5d);
        assertEquals("012.34.5", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_Object
    public void testAppendln_Object() {
        StrBuilder sb = new StrBuilder();
        sb.appendln((Object) null);
        assertEquals("" + SEP, sb.toString());

        sb.appendln((Object) FOO);
        assertEquals(SEP + "foo" + SEP, sb.toString());

        sb.appendln(new Integer(6));
        assertEquals(SEP + "foo" + SEP + "6" + SEP, sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_String
    public void testAppendln_String() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(String str) {
                count[0]++;
                return super.append(str);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln("foo");
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(2, count[0]);  
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_String_int_int
    public void testAppendln_String_int_int() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(String str, int startIndex, int length) {
                count[0]++;
                return super.append(str, startIndex, length);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln("foo", 0, 3);
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_StringBuffer
    public void testAppendln_StringBuffer() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(StringBuffer str) {
                count[0]++;
                return super.append(str);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln(new StringBuffer("foo"));
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_StringBuffer_int_int
    public void testAppendln_StringBuffer_int_int() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(StringBuffer str, int startIndex, int length) {
                count[0]++;
                return super.append(str, startIndex, length);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln(new StringBuffer("foo"), 0, 3);
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_StrBuilder
    public void testAppendln_StrBuilder() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(StrBuilder str) {
                count[0]++;
                return super.append(str);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln(new StrBuilder("foo"));
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_StrBuilder_int_int
    public void testAppendln_StrBuilder_int_int() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(StrBuilder str, int startIndex, int length) {
                count[0]++;
                return super.append(str, startIndex, length);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln(new StrBuilder("foo"), 0, 3);
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_CharArray
    public void testAppendln_CharArray() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(char[] str) {
                count[0]++;
                return super.append(str);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln("foo".toCharArray());
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_CharArray_int_int
    public void testAppendln_CharArray_int_int() {
        final int[] count = new int[2];
        StrBuilder sb = new StrBuilder() {
            public StrBuilder append(char[] str, int startIndex, int length) {
                count[0]++;
                return super.append(str, startIndex, length);
            }
            public StrBuilder appendNewLine() {
                count[1]++;
                return super.appendNewLine();
            }
        };
        sb.appendln("foo".toCharArray(), 0, 3);
        assertEquals("foo" + SEP, sb.toString());
        assertEquals(1, count[0]);
        assertEquals(1, count[1]);
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_Boolean
    public void testAppendln_Boolean() {
        StrBuilder sb = new StrBuilder();
        sb.appendln(true);
        assertEquals("true" + SEP, sb.toString());
        
        sb.clear();
        sb.appendln(false);
        assertEquals("false" + SEP, sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendln_PrimitiveNumber
    public void testAppendln_PrimitiveNumber() {
        StrBuilder sb = new StrBuilder();
        sb.appendln(0);
        assertEquals("0" + SEP, sb.toString());
        
        sb.clear();
        sb.appendln(1L);
        assertEquals("1" + SEP, sb.toString());
        
        sb.clear();
        sb.appendln(2.3f);
        assertEquals("2.3" + SEP, sb.toString());
        
        sb.clear();
        sb.appendln(4.5d);
        assertEquals("4.5" + SEP, sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendPadding
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder();
        sb.append("foo");
        assertEquals("foo", sb.toString());

        sb.appendPadding(-1, '-');
        assertEquals("foo", sb.toString());

        sb.appendPadding(0, '-');
        assertEquals("foo", sb.toString());

        sb.appendPadding(1, '-');
        assertEquals("foo-", sb.toString());

        sb.appendPadding(16, '-');
        assertEquals(20, sb.length());
        
        assertEquals("foo-----------------", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendFixedWidthPadLeft
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("foo", -1, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 0, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 1, '-');
        assertEquals("o", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 2, '-');
        assertEquals("oo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 3, '-');
        assertEquals("foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 4, '-');
        assertEquals("-foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 10, '-');
        assertEquals(10, sb.length());
        
        assertEquals("-------foo", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 5, '-');
        assertEquals("-null", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendFixedWidthPadLeft_int
    public void testAppendFixedWidthPadLeft_int() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(123, -1, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 0, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 1, '-');
        assertEquals("3", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 2, '-');
        assertEquals("23", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 3, '-');
        assertEquals("123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 4, '-');
        assertEquals("-123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 10, '-');
        assertEquals(10, sb.length());
        
        assertEquals("-------123", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendFixedWidthPadRight
    public void testAppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("foo", -1, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 0, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 1, '-');
        assertEquals("f", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 2, '-');
        assertEquals("fo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 3, '-');
        assertEquals("foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 4, '-');
        assertEquals("foo-", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 10, '-');
        assertEquals(10, sb.length());
        
        assertEquals("foo-------", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 5, '-');
        assertEquals("null-", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testLang299
    public void testLang299() {
        StrBuilder sb = new StrBuilder(1);
        sb.appendFixedWidthPadRight("foo", 1, '-');
        assertEquals("f", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendFixedWidthPadRight_int
    public void testAppendFixedWidthPadRight_int() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(123, -1, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 0, '-');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 1, '-');
        assertEquals("1", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 2, '-');
        assertEquals("12", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 3, '-');
        assertEquals("123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 4, '-');
        assertEquals("123-", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 10, '-');
        assertEquals(10, sb.length());
        
        assertEquals("123-------", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendAll_Array
    public void testAppendAll_Array() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll((Object[]) null);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(new Object[0]);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(new Object[]{"foo", "bar", "baz"});
        assertEquals("foobarbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendAll_Collection
    public void testAppendAll_Collection() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll((Collection) null);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(Collections.EMPTY_LIST);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(Arrays.asList(new Object[]{"foo", "bar", "baz"}));
        assertEquals("foobarbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendAll_Iterator
    public void testAppendAll_Iterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll((Iterator) null);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(Collections.EMPTY_LIST.iterator());
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendAll(Arrays.asList(new Object[]{"foo", "bar", "baz"}).iterator());
        assertEquals("foobarbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendWithSeparators_Array
    public void testAppendWithSeparators_Array() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"foo", "bar", "baz"}, ",");
        assertEquals("foo,bar,baz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"foo", "bar", "baz"}, null);
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"foo", null, "baz"}, ",");
        assertEquals("foo,,baz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendWithSeparators_Collection
    public void testAppendWithSeparators_Collection() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Collection) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.EMPTY_LIST, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", "bar", "baz"}), ",");
        assertEquals("foo,bar,baz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", "bar", "baz"}), null);
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", null, "baz"}), ",");
        assertEquals("foo,,baz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendWithSeparators_Iterator
    public void testAppendWithSeparators_Iterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Iterator) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.EMPTY_LIST.iterator(), ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", "bar", "baz"}).iterator(), ",");
        assertEquals("foo,bar,baz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", "bar", "baz"}).iterator(), null);
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", null, "baz"}).iterator(), ",");
        assertEquals("foo,,baz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendWithSeparatorsWithNullText
    public void testAppendWithSeparatorsWithNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("null");
        sb.appendWithSeparators(new Object[]{"foo", null, "baz"}, ",");
        assertEquals("foo,null,baz", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(new Object[]{"foo", null, "baz"}), ",");
        assertEquals("foo,null,baz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendSeparator_String
    public void testAppendSeparator_String() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");  
        assertEquals("", sb.toString());
        sb.append("foo");
        assertEquals("foo", sb.toString());
        sb.appendSeparator(",");
        assertEquals("foo,", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendSeparator_char
    public void testAppendSeparator_char() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(',');  
        assertEquals("", sb.toString());
        sb.append("foo");
        assertEquals("foo", sb.toString());
        sb.appendSeparator(',');
        assertEquals("foo,", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendSeparator_String_int
    public void testAppendSeparator_String_int() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",", 0);  
        assertEquals("", sb.toString());
        sb.append("foo");
        assertEquals("foo", sb.toString());
        sb.appendSeparator(",", 1);
        assertEquals("foo,", sb.toString());
        
        sb.appendSeparator(",", -1);  
        assertEquals("foo,", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testAppendSeparator_char_int
    public void testAppendSeparator_char_int() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(',', 0);  
        assertEquals("", sb.toString());
        sb.append("foo");
        assertEquals("foo", sb.toString());
        sb.appendSeparator(',', 1);
        assertEquals("foo,", sb.toString());
        
        sb.appendSeparator(',', -1);  
        assertEquals("foo,", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testInsert
    public void testInsert() {

        StrBuilder sb = new StrBuilder();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, FOO);
            fail("insert(-1, Object) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, FOO);
            fail("insert(7, Object) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (Object) null);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, FOO);
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, "foo");
            fail("insert(-1, String) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, "foo");
            fail("insert(7, String) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (String) null);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, "foo");
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, new char[]{'f', 'o', 'o'});
            fail("insert(-1, char[]) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, new char[]{'f', 'o', 'o'});
            fail("insert(7, char[]) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (char[]) null);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, new char[0]);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, new char[]{'f', 'o', 'o'});
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 3, 3);
            fail("insert(-1, char[], 3, 3) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 3, 3);
            fail("insert(7, char[], 3, 3) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (char[]) null, 0, 0);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, new char[0], 0, 0);
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, -1, 3);
            fail("insert(0, char[], -1, 3) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 10, 3);
            fail("insert(0, char[], 10, 3) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 0, -1);
            fail("insert(0, char[], 0, -1) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 0, 10);
            fail("insert(0, char[], 0, 10) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 0, 0);
        assertEquals("barbaz", sb.toString());

        sb.insert(0, new char[]{'a', 'b', 'c', 'f', 'o', 'o', 'd', 'e', 'f'}, 3, 3);
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, true);
            fail("insert(-1, boolean) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, true);
            fail("insert(7, boolean) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, true);
        assertEquals("truebarbaz", sb.toString());

        sb.insert(0, false);
        assertEquals("falsetruebarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, '!');
            fail("insert(-1, char) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, '!');
            fail("insert(7, char) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, '!');
        assertEquals("!barbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, 0);
            fail("insert(-1, int) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, 0);
            fail("insert(7, int) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, '0');
        assertEquals("0barbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, 1L);
            fail("insert(-1, long) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, 1L);
            fail("insert(7, long) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, 1L);
        assertEquals("1barbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, 2.3F);
            fail("insert(-1, float) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, 2.3F);
            fail("insert(7, float) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, 2.3F);
        assertEquals("2.3barbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, 4.5D);
            fail("insert(-1, double) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, 4.5D);
            fail("insert(7, double) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, 4.5D);
        assertEquals("4.5barbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderAppendInsertTest::testInsertWithNullText
    public void testInsertWithNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("null");
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, FOO);
            fail("insert(-1, Object) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, FOO);
            fail("insert(7, Object) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (Object) null);
        assertEquals("nullbarbaz", sb.toString());

        sb.insert(0, FOO);
        assertEquals("foonullbarbaz", sb.toString());

        sb.clear();
        sb.append("barbaz");
        assertEquals("barbaz", sb.toString());

        try {
            sb.insert(-1, "foo");
            fail("insert(-1, String) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        try {
            sb.insert(7, "foo");
            fail("insert(7, String) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.insert(0, (String) null);
        assertEquals("nullbarbaz", sb.toString());

        sb.insert(0, "foo");
        assertEquals("foonullbarbaz", sb.toString());

        sb.insert(0, (char[]) null);
        assertEquals("nullfoonullbarbaz", sb.toString());

        sb.insert(0, (char[]) null, 0, 0);
        assertEquals("nullnullfoonullbarbaz", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderTest::testConstructors
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

// org.apache.commons.lang.text.StrBuilderTest::testChaining
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

// org.apache.commons.lang.text.StrBuilderTest::testGetSetNewLineText
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

// org.apache.commons.lang.text.StrBuilderTest::testGetSetNullText
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

// org.apache.commons.lang.text.StrBuilderTest::testCapacityAndLength
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

// org.apache.commons.lang.text.StrBuilderTest::testLength
    public void testLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        
        sb.append("Hello");
        assertEquals(5, sb.length());
    }

// org.apache.commons.lang.text.StrBuilderTest::testSetLength
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

// org.apache.commons.lang.text.StrBuilderTest::testCapacity
    public void testCapacity() {
        StrBuilder sb = new StrBuilder();
        assertEquals(sb.buffer.length, sb.capacity());
        
        sb.append("HelloWorldHelloWorldHelloWorldHelloWorld");
        assertEquals(sb.buffer.length, sb.capacity());
    }

// org.apache.commons.lang.text.StrBuilderTest::testEnsureCapacity
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

// org.apache.commons.lang.text.StrBuilderTest::testMinimizeCapacity
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder();
        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
        
        sb.append("HelloWorld");
        sb.minimizeCapacity();
        assertEquals(10, sb.capacity());
    }

// org.apache.commons.lang.text.StrBuilderTest::testSize
    public void testSize() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.size());
        
        sb.append("Hello");
        assertEquals(5, sb.size());
    }

// org.apache.commons.lang.text.StrBuilderTest::testIsEmpty
    public void testIsEmpty() {
        StrBuilder sb = new StrBuilder();
        assertEquals(true, sb.isEmpty());
        
        sb.append("Hello");
        assertEquals(false, sb.isEmpty());
        
        sb.clear();
        assertEquals(true, sb.isEmpty());
    }

// org.apache.commons.lang.text.StrBuilderTest::testClear
    public void testClear() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals(true, sb.buffer.length >= 5);
    }

// org.apache.commons.lang.text.StrBuilderTest::testCharAt
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

// org.apache.commons.lang.text.StrBuilderTest::testSetCharAt
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteCharAt
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(0);
        assertEquals("bc", sb.toString()); 
        
        try {
            sb.deleteCharAt(1000);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang.text.StrBuilderTest::testToCharArray
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

// org.apache.commons.lang.text.StrBuilderTest::testToCharArrayIntInt
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

// org.apache.commons.lang.text.StrBuilderTest::testGetChars
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

// org.apache.commons.lang.text.StrBuilderTest::testGetCharsIntIntCharArrayInt
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteIntInt
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteAll_char
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteFirst_char
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteAll_String
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteFirst_String
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteAll_StrMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testDeleteFirst_StrMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_int_int_String
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceAll_char_char
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceFirst_char_char
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceAll_String_String
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceFirst_String_String
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceAll_StrMatcher_String
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

// org.apache.commons.lang.text.StrBuilderTest::testReplaceFirst_StrMatcher_String
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryReplace
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryStartIndex
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryEndIndex
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

// org.apache.commons.lang.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryCount
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

// org.apache.commons.lang.text.StrBuilderTest::testReverse
    public void testReverse() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.reverse().toString());
        
        sb.clear().append(true);
        assertEquals("eurt", sb.reverse().toString());
        assertEquals("true", sb.reverse().toString());
    }

// org.apache.commons.lang.text.StrBuilderTest::testTrim
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

// org.apache.commons.lang.text.StrBuilderTest::testStartsWith
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

// org.apache.commons.lang.text.StrBuilderTest::testEndsWith
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

// org.apache.commons.lang.text.StrBuilderTest::testSubstringInt
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

// org.apache.commons.lang.text.StrBuilderTest::testSubstringIntInt
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

// org.apache.commons.lang.text.StrBuilderTest::testMidString
    public void testMidString() {
        StrBuilder sb = new StrBuilder("hello goodbye hello");
        assertEquals("goodbye", sb.midString(6, 7));
        assertEquals("hello", sb.midString(0, 5));
        assertEquals("hello", sb.midString(-5, 5));
        assertEquals("", sb.midString(0, -1));
        assertEquals("", sb.midString(20, 2));
        assertEquals("hello", sb.midString(14, 22));
    }

// org.apache.commons.lang.text.StrBuilderTest::testRightString
    public void testRightString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("right", sb.rightString(5));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-5));
        assertEquals("left right", sb.rightString(15));
    }

// org.apache.commons.lang.text.StrBuilderTest::testLeftString
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("left", sb.leftString(4));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-5));
        assertEquals("left right", sb.leftString(15));
    }

// org.apache.commons.lang.text.StrBuilderTest::testContains_char
    public void testContains_char() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains('a'));
        assertEquals(true, sb.contains('o'));
        assertEquals(true, sb.contains('z'));
        assertEquals(false, sb.contains('1'));
    }

// org.apache.commons.lang.text.StrBuilderTest::testContains_String
    public void testContains_String() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains("a"));
        assertEquals(true, sb.contains("pq"));
        assertEquals(true, sb.contains("z"));
        assertEquals(false, sb.contains("zyx"));
        assertEquals(false, sb.contains((String) null));
    }

// org.apache.commons.lang.text.StrBuilderTest::testContains_StrMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_char
    public void testIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf('a'));
        
        
        assertEquals("abab".indexOf('a'), sb.indexOf('a'));

        assertEquals(1, sb.indexOf('b'));
        assertEquals("abab".indexOf('b'), sb.indexOf('b'));

        assertEquals(-1, sb.indexOf('z'));
    }

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_char_int
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

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_char
    public void testLastIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals (2, sb.lastIndexOf('a'));
        
        assertEquals ("abab".lastIndexOf('a'), sb.lastIndexOf('a'));
        
        assertEquals(3, sb.lastIndexOf('b'));
        assertEquals ("abab".lastIndexOf('b'), sb.lastIndexOf('b'));
        
        assertEquals (-1, sb.lastIndexOf('z'));
    }

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_char_int
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

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_String
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

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_String_int
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

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_String
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

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_String_int
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

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_StrMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testIndexOf_StrMatcher_int
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

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_StrMatcher
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

// org.apache.commons.lang.text.StrBuilderTest::testLastIndexOf_StrMatcher_int
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

// org.apache.commons.lang.text.StrBuilderTest::testAsTokenizer
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

// org.apache.commons.lang.text.StrBuilderTest::testAsReader
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

// org.apache.commons.lang.text.StrBuilderTest::testAsWriter
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

// org.apache.commons.lang.text.StrBuilderTest::testEqualsIgnoreCase
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

// org.apache.commons.lang.text.StrBuilderTest::testEquals
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
        
        assertEquals(false, sb1.equals(new Integer(1)));
        assertEquals(false, sb1.equals("abc"));
    }

// org.apache.commons.lang.text.StrBuilderTest::testHashCode
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

// org.apache.commons.lang.text.StrBuilderTest::testToString
    public void testToString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderTest::testToStringBuffer
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder();
        assertEquals(new StringBuffer().toString(), sb.toStringBuffer().toString());
        
        sb.append("junit");
        assertEquals(new StringBuffer("junit").toString(), sb.toStringBuffer().toString());
    }

// org.apache.commons.lang.text.StrBuilderTest::testLang294
    public void testLang294() {
        StrBuilder sb = new StrBuilder("\n%BLAH%\nDo more stuff\neven more stuff\n%BLAH%\n");
        sb.deleteAll("\n%BLAH%");
        assertEquals("\nDo more stuff\neven more stuff\n", sb.toString()); 
    }

// org.apache.commons.lang.text.StrBuilderTest::testIndexOfLang294
    public void testIndexOfLang294() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertEquals(-1, sb.indexOf("three"));
    }

// org.apache.commons.lang.text.StrBuilderTest::testLang295
    public void testLang295() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertFalse( "The contains(char) method is looking beyond the end of the string", sb.contains('h'));
        assertEquals( "The indexOf(char) method is looking beyond the end of the string", -1, sb.indexOf('h'));
    }

// org.apache.commons.lang.text.StrBuilderTest::testLang412Right
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadRight correctly", "**********", sb.toString());
    }

// org.apache.commons.lang.text.StrBuilderTest::testLang412Left
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadLeft correctly", "**********", sb.toString());
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceSimple
    public void testReplaceSimple() {
        doTestReplace("The quick brown fox jumps over the lazy dog.", "The ${animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceSolo
    public void testReplaceSolo() {
        doTestReplace("quick brown fox", "${animal}", false);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceNoVariables
    public void testReplaceNoVariables() {
        doTestNoReplace("The balloon arrived.");
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceNull
    public void testReplaceNull() {
        doTestNoReplace(null);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceEmpty
    public void testReplaceEmpty() {
        doTestNoReplace("");
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceChangedMap
    public void testReplaceChangedMap() {
        StrSubstitutor sub = new StrSubstitutor(values);
        values.put("target", "moon");
        assertEquals("The quick brown fox jumps over the moon.", sub.replace("The ${animal} jumps over the ${target}."));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceUnknownKey
    public void testReplaceUnknownKey() {
        doTestReplace("The ${person} jumps over the lazy dog.", "The ${person} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceAdjacentAtStart
    public void testReplaceAdjacentAtStart() {
        values.put("code", "GBP");
        values.put("amount", "12.50");
        StrSubstitutor sub = new StrSubstitutor(values);
        assertEquals("GBP12.50 charged", sub.replace("${code}${amount} charged"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceAdjacentAtEnd
    public void testReplaceAdjacentAtEnd() {
        values.put("code", "GBP");
        values.put("amount", "12.50");
        StrSubstitutor sub = new StrSubstitutor(values);
        assertEquals("Amount is GBP12.50", sub.replace("Amount is ${code}${amount}"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceRecursive
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

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceEscaping
    public void testReplaceEscaping() {
        doTestReplace("The ${animal} jumps over the lazy dog.", "The $${animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceSoloEscaping
    public void testReplaceSoloEscaping() {
        doTestReplace("${animal}", "$${animal}", false);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceComplexEscaping
    public void testReplaceComplexEscaping() {
        doTestReplace("The ${quick brown fox} jumps over the lazy dog.", "The $${${animal}} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceNoPefixNoSuffix
    public void testReplaceNoPefixNoSuffix() {
        doTestReplace("The animal jumps over the lazy dog.", "The animal jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceIncompletePefix
    public void testReplaceIncompletePefix() {
        doTestReplace("The {animal} jumps over the lazy dog.", "The {animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplacePrefixNoSuffix
    public void testReplacePrefixNoSuffix() {
        doTestReplace("The ${animal jumps over the ${target} lazy dog.", "The ${animal jumps over the ${target} ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceNoPrefixSuffix
    public void testReplaceNoPrefixSuffix() {
        doTestReplace("The animal} jumps over the lazy dog.", "The animal} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceEmptyKeys
    public void testReplaceEmptyKeys() {
        doTestReplace("The ${} jumps over the lazy dog.", "The ${} jumps over the ${target}.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceToIdentical
    public void testReplaceToIdentical() {
        values.put("animal", "$${${thing}}");
        values.put("thing", "animal");
        doTestReplace("The ${animal} jumps.", "The ${animal} jumps.", true);
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testCyclicReplacement
    public void testCyclicReplacement() {
        Map map = new HashMap();
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

// org.apache.commons.lang.text.StrSubstitutorTest::testReplaceWeirdPattens
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

// org.apache.commons.lang.text.StrSubstitutorTest::testReplacePartialString_noReplace
    public void testReplacePartialString_noReplace() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals("${animal} jumps", sub.replace("The ${animal} jumps over the ${target}.", 4, 15));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testResolveVariable
    public void testResolveVariable() {
        final StrBuilder builder = new StrBuilder("Hi ${name}!");
        Map map = new HashMap();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map) {
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

// org.apache.commons.lang.text.StrSubstitutorTest::testConstructorNoArgs
    public void testConstructorNoArgs() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals("Hi ${name}", sub.replace("Hi ${name}"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testConstructorMapPrefixSuffix
    public void testConstructorMapPrefixSuffix() {
        Map map = new HashMap();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map, "<", ">");
        assertEquals("Hi < commons", sub.replace("Hi $< <name>"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testConstructorMapFull
    public void testConstructorMapFull() {
        Map map = new HashMap();
        map.put("name", "commons");
        StrSubstitutor sub = new StrSubstitutor(map, "<", ">", '!');
        assertEquals("Hi < commons", sub.replace("Hi !< <name>"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testGetSetEscape
    public void testGetSetEscape() {
        StrSubstitutor sub = new StrSubstitutor();
        assertEquals('$', sub.getEscapeChar());
        sub.setEscapeChar('<');
        assertEquals('<', sub.getEscapeChar());
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testGetSetPrefix
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

// org.apache.commons.lang.text.StrSubstitutorTest::testGetSetSuffix
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

// org.apache.commons.lang.text.StrSubstitutorTest::testStaticReplace
    public void testStaticReplace() {
        Map map = new HashMap();
        map.put("name", "commons");
        assertEquals("Hi commons!", StrSubstitutor.replace("Hi ${name}!", map));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testStaticReplacePrefixSuffix
    public void testStaticReplacePrefixSuffix() {
        Map map = new HashMap();
        map.put("name", "commons");
        assertEquals("Hi commons!", StrSubstitutor.replace("Hi <name>!", map, "<", ">"));
    }

// org.apache.commons.lang.text.StrSubstitutorTest::testStaticReplaceSystemProperties
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

// org.apache.commons.lang.text.StrTokenizerTest::test1
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

// org.apache.commons.lang.text.StrTokenizerTest::test2
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

// org.apache.commons.lang.text.StrTokenizerTest::test3
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

// org.apache.commons.lang.text.StrTokenizerTest::test4
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

// org.apache.commons.lang.text.StrTokenizerTest::test5
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

// org.apache.commons.lang.text.StrTokenizerTest::test6
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

// org.apache.commons.lang.text.StrTokenizerTest::test7
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

// org.apache.commons.lang.text.StrTokenizerTest::test8
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasic1
    public void testBasic1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasic2
    public void testBasic2() {
        String input = "a \nb\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasic3
    public void testBasic3() {
        String input = "a \nb\u0001\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b\u0001", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasic4
    public void testBasic4() {
        String input = "a \"b\" c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("\"b\"", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasic5
    public void testBasic5() {
        String input = "a:b':c";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        assertEquals("a", tok.next());
        assertEquals("b'", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicDelim1
    public void testBasicDelim1() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ':');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicDelim2
    public void testBasicDelim2() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ',');
        assertEquals("a:b:c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicEmpty1
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicEmpty2
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted1
    public void testBasicQuoted1() {
        String input = "a 'b' c";
        StrTokenizer tok = new StrTokenizer(input, ' ', '\'');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted2
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted3
    public void testBasicQuoted3() {
        String input = "a:'b''c'";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b'c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted4
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted5
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted6
    public void testBasicQuoted6() {
        String input = "a:'b'\"c':d";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("b\"c:d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuoted7
    public void testBasicQuoted7() {
        String input = "a:\"There's a reason here\":b";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("There's a reason here", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testBasicQuotedTrimmed1
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicTrimmed1
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicTrimmed2
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicIgnoreTrimmed1
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicIgnoreTrimmed2
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicIgnoreTrimmed3
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

// org.apache.commons.lang.text.StrTokenizerTest::testBasicIgnoreTrimmed4
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

// org.apache.commons.lang.text.StrTokenizerTest::testListArray
    public void testListArray() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        String[] array = tok.getTokenArray();
        List list = tok.getTokenList();
        
        assertEquals(Arrays.asList(array), list);
        assertEquals(3, list.size());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testCSV
    public void testCSV(String data) {
        this.testXSVAbc(StrTokenizer.getCSVInstance(data));
        this.testXSVAbc(StrTokenizer.getCSVInstance(data.toCharArray()));
    }

// org.apache.commons.lang.text.StrTokenizerTest::testCSVEmpty
    public void testCSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang.text.StrTokenizerTest::testCSVSimple
    public void testCSVSimple() {
        this.testCSV(CSV_SIMPLE_FIXTURE);
    }

// org.apache.commons.lang.text.StrTokenizerTest::testCSVSimpleNeedsTrim
    public void testCSVSimpleNeedsTrim() {
        this.testCSV("   " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n\t  " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n  " + CSV_SIMPLE_FIXTURE + "\n\n\r");
    }

// org.apache.commons.lang.text.StrTokenizerTest::testGetContent
    public void testGetContent() {
        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals(input, tok.getContent());

        tok = new StrTokenizer(input.toCharArray());
        assertEquals(input, tok.getContent());
        
        tok = new StrTokenizer();
        assertEquals(null, tok.getContent());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testChaining
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

// org.apache.commons.lang.text.StrTokenizerTest::testCloneNotSupportedException
    public void testCloneNotSupportedException() {
        Object notCloned = (new StrTokenizer() {
            Object cloneReset() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("test");
            }
        }).clone();
        assertNull(notCloned);
    }

// org.apache.commons.lang.text.StrTokenizerTest::testCloneNull
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

// org.apache.commons.lang.text.StrTokenizerTest::testCloneReset
    public void testCloneReset() {
        char[] input = new char[]{'a'};
        StrTokenizer tokenizer = new StrTokenizer(input);
        
        assertEquals("a", tokenizer.nextToken());
        tokenizer.reset();
        assertEquals("a", tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        input[0] = 'b';
        tokenizer.reset();
        assertEquals("b", tokenizer.nextToken());
        assertEquals("a", clonedTokenizer.nextToken());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_String
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

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_String_char
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

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_String_char_char
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

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_charArray
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

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_charArray_char
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

// org.apache.commons.lang.text.StrTokenizerTest::testConstructor_charArray_char_char
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

// org.apache.commons.lang.text.StrTokenizerTest::testReset
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

// org.apache.commons.lang.text.StrTokenizerTest::testReset_String
    public void testReset_String() {
        StrTokenizer tok = new StrTokenizer("x x x");
        tok.reset("d e");
        assertEquals("d", tok.next());
        assertEquals("e", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testReset_charArray
    public void testReset_charArray() {
        StrTokenizer tok = new StrTokenizer("x x x");
        
        char[] array = new char[] {'a', ' ', 'c'};
        tok.reset(array);
        array[1] = 'b'; 
        assertEquals("abc", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testTSV
    public void testTSV() {
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE));
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE.toCharArray()));
    }

// org.apache.commons.lang.text.StrTokenizerTest::testTSVEmpty
    public void testTSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang.text.StrTokenizerTest::testIteration
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

// org.apache.commons.lang.text.StrTokenizerTest::testTokenizeSubclassInputChange
    public void testTokenizeSubclassInputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c d e") {
            protected List tokenize(char[] chars, int offset, int count) {
                return super.tokenize("w x y z".toCharArray(), 2, 5);
            }
        };
        assertEquals("x", tkn.next());
        assertEquals("y", tkn.next());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testTokenizeSubclassOutputChange
    public void testTokenizeSubclassOutputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c") {
            protected List tokenize(char[] chars, int offset, int count) {
                List list = super.tokenize(chars, offset, count);
                Collections.reverse(list);
                return list;
            }
        };
        assertEquals("c", tkn.next());
        assertEquals("b", tkn.next());
        assertEquals("a", tkn.next());
    }

// org.apache.commons.lang.text.StrTokenizerTest::testToString
    public void testToString() {
        StrTokenizer tkn = new StrTokenizer("a b c d e");
        assertEquals("StrTokenizer[not tokenized yet]", tkn.toString());
        tkn.next();
        assertEquals("StrTokenizer[a, b, c, d, e]", tkn.toString());
    }
