// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
(keyboard)
@24576 // keyboard register
D = M // keyboard register memory set to D
@white // JMP location to (white)
D; JEQ // JMP condition if D = 0
@black // JMP location to (black)
D; JGT // JMP condition if D > 0
@keyboard
0; JMP

(black)
@24575 // @screen's last pixel
D = M // set register memory to D
@white // JMP to white loop
D; JLT // it goes to white if last pixel is filled, in order to not exceed the register amount
@i // Register i to increment and add in order to blacken entire sscreen
D = M // i memory set to D
@16384 // @screen's first pixel
D = A + D // Register the new D as @screen's first pixel + the incrementing i value
A = D // new register location
M = -1 // set memory of register to -1 making the entire word (11...1) : all black
@i
M = M + 1 // increment 1 for next iteration
@keyboard
0; JMP // back to the start

(white)
@i // Register i to decrement and add in order to whiten entire screen
D = M // i memory set to D
@16384 // @screen's first pixel
D = A + D // Register the new D as @screen's first pixel + the incrementing i value
A = D // new register location
M = 0 // set memory of register to 0 making the entire word (00...0) : all white
@i
M = M - 1 // decrement 1 for next iteration in order to rewhiten blackened pixels
@keyboard
0; JMP // back to the start
