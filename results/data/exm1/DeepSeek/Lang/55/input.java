// buggy code
    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
            stopTime = System.currentTimeMillis();
        this.runningState = STATE_STOPPED;
    }

// relevant test
// org.apache.commons.lang.time.StopWatchTest::testStopWatchSimple
    public void testStopWatchSimple(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long time = watch.getTime();
        assertEquals(time, watch.getTime());
        
        assertTrue(time >= 500);
        assertTrue(time < 700);
        
        watch.reset();
        assertEquals(0, watch.getTime());
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSimpleGet
    public void testStopWatchSimpleGet(){
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("0:00:00.000", watch.toString());
        
        watch.start();
            try {Thread.sleep(500);} catch (InterruptedException ex) {}
        assertTrue(watch.getTime() < 2000);
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSplit
    public void testStopWatchSplit(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.split();
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.unsplit();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();

        assertEquals("Formatted split string not the correct length", 
                     splitStr.length(), 11);
        assertTrue(splitTime >= 500);
        assertTrue(splitTime < 700);
        assertTrue(totalTime >= 1500);
        assertTrue(totalTime < 1900);
    }

// org.apache.commons.lang.time.StopWatchTest::testStopWatchSuspend
    public void testStopWatchSuspend(){
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.resume();
            try {Thread.sleep(550);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        
        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

// org.apache.commons.lang.time.StopWatchTest::testLang315
    public void testLang315() {
        StopWatch watch = new StopWatch();
        watch.start();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.suspend();
        long suspendTime = watch.getTime();
            try {Thread.sleep(200);} catch (InterruptedException ex) {}
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue( suspendTime == totalTime );
    }

// org.apache.commons.lang.time.StopWatchTest::testBadStates
    public void testBadStates() {
        StopWatch watch = new StopWatch();
        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.stop();
            fail("Calling stop on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.suspend();
            fail("Calling suspend on an unstarted StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.split();
            fail("Calling split on a non-running StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.start();

        try {
            watch.start();
            fail("Calling start on a started StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.unsplit();
            fail("Calling unsplit on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.getSplitTime();
            fail("Calling getSplitTime on an unsplit StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        try {
            watch.resume();
            fail("Calling resume on an unsuspended StopWatch should throw an exception. ");
        } catch(IllegalStateException ise) {
            
        }

        watch.stop();

        try {
            watch.start();
            fail("Calling start on a stopped StopWatch should throw an exception as it needs to be reset. ");
        } catch(IllegalStateException ise) {
            
        }

    }
