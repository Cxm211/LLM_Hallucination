// buggy code
    public String caverphone(String txt) {
        // NOTE: Version 1.0 of Caverphone is easily derivable from this code 
        // by commenting out the 2.0 lines and adding in the 1.0 lines

        if( txt == null || txt.length() == 0 ) {
            return "1111111111";
        }

        // 1. Convert to lowercase
        txt = txt.toLowerCase();

        // 2. Remove anything not A-Z
        txt = txt.replaceAll("[^a-z]", "");

        // 2.5. Remove final e
        txt = txt.replaceAll("e$", "");             // 2.0 only

        // 3. Handle various start options
        txt = txt.replaceAll("^cough", "cou2f");
        txt = txt.replaceAll("^rough", "rou2f");
        txt = txt.replaceAll("^tough", "tou2f");
        txt = txt.replaceAll("^enough", "enou2f");  // 2.0 only
        txt = txt.replaceAll("^trough", "trou2f");  // 2.0 only - note the spec says ^enough here again, c+p error I assume
        txt = txt.replaceAll("^gn", "2n");
        txt = txt.replaceAll("^mb", "m2");

        // 4. Handle replacements
        txt = txt.replaceAll("cq", "2q");
        txt = txt.replaceAll("ci", "si");
        txt = txt.replaceAll("ce", "se");
        txt = txt.replaceAll("cy", "sy");
        txt = txt.replaceAll("tch", "2ch");
        txt = txt.replaceAll("c", "k");
        txt = txt.replaceAll("q", "k");
        txt = txt.replaceAll("x", "k");
        txt = txt.replaceAll("v", "f");
        txt = txt.replaceAll("dg", "2g");
        txt = txt.replaceAll("tio", "sio");
        txt = txt.replaceAll("tia", "sia");
        txt = txt.replaceAll("d", "t");
        txt = txt.replaceAll("ph", "fh");
        txt = txt.replaceAll("b", "p");
        txt = txt.replaceAll("sh", "s2");
        txt = txt.replaceAll("z", "s");
        txt = txt.replaceAll("^[aeiou]", "A");
        txt = txt.replaceAll("[aeiou]", "3");
        txt = txt.replaceAll("j", "y");        // 2.0 only
        txt = txt.replaceAll("^y3", "Y3");     // 2.0 only
        txt = txt.replaceAll("^y", "A");       // 2.0 only
        txt = txt.replaceAll("y", "3");        // 2.0 only
        txt = txt.replaceAll("3gh3", "3kh3");
        txt = txt.replaceAll("gh", "22");
        txt = txt.replaceAll("g", "k");
        txt = txt.replaceAll("s+", "S");
        txt = txt.replaceAll("t+", "T");
        txt = txt.replaceAll("p+", "P");
        txt = txt.replaceAll("k+", "K");
        txt = txt.replaceAll("f+", "F");
        txt = txt.replaceAll("m+", "M");
        txt = txt.replaceAll("n+", "N");
        txt = txt.replaceAll("w3", "W3");
        //txt = txt.replaceAll("wy", "Wy");    // 1.0 only
        txt = txt.replaceAll("wh3", "Wh3");
        txt = txt.replaceAll("w$", "3");       // 2.0 only
        //txt = txt.replaceAll("why", "Why");  // 1.0 only
        txt = txt.replaceAll("w", "2");
        txt = txt.replaceAll("^h", "A");
        txt = txt.replaceAll("h", "2");
        txt = txt.replaceAll("r3", "R3");
        txt = txt.replaceAll("r$", "3");       // 2.0 only
        //txt = txt.replaceAll("ry", "Ry");    // 1.0 only
        txt = txt.replaceAll("r", "2");
        txt = txt.replaceAll("l3", "L3");
        txt = txt.replaceAll("l$", "3");       // 2.0 only
        //txt = txt.replaceAll("ly", "Ly");    // 1.0 only
        txt = txt.replaceAll("l", "2");
        //txt = txt.replaceAll("j", "y");      // 1.0 only
        //txt = txt.replaceAll("y3", "Y3");    // 1.0 only
        //txt = txt.replaceAll("y", "2");      // 1.0 only

        // 5. Handle removals
        txt = txt.replaceAll("2", "");
        txt = txt.replaceAll("3$", "A");       // 2.0 only
        txt = txt.replaceAll("3", "");

        // 6. put ten 1s on the end
        txt = txt + "111111" + "1111";        // 1.0 only has 6 1s

        // 7. take the first six characters as the code
        return txt.substring(0, 10);          // 1.0 truncates to 6
    }

    public String metaphone(String txt) {
        boolean hard = false ;
        if ((txt == null) || (txt.length() == 0)) {
            return "" ;
        }
        // single character is itself
        if (txt.length() == 1) {
            return txt.toUpperCase() ;
        }
      
        char[] inwd = txt.toUpperCase(java.util.Locale.ENGLISH).toCharArray() ;
      
        StringBuffer local = new StringBuffer(40); // manipulate
        StringBuffer code = new StringBuffer(10) ; //   output
        // handle initial 2 characters exceptions
        switch(inwd[0]) {
        case 'K' : 
        case 'G' : 
        case 'P' : /* looking for KN, etc*/
            if (inwd[1] == 'N') {
                local.append(inwd, 1, inwd.length - 1);
            } else {
                local.append(inwd);
            }
            break;
        case 'A': /* looking for AE */
            if (inwd[1] == 'E') {
                local.append(inwd, 1, inwd.length - 1);
            } else {
                local.append(inwd);
            }
            break;
        case 'W' : /* looking for WR or WH */
            if (inwd[1] == 'R') {   // WR -> R
                local.append(inwd, 1, inwd.length - 1); 
                break ;
            }
            if (inwd[1] == 'H') {
                local.append(inwd, 1, inwd.length - 1);
                local.setCharAt(0, 'W'); // WH -> W
            } else {
                local.append(inwd);
            }
            break;
        case 'X' : /* initial X becomes S */
            inwd[0] = 'S';
            local.append(inwd);
            break ;
        default :
            local.append(inwd);
        } // now local has working string with initials fixed

        int wdsz = local.length();
        int n = 0 ;

        while ((code.length() < this.getMaxCodeLen()) && 
        	   (n < wdsz) ) { // max code size of 4 works well
            char symb = local.charAt(n) ;
            // remove duplicate letters except C
            if ((symb != 'C') && (isPreviousChar( local, n, symb )) ) {
                n++ ;
            } else { // not dup
                switch(symb) {
                case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
                    if (n == 0) { 
                        code.append(symb);
                    }
                    break ; // only use vowel if leading char
                case 'B' :
                    if ( isPreviousChar(local, n, 'M') && 
                         isLastChar(wdsz, n) ) { // B is silent if word ends in MB
						break;
                    }
                    code.append(symb);
                    break;
                case 'C' : // lots of C special cases
                    /* discard if SCI, SCE or SCY */
                    if ( isPreviousChar(local, n, 'S') && 
                         !isLastChar(wdsz, n) && 
                         (FRONTV.indexOf(local.charAt(n + 1)) >= 0) ) { 
                        break;
                    }
                    if (regionMatch(local, n, "CIA")) { // "CIA" -> X
                        code.append('X'); 
                        break;
                    }
                    if (!isLastChar(wdsz, n) && 
                        (FRONTV.indexOf(local.charAt(n + 1)) >= 0)) {
                        code.append('S');
                        break; // CI,CE,CY -> S
                    }
                    if (isPreviousChar(local, n, 'S') &&
						isNextChar(local, n, 'H') ) { // SCH->sk
                        code.append('K') ; 
                        break ;
                    }
                    if (isNextChar(local, n, 'H')) { // detect CH
                        if ((n == 0) && 
                        	(wdsz >= 3) && 
                            isVowel(local,2) ) { // CH consonant -> K consonant
                            code.append('K');
                        } else { 
                            code.append('X'); // CHvowel -> X
                        }
                    } else { 
                        code.append('K');
                    }
                    break ;
                case 'D' :
                    if (!isLastChar(wdsz, n + 1) && 
                        isNextChar(local, n, 'G') && 
                        (FRONTV.indexOf(local.charAt(n + 2)) >= 0)) { // DGE DGI DGY -> J 
                        code.append('J'); n += 2 ;
                    } else { 
                        code.append('T');
                    }
                    break ;
                case 'G' : // GH silent at end or before consonant
                    if (isLastChar(wdsz, n + 1) && 
                        isNextChar(local, n, 'H')) {
                        break;
                    }
                    if (!isLastChar(wdsz, n + 1) &&  
                        isNextChar(local,n,'H') && 
                        !isVowel(local,n+2)) {
                        break;
                    }
                    if ((n > 0) && 
                    	( regionMatch(local, n, "GN") ||
					      regionMatch(local, n, "GNED") ) ) {
                        break; // silent G
                    }
                    if (isPreviousChar(local, n, 'G')) {
                        hard = true ;
                    } else {
                        hard = false ;
                    }
                    if (!isLastChar(wdsz, n) && 
                        (FRONTV.indexOf(local.charAt(n + 1)) >= 0) && 
                        (!hard)) {
                        code.append('J');
                    } else {
                        code.append('K');
                    }
                    break ;
                case 'H':
                    if (isLastChar(wdsz, n)) {
                        break ; // terminal H
                    }
                    if ((n > 0) && 
                        (VARSON.indexOf(local.charAt(n - 1)) >= 0)) {
                        break;
                    }
                    if (isVowel(local,n+1)) {
                        code.append('H'); // Hvowel
                    }
                    break;
                case 'F': 
                case 'J' : 
                case 'L' :
                case 'M': 
                case 'N' : 
                case 'R' :
                    code.append(symb); 
                    break;
                case 'K' :
                    if (n > 0) { // not initial
                        if (!isPreviousChar(local, n, 'C')) {
                            code.append(symb);
                        }
                    } else {
                        code.append(symb); // initial K
                    }
                    break ;
                case 'P' :
                    if (isNextChar(local,n,'H')) {
                        // PH -> F
                        code.append('F');
                    } else {
                        code.append(symb);
                    }
                    break ;
                case 'Q' :
                    code.append('K');
                    break;
                case 'S' :
                    if (regionMatch(local,n,"SH") || 
					    regionMatch(local,n,"SIO") || 
					    regionMatch(local,n,"SIA")) {
                        code.append('X');
                    } else {
                        code.append('S');
                    }
                    break;
                case 'T' :
                    if (regionMatch(local,n,"TIA") || 
						regionMatch(local,n,"TIO")) {
                        code.append('X'); 
                        break;
                    }
                    if (regionMatch(local,n,"TCH")) {
						// Silent if in "TCH"
                        break;
                    }
                    // substitute numeral 0 for TH (resembles theta after all)
                    if (regionMatch(local,n,"TH")) {
                        code.append('0');
                    } else {
                        code.append('T');
                    }
                    break ;
                case 'V' :
                    code.append('F'); break ;
                case 'W' : case 'Y' : // silent if not followed by vowel
                    if (!isLastChar(wdsz,n) && 
                    	isVowel(local,n+1)) {
                        code.append(symb);
                    }
                    break ;
                case 'X' :
                    code.append('K'); code.append('S');
                    break ;
                case 'Z' :
                    code.append('S'); break ;
                } // end switch
                n++ ;
            } // end else from symb != 'C'
            if (code.length() > this.getMaxCodeLen()) { 
            	code.setLength(this.getMaxCodeLen()); 
            }
        }
        return code.toString();
    }

    static String clean(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int len = str.length();
        char[] chars = new char[len];
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (Character.isLetter(str.charAt(i))) {
                chars[count++] = str.charAt(i);
            }
        }
        if (count == len) {
            return str.toUpperCase();
        }
        return new String(chars, 0, count).toUpperCase(java.util.Locale.ENGLISH);
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

