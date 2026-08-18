// buggy code
  public void integrate(final ExpandableStatefulODE equations, final double t)
      throws MathIllegalStateException, MathIllegalArgumentException {

    sanityChecks(equations, t);
    setEquations(equations);
    final boolean forward = t > equations.getTime();

    // create some internal working arrays
    final double[] y0  = equations.getCompleteState();
    final double[] y = y0.clone();
    final int stages = c.length + 1;
    final double[][] yDotK = new double[stages][y.length];
    final double[] yTmp    = y0.clone();
    final double[] yDotTmp = new double[y.length];

    // set up an interpolator sharing the integrator arrays
    final RungeKuttaStepInterpolator interpolator = (RungeKuttaStepInterpolator) prototype.copy();
    interpolator.reinitialize(this, yTmp, yDotK, forward,
                              equations.getPrimaryMapper(), equations.getSecondaryMappers());
    interpolator.storeTime(equations.getTime());

    // set up integration control objects
    stepStart         = equations.getTime();
    double  hNew      = 0;
    boolean firstTime = true;
    initIntegration(equations.getTime(), y0, t);

    // main integration loop
    isLastStep = false;
    do {

      interpolator.shift();

      // iterate over step size, ensuring local normalized error is smaller than 1
      double error = 10;
      while (error >= 1.0) {

        if (firstTime || !fsal) {
          // first stage
          computeDerivatives(stepStart, y, yDotK[0]);
        }

        if (firstTime) {
          final double[] scale = new double[mainSetDimension];
          if (vecAbsoluteTolerance == null) {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = scalAbsoluteTolerance + scalRelativeTolerance * FastMath.abs(y[i]);
              }
          } else {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * FastMath.abs(y[i]);
              }
          }
          hNew = initializeStep(forward, getOrder(), scale,
                                stepStart, y, yDotK[0], yTmp, yDotK[1]);
          firstTime = false;
        }

        stepSize = hNew;

        // next stages
        for (int k = 1; k < stages; ++k) {

          for (int j = 0; j < y0.length; ++j) {
            double sum = a[k-1][0] * yDotK[0][j];
            for (int l = 1; l < k; ++l) {
              sum += a[k-1][l] * yDotK[l][j];
            }
            yTmp[j] = y[j] + stepSize * sum;
          }

          computeDerivatives(stepStart + c[k-1] * stepSize, yTmp, yDotK[k]);

        }

        // estimate the state at the end of the step
        for (int j = 0; j < y0.length; ++j) {
          double sum    = b[0] * yDotK[0][j];
          for (int l = 1; l < stages; ++l) {
            sum    += b[l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
        }

        // estimate the error at the end of the step
        error = estimateError(yDotK, y, yTmp, stepSize);
        if (error >= 1.0) {
          // reject the step and attempt to reduce error by stepsize control
          final double factor =
              FastMath.min(maxGrowth,
                           FastMath.max(minReduction, safety * FastMath.pow(error, exp)));
          hNew = filterStep(stepSize * factor, forward, false);
        }

      }

      // local error is small enough: accept the step, trigger events and step handlers
      interpolator.storeTime(stepStart + stepSize);
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      System.arraycopy(yDotK[stages - 1], 0, yDotTmp, 0, y0.length);
      stepStart = acceptStep(interpolator, y, yDotTmp, t);
      System.arraycopy(y, 0, yTmp, 0, y.length);

      if (!isLastStep) {

          // prepare next step
          interpolator.storeTime(stepStart);

          if (fsal) {
              // save the last evaluation for the next step
              System.arraycopy(yDotTmp, 0, yDotK[0], 0, y0.length);
          }

          // stepsize control for next step
          final double factor =
              FastMath.min(maxGrowth, FastMath.max(minReduction, safety * FastMath.pow(error, exp)));
          final double  scaledH    = stepSize * factor;
          final double  nextT      = stepStart + scaledH;
          final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
          hNew = filterStep(scaledH, forward, nextIsLast);

          final double  filteredNextT      = stepStart + hNew;
          final boolean filteredNextIsLast = forward ? (filteredNextT >= t) : (filteredNextT <= t);
          if (filteredNextIsLast) {
              hNew = t - stepStart;
          }

      }

    } while (!isLastStep);

    // dispatch results
    equations.setTime(stepStart);
    equations.setCompleteState(y);

    resetInternalState();

  }

