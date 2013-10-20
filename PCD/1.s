.data
.align 0
endl: .asciiz "\n"

.data
.align 0
error: .asciiz "ERROR: abnormal termination\n"

.text
.globl main
main:
	sw $ra, -4($sp)
	move $fp, $sp
	subu $sp, $sp, 16
	li $a0, 4
	li $v0, 9
	syscall
	move $t8, $v0
	li $a0, 4
	li $v0, 9
	syscall
	move $t3, $v0
	la $t6, Fac_ComputeFac
	sw $t6, 0($t8)
	sw $t8, 0($t3)
	move $t8, $t3
	li $t3, 0
	slt $t6, $t3, $t8
	beqz $t6, L1
	lw $t3, 0($t8)
	lw $t6, 0($t3)
	j L0
L1:	la $a0, error
	li $v0, 4
	syscall
	li $v0, 10
	syscall
L0:	li $t3, 10
	move $a0, $t8
	move $a1, $t3
	sw $t3, 8($sp)
	sw $t8, 4($sp)
	sw $t6, 0($sp)
	jalr $t6
	lw $t3, 8($sp)
	lw $t8, 4($sp)
	lw $t6, 0($sp)
	move $t3, $v0
	move $a0, $t3
	li $v0, 1
	syscall
	la $a0, endl
	li $v0, 4
	syscall
	addu $sp, $sp, 16
	lw $ra, -4($sp)
	j $ra

.text
Fac_ComputeFac:
	sw $fp, -8($sp)
	sw $ra, -4($sp)
	move $fp, $sp
	subu $sp, $sp, 28
	sw $s2, 0($sp)
	sw $s4, 4($sp)
	move $t6, $a0
	move $s2, $a1
	li $v0, 1
	slt $s4, $s2, $v0
	beqz $s4, L4
	li $s4, 1
	j L3
L4:	li $t9, 0
	slt $t3, $t9, $t6
	beqz $t3, L2
	lw $t9, 0($t6)
	lw $t3, 0($t9)
	j L5
L2:	la $a0, error
	li $v0, 4
	syscall
	li $v0, 10
	syscall
L5:	li $v0, 1
	sub $t9, $s2, $v0
	move $a0, $t6
	move $a1, $t9
	sw $t3, 8($sp)
	sw $t9, 12($sp)
	sw $t6, 16($sp)
	jalr $t3
	lw $t3, 8($sp)
	lw $t9, 12($sp)
	lw $t6, 16($sp)
	move $t6, $v0
	mul $t9, $s2, $t6
	move $s4, $t9
L3:	move $v0, $s4
	lw $s4, 4($sp)
	lw $s2, 0($sp)
	addu $sp, $sp, 28
	lw $fp, -8($sp)
	lw $ra, -4($sp)
	j $ra
