.globl matmul

.text
# =======================================================
# FUNCTION: Matrix Multiplication of 2 integer matrices
#   d = matmul(m0, m1)
# Arguments:
#   a0 (int*)  is the pointer to the start of m0
#   a1 (int)   is the # of rows (height) of m0
#   a2 (int)   is the # of columns (width) of m0
#   a3 (int*)  is the pointer to the start of m1
#   a4 (int)   is the # of rows (height) of m1
#   a5 (int)   is the # of columns (width) of m1
#   a6 (int*)  is the pointer to the the start of d
# Returns:
#   None (void), sets d = matmul(m0, m1)
# Exceptions:
#   Make sure to check in top to bottom order!
#   - If the dimensions of m0 do not make sense,
#     this function terminates the program with exit code 38
#   - If the dimensions of m1 do not make sense,
#     this function terminates the program with exit code 38
#   - If the dimensions of m0 and m1 don't match,
#     this function terminates the program with exit code 38
# =======================================================
matmul:

	# Error checks
	addi t0, x0, 1 #t0=1
    blt a1, t0, exception
    blt a2, t0, exception
    blt a4, t0, exception
    blt a5, t0, exception
    bne a2, a4, exception
	# Prologue
    addi sp, sp, -40
    sw ra, 36(sp)
    sw s8, 32(sp)
    sw s7, 28(sp)
    sw s6, 24(sp)
    sw s5, 20(sp)
    sw s4, 16(sp)
    sw s3, 12(sp)
    sw s2, 8(sp)
    sw s1, 4(sp)
    sw s0, 0(sp)
    

    add s0, a0, x0
    add s1, a1, x0
    add s2, a2, x0 
    add s3, a3, x0
    add s4, a4, x0
    add s5, a5, x0 
    add s6, a6, x0
    add a1, s3, x0
    addi a3, x0, 1
    add a4, s5, x0
    add s7, x0, x0 
    
    
    
outer_loop_start:
    beq s7, s1, outer_loop_end
    add s8, x0, x0
    addi t3, x0, 4
    mul t3, t3, s2
    mul t3, t3, s7
    add a0, s0, t3
    j inner_loop_start


inner_loop_start:
    beq s8, s5, inner_loop_end
    jal dot
    sw a0, 0(s6)
    addi t3, x0, 4
    mul t3, t3, s2
    mul t3, t3, s7
    add a0, s0, t3
    add a2, s2, x0
    addi a3, x0, 1
    add a4, s5, x0
    addi s6, s6, 4
    addi s8, s8, 1
    addi t3, x0, 4
    mul t3, t3, s8
    add a1, s3, t3 
    j inner_loop_start
 
   
    

inner_loop_end:
    add a1, s3, x0 
    addi s7, s7, 1
    j outer_loop_start


outer_loop_end:
	# Epilogue
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    lw s5, 20(sp)
    lw s6, 24(sp)
    lw s7, 28(sp)
    lw s8, 32(sp)
    lw ra, 36(sp)
    addi sp, sp, 40
	ret
    
exception:
    li a0 38
    j exit