// relevant test
// org.apache.commons.math.ode.ContinuousOutputModelTest::testBoundaries
  public void testBoundaries() {
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    ContinuousOutputModel cm = (ContinuousOutputModel) integ.getStepHandlers().iterator().next();
    cm.setInterpolatedTime(2.0 * pb.getInitialTime() - pb.getFinalTime());
    cm.setInterpolatedTime(2.0 * pb.getFinalTime() - pb.getInitialTime());
    cm.setInterpolatedTime(0.5 * (pb.getFinalTime() + pb.getInitialTime()));
  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testRandomAccess
  public void testRandomAccess() {

    ContinuousOutputModel cm = new ContinuousOutputModel();
    integ.addStepHandler(cm);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    Assert.assertTrue(maxError < 1.0e-9);

  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testModelsMerging
  public void testModelsMerging() {

      
      FirstOrderDifferentialEquations problem =
          new FirstOrderDifferentialEquations() {
              public void computeDerivatives(double t, double[] y, double[] dot) {
                  dot[0] = -y[1];
                  dot[1] =  y[0];
              }
              public int getDimension() {
                  return 2;
              }
          };

      
      ContinuousOutputModel cm1 = new ContinuousOutputModel();
      FirstOrderIntegrator integ1 =
          new DormandPrince853Integrator(0, 1.0, 1.0e-8, 1.0e-8);
      integ1.addStepHandler(cm1);
      integ1.integrate(problem, FastMath.PI, new double[] { -1.0, 0.0 },
                       0, new double[2]);

      
      ContinuousOutputModel cm2 = new ContinuousOutputModel();
      FirstOrderIntegrator integ2 =
          new DormandPrince853Integrator(0, 0.1, 1.0e-12, 1.0e-12);
      integ2.addStepHandler(cm2);
      integ2.integrate(problem, 2.0 * FastMath.PI, new double[] { 1.0, 0.0 },
                       FastMath.PI, new double[2]);

      
      ContinuousOutputModel cm = new ContinuousOutputModel();
      cm.append(cm2);
      cm.append(new ContinuousOutputModel());
      cm.append(cm1);

      
      Assert.assertEquals(2.0 * FastMath.PI, cm.getInitialTime(), 1.0e-12);
      Assert.assertEquals(0, cm.getFinalTime(), 1.0e-12);
      Assert.assertEquals(cm.getFinalTime(), cm.getInterpolatedTime(), 1.0e-12);
      for (double t = 0; t < 2.0 * FastMath.PI; t += 0.1) {
          cm.setInterpolatedTime(t);
          double[] y = cm.getInterpolatedState();
          Assert.assertEquals(FastMath.cos(t), y[0], 1.0e-7);
          Assert.assertEquals(FastMath.sin(t), y[1], 1.0e-7);
      }

  }

// org.apache.commons.math.ode.ContinuousOutputModelTest::testErrorConditions
  public void testErrorConditions() {

      ContinuousOutputModel cm = new ContinuousOutputModel();
      cm.handleStep(buildInterpolator(0, new double[] { 0.0, 1.0, -2.0 }, 1), true);

      
      Assert.assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0 }, 2.0));

      
      Assert.assertTrue(checkAppendError(cm, 10.0, new double[] { 0.0, 1.0, -2.0 }, 20.0));

      
      Assert.assertTrue(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 0.0));

      
      Assert.assertFalse(checkAppendError(cm, 1.0, new double[] { 0.0, 1.0, -2.0 }, 2.0));

  }

// org.apache.commons.math.ode.JacobianMatricesTest::testLowAccuracyExternalDifferentiation
    public void testLowAccuracyExternalDifferentiation() {
        
        
        
        
        
        
        
        
        
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 500);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 30);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 700);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 40);
    }

// org.apache.commons.math.ode.JacobianMatricesTest::testHighAccuracyExternalDifferentiation
    public void testHighAccuracyExternalDifferentiation() {
        FirstOrderIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double hP = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            double[] y = { 1.3, b };
            integ.integrate(brusselator, 0, y, 20.0, y);
            double[] yP = { 1.3, b + hP };
            brusselator.setParameter("b", b + hP);
            integ.integrate(brusselator, 0, yP, 20.0, yP);
            residualsP0.addValue((yP[0] - y[0]) / hP - brusselator.dYdP0());
            residualsP1.addValue((yP[1] - y[1]) / hP - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) > 0.02);
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.03);
        Assert.assertTrue(residualsP0.getStandardDeviation() > 0.003);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.004);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) > 0.04);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() > 0.007);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.008);
    }

// org.apache.commons.math.ode.JacobianMatricesTest::testInternalDifferentiation
    public void testInternalDifferentiation() {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        double hP = 1.0e-12;
        double hY = 1.0e-12;
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            ParamBrusselator brusselator = new ParamBrusselator(b);
            brusselator.setParameter(ParamBrusselator.B, b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, new double[] { hY, hY }, ParamBrusselator.B);
            jacob.setParameterizedODE(brusselator);
            jacob.setParameterStep(ParamBrusselator.B, hP);
            jacob.setInitialParameterJacobian(ParamBrusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(ParamBrusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.02);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math.ode.JacobianMatricesTest::testAnalyticalDifferentiation
    public void testAnalyticalDifferentiation() {
        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-4, 1.0e-4 }, new double[] { 1.0e-4, 1.0e-4 });
        SummaryStatistics residualsP0 = new SummaryStatistics();
        SummaryStatistics residualsP1 = new SummaryStatistics();
        for (double b = 2.88; b < 3.08; b += 0.001) {
            Brusselator brusselator = new Brusselator(b);
            double[] z = { 1.3, b };
            double[][] dZdZ0 = new double[2][2];
            double[]   dZdP  = new double[2];

            JacobianMatrices jacob = new JacobianMatrices(brusselator, Brusselator.B);
            jacob.addParameterJacobianProvider(brusselator);
            jacob.setInitialParameterJacobian(Brusselator.B, new double[] { 0.0, 1.0 });

            ExpandableStatefulODE efode = new ExpandableStatefulODE(brusselator);
            efode.setTime(0);
            efode.setPrimaryState(z);
            jacob.registerVariationalEquations(efode);

            integ.setMaxEvaluations(5000);
            integ.integrate(efode, 20.0);
            jacob.getCurrentMainSetJacobian(dZdZ0);
            jacob.getCurrentParameterJacobian(Brusselator.B, dZdP);

            residualsP0.addValue(dZdP[0] - brusselator.dYdP0());
            residualsP1.addValue(dZdP[1] - brusselator.dYdP1());
        }
        Assert.assertTrue((residualsP0.getMax() - residualsP0.getMin()) < 0.014);
        Assert.assertTrue(residualsP0.getStandardDeviation() < 0.003);
        Assert.assertTrue((residualsP1.getMax() - residualsP1.getMin()) < 0.05);
        Assert.assertTrue(residualsP1.getStandardDeviation() < 0.01);
    }

