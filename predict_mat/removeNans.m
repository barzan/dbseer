function [ newmatrix ] = removeNans(matrix, column )
%UNTITLED6 Summary of this function goes here
%   Detailed explanation goes here

newmatrix = matrix(find(~isnan(matrix(:,column))),:);

end

