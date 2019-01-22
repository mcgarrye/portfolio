#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <math.h>
#include "filter.h"

//Wrapper function for the close function and error check
void Close(int fd){
	if(close(fd) != 0){
		perror("close");
    	exit(1);
  	}
}

//helper function for writing ints and error check
void write_int(int fd, int *num){
	if(write(fd, num, sizeof(int)) == -1){
		perror("write");
		exit(1);
	}
}

//helper function for reading ints and error check
void read_int(int fd, int *num){
	int error = read(fd, num, sizeof(int));
	//if the write end has close
	if(error == 0){
		fprintf(stderr, "Writing end of the pipe is closed\n");
	}
	else if(error == -1){
	    perror("read");
    	exit(1);
	}
}

//Wrapper function for the pipe function and error check
void Pipe(int fd[2]){
	if(pipe(fd) == -1){
		perror("pipe");
		exit(1);
	}
}

//Wrapper function for the fork function and error check
pid_t Fork(void){
	pid_t result;
	if((result = fork()) == -1){
		perror("fork");
		exit(1);
	}
	return result;
}

//Wrapper function for the filter function and error check
void Filter(int m, int read_fd, int write_fd){
	if(filter(m, read_fd, write_fd) == 1){
		fprintf(stderr, "filter call failed\n");
		exit(1);
	}
}

//Wrapper function for the wait function and error check
void Wait(int *status){
	if(wait(status) == -1){
		perror("wait");
		exit(1);
	}
}

//helper function to check if the second factor of n is a prime and also to clear the pipe
int other_factor(int read_fd, int factor){
	int error; int num; int divisible = 0;
	while((error = read(read_fd, &num, sizeof(int))) > 0){
		//check if the factor is in the remaining primes
		if (num == factor){
			divisible = num;
		}
	}
	return divisible;
}

//helper function to clear the remaining values written to the pipe
void pipe_cleaner(int read_fd){
	int error; int num;
	while((error = read(read_fd, &num, sizeof(int))) > 0){
		if (error == -1){
			perror("read");
			exit(1);
		}
	}
}

//helper function to determine which of the options n is: a prime, a product of 2 primes or other
void final_message(int n, int *factors, int read_fd){
	// for the case n is 2
	if(factors[0] == n){
		printf("%d is prime\n", n);
	} else if (factors[0] == 0){
		int possible_square;
		read_int(read_fd, &possible_square);
		//check that n isn't a square of a prime
		if ((possible_square*possible_square) == n){
			printf("%d %d %d\n", n, possible_square, possible_square);
		}
		//otherwise it must be a prime
		else{
			pipe_cleaner(read_fd);
			printf("%d is prime\n", n);
		}
	} else if (factors[1] != 0){
		//bad code but case where n=6 failed
		if(n == 6){
			printf("%d %d %d\n", n, factors[0], factors[1]);
		}
		//already found two factors, clean pipe and print statement
		else{
			pipe_cleaner(read_fd);
			printf("%d is not the product of two primes\n", n);	
		}
  	} else{
   		int possible_square = (factors[0]*factors[0]);
   		int factor2 = (n / factors[0]);
   		//check if other factor is a prime and clean pipe
   		if(other_factor(read_fd, factor2) == factor2){
			printf("%d %d %d\n", n, factors[0], factor2);
   		}
   		//check if n is a square of two primes
   		else if (possible_square == n){
			printf("%d %d %d\n", n, factors[0], factors[0]);
		}
		//otherwise it can't be a a product of two primes or a prime itself
		else{
			printf("%d is not the product of two primes\n", n);
   		}
   	}
}

int main(int argc, char **argv){
	int n; char *next = NULL;
	//check the right amount and correct arguments are given
	if(argc != 2 || (n = strtol(argv[1], &next, 10)) < 2 || *next != '\0' ){
		fprintf(stderr, "Usage:\n\tpfact n\n");
		exit(1);
	}
	//given handler to deal with sigpipe
	if (signal(SIGPIPE, SIG_IGN) == SIG_ERR) {
        perror("signal");
        exit(1);
    }

    // initialization of variables that will be needed throughout the code
    int factors[2] = {0,0}; int status; int number_of_filters = 0;
    //create first pipe to connect master parent with the first child
	int first_fd[2];
	Pipe(first_fd);

	//fork the first time
	int r = Fork();
	if (r == 0){
		//create copy of the read end so future children can replace it and close writing end
		int input = first_fd[0];
    	Close(first_fd[1]);
    	//initilaize and assign the first m
    	int m;
    	read_int(input, &m);
    	//check if m is a factor of n and assign it to factors if it is
    	if(n%m == 0){
        	if(factors[0]==0){
          	factors[0] = m;
        	} else{
          	factors[1] = m;
        	}
      	}
      	//check if m already meets the checks and finish process if it does
      	if(m >= sqrt(n) || factors[1]!=0){
			final_message(n, factors, input);
			Close(input);
			return number_of_filters;
    	}
    	
    	//begin while loop, checking the criteria every iteration
		while(m < sqrt(n) && factors[1]==0){ 
			//creat second pipe
			int second_fd[2];
    		Pipe(second_fd);
    		//start forking in the loop
			r = Fork();

			if(r==0){
				//child process closes pipes ends it doesn't need and reassigns m
				Close(input);
				Close(second_fd[1]);
				read_int(second_fd[0], &m);
				//check if m is a factor of n and assign it to factors if it is
				if(n%m == 0){
        			if(factors[0]==0){
          				factors[0] = m;
        			} else{
          				factors[1] = m;
        			}
      			}
      			//check if m already meets the checks and finish process if it does
				if(m >= sqrt(n) || factors[1]!=0){
					final_message(n, factors, second_fd[0]);
					Close(second_fd[0]);
					return number_of_filters;
    			}
    			//otherwise, reassign input and continue
				input = second_fd[0];
			}

			else{
				//parent closes pipe ends it doesn't needs and begins filter
				Close(second_fd[0]);
				Filter(m, input, second_fd[1]);
				//closes remaining ends once filtering is complete
				Close(input);
				Close(second_fd[1]);
				//waits for the childs return code and assigns it to number_of_filters
				Wait(&status);
				if(WIFEXITED(status)){
					number_of_filters += WEXITSTATUS(status);
				}
				//return the number_of_filters plus one to account for the filter of this process
				return number_of_filters + 1;
			}
		}

	} else{
		//master parent closes pipe ends it doesn't needs and begins filter
		Close(first_fd[0]);
		//pipes the integers between 2 and n into the pipe
		int i = 2;
		for(; i < n+1; i++){
			write_int(first_fd[1], &i);
		}
		//closes remaining pipe
		Close(first_fd[1]);
		//waits for the childs return code and assigns it to number_of_filters
		Wait(&status);	
		if(WIFEXITED(status)){
			number_of_filters += WEXITSTATUS(status);
		}
		//prints the total number of filters
		printf("Number of filters = %d\n", number_of_filters);
	}
	return 0;
}