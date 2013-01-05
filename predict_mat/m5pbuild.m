function [model time] = m5pbuild(Xtr, Ytr, trainParams, binCat, verbose)
% m5pbuild
% Builds a M5' regression tree or model tree.
%
% Call:
%   [model, time] = m5pbuild(Xtr, Ytr, trainParams, binCat, verbose)
%
% All the arguments, except the first two, of this function are optional.
% Empty values are also accepted (the corresponding default values will be
% used).
%
% Input:
%   Xtr, Ytr      : Training data cases (Xtr(i,:), Ytr(i)), i = 1,...,n.
%                   Missing values in Xtr must be indicated as NaNs.
%   trainParams   : A structure of training parameters for the algorithm.
%                   If not provided, default values will be used (see
%                   function m5pparams for details).
%   binCat        : A vector indicating type of each input variable (should
%                   be of the same length as Xtr second dimension). There
%                   are three possible choices:
%                   0 = continuous variable (any other value < 2 has the
%                   same effect);
%                   2 = binary variable;
%                   3 (or any other value > 2) = categorical variable (the
%                   possible values are detected from the training data;
%                   any new values detected later e.g., in the test data,
%                   will be treated as NaNs).
%                   (default value = vector of all zeroes, meaning that all
%                   the variables by default are treated as continuous)
%   verbose       : Set to false for no verbose. (default value = true)
%
% Output:
%   model         : The built M5' model – a structure with the following
%                   elements:
%     binCat      : Information regarding original (continuous / binary /
%                   categorical) variables, transformed (synthetic binary)
%                   variables, possible values for categorical variables,
%                   and lowest values all the variables. Note that this
%                   binCat does not contain the same information as the
%                   function argument with the same name.
%     trainParams : A structure of training parameters for the algorithm
%                   (the same as in the function input).
%     tree        : A structure defining the built M5' tree.
%   time          : Algorithm execution time (in seconds)

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

% Please give a reference to the webpage in any publication describing
% research performed using the software e.g., like this:
% Jekabsons G. M5PrimeLab: M5' regression tree and model tree toolbox for
% Matlab/Octave, 2010, available at http://www.cs.rtu.lv/jekabsons/

% Last update: September 3, 2010

if nargin < 2
    error('Too few input arguments.');
end

if isempty(Xtr) || isempty(Ytr)
    error('Training data is empty.');
end
[n, m] = size(Xtr); % number of data cases and number of input variables
if size(Ytr,1) ~= n
    error('The number of rows in the matrix and the vector should be equal.');
end
if size(Ytr,2) ~= 1
    error('Ytr should have one column.');
end

if (nargin < 3) || isempty(trainParams)
    trainParams = m5pparams();
else
    trainParams.minNumCases = max(2, trainParams.minNumCases);
end
if (nargin < 4) || isempty(binCat)
    binCat = zeros(1,m);
end
if (nargin < 5) || isempty(verbose)
    verbose = true;
end

binCat(binCat < 2) = 0;
% Correct binCat if some supposedly binary variables have more than 2 unique values
for i = 1 : m
    if binCat(i) == 2
        u = unique(Xtr(:,i));
        u = u(~isnan(u)); % no NaNs
        if length(u) > 2
            disp(['Warning: Variable #' num2str(i) ' has ' num2str(length(u)) ' unique values (not 2).']);
            binCat(i) = 0;
        end
    end
