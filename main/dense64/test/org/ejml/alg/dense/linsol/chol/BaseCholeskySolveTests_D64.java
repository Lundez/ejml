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

package org.ejml.alg.dense.linsol.chol;

import org.ejml.EjmlUnitTests;
import org.ejml.UtilEjml;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.ops.CommonOps_D64;
import org.ejml.ops.RandomMatrices_D64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
public abstract class BaseCholeskySolveTests_D64 {

    Random rand = new Random(0x45);

    public void standardTests() {

        solve_dimensionCheck();
        testSolve();
        testInvert();
        testQuality();
        testQuality_scale();
    }

    public abstract LinearSolver<RowMatrix_F64> createSolver();

    public LinearSolver<RowMatrix_F64> createSafeSolver() {
        LinearSolver<RowMatrix_F64> solver = createSolver();
        return new LinearSolverSafe<RowMatrix_F64>(solver);
    }

    @Test
    public void setA_dimensionCheck() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        try {
            RowMatrix_F64 A = RandomMatrices_D64.createRandom(4,5,rand);
            assertTrue(solver.setA(A));
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void solve_dimensionCheck() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        RowMatrix_F64 A = RandomMatrices_D64.createSymmPosDef(4, rand);
        assertTrue(solver.setA(A));

        try {
            RowMatrix_F64 x = RandomMatrices_D64.createRandom(4,3,rand);
            RowMatrix_F64 b = RandomMatrices_D64.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_F64 x = RandomMatrices_D64.createRandom(5,2,rand);
            RowMatrix_F64 b = RandomMatrices_D64.createRandom(4,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}

        try {
            RowMatrix_F64 x = RandomMatrices_D64.createRandom(5,2,rand);
            RowMatrix_F64 b = RandomMatrices_D64.createRandom(5,2,rand);
            solver.solve(b,x);
            fail("Should have thrown an exception");
        } catch( RuntimeException ignore ) {}
    }

    @Test
    public void testSolve() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        RowMatrix_F64 A = new RowMatrix_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        RowMatrix_F64 b = new RowMatrix_F64(3,1, true, 17, 97, 320);
        RowMatrix_F64 x = RandomMatrices_D64.createRandom(3,1,rand);
        RowMatrix_F64 A_orig = A.copy();
        RowMatrix_F64 B_orig = b.copy();

        assertTrue(solver.setA(A));
        solver.solve(b,x);

        // see if the input got modified
        EjmlUnitTests.assertEquals(A,A_orig,UtilEjml.TEST_F64_SQ);
        EjmlUnitTests.assertEquals(b,B_orig,UtilEjml.TEST_F64_SQ);

        RowMatrix_F64 x_expected = new RowMatrix_F64(3,1, true, 1, 2, 3);

        EjmlUnitTests.assertEquals(x_expected,x,UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void testInvert() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        RowMatrix_F64 A = new RowMatrix_F64(3,3, true, 1, 2, 4, 2, 13, 23, 4, 23, 90);
        RowMatrix_F64 found = new RowMatrix_F64(A.numRows,A.numCols);

        assertTrue(solver.setA(A));
        solver.invert(found);

        RowMatrix_F64 A_inv = new RowMatrix_F64(3,3, true, 1.453515, -0.199546, -0.013605, -0.199546, 0.167800, -0.034014, -0.013605, -0.034014, 0.020408);

        EjmlUnitTests.assertEquals(A_inv,found,UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void testQuality() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        RowMatrix_F64 A = CommonOps_D64.diag(3,2,1);
        RowMatrix_F64 B = CommonOps_D64.diag(3,2,0.001);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertTrue(qualityB < qualityA);
    }

    @Test
    public void testQuality_scale() {

        LinearSolver<RowMatrix_F64> solver = createSafeSolver();

        RowMatrix_F64 A = CommonOps_D64.diag(3,2,1);
        RowMatrix_F64 B = A.copy();
        CommonOps_D64.scale(0.001,B);

        assertTrue(solver.setA(A));
        double qualityA = (double)solver.quality();

        assertTrue(solver.setA(B));
        double qualityB = (double)solver.quality();

        assertEquals(qualityB,qualityA, UtilEjml.TEST_F64);
    }
}
