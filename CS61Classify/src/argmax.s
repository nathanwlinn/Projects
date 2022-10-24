.globl argmax

.text
# =================================================================
# FUNCTION: Given a int array, return the index of the largest
#   element. If there are multiple, return the one
#   with the smallest index.
# Arguments:
#   a0 (int*) is the pointer to the start of the array
#   a1 (int)  is the # of elements in the array
# Returns:
#   a0 (int)  is the first index of the largest element
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
# =================================================================
argmax:
	# Prologue
    addi sp, sp, -12
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    addi t0, x0, 1
    add s0, a0, x0
    add t1, x0, x0
    add t0, x0, x0
    add s1, x0, x0
    bgt a1, zero, loop_start
    li a0, 36
    j exit
    
loop_start:
	beq t0, a1, loop_end
    lw t2, 0(s0)
    blt t1, t2, loop_continue
    addi t0, t0, 1
    addi s0, s0, 4
    j loop_start
    

loop_continue:
	add t1, t2, x0
    add s1, t0, x0
    addi t0, t0, 1
    addi s0, s0, 4
    j loop_start

loop_end:
	# Epilogue
    add a0, s1, x0
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    addi sp, sp, 12
	ret
exit_command:
    li a0 36
    j exit