// org.apache.commons.math.ode.JacobianMatricesTest::testFinalResult
    public void testFinalResult() {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        Circle circle = new Circle(y, 1.0, 1.0, 0.1);

        JacobianMatrices jacob = new JacobianMatrices(circle, Circle.CX, Circle.CY, Circle.OMEGA);
        jacob.addParameterJacobianProvider(circle);
        jacob.setInitialMainStateJacobian(circle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(Circle.CX, circle.exactDyDcx(0));
        jacob.setInitialParameterJacobian(Circle.CY, circle.exactDyDcy(0));
        jacob.setInitialParameterJacobian(Circle.OMEGA, circle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(circle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(5000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(circle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(circle.exactDyDy0(t)[i][j], dydy0[i][j], 1.0e-9);
            }
        }
        double[] dydcx = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CX, dydcx);
        for (int i = 0; i < dydcx.length; ++i) {
            Assert.assertEquals(circle.exactDyDcx(t)[i], dydcx[i], 1.0e-7);
        }
        double[] dydcy = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CY, dydcy);
        for (int i = 0; i < dydcy.length; ++i) {
            Assert.assertEquals(circle.exactDyDcy(t)[i], dydcy[i], 1.0e-7);
        }
        double[] dydom = new double[2];
        jacob.getCurrentParameterJacobian(Circle.OMEGA, dydom);
        for (int i = 0; i < dydom.length; ++i) {
            Assert.assertEquals(circle.exactDyDom(t)[i], dydom[i], 1.0e-7);
        }
    }

// org.apache.commons.math.ode.JacobianMatricesTest::testParameterizable
    public void testParameterizable() {

        AbstractIntegrator integ =
            new DormandPrince54Integrator(1.0e-8, 100.0, new double[] { 1.0e-10, 1.0e-10 }, new double[] { 1.0e-10, 1.0e-10 });
        double[] y = new double[] { 0.0, 1.0 };
        ParameterizedCircle pcircle = new ParameterizedCircle(y, 1.0, 1.0, 0.1);

        double hP = 1.0e-12;
        double hY = 1.0e-12;

        JacobianMatrices jacob = new JacobianMatrices(pcircle, new double[] { hY, hY },
                                                      Circle.CX, Circle.OMEGA);
        jacob.addParameterJacobianProvider(pcircle);
        jacob.setParameterizedODE(pcircle);
        jacob.setParameterStep(Circle.OMEGA, hP);
        jacob.setInitialMainStateJacobian(pcircle.exactDyDy0(0));
        jacob.setInitialParameterJacobian(Circle.CX, pcircle.exactDyDcx(0));

        jacob.setInitialParameterJacobian(Circle.OMEGA, pcircle.exactDyDom(0));

        ExpandableStatefulODE efode = new ExpandableStatefulODE(pcircle);
        efode.setTime(0);
        efode.setPrimaryState(y);
        jacob.registerVariationalEquations(efode);

        integ.setMaxEvaluations(50000);

        double t = 18 * FastMath.PI;
        integ.integrate(efode, t);
        y = efode.getPrimaryState();
        for (int i = 0; i < y.length; ++i) {
            Assert.assertEquals(pcircle.exactY(t)[i], y[i], 1.0e-9);
        }

        double[][] dydy0 = new double[2][2];
        jacob.getCurrentMainSetJacobian(dydy0);
        for (int i = 0; i < dydy0.length; ++i) {
            for (int j = 0; j < dydy0[i].length; ++j) {
                Assert.assertEquals(pcircle.exactDyDy0(t)[i][j], dydy0[i][j], 5.0e-4);
            }
        }

        double[] dydp0 = new double[2];
        jacob.getCurrentParameterJacobian(Circle.CX, dydp0);
        for (int i = 0; i < dydp0.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDcx(t)[i], dydp0[i], 5.0e-4);
        }

        double[] dydp1 = new double[2];
        jacob.getCurrentParameterJacobian(Circle.OMEGA, dydp1);
        for (int i = 0; i < dydp1.length; ++i) {
            Assert.assertEquals(pcircle.exactDyDom(t)[i], dydp1[i], 1.0e-2);
        }
    }

// org.apache.commons.math.ode.events.EventStateTest::closeEvents
    public void closeEvents() {

        final double r1  = 90.0;
        final double r2  = 135.0;
        final double gap = r2 - r1;
        EventHandler closeEventsGenerator = new EventHandler() {
            public void init(double t0, double[] y0, double t) {
            }
            public void resetState(double t, double[] y) {
            }
            public double g(double t, double[] y) {
                return (t - r1) * (r2 - t);
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.CONTINUE;
            }
        };

        final double tolerance = 0.1;
        EventState es = new EventState(closeEventsGenerator, 1.5 * gap,
                                       tolerance, 100,
                                       new BrentSolver(tolerance));

        AbstractStepInterpolator interpolator =
            new DummyStepInterpolator(new double[0], new double[0], true);
        interpolator.storeTime(r1 - 2.5 * gap);
        interpolator.shift();
        interpolator.storeTime(r1 - 1.5 * gap);
        es.reinitializeBegin(interpolator);

        interpolator.shift();
        interpolator.storeTime(r1 - 0.5 * gap);
        Assert.assertFalse(es.evaluateStep(interpolator));

        interpolator.shift();
        interpolator.storeTime(0.5 * (r1 + r2));
        Assert.assertTrue(es.evaluateStep(interpolator));
        Assert.assertEquals(r1, es.getEventTime(), tolerance);
        es.stepAccepted(es.getEventTime(), new double[0]);

        interpolator.shift();
        interpolator.storeTime(r2 + 0.4 * gap);
        Assert.assertTrue(es.evaluateStep(interpolator));
        Assert.assertEquals(r2, es.getEventTime(), tolerance);

    }

// org.apache.commons.math.ode.events.EventStateTest::testIssue695
    public void testIssue695() {

        FirstOrderDifferentialEquations equation = new FirstOrderDifferentialEquations() {
            
            public int getDimension() {
                return 1;
            }
            
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.001, 1000, 1.0e-14, 1.0e-14);
        integrator.addEventHandler(new ResettingEvent(10.99), 0.1, 1.0e-9, 1000);
        integrator.addEventHandler(new ResettingEvent(11.01), 0.1, 1.0e-9, 1000);
        integrator.setInitialStepSize(3.0);

        double target = 30.0;
        double[] y = new double[1];
        double tEnd = integrator.integrate(equation, 0.0, y, target, y);
        Assert.assertEquals(target, tEnd, 1.0e-10);
        Assert.assertEquals(32.0, y[0], 1.0e-10);

    }

