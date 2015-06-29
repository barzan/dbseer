% Copyright 2013 Barzan Mozafari
%
% Licensed under the Apache License, Version 2.0 (the "License");
% you may not use this file except in compliance with the License.
% You may obtain a copy of the License at
%
%     http://www.apache.org/licenses/LICENSE-2.0
%
% Unless required by applicable law or agreed to in writing, software
% distributed under the License is distributed on an "AS IS" BASIS,
% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
% See the License for the specific language governing permissions and
% limitations under the License.

function model = barzanRegressTreeLearn(responseVars, features, use_octave)
% learns a compact regression tree
if nargin < 3
    use_octave = 0;
end

if isOctave || use_octave==1
    m5Parameters = m5pparams(false, 10, false);
    [model m5BuildTime] = m5pbuild(features, responseVars, m5Parameters);
else
    model = RegressionTree.fit(features, responseVars, 'MergeLeaves', 'on', 'MinLeaf', 1, 'Prune', 'off', 'MinParent', 10);%, 'kfold', 10);
end

%save('RegresstionTree_Model', 'model')
%load('RegresstionTree_Model', 'model')
%disp 'RTREE PAUSE'
%pause
%model = compact(tree);

end

