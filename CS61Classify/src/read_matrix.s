.globl read_matrix

.text
# ==============================================================================
# FUNCTION: Allocates memory and reads in a binary file as a matrix of integers
#
# FILE FORMAT:
#   The first 8 bytes are two 4 byte ints representing the # of rows and columns
#   in the matrix. Every 4 bytes afterwards is an element of the matrix in
#   row-major order.
# Arguments:
#   a0 (char*) is the pointer to string representing the filename
#   a1 (int*)  is a pointer to an integer, we will set it to the number of rows
#   a2 (int*)  is a pointer to an integer, we will set it to the number of columns
# Returns:
#   a0 (int*)  is the pointer to the matrix in memory
# Exceptions:
#   - If malloc returns an error,
#     this function terminates the program with error code 26
#   - If you receive an fopen error or eof,
#     this function terminates the program with error code 27
#   - If you receive an fclose error or eof,
#     this function terminates the program with error code 28
#   - If you receive an fread error or eof,
#     this function terminates the program with error code 29
# ==============================================================================
read_matrix:

	# Prologue
		addi sp, sp, -32
	sw ra, 4(sp)
	sw s0, 8(sp)
	sw s1, 12(sp)
	sw s2, 16(sp)
	sw s3, 20(sp)
	sw s4, 24(sp)
	sw s5, 28(sp)

	# Epilogue

	mv s1, a1  
	mv s2, a2  
	li a1, 0  
	jal ra, fopen

	li t0, -1
	beq a0, t0, error_fopen
    blt a0, t0, error_fopen
	sw a0, 0(sp) 
	mv a1, s1
	li s3, 4
	mv a2, s3
	jal ra, fread
    bgt a0, s3, error_fread
    blt a0, s3, error_fread

	bne a0, s3, error_fread

	mv a1, s2
	mv a2, s3
	lw a0, 0(sp) 
	jal ra, fread
    bgt a0, s3, error_fread
    blt a0, s3, error_fread

	bne a0, s3, error_fread

	lw t1, 0(s1)  
	lw t2, 0(s2) 
	mul s4, t1, t2
	slli s4, s4, 2  
	mv a0, s4
	jal malloc
	beqz a0, error_malloc

	mv s5, a0  
	lw a0, 0(sp)  
	mv a1, s5
	mv a2, s4
	jal fread
    bgt a0, s4, error_fread
    blt a0, s4, error_fread

	bne a0, s4, error_fread

	lw a0, 0(sp)
	jal fclose
	bnez a0, error_fclose

	mv a0, s5
	lw ra, 4(sp)
	lw s0, 8(sp)
	lw s1, 12(sp)
	lw s2, 16(sp)
	lw s3, 20(sp)
	lw s4, 24(sp)
	lw s5, 28(sp)
	addi sp, sp, 32
	
	ret

error_malloc:
	li a0, 26
	j exit

error_fopen:
	li a0, 27
	j exit

error_fclose:
	li a0, 28
	j exit
    
error_fread:
	li a0, 29
	j exit
