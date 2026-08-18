// org/apache/commons/codec/language/SoundexTest.java
@Test
public void testHWRuleEx3() {
    Assert.assertEquals("T230", this.getStringEncoder().encode("Tahgood"));
    Assert.assertEquals("T230", this.getStringEncoder().encode("Tawhgood"));
    Assert.assertEquals("D230", this.getStringEncoder().encode("Dahgood"));
}