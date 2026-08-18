// buggy code
    private int handleG(String value, 
                        DoubleMetaphoneResult result, 
                        int index, 
                        boolean slavoGermanic) {
        if (charAt(value, index + 1) == 'H') {
            index = handleGH(value, result, index);
        } else if (charAt(value, index + 1) == 'N') {
            if (index == 1 && isVowel(charAt(value, 0)) && !slavoGermanic) {
                result.append("KN", "N");
            } else if (!contains(value, index + 2, 2, "EY") && 
                       charAt(value, index + 1) != 'Y' && !slavoGermanic) {
                result.append("N", "KN");
            } else {
                result.append("KN");
            }
            index = index + 2;
        } else if (contains(value, index + 1, 2, "LI") && !slavoGermanic) {
            result.append("KL", "L");
            index += 2;
        } else if (index == 0 && (charAt(value, index + 1) == 'Y' || contains(value, index + 1, 2, ES_EP_EB_EL_EY_IB_IL_IN_IE_EI_ER))) {
            //-- -ges-, -gep-, -gel-, -gie- at beginning --//
            result.append('K', 'J');
            index += 2;
        } else if ((contains(value, index + 1, 2, "ER") || 
                    charAt(value, index + 1) == 'Y') &&
                   !contains(value, 0, 6, "DANGER", "RANGER", "MANGER") &&
                   !contains(value, index - 1, 1, "E", "I") && 
                   !contains(value, index - 1, 3, "RGY", "OGY")) {
            //-- -ger-, -gy- --//
            result.append('K', 'J');
            index += 2;
        } else if (contains(value, index + 1, 1, "E", "I", "Y") || 
                   contains(value, index - 1, 4, "AGGI", "OGGI")) {
            //-- Italian "biaggi" --//
            if ((contains(value, 0 ,4, "VAN ", "VON ") || contains(value, 0, 3, "SCH")) || contains(value, index + 1, 2, "ET")) {
                //-- obvious germanic --//
                result.append('K');
            } else if (contains(value, index + 1, 4, "IER")) {
                result.append('J');
            } else {
                result.append('J', 'K');
            }
            index += 2;
        } else if (charAt(value, index + 1) == 'G') {
            index += 2;
            result.append('K');
        } else {
            index++;
            result.append('K');
        }
        return index;
    }

// relevant test
// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorNoArgCon
    public void testComparatorNoArgCon() throws Exception {
        new StringEncoderComparator();
    }

// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithSoundex
    public void testComparatorWithSoundex() throws Exception {
        StringEncoderComparator sCompare = 
            new StringEncoderComparator( new Soundex() );

        assertTrue( "O'Brien and O'Brian didn't come out with " +
                    "the same Soundex, something must be wrong here",
                    0 == sCompare.compare( "O'Brien", "O'Brian" ) );
    }

// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithDoubleMetaphone
    public void testComparatorWithDoubleMetaphone() throws Exception {
        StringEncoderComparator sCompare =
            new StringEncoderComparator( new DoubleMetaphone() );
            
        String[] testArray = { "Jordan", "Sosa", "Prior", "Pryor" };
        List testList = Arrays.asList( testArray );        
        
        String[] controlArray = { "Jordan", "Prior", "Pryor", "Sosa" };

        Collections.sort( testList, sCompare);            
        
        String[] resultArray = (String[]) testList.toArray(new String[0]);
        
        for( int i = 0; i < resultArray.length; i++) {
            assertEquals( "Result Array not Equal to Control Array at index: " + i, controlArray[i], resultArray[i] );
        }
    }

// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithDoubleMetaphoneAndInvalidInput
    public void testComparatorWithDoubleMetaphoneAndInvalidInput() throws Exception {
        StringEncoderComparator sCompare =
            new StringEncoderComparator( new DoubleMetaphone() );
           
        int compare = sCompare.compare(new Double(3.0), new Long(3));
        assertEquals( "Trying to compare objects that make no sense to the underlying encoder should return a zero compare code",
                                0, compare);        
        
    }

// org.apache.commons.codec.language.DoubleMetaphone2Test::testDoubleMetaphonePrimary
    public void testDoubleMetaphonePrimary() {
        String value = null;
        for (int i = 0; i < TEST_DATA.length; i++) {
            value = TEST_DATA[i][0];
            assertEquals("Test [" + i + "]=" + value, TEST_DATA[i][1], doubleMetaphone.doubleMetaphone(value, false));
        }
    }

