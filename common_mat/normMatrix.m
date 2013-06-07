function [ normed ] = normMatrix( matrix )

maxAll = repmat(max(matrix), [size(matrix,1) 1]);
minAll = repmat(min(matrix), [size(matrix,1) 1]);
normed = (matrix-minAll)./ (maxAll-minAll);

normed(isnan(normed))=0;
end