end
% Transform categorical variables into a number of synthetic binary variables
% binCat < 2 for continuous variables
% binCat = 2 for original binary variables
% binCat > 2 for binary variables created from original categorical variables
binCatVals = {};
if any(binCat > 2)
    binCatNew = [];
    binCatCounter = 0;
    Xnew = [];
    for i = 1 : m
        if binCat(i) > 2
            u = unique(Xtr(:,i));
            u = u(~isnan(u)); % no NaNs
            if length(u) > 2
                avg = zeros(length(u),1);
                for j = 1 : length(u)
                    avg(j) = mean(Ytr(Xtr(:,i) == u(j)));
                end
                [dummy ind] = sort(avg);
                Xb = zeros(n,length(u)-1);
                for j = 1 : n
                    if isnan(Xtr(j,i))
                        Xb(j,:) = NaN;
                    else
                        Xb(j, 1 : find(Xtr(j,i) == u(ind)) - 1) = 1;
                    end
                end
                Xnew = [Xnew Xb];
                binCatNew = [binCatNew repmat(length(u),1,length(u)-1)];
                binCatCounter = binCatCounter + 1;
                binCatVals{binCatCounter} = u(ind);
            else
                disp(['Warning: Variable #' num2str(i) ' has only ' num2str(length(u)) ' unique value(s).']);
                Xnew = [Xnew Xtr(:,i)];
                if length(u) == 2
                    binCatNew = [binCatNew 2];
                else
                    binCatNew = [binCatNew 0];
                end
            end
            binCat(i) = length(u);
        else
            Xnew = [Xnew Xtr(:,i)];
            binCatNew = [binCatNew binCat(i)];
        end
    end
    Xtr = Xnew;
    m = size(Xtr,2); % number of variables has changed
    model.binCat.catVals = binCatVals;
else
    binCatNew = binCat;
end

model.binCat.binCat = binCat;
model.binCat.binCatNew = binCatNew >= 2; % 0 for continuous; 1 for binary
if any(model.binCat.binCatNew)
    % this is used only for proper output of the tree when there are binary variables (might be synthetic)
    model.binCat.minVals = min(Xtr);
end

model.trainParams = trainParams;

if verbose
    if trainParams.modelTree, str = 'model'; else str = 'regression'; end
    disp(['Growing M5'' ' str ' tree...']);
end
tic;

sd = std2(Ytr);
numNotMissing = sum(~isnan(Xtr),1); % number of non-missing values for each variable

% For original binary and continuous variables beta = 1
% For synthetic binary variables created from original categorical variables beta < 1
beta = exp(7 * (2 - max(2, binCatNew)) / n);

% Growing the tree
model.tree = splitNode(Xtr, Ytr, m, 1:n, sd, numNotMissing, binCatNew, trainParams, beta);

if verbose && trainParams.prune, disp('Pruning...'); end

% Pruning the tree and filling it with models
model.tree = pruneNode(model.tree, Xtr, Ytr, trainParams);

time = toc;
if verbose
    fprintf('Number of rules in the final tree: %d\n', countRules(model.tree, 0));
    fprintf('Execution time: %0.2f seconds\n', time);
end

return

%=========================  Auxiliary functions  ==========================

function node = splitNode(X, Y, m, caseInd, sd, numNotMissing, binCat, trainParams, beta)
% Splits node into left node and right node
node.caseInd = caseInd;
if (length(caseInd) < trainParams.minNumCases * 2) || ...
   (std(Y(caseInd)) < trainParams.splitThreshold * sd)
    node.type = 'LEAF'; % this node will be a leaf node
else
    sdr = -Inf;
    %splitPoint = 0;
    %attr = 0;
    for i = 1 : m
        if length(unique(Y(caseInd))) > 1
            sorted = unique(sort(X(caseInd,i)));
            sorted = sorted(~isnan(sorted)); % NaNs will not be used for split point determination
            if length(sorted) < 2
                continue;
            end
            splitCandidates = (sorted(1:end-1) + sorted(2:end)) ./ 2;
            for j = 1 : length(splitCandidates)
                sdrtmp = splitsdr(splitCandidates(j), X(caseInd,i), Y(caseInd), ...
                                  numNotMissing(i), binCat(i), trainParams.minNumCases, beta(i));
                if sdrtmp > sdr
                    sdr = sdrtmp;
                    splitPoint = splitCandidates(j);
                    attr = i;
                end
            end
        end
    end
    if sdr <= 0
        node.type = 'LEAF'; % this node will be a leaf node
    else
        [leftInd rightInd] = leftright(splitPoint, X(caseInd,attr), Y(caseInd), binCat);
        leftInd = caseInd(leftInd);
        rightInd = caseInd(rightInd);
        node.type = 'INTERIOR'; % this node will be an interior node
        node.splitAttribute = attr;
        node.splitLocation = splitPoint;
        node.left = splitNode(X, Y, m, leftInd, sd, numNotMissing, binCat, trainParams, beta);
        node.right = splitNode(X, Y, m, rightInd, sd, numNotMissing, binCat, trainParams, beta);
    end