// org.apache.commons.math.ode.events.OverlappingEventsTest::testOverlappingEvents0
    public void testOverlappingEvents0() {
        test(0);
    }

// org.apache.commons.math.ode.events.OverlappingEventsTest::testOverlappingEvents1
    public void testOverlappingEvents1() {
        test(1);
    }

// org.apache.commons.math.ode.events.OverlappingEventsTest::test
    public void test(int eventType) {
        double e = 1e-15;
        FirstOrderIntegrator integrator = new DormandPrince853Integrator(e, 100.0, 1e-7, 1e-7);
        BaseSecantSolver rootSolver = new PegasusSolver(e, e);
        EventHandler evt1 = new Event(0, eventType);
        EventHandler evt2 = new Event(1, eventType);
        integrator.addEventHandler(evt1, 0.1, e, 999, rootSolver);
        integrator.addEventHandler(evt2, 0.1, e, 999, rootSolver);
        double t = 0.0;
        double tEnd = 10.0;
        double[] y = {0.0, 0.0};
        List<Double> events1 = new ArrayList<Double>();
        List<Double> events2 = new ArrayList<Double>();
        while (t < tEnd) {
            t = integrator.integrate(this, t, y, tEnd, y);
            

            if (y[0] >= 1.0) {
                y[0] = 0.0;
                events1.add(t);
                
            }
            if (y[1] >= 1.0) {
                y[1] = 0.0;
                events2.add(t);
                
            }
        }
        Assert.assertEquals(EVENT_TIMES1.length, events1.size());
        Assert.assertEquals(EVENT_TIMES2.length, events2.size());
        for(int i = 0; i < EVENT_TIMES1.length; i++) {
            Assert.assertEquals(EVENT_TIMES1[i], events1.get(i), 1e-7);
        }
        for(int i = 0; i < EVENT_TIMES2.length; i++) {
            Assert.assertEquals(EVENT_TIMES2[i], events2.get(i), 1e-7);
        }
        
    }

// org.apache.commons.math.ode.events.ReappearingEventTest::testDormandPrince
    public void testDormandPrince() {
        double tEnd = test(1);
        assertEquals(10.0, tEnd, 1e-7);
    }

// org.apache.commons.math.ode.events.ReappearingEventTest::testGragg
    public void testGragg() {
        double tEnd = test(2);
        assertEquals(10.0, tEnd, 1e-7);
    }

