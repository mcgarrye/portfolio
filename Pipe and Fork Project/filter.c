#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include "filter.h"

int filter(int m, int read_fd, int write_fd){
	int num; int rerror;
	//while the read function doesnt read EOF or return an error -1, read from the pipe
	while((rerror = read(read_fd, &num, sizeof(int))) > 0){		
		// if num is not divisible by m, write it into the next pipe
		if (num % m != 0){
			int werror = write(write_fd, &num, sizeof(int));
			//check the reading end is open
			if (werror == 0){
				fprintf(stderr, "Reading end is closed for the filter\n");
				return 1;
			}
			//check for error
			else if(werror == -1){
				perror("write");
				return 1;
			}
		}
	} 
	return 0;
}