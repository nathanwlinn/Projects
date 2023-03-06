#include "matrix.h"
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

// Include SSE intrinsics
#if defined(_MSC_VER)
#include <intrin.h>
#elif defined(__GNUC__) && (defined(__x86_64__) || defined(__i386__))
#include <immintrin.h>
#include <x86intrin.h>
#endif

/* Below are some intel intrinsics that might be useful
 * void _mm256_storeu_pd (double * mem_addr, __m256d a)
 * __m256d _mm256_set1_pd (double a)
 * __m256d _mm256_set_pd (double e3, double e2, double e1, double e0)
 * __m256d _mm256_loadu_pd (double const * mem_addr)
 * __m256d _mm256_add_pd (__m256d a, __m256d b)
 * __m256d _mm256_sub_pd (__m256d a, __m256d b)
 * __m256d _mm256_fmadd_pd (__m256d a, __m256d b, __m256d c)
 * __m256d _mm256_mul_pd (__m256d a, __m256d b)
 * __m256d _mm256_cmp_pd (__m256d a, __m256d b, const int imm8)
 * __m256d _mm256_and_pd (__m256d a, __m256d b)
 * __m256d _mm256_max_pd (__m256d a, __m256d b)
*/

/* Generates a random double between low and high */
double rand_double(double low, double high) {
    double range = (high - low);
    double div = RAND_MAX / range;
    return low + (rand() / div);
}

/* Generates a random matrix */
void rand_matrix(matrix *result, unsigned int seed, double low, double high) {
    srand(seed);
    for (int i = 0; i < result->rows; i++) {
        for (int j = 0; j < result->cols; j++) {
            set(result, i, j, rand_double(low, high));
        }
    }
}

/*
 * Returns the double value of the matrix at the given row and column.
 * You may assume `row` and `col` are valid. Note that the matrix is in row-major order.
 */
double get(matrix *mat, int row, int col) {
    // Task 1.1 TODO
    return mat->data[row*mat->cols+col];
}

/*
 * Sets the value at the given row and column to val. You may assume `row` and
 * `col` are valid. Note that the matrix is in row-major order.
 */
void set(matrix *mat, int row, int col, double val) {
    // Task 1.1 TODO
    mat->data[row*mat->cols+col] = val;
}

/*
 * Allocates space for a matrix struct pointed to by the double pointer mat with
 * `rows` rows and `cols` columns. You should also allocate memory for the data array
 * and initialize all entries to be zeros. `parent` should be set to NULL to indicate that
 * this matrix is not a slice. You should also set `ref_cnt` to 1.
 * You should return -1 if either `rows` or `cols` or both have invalid values. Return -2 if any
 * call to allocate memory in this function fails.
 * Return 0 upon success.
 */
int allocate_matrix(matrix **mat, int rows, int cols) {
    // Task 1.2 TODO
    // HINTS: Follow these steps.
    // 1. Check if the dimensions are valid. Return -1 if either dimension is not positive.
    // 2. Allocate space for the new matrix struct. Return -2 if allocating memory failed.
    // 3. Allocate space for the matrix data, initializing all entries to be 0. Return -2 if allocating memory failed.
    // 4. Set the number of rows and columns in the matrix struct according to the arguments provided.
    // 5. Set the `parent` field to NULL, since this matrix was not created from a slice.
    // 6. Set the `ref_cnt` field to 1.
    // 7. Store the address of the allocated matrix struct at the location `mat` is pointing at.
    // 8. Return 0 upon success.
    // int one = 1;
    int one = 1;
    if (rows < one || cols < one) {
        return -1; }
    matrix *am = (matrix *)malloc(sizeof(matrix));
    if (am == NULL) {
         return -2; }
    int matsize = rows * cols;
    am->data = (double *)calloc(rows * cols, sizeof(double));
    if (am->data == NULL) {
        return -2; }
    am->rows = rows;
    am->cols = cols;
    am->parent = NULL;
    am->ref_cnt = 1;
    *mat = am;
    return 0;
}

/*
 * You need to make sure that you only free `mat->data` if `mat` is not a slice and has no existing slices,
 * or that you free `mat->parent->data` if `mat` is the last existing slice of its parent matrix and its parent
 * matrix has no other references (including itself).
 */
void deallocate_matrix(matrix *mat) {
    // Task 1.3 TODO
    // HINTS: Follow these steps.
    // 1. If the matrix pointer `mat` is NULL, return.
    // 2. If `mat` has no parent: decrement its `ref_cnt` field by 1. If the `ref_cnt` field becomes 0, then free `mat` and its `data` field.
    // 3. Otherwise, recursively call `deallocate_matrix` on `mat`'s parent, then free `mat`.
    if (mat == NULL) {
        return; }
    if (mat->parent == NULL) {
        mat->ref_cnt -= 1;
        if (mat->ref_cnt == 0) {
            free(mat->data);
            free(mat);
        }
    } else {
        deallocate_matrix(mat->parent);
        free(mat);
    }
}

