// org/mockito/internal/util/TimerTest.java
@Test
public void should_accept_zero_duration() {
    Timer timer = new Timer(0);
    Assert.assertNotNull(timer);
}