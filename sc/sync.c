#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int main()
{
  setuid( 0 );
  system( "/usr/sbin/ntpdate -b -p 8 vise4" );

  return 0;
}