/*
 * Allocates space for a matrix struct pointed to by `mat` with `rows` rows and `cols` columns.
 * Its data should point to the `offset`th entry of `from`'s data (you do not need to allocate memory)
 * for the data field. `parent` should be set to `from` to indicate this matrix is a slice of `from`
 * and the reference counter for `from` should be incremented. Lastly, do not forget to set the
 * matrix's row and column values as well.
 * You should return -1 if either `rows` or `cols` or both have invalid values. Return -2 if any
 * call to allocate memory in this function fails.
 * Return 0 upon success.
 * NOTE: Here we're allocating a matrix struct that refers to already allocated data, so
 * there is no need to allocate space for matrix data.
 */
int allocate_matrix_ref(matrix **mat, matrix *from, int offset, int rows, int cols) {
    // Task 1.4 TODO
    // HINTS: Follow these steps.
    // 1. Check if the dimensions are valid. Return -1 if either dimension is not positive.
    // 2. Allocate space for the new matrix struct. Return -2 if allocating memory failed.
    // 3. Set the `data` field of the new struct to be the `data` field of the `from` struct plus `offset`.
    // 4. Set the number of rows and columns in the new struct according to the arguments provided.
    // 5. Set the `parent` field of the new struct to the `from` struct pointer.
    // 6. Increment the `ref_cnt` field of the `from` struct by 1.
    // 7. Store the address of the allocated matrix struct at the location `mat` is pointing at.
    // 8. Return 0 upon success.
    int one = 1;
    int zero = 0;
    if (rows < one || cols <= zero) {
        return -1; }
    matrix *space1 = (matrix *)malloc(sizeof(matrix));
    if (space1 == NULL) {
        return -2; }
    space1->data = from->data + offset;
    space1->rows = rows;
    space1->cols = cols;
    space1->parent = from;
    from->ref_cnt += 1;
    *mat = space1;
    return 0;



}

/*
 * Sets all entries in mat to val. Note that the matrix is in row-major order.
 */
void fill_matrix(matrix *mat, double val) {
    // Task 1.5 TODO
    int rowlen = mat->rows;
    int colen = mat->cols;
    int totalnum = rowlen * colen;
    __m256d fill_vec = _mm256_set1_pd(val);
    int i = 0;
    for (int i = 0; i < mat->rows * mat->cols / 4 * 4; i+=4) {
        _mm256_storeu_pd(mat->data + i, fill_vec);
    }

    for (; i < mat->rows * mat->cols; i++) {
        *(mat->data + i) = val;
    }
}

/*
 * Store the result of taking the absolute value element-wise to `result`.
 * Return 0 upon success.
 * Note that the matrix is in row-major order.
 */
int abs_matrix(matrix *result, matrix *mat) {
    // Task 1.5 TODO
    if (mat->rows != result->rows || mat->cols != result->cols) {
        return -1; }
    int total_entries = mat->rows * mat->cols;

    __m256d n1 = _mm256_set1_pd(-1);
    int checker = 1;
    int i = 0;
    #pragma omp parallel for
    for (i = 0; i < (total_entries / 4) * 4; i += 4)
    {
        __m256d dat = _mm256_loadu_pd(mat->data + i);
        __m256d neg = _mm256_mul_pd(dat, n1);

        __m256d result_vec = _mm256_max_pd(dat, neg);
        
        _mm256_storeu_pd (result->data + i, result_vec);
    }
    for (i = (total_entries / 4) * 4; i < total_entries; i++) {
        double val = mat->data[i];
        if (val < 0) {
            val = val * (-1);
        }
        result->data[i] = val;
    }
    return 0;

}

/*
 * (OPTIONAL)
 * Store the result of element-wise negating mat's entries to `result`.
 * Return 0 upon success.
 * Note that the matrix is in row-major order.
 */
int neg_matrix(matrix *result, matrix *mat) {
    // Task 1.5 TODO
    int nothing = 0;
    return 0;
}

/*
 * Store the result of adding mat1 and mat2 to `result`.
 * Return 0 upon success.
 * You may assume `mat1` and `mat2` have the same dimensions.
 * Note that the matrix is in row-major order.
 */
