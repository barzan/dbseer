function sched( )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here

n=1;
t = [6 2.3];
W = zeros(1,2);
A = zeros(99,2);
for i=1:99
    r = [i 100-i] / 100.0;
    L = dot(t,r);
    W(1)=0.5 * r(1) * n * L;
    W(2)=(r(1)*n + 0.5*r(2)*n) * L;
    A(i,:)=dot(r,W) + t;
end
plot(A);
end