end;
return

function sdr = splitsdr(split, X, Y, numNotMissing, binCat, minNumCases, beta)
% Calculates SDR for the specific split point
[leftInd rightInd] = leftright(split, X, Y, binCat);
if (length(leftInd) < minNumCases) || (length(rightInd) < minNumCases)
    sdr = -Inf;
else
    sdr = numNotMissing / length(Y) * beta *...
          (std2(Y) - (length(leftInd) * std2(Y(leftInd)) + length(rightInd) * std2(Y(rightInd))) / length(Y));
end
return

function stdev = std2(Y)
% Calculates standard deviation
nn = length(Y);
stdev = sqrt(sum((Y - (sum(Y) / nn)) .^ 2) / (nn - 1));
return

function [leftInd rightInd] = leftright(split, X, Y, binCat)
% Splits all data cases into left and right sets. Deals with NaNs separately. 
leftInd = find(X <= split);
rightInd = find(X > split);
% Place data cases with NaNs in left or right according to their Y values
if (length(leftInd) >= 1) && (length(rightInd) >= 1)
    isNaN = isnan(X);
    if any(isNaN)
        if binCat < 2
            % For continuous variables
            sorted = sort(leftInd);
            leftAvg = mean(Y(sorted(end - min([2 length(leftInd)-1]) : end)));
            sorted = sort(rightInd);
            rightAvg = mean(Y(sorted(1 : min([3 length(rightInd)]))));
        else
            % For both original and synthetic binary variables
            leftAvg = mean(Y(leftInd));
            rightAvg = mean(Y(rightInd));
        end
        avgAvg = (leftAvg + rightAvg) / 2;
        smaller = Y(isNaN) <= avgAvg;
        fn = find(isNaN);
        if leftAvg <= rightAvg
            leftInd = [leftInd; fn(smaller)];
            rightInd = [rightInd; fn(~smaller)];
        else
            leftInd = [leftInd; fn(~smaller)];
            rightInd = [rightInd; fn(smaller)];
        end
    end
end
return

function node = pruneNode(nodeFull, X, Y, trainParams)
% Prunes the tree and fills it with models (or average values).
% If tree pruning is disabled, only filling with models is done.
% For each model, subset selection is done (using backward selection).
node = nodeFull;
if ~trainParams.modelTree
    node.value = mean(Y(node.caseInd));
