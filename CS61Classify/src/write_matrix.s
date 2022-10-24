.globl write_matrix

.text
# ==============================================================================
# FUNCTION: Writes a matrix of integers into a binary file
# FILE FORMAT:
#   The first 8 bytes of the file will be two 4 byte ints representing the
#   numbers of rows and columns respectively. Every 4 bytes thereafter is an
#   element of the matrix in row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is the pointer to the start of the matrix in memory
#   a2 (int)   is the number of rows in the matrix
#   a3 (int)   is the number of columns in the matrix
# Returns:
#   None
# Exceptions:
#   - If you receive an fopen error or eof,
#     this function terminates the program with error code 27
#   - If you receive an fclose error or eof,
#     this function terminates the program with error code 28
#   - If you receive an fwrite error or eof,
#     this function terminates the program with error code 30
# ==============================================================================
write_matrix:

	# Prologue
    addi sp, sp, -20
    sw ra, 0(sp)
    sw s0, 4(sp)
    sw s1, 8(sp)
    sw s2, 12(sp)
    sw s3, 16(sp)
    
    mv s1, a1    # let s1 to save the address to matrix
    mv s2, a2    # let s2 to save the row_num
    mv s3, a3    # let s3 to save the col_num
    
    # open the file
    li a1, 1
    jal fopen
    mv s0, a0
    li t0, -1
    beq s0, t0, fopen_error
    
    # write the number of rows and columns to the file
    li a0, 8
    jal malloc
    mv t0, a0
    beq t0, zero, malloc_error
    sw s2, 0(t0)
    sw s3, 4(t0)
    
    mv a0, s0
    mv a1, t0
    li a2, 2
    li a3, 4
    jal fwrite
    li t0, 2
    bne a0, t0, fwrite_error
    
    # write the data to the file
    mv a0, s0
    mv a1, s1
    mul a2, s2, s3
    li a3, 4
    jal fwrite
    mul t0, s2, s3
    bne a0, t0, fwrite_error
    
    # close the file
    mv a0, s0
    jal fclose
    bne a0, zero, fclose_error

	# Epilogue
    lw ra, 0(sp)
    lw s0, 4(sp)
    lw s1, 8(sp)
    lw s2, 12(sp)
    lw s3, 16(sp)
    addi sp, sp, 20

	ret

malloc_error:
    li a0, 26
    j exit

fopen_error:
    li a0, 27
    j exit

fclose_error:
    li a0, 28
    j exit
    
fwrite_error:
    li a0, 30
    j exit
    
    









	# Epilogue


	ret
