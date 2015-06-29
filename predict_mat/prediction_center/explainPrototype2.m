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
function [result] = explainPrototype2(mv, outlierIdx, difference)

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

	normalIdx = [];
	for i=1:size(mergedMatrix,1)
		if ~ismember(i, outlierIdx)
			normalIdx = [normalIdx i];
		end
	end
	
	wholeMatrix = mergedMatrix;

	% divide matrix into two regions
	normalMatrix = mergedMatrix(normalIdx, :); 
	outlierMatrix = mergedMatrix(outlierIdx, :);

	% for normal region, we filter out areas where avg. latency & total trans
	% count are zero (i.e. time with no transactions at the beginning/end of monitoring data)
	filteredNormalMatrix = normalMatrix;
	filteredNormalMatrix(all(filteredNormalMatrix(:,2)==0,2),:) = [];

	% no filter for outlier for now.
	filteredOutlierMatrix = outlierMatrix;

	% 0 - normal, 1 - outlier
	% this is for DT.
	response = vertcat(zeros(size(filteredNormalMatrix,1),1), ones(size(filteredOutlierMatrix,1),1));
	decisionTreeMatrix = vertcat(filteredNormalMatrix, filteredOutlierMatrix);

	% get correlation coefficient and p-value between columns for normal
	% matrix.
	[corrCoef pVal] = corrcoef(filteredNormalMatrix);

	% find columns with significant correlation with avg latency. (idx = 2 -->
	% overall avg. latency)
	correlatedIdx = find(pVal(:,2) < 0.05); 
	correlationCoeff = corrCoef(correlatedIdx, 2);

	% normalize
	for i=3:size(filteredNormalMatrix,2)
		maxValue = max(wholeMatrix(:,i));
		minValue = min(wholeMatrix(:,i));
		filteredNormalMatrix(:,i) = (filteredNormalMatrix(:,i) - minValue) / (maxValue - minValue);
		filteredOutlierMatrix(:,i) = (filteredOutlierMatrix(:,i) - minValue) / (maxValue - minValue);
	end

	% calculate variance & mean for normal region.
	normalVar = var(filteredNormalMatrix);
	normalMean = mean(filteredNormalMatrix);

	outlierVar = var(filteredOutlierMatrix);
	outlierMean = mean(filteredOutlierMatrix);

	% calculate variance & mean for normal+outlier region.
	includeOutlierVar = var(vertcat(filteredNormalMatrix, filteredOutlierMatrix));
	includeOutlierMean = mean(vertcat(filteredNormalMatrix, filteredOutlierMatrix));

	% calculate fano factor (variance-to-mean ratio)
	normalFano = normalVar ./ normalMean;
	includeOutlierFano = includeOutlierVar ./ includeOutlierMean;

	meanForFanoIncreased = [];
	fanoIncreasedIdx = [];
	meanIncreasedIdx = [];
	fanoDifference = [];
	meanDifference = [];

	% find columns where normalized average in outlier region is significantly different from that of normal region
	% among those significantly correlated columns.
	% these columns are highly correlated to the avg. latency, but show
	% 'unexpected' values in outlier region --> possible explanation for
	% outliers in terms of metrics in our monitoring log.
	for i=1:size(correlatedIdx, 1)
	    if ~ismember(i, [1:2]) % exclude epoch, avg. latency
	    	if difference == 0 && (outlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) * correlationCoeff(i) <= 0 % if current result is greater than expected
    			meanIncreasedIdx = [meanIncreasedIdx correlatedIdx(i)];
    			meanDifference = [meanDifference (outlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i)))];
	    	elseif difference == 1 && (outlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) * correlationCoeff(i) >= 0 % if current result is less than expected
	    		meanIncreasedIdx = [meanIncreasedIdx correlatedIdx(i)];
    			meanDifference = [meanDifference (outlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i)))];
	    	elseif difference == 2
	    		meanIncreasedIdx = [meanIncreasedIdx correlatedIdx(i)];
    			meanDifference = [meanDifference (outlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i)))];
	    	end
	    end
	end

	result = {};
	possibleHeader = {};

	% print possible-explain columns
	for i = 1:size(explainHeader,2)
	    %idx = getfield(columns, char(monitorHeaders(i)));
	    if ismember(i, meanIncreasedIdx) && i ~= 1
	        possibleHeader{end+1} = explainHeader{i};
	    end
	end

	normalizedMeanResult = cell(size(meanIncreasedIdx,2), 4);

	for i=1:size(meanIncreasedIdx,2)
		normalizedMeanResult{i, 1} = possibleHeader{i};
		normalizedMeanResult{i, 2} = meanIncreasedIdx(i);
		normalizedMeanResult{i, 3} = meanDifference(i);
		normalizedMeanResult{i, 4} = abs(meanDifference(i));
	end

	normalizedMeanResult = sortrows(normalizedMeanResult, -4);

	result{end+1} = normalizedMeanResult;

	decisionTreeResult = cell(size(decisionTreeMatrix,2)-2, 2);
	for i=3:size(decisionTreeMatrix, 2)
		tree = ClassificationTree.fit(decisionTreeMatrix(:,i), response, 'PredictorName', {explainHeader{i}});
		prediction = predict(tree, decisionTreeMatrix(:,i));
		trainAccuracy = sum(prediction == response) / size(decisionTreeMatrix, 1);
		decisionTreeResult{i-2, 1} = explainHeader{i};
		decisionTreeResult{i-2, 2} = trainAccuracy;
		decisionTreeResult{i-2, 3} = tree.NumNodes;
	end
	decisionTreeResult = sortrows(decisionTreeResult, -2);

	result{end+1} = decisionTreeResult;

	% plot possible-explain columns
	% hold off
	% for k=1:7
	% %for k=15:29
	%     subplot(2, 4, k)
	%     area(outlierIdx,ones(length(outlierIdx), 1), 'LineStyle', 'none')
	    
	%     alpha(0.25)
	%     hold on
	%     plot(normalizedVal(:,k), 'r')
	%     title(strrep(varHeader{k}, '_', '-'))
	%     axis([1 70 0 1])
	% end

	% subplot(2,4,8)
	% area(outlierIdx,ones(length(outlierIdx), 1), 'LineStyle', 'none')
	% alpha(0.25)
	% hold on
	% plot(normalizedAvgLatency)
	% title('avg latency (normalized)')
	% axis([1 70 0 1])

	% figure
	% % plot possible-explain columns
	% hold off
	% for k=8:22
	% %for k=15:29
	%     subplot(4, 4, k-7)
	%     area(outlierIdx,ones(length(outlierIdx), 1), 'LineStyle', 'none')
	    
	%     alpha(0.25)
	%     hold on
	%     plot(normalizedVal(:,k), 'r')
	%     title(strrep(varHeader{k}, '_', '-'))
	%     axis([1 70 0 1])
	% end

	% subplot(4,4,16)
	% area(outlierIdx,ones(length(outlierIdx), 1), 'LineStyle', 'none')
	% alpha(0.25)
	% hold on
	% plot(normalizedAvgLatency)
	% title('avg latency (normalized)')
	% axis([1 70 0 1])

end