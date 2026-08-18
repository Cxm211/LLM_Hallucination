// buggy code
    private char getMappingCode(final String str, final int index) {
        // map() throws IllegalArgumentException
        final char mappedChar = this.map(str.charAt(index));
        // HW rule check
        if (index > 1 && mappedChar != '0') {
            final char hwChar = str.charAt(index - 1);
            if ('H' == hwChar || 'W' == hwChar) {
                final char preHWChar = str.charAt(index - 2);
                final char firstCode = this.map(preHWChar);
                if (firstCode == mappedChar || 'H' == preHWChar || 'W' == preHWChar) {
                    return 0;
                }
            }
        }
        return mappedChar;
    }

// relevant test
// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithSoundex
    public void testComparatorWithSoundex() throws Exception {
        final StringEncoderComparator sCompare =
            new StringEncoderComparator( new Soundex() );

        assertTrue( "O'Brien and O'Brian didn't come out with " +
                    "the same Soundex, something must be wrong here",
                    0 == sCompare.compare( "O'Brien", "O'Brian" ) );
    }

// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithDoubleMetaphone
    public void testComparatorWithDoubleMetaphone() throws Exception {
        final StringEncoderComparator sCompare = new StringEncoderComparator(new DoubleMetaphone());

        final String[] testArray = { "Jordan", "Sosa", "Prior", "Pryor" };
        final List<String> testList = Arrays.asList(testArray);

        final String[] controlArray = { "Jordan", "Prior", "Pryor", "Sosa" };

        Collections.sort(testList, sCompare);

        final String[] resultArray = testList.toArray(new String[0]);

        for (int i = 0; i < resultArray.length; i++) {
            assertEquals("Result Array not Equal to Control Array at index: " + i, controlArray[i], resultArray[i]);
        }
    }

// org.apache.commons.codec.StringEncoderComparatorTest::testComparatorWithDoubleMetaphoneAndInvalidInput
    public void testComparatorWithDoubleMetaphoneAndInvalidInput() throws Exception {
        final StringEncoderComparator sCompare =
            new StringEncoderComparator( new DoubleMetaphone() );

        final int compare = sCompare.compare(new Double(3.0), Long.valueOf(3));
        assertEquals( "Trying to compare objects that make no sense to the underlying encoder should return a zero compare code",
                                0, compare);
    }

// org.apache.commons.codec.language.SoundexTest::testB650
    public void testB650() throws EncoderException {
        this.checkEncodingVariations("B650", new String[]{
            "BARHAM",
            "BARONE",
            "BARRON",
            "BERNA",
            "BIRNEY",
            "BIRNIE",
            "BOOROM",
            "BOREN",
            "BORN",
            "BOURN",
            "BOURNE",
            "BOWRON",
            "BRAIN",
            "BRAME",
            "BRANN",
            "BRAUN",
            "BREEN",
            "BRIEN",
            "BRIM",
            "BRIMM",
            "BRINN",
            "BRION",
            "BROOM",
            "BROOME",
            "BROWN",
            "BROWNE",
            "BRUEN",
            "BRUHN",
            "BRUIN",
            "BRUMM",
            "BRUN",
            "BRUNO",
            "BRYAN",
            "BURIAN",
            "BURN",
            "BURNEY",
            "BYRAM",
            "BYRNE",
            "BYRON",
            "BYRUM"});
    }

// org.apache.commons.codec.language.SoundexTest::testBadCharacters
    public void testBadCharacters() {
        Assert.assertEquals("H452", this.getStringEncoder().encode("HOL>MES"));

    }

