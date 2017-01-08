/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.example;

import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;

import java.util.ArrayList;
import java.util.List;

/**
 * Compares how fast the filters all run relative to each other.
 *
 * @author Peter Abeles
 */
public class BenchmarkKalmanPerformance {

    private static final int NUM_TRIALS = 200;
    private static final int MAX_STEPS = 1000;
    private static final double T = 1.0;

    private static int measDOF = 8;

    List<KalmanFilter> filters = new ArrayList<KalmanFilter>();

    public void run() {
        RowMatrix_F64 priorX = new RowMatrix_F64(9,1, true, 0.5, -0.2, 0, 0, 0.2, -0.9, 0, 0.2, -0.5);
        RowMatrix_F64 priorP = CommonOps_D64.identity(9);

        RowMatrix_F64 trueX = new RowMatrix_F64(9,1, true, 0, 0, 0, 0.2, 0.2, 0.2, 0.5, 0.1, 0.6);

        List<RowMatrix_F64> meas = createSimulatedMeas(trueX);

        RowMatrix_F64 F = createF(T);
        RowMatrix_F64 Q = createQ(T,0.1);
        RowMatrix_F64 H = createH();

        for(KalmanFilter f : filters ) {

            long timeBefore = System.currentTimeMillis();

            f.configure(F,Q,H);

            for( int trial = 0; trial < NUM_TRIALS; trial++ ) {
                f.setState(priorX,priorP);
                processMeas(f,meas);
            }

            long timeAfter = System.currentTimeMillis();

            System.out.println("Filter = "+f.getClass().getSimpleName());
            System.out.println("Elapsed time: "+(timeAfter-timeBefore));

            System.gc();
        }
    }

    private List<RowMatrix_F64> createSimulatedMeas(RowMatrix_F64 x ) {

        List<RowMatrix_F64> ret = new ArrayList<RowMatrix_F64>();

        RowMatrix_F64 F = createF(T);
        RowMatrix_F64 H = createH();

//        UtilEjml.print(F);
//        UtilEjml.print(H);

        RowMatrix_F64 x_next = new RowMatrix_F64(x);
        RowMatrix_F64 z = new RowMatrix_F64(H.numRows,1);

        for( int i = 0; i < MAX_STEPS; i++ ) {
            CommonOps_D64.mult(F,x,x_next);
            CommonOps_D64.mult(H,x_next,z);
            ret.add(z.copy());
            x.set(x_next);
        }

        return ret;
    }

    private void processMeas( KalmanFilter f ,
                              List<RowMatrix_F64> meas )
    {
        RowMatrix_F64 R = CommonOps_D64.identity(measDOF);

        for(RowMatrix_F64 z : meas ) {
            f.predict();
            f.update(z,R);
        }
    }


    public static RowMatrix_F64 createF(double T ) {
        double []a = new double[]{
                1, 0 , 0 , T , 0 , 0 , 0.5*T*T , 0 , 0 ,
                0, 1 , 0 , 0 , T , 0 , 0 , 0.5*T*T , 0 ,
                0, 0 , 1 , 0 , 0 , T , 0 , 0 , 0.5*T*T ,
                0, 0 , 0 , 1 , 0 , 0 , T , 0 , 0 ,
                0, 0 , 0 , 0 , 1 , 0 , 0 , T , 0 ,
                0, 0 , 0 , 0 , 0 , 1 , 0 , 0 , T ,
                0, 0 , 0 , 0 , 0 , 0 , 1 , 0 , 0 ,
                0, 0 , 0 , 0 , 0 , 0 , 0 , 1 , 0 ,
                0, 0 , 0 , 0 , 0 , 0 , 0 , 0 , 1 };

        return new RowMatrix_F64(9,9, true, a);
    }

    public static RowMatrix_F64 createQ(double T , double var ) {
        RowMatrix_F64 Q = new RowMatrix_F64(9,9);

        double a00 = (1.0/4.0)*T*T*T*T*var;
        double a01 = (1.0/2.0)*T*T*T*var;
        double a02 = (1.0/2.0)*T*T*var;
        double a11 = T*T*var;
        double a12 = T*var;
        double a22 = var;

        for( int i = 0; i < 3; i++ ) {
            Q.set(i,i,a00);
            Q.set(i,3+i,a01);
            Q.set(i,6+i,a02);
            Q.set(3+i,3+i,a11);
            Q.set(3+i,6+i,a12);
            Q.set(6+i,6+i,a22);
        }

        for( int y = 1; y < 9; y++ ) {
            for( int x = 0; x < y; x++ ) {
                Q.set(y,x, Q.get(x,y));
            }
        }

        return Q;
    }

    public static RowMatrix_F64 createH() {
        RowMatrix_F64 H = new RowMatrix_F64(measDOF,9);
        for( int i = 0; i < measDOF; i++ ) {
            H.set(i,i,1.0);
        }

        return H;
    }

    public static void main( String args[] ) {
        BenchmarkKalmanPerformance benchmark = new BenchmarkKalmanPerformance();

        benchmark.filters.add( new KalmanFilterOperations());
        benchmark.filters.add( new KalmanFilterSimple());
        benchmark.filters.add( new KalmanFilterEquation());


        benchmark.run();
    }
}
