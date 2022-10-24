.globl classify

.text
# =====================================
# COMMAND LINE ARGUMENTS
# =====================================
# Args:
#   a0 (int)        argc
#   a1 (char**)     argv
#   a1[1] (char*)   pointer to the filepath string of m0
#   a1[2] (char*)   pointer to the filepath string of m1
#   a1[3] (char*)   pointer to the filepath string of input matrix
#   a1[4] (char*)   pointer to the filepath string of output file
#   a2 (int)        silent mode, if this is 1, you should not print
#                   anything. Otherwise, you should print the
#                   classification and a newline.
# Returns:
#   a0 (int)        Classification
# Exceptions:
#   - If there are an incorrect number of command line args,
#     this function terminates the program with exit code 31
#   - If malloc fails, this function terminates the program with exit code 26
#
# Usage:
#   main.s <M0_PATH> <M1_PATH> <INPUT_PATH> <OUTPUT_PATH>
classify:
	# Read pretrained m0
   
    


	addi sp, sp, -52
    sw ra, 0(sp)
    sw s0, 4(sp)
    sw s1, 8(sp)
    sw s2, 12(sp)
    sw s3, 16(sp)
    sw s4, 20(sp)
    sw s5, 24(sp)
    sw s6, 28(sp)
    sw s7, 32(sp)
    sw s8, 36(sp)
    sw s9, 40(sp)
    sw s10, 44(sp)
    sw s11, 48(sp)
    
    li t0, 5
    bne a0, t0, arg_error
    
    addi sp, sp, -4
    sw a2, 0(sp)   
    mv s0, a1
    
	
    # Read pretrained m0
    li a0, 4
    jal malloc
    mv s1, a0
    beq s1, zero, malloc_error
    beqz s1, malloc_error
    
    li a0, 4
    jal malloc
    mv s2, a0
    beq s2, zero, malloc_error
    beqz s2, malloc_error
    
    lw a0, 4(s0)
    mv a1, s1
    mv a2, s2
    jal read_matrix
    mv s3, a0

	
    # Read pretrained m1
    li a0, 4
    jal malloc
    beqz a0, malloc_error
    mv s4, a0
    beq s4, zero, malloc_error
    beqz s4, malloc_error
    
    li a0, 4
    jal malloc
    beqz a0, malloc_error
    mv s5, a0
    beq s5, zero, malloc_error
    beqz s5, malloc_error
    
    lw a0, 8(s0)
    mv a1, s4
    mv a2, s5
    jal read_matrix
    mv s6, a0

	
    # Read input matrix
    li a0, 4
    jal malloc
    beqz a0, malloc_error
    mv s7, a0
    beq s7, zero, malloc_error
    beqz s7, malloc_error
    
    li a0, 4
    jal malloc
    beqz a0, malloc_error
    mv s8, a0
    beq s8, zero, malloc_error
    beqz s8, malloc_error
    
    lw a0, 12(s0)
    mv a1, s7
    mv a2, s8
    jal read_matrix
    mv s9, a0

	
    # Compute h = matmul(m0, input)
    mul t0, s1, s8
    slli t1, t0, 2
    mv a0, t1
    jal malloc
    mv s10, a0
    beq s10, zero, malloc_error
    beqz s10, malloc_error
    
    mv a0, s3
    lw a1, 0(s1)
    lw a2, 0(s2)
    mv a3, s9
    lw a4, 0(s7)
    lw a5, 0(s8)
    mv a6, s10
    jal matmul

    # Compute h = relu(h)
    mv a0, s10
    lw t0, 0(s1)
    lw t1, 0(s8)
    mul a1, t0, t1
    jal relu

    # Compute o = matmul(m1, h)
    lw t0, 0(s4)
    lw t1, 0(s8)
    mul t2, t0, t1
    slli t3, t2, 2
    mv a0, t3
    jal malloc
    mv s11, a0
    beq s11, zero, malloc_error
    beqz s11, malloc_error
    
    mv a0, s6
    lw a1, 0(s4)
    lw a2, 0(s5)
    mv a3, s10
    lw a4, 0(s1)
    lw a5, 0(s8)
    mv a6, s11
    jal matmul

    # Write output matrix o
    lw a0, 16(s0)
    mv a1, s11
    lw a2, 0(s4)
    lw a3, 0(s8)
    jal write_matrix

    # Compute and return argmax(o)
    mv a0, s11
    lw t0, 0(s4)
    lw t1, 0(s8)
    mul a1, t0, t1
    jal argmax
    mv s10, a0


    # If enabled, print argmax(o) and newline
    lw t0, 0(sp)
    addi sp, sp, 4
    
    li t1, 1
    beq t0, t1, freeing
    mv a0, s10
    jal print_int
    li a0, '\n'
    jal print_char
    
    ebreak  
freeing:
    mv a0, s1
    jal free
    mv a0, s2
    jal free
    mv a0, s3
    jal free
    mv a0, s4
    jal free
    mv a0, s5
    jal free
    mv a0, s6
    jal free
    mv a0, s7
    jal free
    mv a0, s8
    jal free
    mv a0, s9
    jal free  
    mv a0, s11
    jal free
    mv a0, s10


    # Epilogue
    lw ra, 0(sp)
    lw s0, 4(sp)
    lw s1, 8(sp)
    lw s2, 12(sp)
    lw s3, 16(sp)
    lw s4, 20(sp)
    lw s5, 24(sp)
    lw s6, 28(sp)
    lw s7, 32(sp)
    lw s8, 36(sp)
    lw s9, 40(sp)
    lw s10, 44(sp)
    lw s11, 48(sp)
    addi sp, sp, 52
    
	ret

malloc_error:
    li a0, 26
    j exit

arg_error:
    li a0, 31
    j exit
    
fopen_error:
    li a0 27
    j exit
    
fclose_error:
    li a0 28
    j exit
    
fread_error:
    li a0 29
    j exit