// org.apache.commons.codec.language.SoundexTest::testDifference
    public void testDifference() throws EncoderException {
        
        Assert.assertEquals(0, this.getStringEncoder().difference(null, null));
        Assert.assertEquals(0, this.getStringEncoder().difference("", ""));
        Assert.assertEquals(0, this.getStringEncoder().difference(" ", " "));
        
        Assert.assertEquals(4, this.getStringEncoder().difference("Smith", "Smythe"));
        Assert.assertEquals(2, this.getStringEncoder().difference("Ann", "Andrew"));
        Assert.assertEquals(1, this.getStringEncoder().difference("Margaret", "Andrew"));
        Assert.assertEquals(0, this.getStringEncoder().difference("Janet", "Margaret"));
        
        Assert.assertEquals(4, this.getStringEncoder().difference("Green", "Greene"));
        Assert.assertEquals(0, this.getStringEncoder().difference("Blotchet-Halls", "Greene"));
        
        Assert.assertEquals(4, this.getStringEncoder().difference("Smith", "Smythe"));
        Assert.assertEquals(4, this.getStringEncoder().difference("Smithers", "Smythers"));
        Assert.assertEquals(2, this.getStringEncoder().difference("Anothers", "Brothers"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBasic
    public void testEncodeBasic() {
        Assert.assertEquals("T235", this.getStringEncoder().encode("testing"));
        Assert.assertEquals("T000", this.getStringEncoder().encode("The"));
        Assert.assertEquals("Q200", this.getStringEncoder().encode("quick"));
        Assert.assertEquals("B650", this.getStringEncoder().encode("brown"));
        Assert.assertEquals("F200", this.getStringEncoder().encode("fox"));
        Assert.assertEquals("J513", this.getStringEncoder().encode("jumped"));
        Assert.assertEquals("O160", this.getStringEncoder().encode("over"));
        Assert.assertEquals("T000", this.getStringEncoder().encode("the"));
        Assert.assertEquals("L200", this.getStringEncoder().encode("lazy"));
        Assert.assertEquals("D200", this.getStringEncoder().encode("dogs"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch2
    public void testEncodeBatch2() {
        Assert.assertEquals("A462", this.getStringEncoder().encode("Allricht"));
        Assert.assertEquals("E166", this.getStringEncoder().encode("Eberhard"));
        Assert.assertEquals("E521", this.getStringEncoder().encode("Engebrethson"));
        Assert.assertEquals("H512", this.getStringEncoder().encode("Heimbach"));
        Assert.assertEquals("H524", this.getStringEncoder().encode("Hanselmann"));
        Assert.assertEquals("H431", this.getStringEncoder().encode("Hildebrand"));
        Assert.assertEquals("K152", this.getStringEncoder().encode("Kavanagh"));
        Assert.assertEquals("L530", this.getStringEncoder().encode("Lind"));
        Assert.assertEquals("L222", this.getStringEncoder().encode("Lukaschowsky"));
        Assert.assertEquals("M235", this.getStringEncoder().encode("McDonnell"));
        Assert.assertEquals("M200", this.getStringEncoder().encode("McGee"));
        Assert.assertEquals("O155", this.getStringEncoder().encode("Opnian"));
        Assert.assertEquals("O155", this.getStringEncoder().encode("Oppenheimer"));
        Assert.assertEquals("R355", this.getStringEncoder().encode("Riedemanas"));
        Assert.assertEquals("Z300", this.getStringEncoder().encode("Zita"));
        Assert.assertEquals("Z325", this.getStringEncoder().encode("Zitzmeinn"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch3
    public void testEncodeBatch3() {
        Assert.assertEquals("W252", this.getStringEncoder().encode("Washington"));
        Assert.assertEquals("L000", this.getStringEncoder().encode("Lee"));
        Assert.assertEquals("G362", this.getStringEncoder().encode("Gutierrez"));
        Assert.assertEquals("P236", this.getStringEncoder().encode("Pfister"));
        Assert.assertEquals("J250", this.getStringEncoder().encode("Jackson"));
        Assert.assertEquals("T522", this.getStringEncoder().encode("Tymczak"));
        
        
        Assert.assertEquals("V532", this.getStringEncoder().encode("VanDeusen"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch4
    public void testEncodeBatch4() {
        Assert.assertEquals("H452", this.getStringEncoder().encode("HOLMES"));
        Assert.assertEquals("A355", this.getStringEncoder().encode("ADOMOMI"));
        Assert.assertEquals("V536", this.getStringEncoder().encode("VONDERLEHR"));
        Assert.assertEquals("B400", this.getStringEncoder().encode("BALL"));
        Assert.assertEquals("S000", this.getStringEncoder().encode("SHAW"));
        Assert.assertEquals("J250", this.getStringEncoder().encode("JACKSON"));
        Assert.assertEquals("S545", this.getStringEncoder().encode("SCANLON"));
        Assert.assertEquals("S532", this.getStringEncoder().encode("SAINTJOHN"));

    }

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreApostrophes
    public void testEncodeIgnoreApostrophes() throws EncoderException {
        this.checkEncodingVariations("O165", new String[]{
            "OBrien",
            "'OBrien",
            "O'Brien",
            "OB'rien",
            "OBr'ien",
            "OBri'en",
            "OBrie'n",
            "OBrien'"});
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreHyphens
    public void testEncodeIgnoreHyphens() throws EncoderException {
        this.checkEncodingVariations("K525", new String[]{
            "KINGSMITH",
            "-KINGSMITH",
            "K-INGSMITH",
            "KI-NGSMITH",
            "KIN-GSMITH",
            "KING-SMITH",
            "KINGS-MITH",
            "KINGSM-ITH",
            "KINGSMI-TH",
            "KINGSMIT-H",
            "KINGSMITH-"});
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreTrimmable
    public void testEncodeIgnoreTrimmable() {
        Assert.assertEquals("W252", this.getStringEncoder().encode(" \t\n\r Washington \t\n\r "));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx1
    public void testHWRuleEx1() {
        
        
        
        
        Assert.assertEquals("A261", this.getStringEncoder().encode("Ashcraft"));
        Assert.assertEquals("A261", this.getStringEncoder().encode("Ashcroft"));
        Assert.assertEquals("Y330", this.getStringEncoder().encode("yehudit"));
        Assert.assertEquals("Y330", this.getStringEncoder().encode("yhwdyt"));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx2
    public void testHWRuleEx2() {
        Assert.assertEquals("B312", this.getStringEncoder().encode("BOOTHDAVIS"));
        Assert.assertEquals("B312", this.getStringEncoder().encode("BOOTH-DAVIS"));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx3
    public void testHWRuleEx3() throws EncoderException {
        Assert.assertEquals("S460", this.getStringEncoder().encode("Sgler"));
        Assert.assertEquals("S460", this.getStringEncoder().encode("Swhgler"));
        
        this.checkEncodingVariations("S460", new String[]{
            "SAILOR",
            "SALYER",
            "SAYLOR",
            "SCHALLER",
            "SCHELLER",
            "SCHILLER",
            "SCHOOLER",
            "SCHULER",
            "SCHUYLER",
            "SEILER",
            "SEYLER",
            "SHOLAR",
            "SHULER",
            "SILAR",
            "SILER",
            "SILLER"});
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer1
    public void testMsSqlServer1() {
        Assert.assertEquals("S530", this.getStringEncoder().encode("Smith"));
        Assert.assertEquals("S530", this.getStringEncoder().encode("Smythe"));
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer2
    public void testMsSqlServer2() throws EncoderException {
        this.checkEncodingVariations("E625", new String[]{"Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"});
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer3
    public void testMsSqlServer3() {
        Assert.assertEquals("A500", this.getStringEncoder().encode("Ann"));
        Assert.assertEquals("A536", this.getStringEncoder().encode("Andrew"));
        Assert.assertEquals("J530", this.getStringEncoder().encode("Janet"));
        Assert.assertEquals("M626", this.getStringEncoder().encode("Margaret"));
        Assert.assertEquals("S315", this.getStringEncoder().encode("Steven"));
        Assert.assertEquals("M240", this.getStringEncoder().encode("Michael"));
        Assert.assertEquals("R163", this.getStringEncoder().encode("Robert"));
        Assert.assertEquals("L600", this.getStringEncoder().encode("Laura"));
        Assert.assertEquals("A500", this.getStringEncoder().encode("Anne"));
    }

// org.apache.commons.codec.language.SoundexTest::testNewInstance
    public void testNewInstance() {
        Assert.assertEquals("W452", new Soundex().soundex("Williams"));
    }

// org.apache.commons.codec.language.SoundexTest::testNewInstance2
    public void testNewInstance2() {
        Assert.assertEquals("W452", new Soundex(Soundex.US_ENGLISH_MAPPING_STRING.toCharArray()).soundex("Williams"));
    }

// org.apache.commons.codec.language.SoundexTest::testNewInstance3
    public void testNewInstance3() {
        Assert.assertEquals("W452", new Soundex(Soundex.US_ENGLISH_MAPPING_STRING).soundex("Williams"));
    }

// org.apache.commons.codec.language.SoundexTest::testSoundexUtilsConstructable
    public void testSoundexUtilsConstructable() {
        new SoundexUtils();
    }

// org.apache.commons.codec.language.SoundexTest::testSoundexUtilsNullBehaviour
    public void testSoundexUtilsNullBehaviour() {
        Assert.assertEquals(null, SoundexUtils.clean(null));
        Assert.assertEquals("", SoundexUtils.clean(""));
        Assert.assertEquals(0, SoundexUtils.differenceEncoded(null, ""));
        Assert.assertEquals(0, SoundexUtils.differenceEncoded("", null));
    }

// org.apache.commons.codec.language.SoundexTest::testUsEnglishStatic
    public void testUsEnglishStatic() {
        Assert.assertEquals("W452", Soundex.US_ENGLISH.soundex("Williams"));
    }

// org.apache.commons.codec.language.SoundexTest::testUsMappingEWithAcute
    public void testUsMappingEWithAcute() {
        Assert.assertEquals("E000", this.getStringEncoder().encode("e"));
        if (Character.isLetter('\u00e9')) { 
            try {
                
                Assert.assertEquals("\u00c9000", this.getStringEncoder().encode("\u00e9"));
                Assert.fail("Expected IllegalArgumentException not thrown");
            } catch (final IllegalArgumentException e) {
                
            }
        } else {
            Assert.assertEquals("", this.getStringEncoder().encode("\u00e9"));
        }
    }

// org.apache.commons.codec.language.SoundexTest::testUsMappingOWithDiaeresis
    public void testUsMappingOWithDiaeresis() {
        Assert.assertEquals("O000", this.getStringEncoder().encode("o"));
        if (Character.isLetter('\u00f6')) { 
            try {
                
                Assert.assertEquals("\u00d6000", this.getStringEncoder().encode("\u00f6"));
                Assert.fail("Expected IllegalArgumentException not thrown");
            } catch (final IllegalArgumentException e) {
                
            }
        } else {
            Assert.assertEquals("", this.getStringEncoder().encode("\u00f6"));
        }
    }

// org.apache.commons.codec.language.SoundexTest::testWikipediaAmericanSoundex
    public void testWikipediaAmericanSoundex() {
        Assert.assertEquals("R163", this.getStringEncoder().encode("Robert"));        
        Assert.assertEquals("R163", this.getStringEncoder().encode("Rupert"));        
        Assert.assertEquals("A261", this.getStringEncoder().encode("Ashcraft"));        
        Assert.assertEquals("A261", this.getStringEncoder().encode("Ashcroft"));        
        Assert.assertEquals("T522", this.getStringEncoder().encode("Tymczak"));        
        Assert.assertEquals("P236", this.getStringEncoder().encode("Pfister"));        
    }
