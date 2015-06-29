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

function model = barzanKccaLearn(responseVars, features)

kernel = 'gauss';
kernelpar = 1;
[y1,y2,alpha1,alpha2,K1,K2,beta] = km_kcca_barzan(features, responseVars, kernel, kernelpar, 'euclid', [], 1E-5);

model = struct('projectedX', y1, 'projectedY', y2, 'X', features, 'Y', responseVars, 'alphaX', alpha1, 'alphaY', alpha2, 'KX', K1, 'KY', K2, 'beta', beta, 'kernel', kernel, 'kernelpar', kernelpar);

end

