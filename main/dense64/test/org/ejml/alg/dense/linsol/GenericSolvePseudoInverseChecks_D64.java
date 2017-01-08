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

package org.ejml.alg.dense.linsol;

import org.ejml.UtilEjml;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.MatrixFeatures_D64;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the ability of a solver to handle different type of rank deficient matrices
 *
 * @author Peter Abeles
 */
public class GenericSolvePseudoInverseChecks_D64 {

    Random rand = new Random(234);

    LinearSolver<RowMatrix_F64> solver;

    public GenericSolvePseudoInverseChecks_D64(LinearSolver<RowMatrix_F64> solver) {
        this.solver = new LinearSolverSafe<RowMatrix_F64>( solver );
    }

    public void all() {
        zeroMatrix();
        underDetermined_wide_solve();
        underDetermined_wide_inv();
        underDetermined_tall_solve();
        singular_solve();
        singular_inv();
    }

    /**
     * Shouldn't blow if it the input matrix is zero.  But there is no solution...
     */
    public void zeroMatrix() {
        RowMatrix_F64 A = new RowMatrix_F64(3,3);
        RowMatrix_F64 y = new RowMatrix_F64(3,1,true,4,7,8);

        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(3,1);
        solver.solve(y, x);

        assertFalse(MatrixFeatures_D64.hasUncountable(x));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_wide_solve() {
        // create a matrix where two rows are linearly dependent
        RowMatrix_F64 A = new RowMatrix_F64(2,3,true,1,2,3,2,3,4);

        RowMatrix_F64 y = new RowMatrix_F64(2,1,true,4,7);
        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(3,1);
        solver.solve(y,x);

        RowMatrix_F64 found = new RowMatrix_F64(2,1);
        CommonOps_D64.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_D64.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void underDetermined_wide_inv() {
        // create a matrix where two rows are linearly dependent
        RowMatrix_F64 A = new RowMatrix_F64(2,3,true,1,2,3,2,3,4);

        RowMatrix_F64 y = new RowMatrix_F64(2,1,true,4,7);
        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(3,1);
        solver.solve(y,x);

        // now test the pseudo inverse
        RowMatrix_F64 A_pinv = new RowMatrix_F64(3,2);
        RowMatrix_F64 found = new RowMatrix_F64(3,1);
        solver.invert(A_pinv);

        CommonOps_D64.mult(A_pinv,y,found);

        assertTrue(MatrixFeatures_D64.isEquals(x, found,UtilEjml.TEST_F64));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void underDetermined_tall_solve() {
        // create a matrix where two rows are linearly dependent
        RowMatrix_F64 A = new RowMatrix_F64(3,2,true,1,2,1,2,2,4);

        RowMatrix_F64 y = new RowMatrix_F64(3,1,true,4,4,8);
        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(2,1);
        solver.solve(y,x);

        RowMatrix_F64 found = new RowMatrix_F64(3,1);
        CommonOps_D64.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_D64.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute a solution for a system with more variables than equations
     */
    public void singular_solve() {
        // create a matrix where two rows are linearly dependent
        RowMatrix_F64 A = new RowMatrix_F64(3,3,true,1,2,3,2,3,4,2,3,4);

        RowMatrix_F64 y = new RowMatrix_F64(3,1,true,4,7,7);
        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(3,1);
        solver.solve(y,x);

        RowMatrix_F64 found = new RowMatrix_F64(3,1);
        CommonOps_D64.mult(A, x, found);

        // there are multiple 'x' which will generate the same solution, see if this is one of them
        assertTrue(MatrixFeatures_D64.isEquals(y, found, UtilEjml.TEST_F64));
    }

    /**
     * Compute the pseudo inverse a system with more variables than equations
     */
    public void singular_inv() {
        // create a matrix where two rows are linearly dependent
        RowMatrix_F64 A = new RowMatrix_F64(3,3,true,1,2,3,2,3,4,2,3,4);

        RowMatrix_F64 y = new RowMatrix_F64(3,1,true,4,7,7);
        assertTrue(solver.setA(A));

        RowMatrix_F64 x = new RowMatrix_F64(3,1);
        solver.solve(y,x);

        // now test the pseudo inverse
        RowMatrix_F64 A_pinv = new RowMatrix_F64(3,3);
        RowMatrix_F64 found = new RowMatrix_F64(3,1);
        solver.invert(A_pinv);

        CommonOps_D64.mult(A_pinv,y,found);

        assertTrue(MatrixFeatures_D64.isEquals(x, found,UtilEjml.TEST_F64));
    }
}