int add_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    // Task 1.5 TODO
    //
    
    int one = 0;
    int total = mat1->rows * mat1->cols;
    int roww = mat1->rows;
    int colss = mat1->cols;

    int i = 0;
    #pragma omp parallel for
    for (i = 0; i < (total / 4) * 4; i += 4)
    {
        __m256d da =  _mm256_loadu_pd(mat1->data + i);

        __m256d db =  _mm256_loadu_pd(mat2->data + i);

        __m256d res = _mm256_add_pd(da, db);

        _mm256_storeu_pd (result->data + i, res);
    }
    for (i = (total / 4) * 4; i < total; i++) {
        result->data[i] = mat1->data[i] + mat2->data[i];
    }


    if (one == 1) {
        return 1;
    }
    return 0;

}

/*
 * (OPTIONAL)
 * Store the result of subtracting mat2 from mat1 to `result`.
 * Return 0 upon success.
 * You may assume `mat1` and `mat2` have the same dimensions.
 * Note that the matrix is in row-major order.
 */
int sub_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    // Task 1.5 TODO
    int x  = 0;
    int y = 0;
    int z = 0;
    return 0;
}

/*
 * Store the result of multiplying mat1 and mat2 to `result`.
 * Return 0 upon success.
 * Remember that matrix multiplication is not the same as multiplying individual elements.
 * You may assume `mat1`'s number of columns is equal to `mat2`'s number of rows.
 * Note that the matrix is in row-major order.
 */
int mul_matrix(matrix *result, matrix *mat1, matrix *mat2) {
    // Task 1.6 TODO
    int one = 0;
    int chec = 0;
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    int x = 0;
    matrix *matt = NULL;
    allocate_matrix(&matt, mat2->cols, mat2->rows);
    #pragma omp parallel for collapse (2)
    for (int i = 0; i < mat2->rows; i++) {
        for (int k = 0; k < mat2->cols; k++) {
            set(matt, k, i, get(mat2, i, k));
        }
    }
    #pragma omp parallel for
    for (int i = 0; i < result->rows; i++) {
        for (int k = 0; k < result->cols; k++) {
            __m256d vec = _mm256_set1_pd(0);
            
            for (unsigned int j = 0; j < mat1->cols / 4 * 4; j+=4) {
                __m256d mat1_row = _mm256_loadu_pd(mat1->data + i * (mat1->cols) + j);
                __m256d mat2_row = _mm256_loadu_pd(matt->data + k * (matt->cols) + j);
                vec = _mm256_fmadd_pd(mat1_row, mat2_row, vec);
            }
            

            int j = 0;
            int check = 0;
            double temp_arr[4];
            _mm256_storeu_pd(temp_arr, vec);
            double total = (temp_arr[0] + temp_arr[1] + temp_arr[2] + temp_arr[3]);
            
            for (int j = mat1->cols / 4 * 4; j < mat1->cols; j++) {
                total += get(mat1, i, j) * get(matt, k, j);
                
            }
            set(result, i, k, total);
            
        }    
    }
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    
    deallocate_matrix(matt);
    return 0;
}

/*
 * Store the result of raising mat to the (pow)th power to `result`.
 * Return 0 upon success.
 * Remember that pow is defined with matrix multiplication, not element-wise multiplication.
 * You may assume `mat` is a square matrix and `pow` is a non-negative integer.
 * Note that the matrix is in row-major order.
 */
int pow_matrix(matrix *result, matrix *mat, int pow) {
    // Task 1.6 TODO
    //
    int one = 0;
    int chec = 0;
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    int x = 0;
    int rowlen = mat->rows;
    int colen = mat->cols;
    int length = mat->rows * mat->cols * sizeof(double);
    if (pow == 0) {
        #pragma omp parallel for
        for (int i = 0; i < result->rows; i += 1) {
            for (int j = 0; j < result->cols; j += 1) {
                if (i == j) {
                    result->data[i * result->cols + j] = 1;
                } else {
                    result->data[i * result->cols + j] = 0;
                }
            }
        }
        return 0;
    }
    if (pow == 1) {
        memcpy(result->data, mat->data, length);
    }
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    matrix* tmp;
    allocate_matrix(&tmp, mat->rows, mat->cols);
    mul_matrix(tmp, mat, mat);
    pow_matrix(result, tmp, pow/2);
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    if (pow % 2 == 1) {
        memcpy(tmp->data, result->data, length);
        mul_matrix(result, tmp, mat);
    }
    if (chec == 1) {
        for (int x = 0; x < chec; x += 1) {
            one = one + 1;
        }
    }
    deallocate_matrix(tmp);
    return 0;
}

int lengthfinder(matrix * mat, int x, int y, int z) {
    x = mat->rows;
    y = mat->cols;
    z = x * y;
    
    return z;
}
