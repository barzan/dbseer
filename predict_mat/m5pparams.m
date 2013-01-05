function trainParams = m5pparams(modelTree, minNumCases, prune, ...
    smoothing, smoothing_k, splitThreshold)
% m5pparams
% Creates a structure of M5' configuration parameter values for further
% use with m5pbuild or m5pcv functions.
%
% Call:
%   trainParams = m5pparams(modelTree, minNumCases, prune, ...
%                           smoothing, smoothing_k, splitThreshold)
%
% All the arguments of this function are optional. Empty values are also
% accepted (the corresponding default values will be used).
% 
% Input:
% For most applications, it can be expected that the most attention should
% be paid to the following parameters: modelTree, smoothing, and maybe
% smoothing_k or minNumCases.
%   modelTree     : Whether to build a model tree (true) or a regression
%                   tree (false). (default value = true)
%   minNumCases   : The minimum number of training data cases one node may
%                   represent. Values lower than 2 are not allowed.
%                   (default value = 4)
%   prune         : Whether to prune the tree. (default value = true)
%   smoothing     : Whether to perform smoothing when the tree is used for
%                   prediction (in m5ppredict). (default value = false)
%   smoothing_k   : This is a kind of smoothing coefficient for the
%                   smoothing process. For larger values, more smoothing is
%                   applied. For large (relatively to the number of
%                   training data cases) values, the tree will essentially
%                   behave like containing just one leaf (corresponding to
%                   the root node). For value 0, no smoothing is applied.
%                   (default value = 15 [Wang & Witten, 1997])
%   splitThreshold : A node is not splitted if the standard deviation of
%                   the output variable values at the node is less than
%                   splitThreshold of the standard deviation of the output
%                   variable values of the entire original data set.
%                   (default value = 0.05 [Wang & Witten, 1997]) The
%                   results are usually not very sensitive to the exact
%                   choice of the threshold [Wang & Witten, 1997].
%
% Output:
%   trainParams   : A structure of training parameters for m5pbuild
%                   function containing the provided values of the
%                   parameters (or default ones, if not provided).

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

if (nargin < 1) || isempty(modelTree)
    trainParams.modelTree = true;
else
    trainParams.modelTree = modelTree;
end

if (nargin < 2) || isempty(minNumCases)
    trainParams.minNumCases = 4;
else
    trainParams.minNumCases = max(2, minNumCases);
end

if (nargin < 3) || isempty(prune)
    trainParams.prune = true;
else
    trainParams.prune = prune;
end

if (nargin < 4) || isempty(smoothing)
    trainParams.smoothing = false;
else
    trainParams.smoothing = smoothing;
end

if (nargin < 5) || isempty(smoothing_k) || (smoothing_k < 0)
    trainParams.smoothing_k = 15;
else
    trainParams.smoothing_k = smoothing_k;
end

if (nargin < 6) || isempty(splitThreshold)
    trainParams.splitThreshold = 0.05;
else
    trainParams.splitThreshold = splitThreshold;
end

return
