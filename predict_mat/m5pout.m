function m5pout(model, showNumCases, precision)
% m5pout
% Outputs the built M5' tree in a user-readable way.
%
% Call:
%   m5pout(model, showNumCases, precision)
%
% Input:
%   model         : M5' model
%   showNumCases  : Whether to show the number of training data cases
%                   corresponding to each leaf.
%   precision     : Number of digits in the model coefficients, split
%                   values etc.
%
% Remarks:
% 1. If the training data contained categorical variables, the
% corresponding synthetic variables will be shown.
% 2. The outputted tree will not reflect smoothing. Smoothing is performed
% only while predicting output values (in m5ppredict function).

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

if nargin < 1
    error('Too few input arguments.');
end
if (nargin < 2) || isempty(showNumCases)
    showNumCases = true;
end
if (nargin < 3) || isempty(precision)
    precision = 15;
end

% Show synthetic variables
if any(model.binCat.binCat > 2)
    disp('Synthetic variables:');
    indCounter = 0;
    binCatCounter = 0;
    for i = 1 : length(model.binCat.binCat)
        if model.binCat.binCat(i) > 2
            binCatCounter = binCatCounter + 1;
            for j = 1 : length(model.binCat.catVals{binCatCounter})-1
                indCounter = indCounter + 1;
                str = num2str(model.binCat.catVals{binCatCounter}(j+1:end)', [' %.' num2str(precision) 'g,']);
                disp(['z' num2str(indCounter) ' = 1 if x' num2str(i) ' is in {' str(1:end-1) '} else = 0']);
            end
        else
            indCounter = indCounter + 1;
            disp(['z' num2str(indCounter) ' = x' num2str(i)]);
        end
    end
    zx = 'z';
else
    zx = 'x';
end

if model.trainParams.smoothing
    disp('Warning: The tree does not reflect smoothing.');
end
disp('The tree:');
if isfield(model.binCat, 'minVals')
    minVals = model.binCat.minVals;
else
    minVals = [];
end
numRules = output(model.tree, model.trainParams.modelTree, model.binCat.binCatNew, ...
                  minVals, 0, 0, zx, showNumCases, precision);
disp(['Number of rules in the tree: ' num2str(numRules)]);
return

function numRules = output(node, modelTree, binCatNew, minVals, offset, numR, zx, showNumCases, precision)
p = ['%.' num2str(precision) 'g'];
if strcmp(node.type, 'INTERIOR')
    if binCatNew(node.splitAttribute) % a binary variable (might be synthetic)
        disp([repmat(' ',1,offset) 'if ' zx num2str(node.splitAttribute) ' = ' num2str(minVals(node.splitAttribute),p)]);
    else % a continuous variable
        disp([repmat(' ',1,offset) 'if ' zx num2str(node.splitAttribute) ' <= ' num2str(node.splitLocation)]);
    end
    numRules = output(node.left, modelTree, binCatNew, minVals, offset + 1, numR, zx, showNumCases, precision);
    disp([repmat(' ',1,offset) 'else']);
    numRules = output(node.right, modelTree, binCatNew, minVals, offset + 1, numRules, zx, showNumCases, precision);
    %disp([repmat(' ',1,offset) 'end']);
else
    if ~modelTree
        str = [repmat(' ',1,offset) 'y = ' num2str(node.value,p)];
    else
        % show regression model
        str = [repmat(' ',1,offset) 'y = ' num2str(node.model.coefs(1),p)];
        for i = 1 : length(node.model.attrInd)
            if node.model.coefs(i+1) >= 0
                str = [str ' +'];
            else
                str = [str ' '];
            end
            str = [str num2str(node.model.coefs(i+1),p) '*' zx num2str(node.model.attrInd(i))];
        end
    end
    if showNumCases
        str = [str ' (' num2str(length(node.caseInd)) ')'];
    end
    disp(str);
    numRules = numR + 1;
end
return
