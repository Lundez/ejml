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

package org.ejml;

import org.ejml.data.BlockMatrix_F64;
import org.ejml.data.D1Matrix_F64;
import org.ejml.data.RealMatrix_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.ops.CommonOps_D64;


/**
 * @author Peter Abeles
 */
public class BenchmarkInstanceOf {

    public static final double SCALE = 1.1;

    public static final StuffA stuff = new StuffA();

    public interface Stuff
    {
        public void process( Stuff a, RealMatrix_F64 M );
    }

    public static class StuffA implements Stuff
    {

        @Override
        public void process(Stuff a, RealMatrix_F64 M) {

            if( M instanceof BlockMatrix_F64) {
                CommonOps_D64.scale(1.0,(BlockMatrix_F64)M);
            } else if( M instanceof RowMatrix_F64) {
                CommonOps_D64.scale(SCALE,(RowMatrix_F64)M);
//                CommonOps.scale(0.5,(RowMatrix_F64)M);
            } else if(M instanceof D1Matrix_F64) {
                CommonOps_D64.scale(1.0,(D1Matrix_F64)M);
            } else {
               throw new IllegalArgumentException("Who knows");
            }
        }
    }

    public static void withIfStatement( RowMatrix_F64 M )
    {
        if( M.numCols > 10 ) {
            CommonOps_D64.scale(2.0,M);
        } else if( M.numRows > 12 ) {
            CommonOps_D64.scale(2.0,M);
        } else {
            CommonOps_D64.scale(SCALE,M);
//            CommonOps.scale(0.5,M);
        }
    }


    public static long processInstanceOf(RowMatrix_F64 M , int N ) {

        long before = System.currentTimeMillis();

        for( int i = 0; i < N; i++ ) {
            stuff.process(null,M);
        }

        return System.currentTimeMillis() - before;
    }

    public static long processDirect(RowMatrix_F64 M , int N ) {

        long before = System.currentTimeMillis();

        for( int i = 0; i < N; i++ ) {
            CommonOps_D64.scale(SCALE,M);
//            CommonOps.scale(0.5,M);
        }

        return System.currentTimeMillis() - before;
    }

    public static long processIf(RowMatrix_F64 M , int N ) {

        long before = System.currentTimeMillis();

        for( int i = 0; i < N; i++ ) {
            withIfStatement(M);
        }

        return System.currentTimeMillis() - before;
    }


    public static void main( String args[] ) {
        RowMatrix_F64 A = new RowMatrix_F64(2,2,true,0.1,0.5,0.7,10.0);

        int N = 200000000;

        System.out.println("instanceof "+processInstanceOf(A,N));
        System.out.println("direct     "+processDirect(A,N));
        System.out.println("if         "+processIf(A,N));
    }
}
