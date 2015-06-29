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

function predictions = barzanRegressTreeInvoke(model, features, use_octave)
% Invokes the regression tree for the given set of features
if nargin < 3
    use_octave = 0;
end

if isOctave || use_octave==1
    test_size = size(features);
    for i = 1:test_size(1)
        predictions(i,1) = m5ppredict(model, features(i,:));
    end
else
    predictions = predict(model, features);
end

end

