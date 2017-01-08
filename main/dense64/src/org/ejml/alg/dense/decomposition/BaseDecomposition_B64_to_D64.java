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

package org.ejml.alg.dense.decomposition;

import org.ejml.alg.block.MatrixOps_B64;
import org.ejml.data.BlockMatrix_F64;
import org.ejml.data.RowMatrix_F64;
import org.ejml.interfaces.decomposition.DecompositionInterface;


/**
 * Generic interface for wrapping a {@link BlockMatrix_F64} decomposition for
 * processing of {@link RowMatrix_F64}.
 *
 * @author Peter Abeles
 */
public class BaseDecomposition_B64_to_D64 implements DecompositionInterface<RowMatrix_F64> {

    protected DecompositionInterface<BlockMatrix_F64> alg;

    protected double[]tmp;
    protected BlockMatrix_F64 Ablock = new BlockMatrix_F64();
    protected int blockLength;

    public BaseDecomposition_B64_to_D64(DecompositionInterface<BlockMatrix_F64> alg,
                                        int blockLength) {
        this.alg = alg;
        this.blockLength = blockLength;
    }

    @Override
    public boolean decompose(RowMatrix_F64 A) {
        Ablock.numRows = A.numRows;
        Ablock.numCols = A.numCols;
        Ablock.blockLength = blockLength;
        Ablock.data = A.data;

        int tmpLength = Math.min( Ablock.blockLength , A.numRows ) * A.numCols;

        if( tmp == null || tmp.length < tmpLength )
            tmp = new double[ tmpLength ];

        // doing an in-place convert is much more memory efficient at the cost of a little
        // but of CPU
        MatrixOps_B64.convertRowToBlock(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);

        boolean ret = alg.decompose(Ablock);

        // convert it back to the normal format if it wouldn't have been modified
        if( !alg.inputModified() ) {
            MatrixOps_B64.convertBlockToRow(A.numRows,A.numCols,Ablock.blockLength,A.data,tmp);
        }

        return ret;
    }

    public void convertBlockToRow(int numRows , int numCols , int blockLength ,
                                  double[] data) {
        int tmpLength = Math.min( blockLength , numRows ) * numCols;

        if( tmp == null || tmp.length < tmpLength )
            tmp = new double[ tmpLength ];

        MatrixOps_B64.convertBlockToRow(numRows,numCols,Ablock.blockLength,data,tmp);
    }

    @Override
    public boolean inputModified() {
        return alg.inputModified();
    }
}
