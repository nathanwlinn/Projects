.globl relu

.text
# ==============================================================================
# FUNCTION: Performs an inplace element-wise ReLU on an array of ints
# Arguments:
#   a0 (int*) is the pointer to the array
#   a1 (int)  is the # of elements in the array
# Returns:
#   None
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
# ==============================================================================
relu:
	# Prologue
    li t5, 1
    blt a1, t5, exit_command
    add t0, x0, x0
    addi t1, x0, 4
    


loop_start:
	beq t0, a1, loop_end
    mul t2, t0, t1
    add t2, t2, a0
    lw t3, 0(t2)
    bgt t3, x0, loop_continue
    sub t3, t3, t3
    sw t3, 0(t2)






loop_continue:
	addi t0, t0, 1
    j loop_start


loop_end:


	# Epilogue


	ret
exit_command:
	li a0, 36
    j exit
