// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Put your code here.
@2
M=0 // intitialise R2

//the idea is to add R0 to itself R1 times and input it to R2
(loop) // begin loop

@1 // verifying if R1 >= 0
D = M
@end //if not go to end
D; JLE

@0
D = M
@2
M = M + D// add R0 to R2

@1
M = M-1 // decrement R1 until it reaches 0
@loop
0; JMP


(end)
@end
0; JMP //infinite loop



