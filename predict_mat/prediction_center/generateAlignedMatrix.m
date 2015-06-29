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

% Copyright [yyyy] [name of copyright owner]
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
function [result] = generateAlignedMatrix(mv, filename, maxRow)

	% difference = 0: greater than expected, 1: less than expected: >=2: just different

	explainHeader = {};
	mergedMatrix = [];
	mvFields = fieldnames(mv);
	mergedMatrix(:,1) = [1:size(mean(mv.clientTransLatency(:,1:end),2),1)];

	combinedLatency = sum(mv.clientTransLatency .* mv.clientIndividualSubmittedTrans,2)./mv.clientTotalSubmittedTrans;
    combinedLatency(isnan(combinedLatency)) = 0;

	mergedMatrix(:,2) = combinedLatency;
	explainHeader{end+1} = 'Epoch';
	explainHeader{end+1} = 'Combined Avg Latency';
	count = 3;
	for i = 1:size(mvFields,1)
		if ~isempty(strfind(mvFields{i}, 'os')) || ~isempty(strfind(mvFields{i}, 'dbms')) || ~isempty(strfind(mvFields{i}, 'cpu')) || ~isempty(strfind(mvFields{i}, 'Cpu'))

			field = getfield(mv, mvFields{i});
			for k = 1:size(field,2)
				if ~isempty(strfind(mvFields{i}, 'cpu'))
					explainHeader{end+1} = horzcat(mvFields{i}, ' (core #', num2str(k), ')');
				else
					explainHeader{end+1} = mvFields{i};
				end
				
				mergedMatrix(:,count) = field(:,k);
				count = count + 1;
			end
		end
	end
	
	fields = {};
    if nargin > 2
        if (size(mergedMatrix,1) < maxRow)
            maxRow = size(mergedMatrix,1);
        end
        fields{1} = mergedMatrix([1:maxRow],:);
    else
        fields{1} = mergedMatrix;
    end
	
	fields{2} = explainHeader;
    
    if nargin < 2
        save('wholeMatrixData', 'fields');
    else
        save(filename, 'fields');
    end

end