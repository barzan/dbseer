clc;
tripLenInHour = 12;
timeInLA = 18.75;
x = 0;

unit=0.25;
while x<=1
    TimeAtX = mod(timeInLA + 10 *x, 24);
    fprintf(1,'time in LA=%d, progress=%f, timeRightHere=%d, time in Turkey=%d\n', timeInLA, x, TimeAtX, mod(timeInLA+10,24));
    x=x+unit/tripLenInHour;
    timeInLA=mod(timeInLA+unit, 24);
end

