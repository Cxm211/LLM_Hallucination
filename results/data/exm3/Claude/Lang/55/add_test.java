// org/apache/commons/lang/time/StopWatchTest.java
public void testStopFromRunningState() {
    StopWatch watch = new StopWatch();
    watch.start();
    try {Thread.sleep(100);} catch (InterruptedException ex) {}
    watch.stop();
    long time1 = watch.getTime();
    try {Thread.sleep(100);} catch (InterruptedException ex) {}
    long time2 = watch.getTime();
    assertTrue(time1 == time2);
    assertTrue(time1 >= 100);
}