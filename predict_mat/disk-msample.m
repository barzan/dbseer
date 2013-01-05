A = load('coefsXYZ.ready');
B = load('valXYZ.ready');

addpath('/home/barzan/scripts/');

smoothinwindow = 5;
A = winsum(A,smoothinwindow);
B = winavg(B,smoothinwindow);

%A =[A ones(size(A,1),1)];

numOfEquations = 50;

s=1:size(B,1);

for i=1:floor(size(B,1)/numOfEquations),
%s = randperm(length(B));
  r = s((i-1)*numOfEquations+1:i*numOfEquations); %100 could be at little as 5
  X = linsolve([A(r,:) sum(B(r,2:3)')'],100-B(r,1:1));
  if (i==1),
    unknowns = X';
  else
    unknowns = [unknowns; X'];
  end
end


dlmwrite('disk-means.dat', mean(unknowns), 'delimiter',',','-append');
dlmwrite('disk-variances.dat', var(unknowns), 'delimiter',',','-append');


