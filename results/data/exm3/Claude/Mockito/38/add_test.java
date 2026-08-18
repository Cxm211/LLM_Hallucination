// org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java
@Test
public void shouldHandleNullArgumentWithNonNullMatcher() {
    //when
    Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes((List) Arrays.asList(new Equals("test")), new Object[] {null});
    
    //then
    assertEquals(1, suspicious.length);
    assertEquals(0, suspicious[0].intValue());
}