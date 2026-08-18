// org/apache/commons/math/ode/events/EventStateTest.java
@Test
    public void testEventAfterStepBackward()
        throws EventException, ConvergenceException, DerivativeException {
        final double convergence = 0.1;
        final double maxCheckInterval = 10.0;
        final double t0 = 0.0;
        final double t1 = -10.0;
        final double eventTime = -10.05;
        EventHandler handler = new EventHandler() {
            public void resetState(double t, double[] y) {
            }
            public double g(double t, double[] y) {
                return t - eventTime;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }
        };
        EventState es = new EventState(handler, maxCheckInterval, convergence, 10);
        es.reinitializeBegin(t0, new double[0]);
        AbstractStepInterpolator interpolator = new DummyStepInterpolator(new double[0], false);
        interpolator.storeTime(t0);
        interpolator.shift();
        interpolator.storeTime(t1);
        Assert.assertFalse(es.evaluateStep(interpolator));
    }