// org.apache.commons.math.ode.events.ReappearingEventTest::test
    public double test(int integratorType) {
        double e = 1e-15;
        FirstOrderIntegrator integrator;
        integrator = (integratorType == 1)
                     ? new DormandPrince853Integrator(e, 100.0, 1e-7, 1e-7)
                     : new GraggBulirschStoerIntegrator(e, 100.0, 1e-7, 1e-7);
        PegasusSolver rootSolver = new PegasusSolver(e, e);
        integrator.addEventHandler(new Event(), 0.1, e, 1000, rootSolver);
        double t0 = 6.0;
        double tEnd = 10.0;
        double[] y = {2.0, 2.0, 2.0, 4.0, 2.0, 7.0, 15.0};
        return integrator.integrate(new Ode(), t0, y, tEnd, y);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::dimensionCheck
    public void dimensionCheck() {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsBashforthIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testMinStep
    public void testMinStep() {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                    vecAbsoluteTolerance,
                                                                    vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -5; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > (50.0 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (300.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::backward
    public void backward() {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsBashforthIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.5e-8);
        Assert.assertTrue(handler.getMaximalValueError() < 1.5e-8);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Bashforth", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsBashforthIntegratorTest::polynomial
    public void polynomial() {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsBashforthIntegrator integ =
                new AdamsBashforthIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 1.0e-03);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 4.0e-12);
            }
        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::dimensionCheck
    public void dimensionCheck() {
        TestProblem1 pb = new TestProblem1();
        FirstOrderIntegrator integ =
            new AdamsMoultonIntegrator(2, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.integrate(pb,
                        0.0, new double[pb.getDimension()+10],
                        1.0, new double[pb.getDimension()+10]);
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testMinStep
    public void testMinStep() {

          TestProblem1 pb = new TestProblem1();
          double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
          double maxStep = pb.getFinalTime() - pb.getInitialTime();
          double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
          double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

          FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
          TestProblemHandler handler = new TestProblemHandler(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(pb,
                          pb.getInitialTime(), pb.getInitialState(),
                          pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::testIncreasingTolerance
    public void testIncreasingTolerance()
        {

        int previousCalls = Integer.MAX_VALUE;
        for (int i = -12; i < -2; ++i) {
            TestProblem1 pb = new TestProblem1();
            double minStep = 0;
            double maxStep = pb.getFinalTime() - pb.getInitialTime();
            double scalAbsoluteTolerance = FastMath.pow(10.0, i);
            double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

            FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb,
                            pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);

            
            
            
            Assert.assertTrue(handler.getMaximalValueError() > ( 0.5 * scalAbsoluteTolerance));
            Assert.assertTrue(handler.getMaximalValueError() < (11.0 * scalAbsoluteTolerance));
            Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);

            int calls = pb.getCalls();
            Assert.assertEquals(integ.getEvaluations(), calls);
            Assert.assertTrue(calls <= previousCalls);
            previousCalls = calls;

        }

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::exceedMaxEvaluations
    public void exceedMaxEvaluations() {

        TestProblem1 pb  = new TestProblem1();
        double range = pb.getFinalTime() - pb.getInitialTime();

        AdamsMoultonIntegrator integ = new AdamsMoultonIntegrator(2, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.setMaxEvaluations(650);
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::backward
    public void backward() {

        TestProblem5 pb = new TestProblem5();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        FirstOrderIntegrator integ = new AdamsMoultonIntegrator(4, 0, range, 1.0e-12, 1.0e-12);
        TestProblemHandler handler = new TestProblemHandler(pb, integ);
        integ.addStepHandler(handler);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        Assert.assertTrue(handler.getLastError() < 1.0e-9);
        Assert.assertTrue(handler.getMaximalValueError() < 1.0e-9);
        Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-16);
        Assert.assertEquals("Adams-Moulton", integ.getName());
    }

// org.apache.commons.math.ode.nonstiff.AdamsMoultonIntegratorTest::polynomial
    public void polynomial() {
        TestProblem6 pb = new TestProblem6();
        double range = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());

        for (int nSteps = 2; nSteps < 8; ++nSteps) {
            AdamsMoultonIntegrator integ =
                new AdamsMoultonIntegrator(nSteps, 1.0e-6 * range, 0.1 * range, 1.0e-5, 1.0e-5);
            TestProblemHandler handler = new TestProblemHandler(pb, integ);
            integ.addStepHandler(handler);
            integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                            pb.getFinalTime(), new double[pb.getDimension()]);
            if (nSteps < 4) {
                Assert.assertTrue(handler.getMaximalValueError() > 7.0e-04);
            } else {
                Assert.assertTrue(handler.getMaximalValueError() < 3.0e-13);
            }
        }

    }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
      TestProblem1 pb = new TestProblem1();
      DormandPrince54Integrator integrator = new DormandPrince54Integrator(0.0, 1.0,
                                                                           1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testMinStep
  public void testMinStep() {

      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                 vecAbsoluteTolerance,
                                                                 vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testSmallLastStep
  public void testSmallLastStep()
    {

    TestProblemAbstract pb = new TestProblem5();
    double minStep = 1.25;
    double maxStep = FastMath.abs(pb.getFinalTime() - pb.getInitialTime());
    double scalAbsoluteTolerance = 6.0e-4;
    double scalRelativeTolerance = 6.0e-4;

    AdaptiveStepsizeIntegrator integ =
      new DormandPrince54Integrator(minStep, maxStep,
                                    scalAbsoluteTolerance,
                                    scalRelativeTolerance);

    DP54SmallLastHandler handler = new DP54SmallLastHandler(minStep);
    integ.addStepHandler(handler);
    integ.setInitialStepSize(1.7);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertTrue(handler.wasLastSeen());
    Assert.assertEquals("Dormand-Prince 5(4)", integ.getName());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testBackward
  public void testBackward()
      {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                 scalAbsoluteTolerance,
                                                                 scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 2.0e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 2.0e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Dormand-Prince 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      EmbeddedRungeKuttaIntegrator integ =
          new DormandPrince54Integrator(minStep, maxStep,
                                        scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.setSafety(0.8);
      integ.setMaxGrowth(5.0);
      integ.setMinReduction(0.3);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.assertEquals(0.8, integ.getSafety(), 1.0e-12);
      Assert.assertEquals(5.0, integ.getMaxGrowth(), 1.0e-12);
      Assert.assertEquals(0.3, integ.getMinReduction(), 1.0e-12);

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (0.7 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testEvents
  public void testEvents()
    {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getMaximalValueError() < 5.0e-6);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testKepler
  public void testKepler()
    {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertEquals(integ.getEvaluations(), pb.getCalls());
    Assert.assertTrue(pb.getCalls() < 2800);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54IntegratorTest::testVariableSteps
  public void testVariableSteps()
    {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new VariableHandler());
    double stopTime = integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                                      pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                    scalAbsoluteTolerance,
                                                                    scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    Assert.assertTrue(bos.size () > 135000);
    Assert.assertTrue(bos.size () < 145000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    Assert.assertTrue(maxError < 7.0e-10);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince54StepInterpolatorTest::checkClone
  public void checkClone()
    {
      TestProblem3 pb = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = scalAbsoluteTolerance;
      DormandPrince54Integrator integ = new DormandPrince54Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
      integ.addStepHandler(new StepHandler() {
          public void handleStep(StepInterpolator interpolator, boolean isLast) {
              StepInterpolator cloned = interpolator.copy();
              double tA = cloned.getPreviousTime();
              double tB = cloned.getCurrentTime();
              double halfStep = FastMath.abs(tB - tA) / 2;
              Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
              Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
              for (int i = 0; i < 10; ++i) {
                  double t = (i * tB + (9 - i) * tA) / 9;
                  interpolator.setInterpolatedTime(t);
                  Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                  cloned.setInterpolatedTime(t);
                  Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                  double[] referenceState = interpolator.getInterpolatedState();
                  double[] cloneState     = cloned.getInterpolatedState();
                  for (int j = 0; j < referenceState.length; ++j) {
                      Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                  }
              }
          }
          public void init(double t0, double[] y0, double t) {
          }
      });
      integ.integrate(pb,
              pb.getInitialTime(), pb.getInitialState(),
              pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent
  public void testMissedEndEvent() {
      final double   t0     = 1878250320.0000029;
      final double   tEvent = 1878250379.9999986;
      final double[] k  = { 1.0e-4, 1.0e-5, 1.0e-6 };
      FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return k.length;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              for (int i = 0; i < y.length; ++i) {
                  yDot[i] = k[i] * y[i];
              }
          }
      };

      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 100.0,
                                                                             1.0e-10, 1.0e-10);

      double[] y0   = new double[k.length];
      for (int i = 0; i < y0.length; ++i) {
          y0[i] = i + 1;
      }
      double[] y    = new double[k.length];

      integrator.setInitialStepSize(60.0);
      double finalT = integrator.integrate(ode, t0, y0, tEvent, y);
      Assert.assertEquals(tEvent, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

      integrator.setInitialStepSize(60.0);
      integrator.addEventHandler(new EventHandler() {

          public void init(double t0, double[] y0, double t) {
          }

          public void resetState(double t, double[] y) {
          }

          public double g(double t, double[] y) {
              return t - tEvent;
          }

          public Action eventOccurred(double t, double[] y, boolean increasing) {
              Assert.assertEquals(tEvent, t, 5.0e-6);
              return Action.CONTINUE;
          }
      }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
      finalT = integrator.integrate(ode, t0, y0, tEvent + 120, y);
      Assert.assertEquals(tEvent + 120, finalT, 5.0e-6);
      for (int i = 0; i < y.length; ++i) {
          Assert.assertEquals(y0[i] * FastMath.exp(k[i] * (finalT - t0)), y[i], 1.0e-9);
      }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testDimensionCheck
  public void testDimensionCheck() {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()+10],
                           1.0, new double[pb.getDimension()+10]);
      Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testNullIntervalCheck
  public void testNullIntervalCheck() {
      TestProblem1 pb = new TestProblem1();
      DormandPrince853Integrator integrator = new DormandPrince853Integrator(0.0, 1.0,
                                                                             1.0e-10, 1.0e-10);
      integrator.integrate(pb,
                           0.0, new double[pb.getDimension()],
                           0.0, new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMinStep
  public void testMinStep() {

      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  vecAbsoluteTolerance,
                                                                  vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    {

    int previousCalls = Integer.MAX_VALUE;
    AdaptiveStepsizeIntegrator integ =
        new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY,
                                       Double.NaN, Double.NaN);
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;
      integ.setStepSizeControl(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance);

      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testTooLargeFirstStep
  public void testTooLargeFirstStep() {

      AdaptiveStepsizeIntegrator integ =
              new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
      final double start = 0.0;
      final double end   = 0.001;
      FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {

          public int getDimension() {
              return 1;
          }

          public void computeDerivatives(double t, double[] y, double[] yDot) {
              Assert.assertTrue(t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
              Assert.assertTrue(t <= FastMath.nextAfter(end,   Double.POSITIVE_INFINITY));
              yDot[0] = -100.0 * y[0];
          }

      };

      integ.setStepSizeControl(0, 1.0, 1.0e-6, 1.0e-8);
      integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testBackward
  public void testBackward()
      {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                  scalAbsoluteTolerance,
                                                                  scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 1.1e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 1.1e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testEvents
  public void testEvents()
    {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-9;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l], Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertEquals(0, handler.getMaximalValueError(), 2.1e-7);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testKepler
  public void testKepler()
    {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                scalAbsoluteTolerance,
                                                                scalRelativeTolerance);
    integ.addStepHandler(new KeplerHandler(pb));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertEquals(integ.getEvaluations(), pb.getCalls());
    Assert.assertTrue(pb.getCalls() < 3300);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testVariableSteps
  public void testVariableSteps()
    {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                               scalAbsoluteTolerance,
                                                               scalRelativeTolerance);
    integ.addStepHandler(new VariableHandler());
    double stopTime = integ.integrate(pb,
                                      pb.getInitialTime(), pb.getInitialState(),
                                      pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertEquals(pb.getFinalTime(), stopTime, 1.0e-10);
    Assert.assertEquals("Dormand-Prince 8 (5, 3)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testUnstableDerivative
  public void testUnstableDerivative()
  {
    final StepProblem stepProblem = new StepProblem(0.0, 1.0, 2.0);
    FirstOrderIntegrator integ =
      new DormandPrince853Integrator(0.1, 10, 1.0e-12, 0.0);
    integ.addEventHandler(stepProblem, 1.0, 1.0e-12, 1000);
    double[] y = { Double.NaN };
    integ.integrate(stepProblem, 0.0, new double[] { 0.0 }, 10.0, y);
    Assert.assertEquals(8.0, y[0], 1.0e-12);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.0e-10);
  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    Assert.assertTrue(bos.size () > 90000);
    Assert.assertTrue(bos.size () < 100000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    Assert.assertTrue(maxError < 2.4e-10);

  }

// org.apache.commons.math.ode.nonstiff.DormandPrince853StepInterpolatorTest::checklone
  public void checklone()
  {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    DormandPrince853Integrator integ = new DormandPrince853Integrator(minStep, maxStep,
                                                                      scalAbsoluteTolerance,
                                                                      scalRelativeTolerance);
    integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public void init(double t0, double[] y0, double t) {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testWrongDerivative
  public void testWrongDerivative() throws Exception {
      HighamHall54Integrator integrator =
          new HighamHall54Integrator(0.0, 1.0, 1.0e-10, 1.0e-10);
      FirstOrderDifferentialEquations equations =
          new FirstOrderDifferentialEquations() {
            public void computeDerivatives(double t, double[] y, double[] dot) {
            if (t < -0.5) {
                throw new LocalException(t);
            } else {
                throw new RuntimeException("oops");
           }
          }
          public int getDimension() {
              return 1;
          }
      };

      try  {
        integrator.integrate(equations, -1.0, new double[1], 0.0, new double[1]);
        Assert.fail("an exception should have been thrown");
      } catch(LocalException de) {
        
      }

      try  {
        integrator.integrate(equations, 0.0, new double[1], 1.0, new double[1]);
        Assert.fail("an exception should have been thrown");
      } catch(RuntimeException de) {
        
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testMinStep
  public void testMinStep() {

      TestProblem1 pb = new TestProblem1();
      double minStep = 0.1 * (pb.getFinalTime() - pb.getInitialTime());
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double[] vecAbsoluteTolerance = { 1.0e-15, 1.0e-16 };
      double[] vecRelativeTolerance = { 1.0e-15, 1.0e-16 };

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              vecAbsoluteTolerance,
                                                              vecRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testIncreasingTolerance
  public void testIncreasingTolerance()
    {

    int previousCalls = Integer.MAX_VALUE;
    for (int i = -12; i < -2; ++i) {
      TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = FastMath.pow(10.0, i);
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      
      
      
      Assert.assertTrue(handler.getMaximalValueError() < (1.3 * scalAbsoluteTolerance));
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);

      int calls = pb.getCalls();
      Assert.assertEquals(integ.getEvaluations(), calls);
      Assert.assertTrue(calls <= previousCalls);
      previousCalls = calls;

    }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testBackward
  public void testBackward()
      {

      TestProblem5 pb = new TestProblem5();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);
      integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

      Assert.assertTrue(handler.getLastError() < 5.0e-7);
      Assert.assertTrue(handler.getMaximalValueError() < 5.0e-7);
      Assert.assertEquals(0, handler.getMaximalTimeError(), 1.0e-12);
      Assert.assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEvents
  public void testEvents()
    {

    TestProblem4 pb = new TestProblem4();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            scalAbsoluteTolerance,
                                                            scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);
    EventHandler[] functions = pb.getEventsHandlers();
    double convergence = 1.0e-8 * maxStep;
    for (int l = 0; l < functions.length; ++l) {
      integ.addEventHandler(functions[l],
                                 Double.POSITIVE_INFINITY, convergence, 1000);
    }
    Assert.assertEquals(functions.length, integ.getEventHandlers().size());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    Assert.assertTrue(handler.getMaximalValueError() < 1.0e-7);
    Assert.assertEquals(0, handler.getMaximalTimeError(), convergence);
    Assert.assertEquals(12.0, handler.getLastTime(), convergence);
    integ.clearEventHandlers();
    Assert.assertEquals(0, integ.getEventHandlers().size());

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsErrors
  public void testEventsErrors() {

      final TestProblem1 pb = new TestProblem1();
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();
      double scalAbsoluteTolerance = 1.0e-8;
      double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

      FirstOrderIntegrator integ =
          new HighamHall54Integrator(minStep, maxStep,
                                     scalAbsoluteTolerance, scalRelativeTolerance);
      TestProblemHandler handler = new TestProblemHandler(pb, integ);
      integ.addStepHandler(handler);

      integ.addEventHandler(new EventHandler() {
        public void init(double t0, double[] y0, double t) {
        }
        public Action eventOccurred(double t, double[] y, boolean increasing) {
          return Action.CONTINUE;
        }
        public double g(double t, double[] y) {
          double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
          double offset = t - middle;
          if (offset > 0) {
            throw new LocalException(t);
          }
          return offset;
        }
        public void resetState(double t, double[] y) {
        }
      }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 1000);

      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testEventsNoConvergence
  public void testEventsNoConvergence() throws Exception {

    final TestProblem1 pb = new TestProblem1();
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = 0.01 * scalAbsoluteTolerance;

    FirstOrderIntegrator integ =
        new HighamHall54Integrator(minStep, maxStep,
                                   scalAbsoluteTolerance, scalRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ);
    integ.addStepHandler(handler);

    integ.addEventHandler(new EventHandler() {
      public void init(double t0, double[] y0, double t) {
      }
      public Action eventOccurred(double t, double[] y, boolean increasing) {
        return Action.CONTINUE;
      }
      public double g(double t, double[] y) {
        double middle = (pb.getInitialTime() + pb.getFinalTime()) / 2;
        double offset = t - middle;
        return (offset > 0) ? (offset + 0.5) : (offset - 0.5);
      }
      public void resetState(double t, double[] y) {
      }
    }, Double.POSITIVE_INFINITY, 1.0e-8 * maxStep, 3);

    try {
      integ.integrate(pb,
                      pb.getInitialTime(), pb.getInitialState(),
                      pb.getFinalTime(), new double[pb.getDimension()]);
      Assert.fail("an exception should have been thrown");
    } catch (TooManyEvaluationsException tmee) {
        
    }

}

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testSanityChecks
  public void testSanityChecks() throws Exception {
      final TestProblem3 pb  = new TestProblem3(0.9);
      double minStep = 0;
      double maxStep = pb.getFinalTime() - pb.getInitialTime();

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), new double[6],
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[6]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[2], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[2]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (DimensionMismatchException ie) {
        
      }

      try {
        FirstOrderIntegrator integ =
            new HighamHall54Integrator(minStep, maxStep, new double[4], new double[4]);
        integ.integrate(pb, pb.getInitialTime(), pb.getInitialState(),
                        pb.getInitialTime(), new double[pb.getDimension()]);
        Assert.fail("an exception should have been thrown");
      } catch (NumberIsTooSmallException ie) {
        
      }

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54IntegratorTest::testKepler
  public void testKepler()
    {

    final TestProblem3 pb  = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double[] vecAbsoluteTolerance = { 1.0e-8, 1.0e-8, 1.0e-10, 1.0e-10 };
    double[] vecRelativeTolerance = { 1.0e-10, 1.0e-10, 1.0e-8, 1.0e-8 };

    FirstOrderIntegrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                            vecAbsoluteTolerance,
                                                            vecRelativeTolerance);
    TestProblemHandler handler = new TestProblemHandler(pb, integ); 
    integ.addStepHandler(handler);
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertEquals(0.0, handler.getMaximalValueError(), 1.5e-4);
    Assert.assertEquals("Higham-Hall 5(4)", integ.getName());
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::derivativesConsistency
  public void derivativesConsistency()
  {
    TestProblem3 pb = new TestProblem3(0.1);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 1.1e-10);
  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::serialization
  public void serialization()
    throws IOException, ClassNotFoundException {

    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    integ.addStepHandler(new ContinuousOutputModel());
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(bos);
    for (StepHandler handler : integ.getStepHandlers()) {
        oos.writeObject(handler);
    }

    Assert.assertTrue(bos.size () > 185000);
    Assert.assertTrue(bos.size () < 195000);

    ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream     ois = new ObjectInputStream(bis);
    ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

    Random random = new Random(347588535632l);
    double maxError = 0.0;
    for (int i = 0; i < 1000; ++i) {
      double r = random.nextDouble();
      double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
      cm.setInterpolatedTime(time);
      double[] interpolatedY = cm.getInterpolatedState ();
      double[] theoreticalY  = pb.computeTheoreticalState(time);
      double dx = interpolatedY[0] - theoreticalY[0];
      double dy = interpolatedY[1] - theoreticalY[1];
      double error = dx * dx + dy * dy;
      if (error > maxError) {
        maxError = error;
      }
    }

    Assert.assertTrue(maxError < 1.6e-10);

  }

// org.apache.commons.math.ode.nonstiff.HighamHall54StepInterpolatorTest::checkClone
  public void checkClone()
  {
    TestProblem3 pb = new TestProblem3(0.9);
    double minStep = 0;
    double maxStep = pb.getFinalTime() - pb.getInitialTime();
    double scalAbsoluteTolerance = 1.0e-8;
    double scalRelativeTolerance = scalAbsoluteTolerance;
    HighamHall54Integrator integ = new HighamHall54Integrator(minStep, maxStep,
                                                              scalAbsoluteTolerance,
                                                              scalRelativeTolerance);
    integ.addStepHandler(new StepHandler() {
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            StepInterpolator cloned = interpolator.copy();
            double tA = cloned.getPreviousTime();
            double tB = cloned.getCurrentTime();
            double halfStep = FastMath.abs(tB - tA) / 2;
            Assert.assertEquals(interpolator.getPreviousTime(), tA, 1.0e-12);
            Assert.assertEquals(interpolator.getCurrentTime(), tB, 1.0e-12);
            for (int i = 0; i < 10; ++i) {
                double t = (i * tB + (9 - i) * tA) / 9;
                interpolator.setInterpolatedTime(t);
                Assert.assertTrue(FastMath.abs(cloned.getInterpolatedTime() - t) > (halfStep / 10));
                cloned.setInterpolatedTime(t);
                Assert.assertEquals(t, cloned.getInterpolatedTime(), 1.0e-12);
                double[] referenceState = interpolator.getInterpolatedState();
                double[] cloneState     = cloned.getInterpolatedState();
                for (int j = 0; j < referenceState.length; ++j) {
                    Assert.assertEquals(referenceState[j], cloneState[j], 1.0e-12);
                }
            }
        }
        public void init(double t0, double[] y0, double t) {
        }
    });
    integ.integrate(pb,
            pb.getInitialTime(), pb.getInitialState(),
            pb.getFinalTime(), new double[pb.getDimension()]);

  }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::derivativesConsistency
    public void derivativesConsistency() {
        TestProblem3 pb = new TestProblem3();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 5e-9);
    }

// org.apache.commons.math.ode.sampling.NordsieckStepInterpolatorTest::serialization
    public void serialization()
    throws IOException, ClassNotFoundException {

        TestProblem1 pb = new TestProblem1();
        AdamsBashforthIntegrator integ = new AdamsBashforthIntegrator(4, 0.0, 1.0, 1.0e-10, 1.0e-10);
        integ.addStepHandler(new ContinuousOutputModel());
        integ.integrate(pb,
                        pb.getInitialTime(), pb.getInitialState(),
                        pb.getFinalTime(), new double[pb.getDimension()]);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream    oos = new ObjectOutputStream(bos);
        for (StepHandler handler : integ.getStepHandlers()) {
            oos.writeObject(handler);
        }

        Assert.assertTrue(bos.size () >  25500);
        Assert.assertTrue(bos.size () <  26500);

        ByteArrayInputStream  bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream     ois = new ObjectInputStream(bis);
        ContinuousOutputModel cm  = (ContinuousOutputModel) ois.readObject();

        Random random = new Random(347588535632l);
        double maxError = 0.0;
        for (int i = 0; i < 1000; ++i) {
            double r = random.nextDouble();
            double time = r * pb.getInitialTime() + (1.0 - r) * pb.getFinalTime();
            cm.setInterpolatedTime(time);
            double[] interpolatedY = cm.getInterpolatedState ();
            double[] theoreticalY  = pb.computeTheoreticalState(time);
            double dx = interpolatedY[0] - theoreticalY[0];
            double dy = interpolatedY[1] - theoreticalY[1];
            double error = dx * dx + dy * dy;
            if (error > maxError) {
                maxError = error;
            }
        }

        Assert.assertTrue(maxError < 1.0e-6);

    }

// org.apache.commons.math.ode.sampling.StepNormalizerTest::testBoundaries
  public void testBoundaries()
    {
    double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.0,
                                       new FixedStepHandler() {
                                         private boolean firstCall = true;
                                         public void init(double t0, double[] y0, double t) {
                                         }
                                         public void handleStep(double t,
                                                                double[] y,
                                                                double[] yDot,
                                                                boolean isLast) {
                                           if (firstCall) {
                                             checkValue(t, pb.getInitialTime());
                                             firstCall = false;
                                           }
                                           if (isLast) {
                                             setLastSeen(true);
                                             checkValue(t, pb.getFinalTime());
                                           }
                                         }
                                       }));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertTrue(lastSeen);
  }

// org.apache.commons.math.ode.sampling.StepNormalizerTest::testBeforeEnd
  public void testBeforeEnd()
    {
    final double range = pb.getFinalTime() - pb.getInitialTime();
    setLastSeen(false);
    integ.addStepHandler(new StepNormalizer(range / 10.5,
                                       new FixedStepHandler() {
                                         public void init(double t0, double[] y0, double t) {
                                         }
                                         public void handleStep(double t,
                                                                double[] y,
                                                                double[] yDot,
                                                                boolean isLast) {
                                           if (isLast) {
                                             setLastSeen(true);
                                             checkValue(t,
                                                        pb.getFinalTime() - range / 21.0);
                                           }
                                         }
                                       }));
    integ.integrate(pb,
                    pb.getInitialTime(), pb.getInitialState(),
                    pb.getFinalTime(), new double[pb.getDimension()]);
    Assert.assertTrue(lastSeen);
  }
