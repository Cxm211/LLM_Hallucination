// org/apache/commons/math3/ode/nonstiff/DormandPrince853IntegratorTest.java
@Test
  public void testEventStopWithMultipleEvents() {

      FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              yDot[0] = 1.0;
          }

      };

      final List<Double> event1Times = new ArrayList<Double>();
      final List<Double> event2Times = new ArrayList<Double>();

      EventHandler event1 = new EventHandler() {
          public void init(double t0, double[] y0, double t) {}
          public double g(double t, double[] y) {
              return t - 5.0;
          }
          public Action eventOccurred(double t, double[] y, boolean increasing) {
              event1Times.add(t);
              return Action.STOP;
          }
          public void resetState(double t, double[] y) {}
      };

      EventHandler event2 = new EventHandler() {
          public void init(double t0, double[] y0, double t) {}
          public double g(double t, double[] y) {
              return t - 7.0;
          }
          public Action eventOccurred(double t, double[] y, boolean increasing) {
              event2Times.add(t);
              return Action.CONTINUE;
          }
          public void resetState(double t, double[] y) {}
      };

      FirstOrderIntegrator integ = new DormandPrince853Integrator(0.001, 1.0, 1.0e-12, 0.0);
      integ.addEventHandler(event1, 0.01, 1.0e-7, 100);
      integ.addEventHandler(event2, 0.01, 1.0e-7, 100);

      double[] y0 = new double[] { 0.0 };
      double[] y = new double[1];
      double finalT = integ.integrate(ode, 0.0, y0, 10.0, y);

      Assert.assertEquals(5.0, finalT, 1.0e-7);
      Assert.assertEquals(1, event1Times.size());
      Assert.assertEquals(5.0, event1Times.get(0), 1.0e-7);
      Assert.assertEquals(0, event2Times.size());
  }