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

package org.ejml.alg.dense.linsol.lu;

import org.ejml.alg.dense.decomposition.lu.LUDecompositionAlt_D64;
import org.ejml.alg.dense.linsol.GenericLinearSolverChecks_D64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.linsol.LinearSolver;


/**
 * @author Peter Abeles
 */
public class TestLinearSolverLu_D64 extends GenericLinearSolverChecks_D64 {

    public TestLinearSolverLu_D64() {
        shouldWorkRectangle = false;
        shouldFailSingular = false;
    }

    @Override
    protected LinearSolver<RowMatrix_F64> createSolver(RowMatrix_F64 A ) {
        LUDecompositionAlt_D64 decomp = new LUDecompositionAlt_D64();

        return new LinearSolverLu_D64(decomp);
    }
}
