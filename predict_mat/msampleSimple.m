function msampleSimple(columns, coefsFileReady, valFileReady);
A = load(coefsFileReady);
B = load(valFileReady);

addpath('/home/barzan/scripts/');

X = linsolve(A(:,1:columns),B(:,1:1)+B(:,2:2));


dlmwrite('means.dat', mean(X), 'delimiter',',','-append');
dlmwrite('variances.dat', var(X), 'delimiter',',','-append');

err = mean(abs(A(:,1:columns)*X - B(:,1:1)-B(:,2:2)))
err2 = mean(A(:,1:columns)*X - B(:,1:1)-B(:,2:2))


