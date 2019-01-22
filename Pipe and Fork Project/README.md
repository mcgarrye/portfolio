#Pipe and Fork Project
This project utilizes C to create a program that is able to factor products of two primes into those exact primes. To be exact,
the program pfact is given a number, if it is a product of two prime numbers it returns that along with the two numbers,
if it is a prime number it returns that it is, or it informs the user that it is another composite. The program takes advantage
of the C programming language, using Makefiles, pipes and forking. Simply calling "make" in this directory will allow you to
then call ./pfact {number}. The program runs very quickly for numbers with 6 digits or less, after this it begins to slow down.
The requirements for this project were to run quickly on up to 5 digits. This project was created exclusively by me.
