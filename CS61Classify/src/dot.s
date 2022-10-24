.globl dot

.text
# =======================================================
# FUNCTION: Dot product of 2 int arrays
# Arguments:
#   a0 (int*) is the pointer to the start of arr0
#   a1 (int*) is the pointer to the start of arr1
#   a2 (int)  is the number of elements to use
#   a3 (int)  is the stride of arr0
#   a4 (int)  is the stride of arr1
# Returns:
#   a0 (int)  is the dot product of arr0 and arr1
# Exceptions:
#   - If the length of the array is less than 1,
#     this function terminates the program with error code 36
#   - If the stride of either array is less than 1,
#     this function terminates the program with error code 37
# =======================================================
dot:

	# Prologue
        addi sp, sp, -20
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)
    sw s3, 12(sp)
    sw s4, 16(sp)

    li t0, 1
    blt a2, t0, exception1
    blt a3, t0, exception2
    blt a4, t0, exception2

loop_start:
    mv s0, a0
    mv s1, a1
    mv s2, a2
    mv s3, a3
    mv s4, a4
    li t0, 0   # set t0 as counter
    li t1, 0   # set t1 as the index of array0
    li t2, 0   # set t2 as the index of array1
    li t3, 0   # set t3 as summation

loop_continue:
    slli t4, t1, 2
    add t4, t4, s0    # load the t1 th element of array0
    lw t4, 0(t4)
    
    slli t5, t2, 2
    add t5, t5, s1    # load the t2 th element of array1
    lw t5, 0(t5)
    
    mul t5, t4, t5
    add t3, t3, t5     # do production and accumulation
    
    add t1, t1, s3     # increase t1 by the stride of array0
    add t2, t2, s4     # increase t2 by the stride of array1
    addi t0, t0, 1
    bne t0, s2 loop_continue

loop_end:
    mv a0, t3

	# Epilogue
    lw s0, 0(sp)
    lw s1, 4(sp)
    lw s2, 8(sp)
    lw s3, 12(sp)
    lw s4, 16(sp)
    addi sp, sp, 20

	ret

exception1:
    li a0, 36
    j exit

exception2:
    li a0, 37
    j exit