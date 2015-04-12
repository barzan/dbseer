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