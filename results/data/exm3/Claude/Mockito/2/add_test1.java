// org/mockito/internal/util/TimerTest.java
@Test
public void should_accept_positive_duration() {
    Timer timer = new Timer(100);
    Assert.assertNotNull(timer);
    timer.start();
}