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

package org.ejml.alg.dense.decompose.lu;

import org.ejml.UtilEjml;
import org.ejml.alg.dense.decompose.TriangularSolver_CD64;
import org.ejml.alg.dense.decompose.UtilDecompositons_CD64;
import org.ejml.data.Complex_F64;
import org.ejml.data.RowMatrix_C64;
import org.ejml.interfaces.decomposition.LUDecomposition_F64;
import org.ejml.ops.SpecializedOps_CD64;


/**
 * <p>
 * Contains common data structures and operations for LU decomposition algorithms.
 * </p>
 * @author Peter Abeles
 */
public abstract class LUDecompositionBase_CD64
        implements LUDecomposition_F64<RowMatrix_C64> {
    // the decomposed matrix
    protected RowMatrix_C64 LU;

    // it can decompose a matrix up to this size
    protected int maxWidth=-1;

    // the shape of the matrix
    protected int m,n,stride;
    // data in the matrix
    protected double dataLU[];

    // used in set, solve, invert
    protected double vv[];
    // used in set
    protected int indx[];
    protected int pivot[];

    // used by determinant
    protected double pivsign;
    protected Complex_F64 det = new Complex_F64();

    public void setExpectedMaxSize( int numRows , int numCols )
    {
        LU = new RowMatrix_C64(numRows,numCols);

        this.dataLU = LU.data;
        maxWidth = Math.max(numRows,numCols);

        vv = new double[ maxWidth*2 ];
        indx = new int[ maxWidth ];
        pivot = new int[ maxWidth ];
    }

    public RowMatrix_C64 getLU() {
        return LU;
    }

    public int[] getIndx() {
        return indx;
    }

    public int[] getPivot() {
        return pivot;
    }

    @Override
    public boolean inputModified() {
        return false;
    }

    /**
     * Writes the lower triangular matrix into the specified matrix.
     *
     * @param lower Where the lower triangular matrix is written to.
     */
    @Override
    public RowMatrix_C64 getLower(RowMatrix_C64 lower )
    {
        int numRows = LU.numRows;
        int numCols = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;

        lower = UtilDecompositons_CD64.checkZerosUT(lower, numRows,numCols);

        for( int i = 0; i < numCols; i++ ) {
            lower.set(i,i,1.0,0.0);

            for( int j = 0; j < i; j++ ) {
                int indexLU = LU.getIndex(i,j);
                int indexL = lower.getIndex(i,j);

                double real = LU.data[indexLU];
                double imaginary = LU.data[indexLU+1];

                lower.data[indexL] = real;
                lower.data[indexL+1] = imaginary;
            }
        }

        if( numRows > numCols ) {
            for( int i = numCols; i < numRows; i++ ) {
                for( int j = 0; j < numCols; j++ ) {
                    int indexLU = LU.getIndex(i,j);
                    int indexL = lower.getIndex(i,j);

                    double real = LU.data[indexLU];
                    double imaginary = LU.data[indexLU+1];

                    lower.data[indexL] = real;
                    lower.data[indexL+1] = imaginary;
                }
            }
        }
        return lower;
    }

    /**
     * Writes the upper triangular matrix into the specified matrix.
     *
     * @param upper Where the upper triangular matrix is writen to.
     */
    @Override
    public RowMatrix_C64 getUpper(RowMatrix_C64 upper )
    {
        int numRows = LU.numRows < LU.numCols ? LU.numRows : LU.numCols;
        int numCols = LU.numCols;

        upper = UtilDecompositons_CD64.checkZerosLT(upper, numRows,numCols);

        for( int i = 0; i < numRows; i++ ) {
            for( int j = i; j < numCols; j++ ) {
                int indexLU = LU.getIndex(i,j);
                int indexU = upper.getIndex(i,j);

                double real = LU.data[indexLU];
                double imaginary = LU.data[indexLU+1];

                upper.data[indexU] = real;
                upper.data[indexU+1] = imaginary;
            }
        }

        return upper;
    }

    public RowMatrix_C64 getPivot(RowMatrix_C64 pivot ) {
        return SpecializedOps_CD64.pivotMatrix(pivot, this.pivot, LU.numRows, false);
    }

    protected void decomposeCommonInit(RowMatrix_C64 a) {
        if( a.numRows > maxWidth || a.numCols > maxWidth ) {
            setExpectedMaxSize(a.numRows,a.numCols);
        }

        m = a.numRows;
        n = a.numCols;
        stride = n*2;

        LU.set(a);
        for (int i = 0; i < m; i++) {
            pivot[i] = i;
        }
        pivsign = 1;
    }

    /**
     * Determines if the decomposed matrix is singular.  This function can return
     * false and the matrix be almost singular, which is still bad.
     *
     * @return true if singular false otherwise.
     */
    @Override
    public boolean isSingular() {

        for( int i = 0; i < m; i++ ) {
            double real = dataLU[i*stride+i*2];
            double imaginary = dataLU[i*stride+i*2+1];

            double mag2 = real*real + imaginary*imaginary;

            if( mag2 < UtilEjml.EPS*UtilEjml.EPS )
                return true;
        }
        return false;
    }

    /**
     * Computes the determinant from the LU decomposition.
     *
     * @return The matrix's determinant.
     */
    @Override
    public Complex_F64 computeDeterminant() {
        if( m != n )
            throw new IllegalArgumentException("Must be a square matrix.");

        double realRet = pivsign;
        double realImg = 0;

        int total = m*stride;
        for( int i = 0; i < total; i += stride + 2 ) {
            double real = dataLU[i];
            double imaginary = dataLU[i+1];

            double r = realRet*real - realImg*imaginary;
            double t = realRet*imaginary + realImg*real;

            realRet = r;
            realImg = t;
        }

        det.set(realRet,realImg);
        return det;
    }

    public /**/double quality() {
        return SpecializedOps_CD64.qualityTriangular(LU);
    }

    /**
     * a specialized version of solve that avoid additional checks that are not needed.
     */
    public void _solveVectorInternal( double []vv )
    {
        // Solve L*Y = B
        solveL(vv);

        // Solve U*X = Y;
        TriangularSolver_CD64.solveU(dataLU, vv, n);
    }

    /**
     * Solve the using the lower triangular matrix in LU.  Diagonal elements are assumed
     * to be 1
     */
    protected void solveL(double[] vv) {

        int ii = 0;

        for( int i = 0; i < n; i++ ) {
            int ip = indx[i];
            double sumReal = vv[ip*2];
            double sumImg = vv[ip*2+1];

            vv[ip*2] = vv[i*2];
            vv[ip*2+1] = vv[i*2+1];

            if( ii != 0 ) {
//                for( int j = ii-1; j < i; j++ )
//                    sum -= dataLU[i* n +j]*vv[j];
                int index = i*stride + (ii-1)*2;
                for( int j = ii-1; j < i; j++ ){
                    double luReal = dataLU[index++];
                    double luImg  = dataLU[index++];

                    double vvReal = vv[j*2];
                    double vvImg  = vv[j*2+1];

                    sumReal -= luReal*vvReal - luImg*vvImg;
                    sumImg  -= luReal*vvImg  + luImg*vvReal;
                }
            } else if( sumReal*sumReal + sumImg*sumImg != 0.0 ) {
                ii=i+1;
            }
            vv[i*2] = sumReal;
            vv[i*2+1] = sumImg;
        }
    }

    public double[] _getVV() {
        return vv;
    }
}