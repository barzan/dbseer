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

function createCausalModel(model_directory, modelName, causeString, effect)
	model = struct();
	model = setfield(model, 'cause', causeString);
	model = setfield(model, 'predicates', effect);

	if isempty(modelName)
		model_path = tempname(model_directory);
	else
		model_path = [model_directory '/' modelName];
	end
	if isOctave
		save('-mat', [model_path '.mat'], 'model');
	else
		save(model_path, 'model');
	end
end
