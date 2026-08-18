// org/apache/commons/codec/language/SoundexTest.java
@Test
    public void testHWRuleBug() {
        Soundex soundex = this.getStringEncoder();
        // Cases where preHWChar is H or W but previous non-H/W mapping differs
        Assert.assertEquals("H100", soundex.encode("HHB"));
        Assert.assertEquals("H100", soundex.encode("HWB"));
        Assert.assertEquals("W100", soundex.encode("WHB"));
        Assert.assertEquals("A100", soundex.encode("AHHB"));
        Assert.assertEquals("A100", soundex.encode("AWHB"));
        Assert.assertEquals("H100", soundex.encode("HHHB"));
    }
