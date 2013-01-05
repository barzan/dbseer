function result = align(columns, coefFileDev, valFileCut, alignFile1, alignFile2)

A = load(coefFileDev);
B = load(valFileCut);

X = sum(A(:,1:columns)');
Y = B(:,1:1)+B(:,2:2);

X1=xcorr(X-mean(X),Y-mean(Y));
%[m,d]=max(X1);
%delay=d-max(length(X),length(Y));
mm = max(length(X),length(Y));
reasonable = X1(max(1,mm-120):mm);
[maxCor,d]=max(reasonable);
d = d + mm - length(reasonable);
elmOfBonA1 = mm+1-d;
elmOfBonA1

commonLen = min(length(X)-1, length(Y)-elmOfBonA1); 

dlmwrite(alignFile1, A(1:1+commonLen,:), 'delimiter',',');
dlmwrite(alignFile2, B(elmOfBonA1:elmOfBonA1+commonLen,:), 'delimiter',',');

result = 1
