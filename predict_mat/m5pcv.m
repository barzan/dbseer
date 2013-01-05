function [avgMSE, avgRMSE, avgRRMSE, avgR2, avgMAE, avgTime] = ...
    m5pcv(X, Y, trainParams, binCat, k, shuffle, verbose)
% m5pcv
% Tests M5' performance using k-fold Cross-Validation.
%
% Call:
%   [avgMSE, avgRMSE, avgRRMSE, avgR2, avgMAE, avgTime] = ...
%       m5pcv(X, Y, trainParams, binCat, k, shuffle, verbose)
%
% All the arguments, except the first two, of this function are optional.
% Empty values are also accepted (the corresponding default values will be
% used).
%
% Input:
%   X, Y          : Data cases (X(i,:), Y(i)), i = 1,...,n. Missing values
%                   in X must be indicated as NaNs.
%   trainParams   : See function m5pbuild.
%   binCat        : See function m5pbuild.
%   k             : Value of k for k-fold Cross-Validation. The typical
%                   values are 5 or 10. For Leave-One-Out Cross-Validation
%                   set k equal to n. (default value = 10)
%   shuffle       : Whether to shuffle the order of the data cases before
%                   performing Cross-Validation. Note that the random seed
%                   value can be controlled externally before calling
%                   m5pcv. (default value = true)
%   verbose       : Set to false for no verbose. (default value = true)
%
% Output:
%   avgMSE        : Average Mean Squared Error
%   avgRMSE       : Average Root Mean Squared Error
%   avgRRMSE      : Average Relative Root Mean Squared Error
%   avgR2         : Average Coefficient of Determination
%   avgMAE        : Average Mean Absolute Error
%   avgTime       : Average execution time

% =========================================================================
% M5PrimeLab: M5' regression tree and model tree toolbox for Matlab/Octave
% Author: Gints Jekabsons (gints.jekabsons@rtu.lv)
% URL: http://www.cs.rtu.lv/jekabsons/
%
% Copyright (C) 2010  Gints Jekabsons
%
% This program is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% This program is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with this program. If not, see <http://www.gnu.org/licenses/>.
% =========================================================================

% Last update: April 9, 2010

if nargin < 2
    error('Too few input arguments.');
end

if isempty(X) || isempty(Y)
    error('Data is empty.');
end
[n, d] = size(X); % number of data cases and number of input variables
if size(Y,1) ~= n
    error('The number of rows in the matrix and the vector should be equal.');
end
if size(Y,2) ~= 1
    error('The vector Y should have one column.');
end

if nargin < 3
    trainParams = [];
end
if nargin < 4
    binCat = [];
end
if (nargin < 5) || isempty(k)
    k = 10;
end
if k < 2
    error('k should not be smaller than 2.');
end
if k > n
    error('k should not be larger than the number of data cases.');
end
if (nargin < 6) || isempty(shuffle)
    shuffle = true;
end
if (nargin < 7) || isempty(verbose)
    verbose = true;
end

if shuffle
    ind = randperm(n); % shuffle the data
else
    ind = 1 : n;
end

% divide the data into k subsets
minsize = floor(n / k);
remainder = n - minsize * k;
sizes = zeros(k, 1);
for i = 1 : k
    sizes(i) = minsize;
    if remainder > 0
        sizes(i) = sizes(i) + 1;
        remainder = remainder - 1;
    end
end
offsets = ones(k, 1);
for i = 2 : k
    offsets(i) = offsets(i-1) + sizes(i-1);
end

% perform the training and testing k times
MSE = NaN(k,1);
RMSE = NaN(k,1);
RRMSE = NaN(k,1);
R2 = NaN(k,1);
MAE = NaN(k,1);
time = NaN(k,1);
for i = 1 : k
    Xtr = zeros(n-sizes(k-i+1), d);
    Ytr = zeros(n-sizes(k-i+1), 1);
    currsize = 0;
    for j = 1 : k
        if k-i+1 ~= j
            Xtr(currsize+1 : currsize+1+sizes(j)-1, :) = X(ind(offsets(j):offsets(j)+sizes(j)-1), :);
            Ytr(currsize+1 : currsize+1+sizes(j)-1, 1) = Y(ind(offsets(j):offsets(j)+sizes(j)-1), 1);
            currsize = currsize + sizes(j);
        end
    end
    Xtst = X(ind(offsets(k-i+1):offsets(k-i+1)+sizes(k-i+1)-1), :);
    Ytst = Y(ind(offsets(k-i+1):offsets(k-i+1)+sizes(k-i+1)-1), 1);
    if verbose, disp(['Fold #' num2str(i)]); end
    [model, time(i)] = m5pbuild(Xtr, Ytr, trainParams, binCat, verbose);
    [MSE(i), RMSE(i), RRMSE(i), R2(i), MAE(i)] = m5ptest(model, Xtst, Ytst);
end

avgMSE = mean(MSE);
avgRMSE = mean(RMSE);
avgRRMSE = mean(RRMSE);
avgR2 = mean(R2);
avgMAE = mean(MAE);
avgTime = mean(time);
return