// org.apache.commons.codec.language.CaverphoneTest::testSpecificationExamples
    public void testSpecificationExamples() {
        Caverphone caverphone = new Caverphone();
        String[][] data = {
            {"Stevenson", "STFNSN1111"},
            {"Peter",     "PTA1111111"},
            {"ready",     "RTA1111111"},
            {"social",    "SSA1111111"},
            {"able",      "APA1111111"},
            {"Tedder",    "TTA1111111"},
            {"Karleen",   "KLN1111111"},
            {"Dyun",      "TN11111111"},
        };

        for(int i=0; i<data.length; i++) {
            assertEquals( data[i][1], caverphone.caverphone(data[i][0]) );
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
    }

// org.apache.commons.codec.language.DoubleMetaphoneTest::testEmpty
    public void testEmpty() {
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(null));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(""));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone(" "));
        assertEquals(null, this.getDoubleMetaphone().doubleMetaphone("\t\n\r "));
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

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqual1
    public void testIsMetaphoneEqual1() {
        this.assertMetaphoneEqual(new String[][] { { "Case", "case" }, {
                "CASE", "Case" }, {
                "caSe", "cAsE" }, {
                "quick", "cookie" }
        });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqual2
    public void testIsMetaphoneEqual2() {
        this.assertMetaphoneEqual(new String[][] { { "Lawrence", "Lorenza" }, {
                "Gary", "Cahra" }, });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualAero
    public void testIsMetaphoneEqualAero() {
        this.assertIsMetaphoneEqual("Aero", new String[] { "Eure" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualWhite
    public void testIsMetaphoneEqualWhite() {
        this.assertIsMetaphoneEqual(
            "White",
            new String[] { "Wade", "Wait", "Waite", "Wat", "Whit", "Wiatt", "Wit", "Wittie", "Witty", "Wood", "Woodie", "Woody" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualAlbert
    public void testIsMetaphoneEqualAlbert() {
        this.assertIsMetaphoneEqual("Albert", new String[] { "Ailbert", "Alberik", "Albert", "Alberto", "Albrecht" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualGary
    public void testIsMetaphoneEqualGary() {
        this.assertIsMetaphoneEqual(
            "Gary",
            new String[] {
                "Cahra",
                "Cara",
                "Carey",
                "Cari",
                "Caria",
                "Carie",
                "Caro",
                "Carree",
                "Carri",
                "Carrie",
                "Carry",
                "Cary",
                "Cora",
                "Corey",
                "Cori",
                "Corie",
                "Correy",
                "Corri",
                "Corrie",
                "Corry",
                "Cory",
                "Gray",
                "Kara",
                "Kare",
                "Karee",
                "Kari",
                "Karia",
                "Karie",
                "Karrah",
                "Karrie",
                "Karry",
                "Kary",
                "Keri",
                "Kerri",
                "Kerrie",
                "Kerry",
                "Kira",
                "Kiri",
                "Kora",
                "Kore",
                "Kori",
                "Korie",
                "Korrie",
                "Korry" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualJohn
    public void testIsMetaphoneEqualJohn() {
        this.assertIsMetaphoneEqual(
            "John",
            new String[] {
                "Gena",
                "Gene",
                "Genia",
                "Genna",
                "Genni",
                "Gennie",
                "Genny",
                "Giana",
                "Gianna",
                "Gina",
                "Ginni",
                "Ginnie",
                "Ginny",
                "Jaine",
                "Jan",
                "Jana",
                "Jane",
                "Janey",
                "Jania",
                "Janie",
                "Janna",
                "Jany",
                "Jayne",
                "Jean",
                "Jeana",
                "Jeane",
                "Jeanie",
                "Jeanna",
                "Jeanne",
                "Jeannie",
                "Jen",
                "Jena",
                "Jeni",
                "Jenn",
                "Jenna",
                "Jennee",
                "Jenni",
                "Jennie",
                "Jenny",
                "Jinny",
                "Jo Ann",
                "Jo-Ann",
                "Jo-Anne",
                "Joan",
                "Joana",
                "Joane",
                "Joanie",
                "Joann",
                "Joanna",
                "Joanne",
                "Joeann",
                "Johna",
                "Johnna",
                "Joni",
                "Jonie",
                "Juana",
                "June",
                "Junia",
                "Junie" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualKnight
    public void testIsMetaphoneEqualKnight() {
        this.assertIsMetaphoneEqual(
            "Knight",
            new String[] {
                "Hynda",
                "Nada",
                "Nadia",
                "Nady",
                "Nat",
                "Nata",
                "Natty",
                "Neda",
                "Nedda",
                "Nedi",
                "Netta",
                "Netti",
                "Nettie",
                "Netty",
                "Nita",
                "Nydia" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualMary
    public void testIsMetaphoneEqualMary() {
        this.assertIsMetaphoneEqual(
            "Mary",
            new String[] {
                "Mair",
                "Maire",
                "Mara",
                "Mareah",
                "Mari",
                "Maria",
                "Marie",
                "Mary",
                "Maura",
                "Maure",
                "Meara",
                "Merrie",
                "Merry",
                "Mira",
                "Moira",
                "Mora",
                "Moria",
                "Moyra",
                "Muire",
                "Myra",
                "Myrah" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualParis
    public void testIsMetaphoneEqualParis() {
        this.assertIsMetaphoneEqual("Paris", new String[] { "Pearcy", "Perris", "Piercy", "Pierz", "Pryse" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualPeter
    public void testIsMetaphoneEqualPeter() {
        this.assertIsMetaphoneEqual(
            "Peter",
            new String[] { "Peadar", "Peder", "Pedro", "Peter", "Petr", "Peyter", "Pieter", "Pietro", "Piotr" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualRay
    public void testIsMetaphoneEqualRay() {
        this.assertIsMetaphoneEqual("Ray", new String[] { "Ray", "Rey", "Roi", "Roy", "Ruy" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualSusan
    public void testIsMetaphoneEqualSusan() {
        this.assertIsMetaphoneEqual(
            "Susan",
            new String[] {
                "Siusan",
                "Sosanna",
                "Susan",
                "Susana",
                "Susann",
                "Susanna",
                "Susannah",
                "Susanne",
                "Suzann",
                "Suzanna",
                "Suzanne",
                "Zuzana" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualWright
    public void testIsMetaphoneEqualWright() {
        this.assertIsMetaphoneEqual("Wright", new String[] { "Rota", "Rudd", "Ryde" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testIsMetaphoneEqualXalan
    public void testIsMetaphoneEqualXalan() {
        this.assertIsMetaphoneEqual(
            "Xalan",
            new String[] { "Celene", "Celina", "Celine", "Selena", "Selene", "Selina", "Seline", "Suellen", "Xylina" });
    }

// org.apache.commons.codec.language.MetaphoneTest::testMetaphone
    public void testMetaphone() {
		assertEquals("HL", this.getMetaphone().metaphone("howl"));
        assertEquals("TSTN", this.getMetaphone().metaphone("testing"));
        assertEquals("0", this.getMetaphone().metaphone("The"));
        assertEquals("KK", this.getMetaphone().metaphone("quick"));
        assertEquals("BRN", this.getMetaphone().metaphone("brown"));
        assertEquals("FKS", this.getMetaphone().metaphone("fox"));
        assertEquals("JMPT", this.getMetaphone().metaphone("jumped"));
        assertEquals("OFR", this.getMetaphone().metaphone("over"));
        assertEquals("0", this.getMetaphone().metaphone("the"));
        assertEquals("LS", this.getMetaphone().metaphone("lazy"));
        assertEquals("TKS", this.getMetaphone().metaphone("dogs"));
    }

// org.apache.commons.codec.language.MetaphoneTest::testWordEndingInMB
	public void testWordEndingInMB() {
		assertEquals( "KM", this.getMetaphone().metaphone("COMB") );
		assertEquals( "TM", this.getMetaphone().metaphone("TOMB") );
		assertEquals( "WM", this.getMetaphone().metaphone("WOMB") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testDiscardOfSCEOrSCIOrSCY
	public void testDiscardOfSCEOrSCIOrSCY() {
		assertEquals( "SNS", this.getMetaphone().metaphone("SCIENCE") );
		assertEquals( "SN", this.getMetaphone().metaphone("SCENE") );
		assertEquals( "S", this.getMetaphone().metaphone("SCY") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testWhy
    public void testWhy() {
        
        assertEquals("", this.getMetaphone().metaphone("WHY"));
    }

// org.apache.commons.codec.language.MetaphoneTest::testWordsWithCIA
    public void testWordsWithCIA() {
        assertEquals( "XP", this.getMetaphone().metaphone("CIAPO") );
    }

// org.apache.commons.codec.language.MetaphoneTest::testTranslateOfSCHAndCH
	public void testTranslateOfSCHAndCH() {
		assertEquals( "SKTL", this.getMetaphone().metaphone("SCHEDULE") );
		assertEquals( "SKMT", this.getMetaphone().metaphone("SCHEMATIC") );

		assertEquals( "KRKT", this.getMetaphone().metaphone("CHARACTER") );
		assertEquals( "TX", this.getMetaphone().metaphone("TEACH") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testTranslateToJOfDGEOrDGIOrDGY
	public void testTranslateToJOfDGEOrDGIOrDGY() {
		assertEquals( "TJ", this.getMetaphone().metaphone("DODGY") );
		assertEquals( "TJ", this.getMetaphone().metaphone("DODGE") );
		assertEquals( "AJMT", this.getMetaphone().metaphone("ADGIEMTI") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testDiscardOfSilentHAfterG
	public void testDiscardOfSilentHAfterG() {
		assertEquals( "KNT", this.getMetaphone().metaphone("GHENT") );
		assertEquals( "B", this.getMetaphone().metaphone("BAUGH") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testDiscardOfSilentGN
	public void testDiscardOfSilentGN() {
		assertEquals( "N", this.getMetaphone().metaphone("GNU") );
		assertEquals( "SNT", this.getMetaphone().metaphone("SIGNED") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testPHTOF
	public void testPHTOF() {
		assertEquals( "FX", this.getMetaphone().metaphone("PHISH") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testSHAndSIOAndSIAToX
	public void testSHAndSIOAndSIAToX() {
		assertEquals( "XT", this.getMetaphone().metaphone("SHOT") );
		assertEquals( "OTXN", this.getMetaphone().metaphone("ODSIAN") );
		assertEquals( "PLXN", this.getMetaphone().metaphone("PULSION") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testTIOAndTIAToX
	public void testTIOAndTIAToX() {
		assertEquals( "OX", this.getMetaphone().metaphone("OTIA") );
		assertEquals( "PRXN", this.getMetaphone().metaphone("PORTION") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testTCH
	public void testTCH() {
		assertEquals( "RX", this.getMetaphone().metaphone("RETCH") );
		assertEquals( "WX", this.getMetaphone().metaphone("WATCH") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testExceedLength
	public void testExceedLength() {
		
		assertEquals( "AKSK", this.getMetaphone().metaphone("AXEAXE") );
	}

// org.apache.commons.codec.language.MetaphoneTest::testSetMaxLengthWithTruncation
	public void testSetMaxLengthWithTruncation() {
		
		this.getMetaphone().setMaxCodeLen( 6 );
		assertEquals( "AKSKSK", this.getMetaphone().metaphone("AXEAXEAXE") );
	}

// org.apache.commons.codec.language.RefinedSoundexTest::testDifference
    public void testDifference() throws EncoderException {
        
        assertEquals(0, this.getEncoder().difference(null, null));
        assertEquals(0, this.getEncoder().difference("", ""));
        assertEquals(0, this.getEncoder().difference(" ", " "));
        
        assertEquals(6, this.getEncoder().difference("Smith", "Smythe"));
        assertEquals(3, this.getEncoder().difference("Ann", "Andrew"));
        assertEquals(1, this.getEncoder().difference("Margaret", "Andrew"));
        assertEquals(1, this.getEncoder().difference("Janet", "Margaret"));
        
		
        assertEquals(5, this.getEncoder().difference("Green", "Greene"));
        assertEquals(1, this.getEncoder().difference("Blotchet-Halls", "Greene"));
        
		
        assertEquals(6, this.getEncoder().difference("Smith", "Smythe"));
        assertEquals(8, this.getEncoder().difference("Smithers", "Smythers"));
        assertEquals(5, this.getEncoder().difference("Anothers", "Brothers"));
    }

// org.apache.commons.codec.language.RefinedSoundexTest::testEncode
    public void testEncode() {
        assertEquals("T6036084", this.getEncoder().encode("testing"));
        assertEquals("T6036084", this.getEncoder().encode("TESTING"));
        assertEquals("T60", this.getEncoder().encode("The"));
        assertEquals("Q503", this.getEncoder().encode("quick"));
        assertEquals("B1908", this.getEncoder().encode("brown"));
        assertEquals("F205", this.getEncoder().encode("fox"));
        assertEquals("J408106", this.getEncoder().encode("jumped"));
        assertEquals("O0209", this.getEncoder().encode("over"));
        assertEquals("T60", this.getEncoder().encode("the"));
        assertEquals("L7050", this.getEncoder().encode("lazy"));
        assertEquals("D6043", this.getEncoder().encode("dogs"));

        
        assertEquals("D6043", RefinedSoundex.US_ENGLISH.encode("dogs"));
    }

// org.apache.commons.codec.language.RefinedSoundexTest::testGetMappingCodeNonLetter
	public void testGetMappingCodeNonLetter() {
		char code = this.getEncoder().getMappingCode('#');
		assertEquals("Code does not equals zero", 0, code);
	}

// org.apache.commons.codec.language.SoundexTest::testB650
    public void testB650() {
        this.encodeAll(
            new String[] {
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
                "BYRUM" },
            "B650");
    }

// org.apache.commons.codec.language.SoundexTest::testDifference
    public void testDifference() throws EncoderException {
        
        assertEquals(0, this.getEncoder().difference(null, null));
        assertEquals(0, this.getEncoder().difference("", ""));
        assertEquals(0, this.getEncoder().difference(" ", " "));
        
        assertEquals(4, this.getEncoder().difference("Smith", "Smythe"));
        assertEquals(2, this.getEncoder().difference("Ann", "Andrew"));
        assertEquals(1, this.getEncoder().difference("Margaret", "Andrew"));
        assertEquals(0, this.getEncoder().difference("Janet", "Margaret"));
        
        assertEquals(4, this.getEncoder().difference("Green", "Greene"));
        assertEquals(0, this.getEncoder().difference("Blotchet-Halls", "Greene"));
        
        assertEquals(4, this.getEncoder().difference("Smith", "Smythe"));
        assertEquals(4, this.getEncoder().difference("Smithers", "Smythers"));
        assertEquals(2, this.getEncoder().difference("Anothers", "Brothers"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBasic
    public void testEncodeBasic() {
        assertEquals("T235", this.getEncoder().encode("testing"));
        assertEquals("T000", this.getEncoder().encode("The"));
        assertEquals("Q200", this.getEncoder().encode("quick"));
        assertEquals("B650", this.getEncoder().encode("brown"));
        assertEquals("F200", this.getEncoder().encode("fox"));
        assertEquals("J513", this.getEncoder().encode("jumped"));
        assertEquals("O160", this.getEncoder().encode("over"));
        assertEquals("T000", this.getEncoder().encode("the"));
        assertEquals("L200", this.getEncoder().encode("lazy"));
        assertEquals("D200", this.getEncoder().encode("dogs"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch2
    public void testEncodeBatch2() {
        assertEquals("A462", this.getEncoder().encode("Allricht"));
        assertEquals("E166", this.getEncoder().encode("Eberhard"));
        assertEquals("E521", this.getEncoder().encode("Engebrethson"));
        assertEquals("H512", this.getEncoder().encode("Heimbach"));
        assertEquals("H524", this.getEncoder().encode("Hanselmann"));
        assertEquals("H431", this.getEncoder().encode("Hildebrand"));
        assertEquals("K152", this.getEncoder().encode("Kavanagh"));
        assertEquals("L530", this.getEncoder().encode("Lind"));
        assertEquals("L222", this.getEncoder().encode("Lukaschowsky"));
        assertEquals("M235", this.getEncoder().encode("McDonnell"));
        assertEquals("M200", this.getEncoder().encode("McGee"));
        assertEquals("O155", this.getEncoder().encode("Opnian"));
        assertEquals("O155", this.getEncoder().encode("Oppenheimer"));
        assertEquals("R355", this.getEncoder().encode("Riedemanas"));
        assertEquals("Z300", this.getEncoder().encode("Zita"));
        assertEquals("Z325", this.getEncoder().encode("Zitzmeinn"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch3
    public void testEncodeBatch3() {
        assertEquals("W252", this.getEncoder().encode("Washington"));
        assertEquals("L000", this.getEncoder().encode("Lee"));
        assertEquals("G362", this.getEncoder().encode("Gutierrez"));
        assertEquals("P236", this.getEncoder().encode("Pfister"));
        assertEquals("J250", this.getEncoder().encode("Jackson"));
        assertEquals("T522", this.getEncoder().encode("Tymczak"));
        
        
        assertEquals("V532", this.getEncoder().encode("VanDeusen"));
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeBatch4
    public void testEncodeBatch4() {
        assertEquals("H452", this.getEncoder().encode("HOLMES"));
        assertEquals("A355", this.getEncoder().encode("ADOMOMI"));
        assertEquals("V536", this.getEncoder().encode("VONDERLEHR"));
        assertEquals("B400", this.getEncoder().encode("BALL"));
        assertEquals("S000", this.getEncoder().encode("SHAW"));
        assertEquals("J250", this.getEncoder().encode("JACKSON"));
        assertEquals("S545", this.getEncoder().encode("SCANLON"));
        assertEquals("S532", this.getEncoder().encode("SAINTJOHN"));

    }

// org.apache.commons.codec.language.SoundexTest::testBadCharacters
	public void testBadCharacters() {
		assertEquals("H452", this.getEncoder().encode("HOL>MES"));

	}

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreApostrophes
    public void testEncodeIgnoreApostrophes() {
        this.encodeAll(new String[] { "OBrien", "'OBrien", "O'Brien", "OB'rien", "OBr'ien", "OBri'en", "OBrie'n", "OBrien'" }, "O165");
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreHyphens
    public void testEncodeIgnoreHyphens() {
        this.encodeAll(
            new String[] {
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
                "KINGSMITH-" },
            "K525");
    }

// org.apache.commons.codec.language.SoundexTest::testEncodeIgnoreTrimmable
    public void testEncodeIgnoreTrimmable() {
        assertEquals("W252", this.getEncoder().encode(" \t\n\r Washington \t\n\r "));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx1
    public void testHWRuleEx1() {
        
        
        
        
        assertEquals("A261", this.getEncoder().encode("Ashcraft"));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx2
    public void testHWRuleEx2() {
        assertEquals("B312", this.getEncoder().encode("BOOTHDAVIS"));
        assertEquals("B312", this.getEncoder().encode("BOOTH-DAVIS"));
    }

// org.apache.commons.codec.language.SoundexTest::testHWRuleEx3
    public void testHWRuleEx3() {
        assertEquals("S460", this.getEncoder().encode("Sgler"));
        assertEquals("S460", this.getEncoder().encode("Swhgler"));
        
        this.encodeAll(
            new String[] {
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
                "SILLER" },
            "S460");
    }

// org.apache.commons.codec.language.SoundexTest::testMaxLength
    public void testMaxLength() throws Exception {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(soundex.getMaxLength());
        assertEquals("S460", this.getEncoder().encode("Sgler"));
    }

// org.apache.commons.codec.language.SoundexTest::testMaxLengthLessThan3Fix
    public void testMaxLengthLessThan3Fix() throws Exception {
        Soundex soundex = new Soundex();
        soundex.setMaxLength(2);
        assertEquals("S460", soundex.encode("SCHELLER"));
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer1
    public void testMsSqlServer1() {
        assertEquals("S530", this.getEncoder().encode("Smith"));
        assertEquals("S530", this.getEncoder().encode("Smythe"));
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer2
    public void testMsSqlServer2() {
        this.encodeAll(new String[]{"Erickson", "Erickson", "Erikson", "Ericson", "Ericksen", "Ericsen"}, "E625");
    }

// org.apache.commons.codec.language.SoundexTest::testMsSqlServer3
    public void testMsSqlServer3() {
        assertEquals("A500", this.getEncoder().encode("Ann"));
        assertEquals("A536", this.getEncoder().encode("Andrew"));
        assertEquals("J530", this.getEncoder().encode("Janet"));
        assertEquals("M626", this.getEncoder().encode("Margaret"));
        assertEquals("S315", this.getEncoder().encode("Steven"));
        assertEquals("M240", this.getEncoder().encode("Michael"));
        assertEquals("R163", this.getEncoder().encode("Robert"));
        assertEquals("L600", this.getEncoder().encode("Laura"));
        assertEquals("A500", this.getEncoder().encode("Anne"));
    }

// org.apache.commons.codec.language.SoundexTest::testUsMappingOWithDiaeresis
    public void testUsMappingOWithDiaeresis() {
        assertEquals("O000", this.getEncoder().encode("o"));
        if ( Character.isLetter('ö') ) {
            try {
                assertEquals("Ö000", this.getEncoder().encode("ö"));
                fail("Expected IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
                
            }
        } else {
            assertEquals("", this.getEncoder().encode("ö"));
        }
    }

// org.apache.commons.codec.language.SoundexTest::testUsMappingEWithAcute
    public void testUsMappingEWithAcute() {
        assertEquals("E000", this.getEncoder().encode("e"));
        if ( Character.isLetter('é') ) {
            try {
                assertEquals("É000", this.getEncoder().encode("é"));
                fail("Expected IllegalArgumentException not thrown");
            } catch (IllegalArgumentException e) {
                
            }
        } else {
            assertEquals("", this.getEncoder().encode("é"));
        }
    }

// org.apache.commons.codec.language.SoundexTest::testUsEnglishStatic
    public void testUsEnglishStatic() {
        assertEquals(Soundex.US_ENGLISH.soundex("Williams"), "W452");
    }

// org.apache.commons.codec.language.SoundexTest::testNewInstance
    public void testNewInstance() {
        assertEquals(new Soundex().soundex("Williams"), "W452");
    }

// org.apache.commons.codec.StringEncoderAbstractTest::testEncodeEmpty
    public void testEncodeEmpty() throws Exception {
        Encoder encoder = makeEncoder();
        encoder.encode("");
        encoder.encode(" ");
        encoder.encode("\t");
    }

// org.apache.commons.codec.StringEncoderAbstractTest::testEncodeNull
    public void testEncodeNull() throws Exception {
        StringEncoder encoder = makeEncoder();
        
        try {
            encoder.encode(null);
        } catch( EncoderException ee ) {
            
        }
    }

// org.apache.commons.codec.StringEncoderAbstractTest::testEncodeWithInvalidObject
    public void testEncodeWithInvalidObject() throws Exception {

        boolean exceptionThrown = false;
        try {
            StringEncoder encoder = makeEncoder();
            encoder.encode( new Float( 3.4 ) );
        } catch( Exception e ) {
            exceptionThrown = true;
        }

        assertTrue( "An exception was not thrown when we tried to encode " +
                    "a Float object", exceptionThrown );
    }

// org.apache.commons.codec.StringEncoderAbstractTest::testLocaleIndependence
    public void testLocaleIndependence() throws Exception {
        StringEncoder encoder = makeEncoder();

        String[] data = { "I", "i", };

        Locale orig = Locale.getDefault();
        Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };

        try {
            for (int i = 0; i < data.length; i++) {
                String ref = null;
                for (int j = 0; j < locales.length; j++) {
                    Locale.setDefault(locales[j]);
                    if (j <= 0) {
                        ref = encoder.encode(data[i]);
                    } else {
                        String cur = null;
                        try {
                            cur = encoder.encode(data[i]);
                        } catch (Exception e) {
                            fail(Locale.getDefault().toString() + ": " + e.getMessage());
                        }
                        assertEquals(Locale.getDefault().toString() + ": ", ref, cur);
                    }
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }
