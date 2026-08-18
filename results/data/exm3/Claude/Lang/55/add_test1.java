// org/apache/commons/lang/time/StopWatchTest.java
public void testMultipleSuspendResumeBeforeStop() {
    StopWatch watch = new StopWatch();
    watch.start();
    try {Thread.sleep(50);} catch (InterruptedException ex) {}
    watch.suspend();
    try {Thread.sleep(100);} catch (InterruptedException ex) {}
    watch.resume();
    try {Thread.sleep(50);} catch (InterruptedException ex) {}
    watch.suspend();
    long suspendTime = watch.getTime();
    try {Thread.sleep(100);} catch (InterruptedException ex) {}
    watch.stop();
    long totalTime = watch.getTime();
    assertTrue(suspendTime == totalTime);
    assertTrue(totalTime >= 100 && totalTime < 200);
}