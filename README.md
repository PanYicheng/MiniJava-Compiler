MiniJava Compiler
=================

A perfect MiniJava compiler, convert MiniJava codes to Piglet, SPiglet, Kanga and MIPS assembly.<br/>
It includes five stages of work:<br/>
- Check the errors of codes.
- Convert MiniJava to Piglet: Transform an Object-Oriented language to a Procedure-Oriented language.
- Convert Piglet to SPiglet: Removed all nested expressions.
- Convert SPiglet to Kanga:
1. Anaylze all reachable definitions in the intermediate codes
2. Transform the codes into SSA (Single-Static Assignment) Form
3. Anaylze the liveness of all variables and allocate registers
- Convert Kanga to MIPS assembly

This program passed all five automatica tests perfectly and got a score of 99 in the end of the semester.

