function Yq = m5ppredict(model, Xq)
% m5ppredict
% Predicts output values for the given query points Xq using an M5' model.
%
% Call:
%   Yq = m5ppredict(model, Xq)
%
% Input:
%   model         : M5' model
%   Xq            : Inputs of query data points (Xq(i,:)), i = 1,...,nq.
%                   Missing values in Xq must be indicated as NaNs.
%
% Output:
%   Yq            : Predicted outputs of the query data points (Yq(i)),
%                   i = 1,...,nq
%
% Remarks:
% 1. If the data contains categorical variables, they are transformed in a
% number of synthetic binary variables (exactly the same way as m5pbuild
% does).
% 2. Every previously unseen value of a categorical variable is treated as
% NaN.

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
[nq m] = size(Xq);
Yq = zeros(nq,1);

% Transform all the categorical variables to binary ones (exactly the same way as with training data)
if any(model.binCat.binCat > 2)
    binCatCounter = 0;
    Xnew = [];
    for i = 1 : m
        if model.binCat.binCat(i) > 2
            binCatCounter = binCatCounter + 1;
            u = unique(Xq(:,i));
            u = u(~isnan(u)); % no NaNs
            if length(setdiff(u, model.binCat.catVals{binCatCounter})) > 0
                disp(['Warning: Categorical variable #' num2str(i) ...
                      ' has one or more previously unseen values. Treating as NaNs.']);
            end
            Xb = zeros(nq,length(model.binCat.catVals{binCatCounter})-1);
            for j = 1 : nq
                if isnan(Xq(j,i))
                    Xb(j,:) = NaN;
                else
                    k = find(Xq(j,i) == model.binCat.catVals{binCatCounter});
                    if length(k) > 0
                        Xb(j,1:k-1) = 1;
                    else
                        Xb(j,:) = NaN; % treating as NaN
                    end
                end
            end
            Xnew = [Xnew Xb];
        else
            Xnew = [Xnew Xq(:,i)];
        end
    end
    Xq = Xnew;
end

for i = 1 : nq
    Yq(i) = predict(model.tree, Xq(i,:), model.trainParams.modelTree, ...
                    model.trainParams.smoothing, model.trainParams.smoothing_k);
end
return

function Yq = predict(node, Xq, modelTree, smoothing, smoothing_k)
if strcmp(node.type, 'INTERIOR')
    % Replace NaNs by the average values of the corresponding
    % variables of the training data cases reaching the node
    if (isnan(Xq(node.splitAttribute)) && (node.splitAttrAvg <= node.splitLocation)) || ...
       (Xq(node.splitAttribute) <= node.splitLocation)
        Yq = predict(node.left, Xq, modelTree, smoothing, smoothing_k);
        if smoothing
            s_n = length(node.left.caseInd);
        end
    else
        Yq = predict(node.right, Xq, modelTree, smoothing, smoothing_k);
        if smoothing
            s_n = length(node.right.caseInd);
        end
    end
    if smoothing
        if ~modelTree
            s_q = node.value;
        else
            if length(node.model.attrInd) > 0
                % Replace NaNs by the average values of the corresponding
                % variables of the training data cases reaching the node
                isNaN = isnan(Xq(node.model.attrInd));
                Xq(node.model.attrInd(isNaN)) = node.attrAvg(isNaN);
                % Calculate prediction
                s_q = [1 Xq(node.model.attrInd)] * node.model.coefs;
            else
                s_q = node.model.coefs;
            end
        end
        Yq = (s_n * Yq + smoothing_k * s_q) / (s_n + smoothing_k);
    end
else
    if ~modelTree
        Yq = node.value;
    else
        if length(node.model.attrInd) > 0
            % Replace NaNs by the average values of the corresponding
            % variables of the training data cases reaching the node
            isNaN = isnan(Xq(node.model.attrInd));
            Xq(node.model.attrInd(isNaN)) = node.attrAvg(isNaN);
            % Calculate prediction
            Yq = [1 Xq(node.model.attrInd)] * node.model.coefs;
        else
            Yq = node.model.coefs;
        end
    end
end
return
