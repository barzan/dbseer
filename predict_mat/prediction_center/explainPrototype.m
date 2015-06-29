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
function [varHeader meanForFanoIncreased] = explainPrototype(mv, outlierIdx, difference)

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

	% get correlation coefficient and p-value between columns for normal
	% matrix.
	[corrCoef pVal] = corrcoef(filteredNormalMatrix);

	% find columns with significant correlation with avg latency. (idx = 2 -->
	% overall avg. latency)
	correlatedIdx = find(pVal(:,2) < 0.05); 
	correlationCoeff = corrCoef(correlatedIdx, 2);

	% normalize
	% for i=3:size(filteredNormalMatrix,2)
	% 	maxValue = max(filteredNormalMatrix(:,i));
	% 	minValue = min(filteredNormalMatrix(:,i));
	% 	filteredNormalMatrix(:,i) = (filteredNormalMatrix(:,i) - minValue) / (maxValue - minValue);

	% 	maxValue = max(filteredOutlierMatrix(:,i));
	% 	minValue = min(filteredOutlierMatrix(:,i));
	% 	filteredOutlierMatrix(:,i) = (filteredOutlierMatrix(:,i) - minValue) / (maxValue - minValue);
	% end

	% calculate variance & mean for normal region.
	normalVar = var(filteredNormalMatrix);
	normalMean = mean(filteredNormalMatrix);

	% calculate variance & mean for normal+outlier region.
	includeOutlierVar = var(vertcat(filteredNormalMatrix, filteredOutlierMatrix));
	includeOutlierMean = mean(vertcat(filteredNormalMatrix, filteredOutlierMatrix));

	% calculate fano factor (variance-to-mean ratio)
	normalFano = normalVar ./ normalMean;
	includeOutlierFano = includeOutlierVar ./ includeOutlierMean;

	meanForFanoIncreased = [];
	fanoIncreasedIdx = [];
	fanoDifference = [];

	% find columns where adding outlier points to normal region results in higher variance-to-mean ratio than
	% that of normal region among those significantly correlated columns.
	% these columns are highly correlated to the avg. latency, but show
	% 'unexpected' values in outlier region --> possible explanation for
	% outliers in terms of metrics in our monitoring log.
	for i=1:size(correlatedIdx, 1)
	    if includeOutlierFano(correlatedIdx(i)) > normalFano(correlatedIdx(i)) && ~ismember(i, [1:2]) % exclude epoch, avg. latency
	    	if difference == 0 % if current result is greater than expected
	    		if (includeOutlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) * correlationCoeff(i) <= 0
	    			meanForFanoIncreased = [meanForFanoIncreased ((includeOutlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) / normalMean(correlatedIdx(i)))];
	    			fanoIncreasedIdx = [fanoIncreasedIdx correlatedIdx(i)];
	    			fanoDifference = [fanoDifference (includeOutlierFano(correlatedIdx(i)) - normalFano(correlatedIdx(i)))];
	    		end
	    	elseif difference == 1 % if current result is less than expected
	    		if (includeOutlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) * correlationCoeff(i) >= 0
	    			meanForFanoIncreased = [meanForFanoIncreased ((includeOutlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) / normalMean(correlatedIdx(i)))];
	    			fanoIncreasedIdx = [fanoIncreasedIdx correlatedIdx(i)];
	    			fanoDifference = [fanoDifference (includeOutlierFano(correlatedIdx(i)) - normalFano(correlatedIdx(i)))];
	    		end
	    	else
    			meanForFanoIncreased = [meanForFanoIncreased ((includeOutlierMean(correlatedIdx(i)) - normalMean(correlatedIdx(i))) / normalMean(correlatedIdx(i)))];
		        fanoIncreasedIdx = [fanoIncreasedIdx correlatedIdx(i)];
		        fanoDifference = [fanoDifference (includeOutlierFano(correlatedIdx(i)) - normalFano(correlatedIdx(i)))];
	    	end
	    end
	end

	% normalize avg. latency and possible-explain columns for plotting
	% later manually (normalize ~ [0,1])
	normalizedAvgLatency = (wholeMatrix(:,2)-min(wholeMatrix(:,2))) ./ (max(wholeMatrix(:,2)) - min(wholeMatrix(:,2)));    
	
	normalizedVal = [];
	for i=1:size(fanoIncreasedIdx, 2)
	    idx = fanoIncreasedIdx(i);
	    normalizedVal(:,i) = (wholeMatrix(:,idx)-min(wholeMatrix(:,idx))) ./ (max(wholeMatrix(:,idx)) - min(wholeMatrix(:,idx)));
	end

	varHeader = {};
	printBuf = [];

	% print possible-explain columns
	for i = 1:size(explainHeader,2)
	    %idx = getfield(columns, char(monitorHeaders(i)));
	    if ismember(i, fanoIncreasedIdx) && i ~= 1
	        varHeader{end+1} = explainHeader{i};
	    end
	end
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