else
    attrInd = unique(attrlist(node));
    XX = X;
    isNaN = isnan(X(node.caseInd,attrInd));
    for i = 1 : length(attrInd)
        % Store average values of the variables (required when the tree is
        % used for prediction and NaN is encountered)
        node.attrAvg(i) = mean(X(node.caseInd(~isNaN(:,i)),attrInd(i)));
        % Replace NaNs by the average values of the corresponding variables
        % of the training data cases reaching the node
        XX(node.caseInd(isNaN(:,i)),attrInd(i)) = node.attrAvg(i);
    end
    % Perform variable selection
    A = [ones(length(node.caseInd),1) XX(node.caseInd,attrInd)];
    node.model.coefs = (A' * A) \ (A' * Y(node.caseInd));
    node.model.attrInd = attrInd;
    if length(attrInd) > 0
        errBest = calcErr(node, XX, Y, trainParams.modelTree);
        attrIndBest = attrInd;
        coefsBest = node.model.coefs;
        changed = false;
        for i = 1 : length(attrInd)
            attrIndOld = node.model.attrInd;
            for i = 1 : length(attrIndOld)
                node.model.attrInd = attrIndOld;
                node.model.attrInd(i) = [];
                A = [ones(length(node.caseInd),1) XX(node.caseInd,node.model.attrInd)];
                node.model.coefs = (A' * A) \ (A' * Y(node.caseInd));
                errTry = calcErr(node, XX, Y, trainParams.modelTree);
                if errTry < errBest
                    attrIndBest = node.model.attrInd;
                    errBest = errTry;
                    coefsBest = node.model.coefs;
                    changed = true;
                end
            end
            node.model.attrInd = attrIndBest;
            node.model.coefs = coefsBest;
            if ~changed
                break;
            end
        end
    end
    % Update node.attrAvg if the used subset of variables has changed
    if length(node.model.attrInd) < length(attrInd)
        for i = 1 : length(node.model.attrInd)
            node.attrAvg(i) = node.attrAvg(attrInd == node.model.attrInd(i));
        end
        node.attrAvg = node.attrAvg(1:length(node.model.attrInd));
    end
end
if strcmp(node.type, 'INTERIOR')
    node.left = pruneNode(node.left, X, Y, trainParams);
    node.right = pruneNode(node.right, X, Y, trainParams);
    if trainParams.prune && ...
       (calcErrSubtree(node, X, Y, trainParams.modelTree) > calcErr(node, X, Y, trainParams.modelTree))
        % this node will be a leaf node
        node.type = 'LEAF';
        node = rmfield(node, 'splitAttribute');
        node = rmfield(node, 'splitLocation');
        node = rmfield(node, 'left');
        node = rmfield(node, 'right');
    else
        % Store average value of the split variable (required when the tree
        % is used for prediction and NaN is encountered)
        notNaN = node.caseInd(~isnan(X(node.caseInd,node.splitAttribute)));
        % splitAttrAvg will be used for splitting in prediction process
        % when NaN is encountered
        node.splitAttrAvg = mean(X(notNaN,node.splitAttribute));
    end
end
return

function list = attrlist(node)
% Returns full list of variables used in the subtree
if strcmp(node.type, 'INTERIOR')
    list = node.splitAttribute;
    list = [list attrlist(node.left)];
    list = [list attrlist(node.right)];
else
    list = [];
end
return

function err = calcErrSubtree(node, X, Y, modelTree)
% Calculates error of the subtree
if strcmp(node.type, 'INTERIOR')
    err = (length(node.left.caseInd) * calcErrSubtree(node.left, X, Y, modelTree) + ...
           length(node.right.caseInd) * calcErrSubtree(node.right, X, Y, modelTree)) / ...
           length(node.caseInd);
else
    err = calcErr(node, X, Y, modelTree);
end
return

function err = calcErr(node, X, Y, modelTree)
% Calculates error of the node
nn = length(node.caseInd);
if ~modelTree
    v = 1;
    absdev = sum(abs(node.value - Y(node.caseInd)));
else
    % Replace NaNs by the average values of the corresponding variables of
    % the training data cases reaching the node
    isNaN = isnan(X(node.caseInd,node.model.attrInd));
    for i = 1 : length(node.model.attrInd)
        X(node.caseInd(isNaN(:,i)),node.model.attrInd(i)) = node.attrAvg(i);
    end
    % Calculate the error
    v = length(node.model.attrInd) + 1;
    val = [ones(length(node.caseInd),1) X(node.caseInd,node.model.attrInd)] * node.model.coefs;
    absdev = sum(abs(val - Y(node.caseInd)));
end
err = (nn + v) / (nn - v) * absdev / nn;
return

function numRules = countRules(node, numR)
% Counts all the rules (equal to the number of leaf nodes) in the tree
if strcmp(node.type, 'INTERIOR')
    numRules = countRules(node.left, numR);
    numRules = countRules(node.right, numRules);
else
    numRules = numR + 1;
end
return
