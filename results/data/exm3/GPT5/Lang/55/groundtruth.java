// ===== FIXED org.apache.commons.lang.time.StopWatch :: stop() [lines 114-122] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-55-fixed/src/java/org/apache/commons/lang/time/StopWatch.java =====
    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if(this.runningState == STATE_RUNNING) {
            stopTime = System.currentTimeMillis();
        }
        this.runningState = STATE_STOPPED;
    }