// org.apache.commons.codec.language.DoubleMetaphone2Test::testDoubleMetaphoneAlternate
    public void testDoubleMetaphoneAlternate() {
        String value = null;
        for (int i = 0; i < TEST_DATA.length; i++) {
            value = TEST_DATA[i][0];
            assertEquals("Test [" + i + "]=" + value, TEST_DATA[i][2], doubleMetaphone.doubleMetaphone(value, true));
        }
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testDoubleMetaphone
    public void testDoubleMetaphone() {
        assertDoubleMetaphone("TSTN", "testing");
        assertDoubleMetaphone("0", "The");
        assertDoubleMetaphone("KK", "quick");
        assertDoubleMetaphone("PRN", "brown");
        assertDoubleMetaphone("FKS", "fox");
        assertDoubleMetaphone("JMPT", "jumped");
        assertDoubleMetaphone("AFR", "over");
        assertDoubleMetaphone("0", "the");
        assertDoubleMetaphone("LS", "lazy");
        assertDoubleMetaphone("TKS", "dogs");
        assertDoubleMetaphone("MKFR", "MacCafferey");
        assertDoubleMetaphone("STFN", "Stephan");
        assertDoubleMetaphone("KSSK", "Kuczewski");
        assertDoubleMetaphone("MKLL", "McClelland");
        assertDoubleMetaphone("SNHS", "san jose");
        assertDoubleMetaphone("SNFP", "xenophobia");

        assertDoubleMetaphoneAlt("TSTN", "testing");
        assertDoubleMetaphoneAlt("T", "The");
        assertDoubleMetaphoneAlt("KK", "quick");
        assertDoubleMetaphoneAlt("PRN", "brown");
        assertDoubleMetaphoneAlt("FKS", "fox");
        assertDoubleMetaphoneAlt("AMPT", "jumped");
        assertDoubleMetaphoneAlt("AFR", "over");
        assertDoubleMetaphoneAlt("T", "the");
        assertDoubleMetaphoneAlt("LS", "lazy");
        assertDoubleMetaphoneAlt("TKS", "dogs");
        assertDoubleMetaphoneAlt("MKFR", "MacCafferey");
        assertDoubleMetaphoneAlt("STFN", "Stephan");
        assertDoubleMetaphoneAlt("KXFS", "Kutchefski");
        assertDoubleMetaphoneAlt("MKLL", "McClelland");
        assertDoubleMetaphoneAlt("SNHS", "san jose");
        assertDoubleMetaphoneAlt("SNFP", "xenophobia");
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testEmpty
    public void testEmpty() {
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(null));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(""));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(" "));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone("\t\n\r "));
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testSetMaxCodeLength
    public void testSetMaxCodeLength() {
        String value = "jumped";
        
        DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

        
        assertEquals("Default Max Code Length", 4, doubleMetaphone.getMaxCodeLen());
        assertEquals("Default Primary",   "JMPT", doubleMetaphone.doubleMetaphone(value, false));
        assertEquals("Default Alternate", "AMPT", doubleMetaphone.doubleMetaphone(value, true));

        
        doubleMetaphone.setMaxCodeLen(3);
        assertEquals("Set Max Code Length", 3, doubleMetaphone.getMaxCodeLen());
        assertEquals("Max=3 Primary",   "JMP", doubleMetaphone.doubleMetaphone(value, false));
        assertEquals("Max=3 Alternate", "AMP", doubleMetaphone.doubleMetaphone(value, true));
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualBasic
    public void testIsDoubleMetaphoneEqualBasic() {
        String[][] testFixture = new String[][] { { "Case", "case" }, {
                "CASE", "Case" }, {
                "caSe", "cAsE" }, {
                "cookie", "quick" }, {
                "quick", "cookie" }, {
                "Brian", "Bryan" }, {
                "Auto", "Otto" }, {
                "Steven", "Stefan" }, {
                "Philipowitz", "Filipowicz" }
        };
        doubleMetaphoneEqualTest(testFixture, false);
        doubleMetaphoneEqualTest(testFixture, true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended1
    public void testIsDoubleMetaphoneEqualExtended1() {
        
        
        
        
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended2
    public void testIsDoubleMetaphoneEqualExtended2() {
        String[][] testFixture = new String[][] { { "Jablonski", "Yablonsky" }
        };
        
        doubleMetaphoneEqualTest(testFixture, true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualExtended3
    public void testIsDoubleMetaphoneEqualExtended3() {
        this.validateFixture(FIXTURE);
        StringBuffer failures = new StringBuffer();
        StringBuffer matches = new StringBuffer();
        String cr = System.getProperty("line.separator");
        matches.append("private static final String[][] MATCHES = {" + cr);
        int failCount = 0;
        for (int i = 0; i < FIXTURE.length; i++) {
            String name0 = FIXTURE[i][0];
            String name1 = FIXTURE[i][1];
            boolean match1 = this.getDoubleMetaphone().isDoubleMetaphoneEqual(name0, name1, false);
            boolean match2 = this.getDoubleMetaphone().isDoubleMetaphoneEqual(name0, name1, true);
            if (match1 == false && match2 == false) {
                String failMsg = "[" + i + "] " + name0 + " and " + name1 + cr;
                failures.append(failMsg);
                failCount++;
            } else {
                matches.append("{\"" + name0 + "\", \"" + name1 + "\"}," + cr);
            }
        }
        String msg = failures.toString();
        matches.append("};");
        
        
        if (msg.length() > 0) {
            
            
            
        }
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneEqualWithMATCHES
    public void testIsDoubleMetaphoneEqualWithMATCHES() {
        this.validateFixture(MATCHES);
        for (int i = 0; i < MATCHES.length; i++) {
            String name0 = MATCHES[i][0];
            String name1 = MATCHES[i][1];
            boolean match1 = this.getDoubleMetaphone().isDoubleMetaphoneEqual(name0, name1, false);
            boolean match2 = this.getDoubleMetaphone().isDoubleMetaphoneEqual(name0, name1, true);
            if (match1 == false && match2 == false) {
                fail("Expected match [" + i + "] " + name0 + " and " + name1);
            }
        }
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testIsDoubleMetaphoneNotEqual
    public void testIsDoubleMetaphoneNotEqual() {
        doubleMetaphoneNotEqualTest(false);
        doubleMetaphoneNotEqualTest(true);
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testCCedilla
    public void testCCedilla() {
        this.getDoubleMetaphone().isDoubleMetaphoneEqual("ç", "S");
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testNTilde
    public void testNTilde() {
        this.getDoubleMetaphone().isDoubleMetaphoneEqual("ñ", "N");
    }
