// org/apache/commons/lang/time/StopWatchTest.java
public void testStopFromSuspendedImmediate() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        long suspendTime = watch.getTime();
        try {Thread.sleep(50);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue( suspendTime == totalTime );
    }
