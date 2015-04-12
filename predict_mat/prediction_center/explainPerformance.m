function [predicates explanation] = explainPerformance(mv, outlierIdx, normalIdx, model_directory, num_discrete, normalized_diff_threshold, outlier_multiplier)

    HAS_PREDICATE = 0;
    IN_CONFLICT = 1;
    
    NORMAL_PARTITION = 1;
    OUTLIER_PARTITION = 2;
	CONFLICT_PARTITION = 3;
    
    if nargin < 4
        num_discrete = 1000;
    end
    if nargin < 5
        normalized_diff_threshold = 0.2;
    end
    if nargin < 6
        outlier_multiplier = 10;
    end
    
	mergedMatrix = [];
	fieldNames = {};

	mvFields = fieldnames(mv);
	mergedMatrix(:,1) = [1:size(mean(mv.clientTransLatency(:,1:end),2),1)];

	combinedLatency = sum(mv.clientTransLatency .* mv.clientIndividualSubmittedTrans,2)./mv.clientTotalSubmittedTrans;
    combinedLatency(isnan(combinedLatency)) = 0;

	mergedMatrix(:,2) = combinedLatency;
	fieldNames{end+1} = 'Epoch';
	fieldNames{end+1} = 'Combined Avg Latency';
	count = 3;

	for i = 1:size(mvFields,1)
		if ~isempty(strfind(mvFields{i}, 'dbmsCum'))
			continue % skip cumulateive DB metrics
		end
		if ~isempty(strfind(mvFields{i}, 'os')) || ~isempty(strfind(mvFields{i}, 'dbms')) || ~isempty(strfind(mvFields{i}, 'cpu')) || ~isempty(strfind(mvFields{i}, 'Cpu'))

			field = getfield(mv, mvFields{i});
			for k = 1:size(field,2)
				if ~isempty(strfind(mvFields{i}, 'cpu'))
					fieldNames{end+1} = horzcat(mvFields{i}, ' (core #', num2str(k), ')');
				else
					fieldNames{end+1} = mvFields{i};
				end
				
				mergedMatrix(:,count) = field(:,k);
				count = count + 1;
			end
		elseif ~isempty(strfind(mvFields{i}, 'clientIndividualSubmittedTrans'))
			field = getfield(mv, mvFields{i});
			for k = 1:size(field,2)
				fieldNames{end+1} = horzcat('numTrans_', num2str(k));
				mergedMatrix(:,count) = field(:,k);
				count = count + 1;
			end
		end
	end

	wholeMatrix = mergedMatrix;
	isNormalEmpty = false;
	respectUserSelectedAnomaly = false;

	if isempty(normalIdx)
		isNormalEmpty = true;
		for i=1:size(wholeMatrix,1)
			if ~ismember(i, outlierIdx)
				normalIdx = [normalIdx i];
			end
		end
	end

	normalSize = size(normalIdx,2);
	outlierSize = size(outlierIdx,2);

	% when only abnormal region is selected by a user and it is relatively smaller than normal region (less than 50% of normal region),
	% we compare # of tuples in the confliected partition (i.e. partition with both normal and abnormal tuples), and label it accordingly.
	if isNormalEmpty && outlierSize < normalSize * 0.5
		respectUserSelectedAnomaly = true;
	end
    
    % divide matrix into two regions
	normalMatrix = wholeMatrix(normalIdx, :); 
	outlierMatrix = wholeMatrix(outlierIdx, :);

    normalPartitions = {};
    outlierPartitions = {};
    partitionLabels = {};
    partitionLabelsInitial = {};
    partitionLabelsAfterReset = {};
    numAlternatingPartitions = {};
    conflictCount = {};
    forcedNeutralCount = {};
    
    attributeStatus = {};
    normalizedNormalAverage = {};
    normalizedOutlierAverage = {};
    
    normalAverage = {};
    outlierAverage = {};
    
    % Discretization
    for i=3:size(wholeMatrix, 2)
        
        conflictCount{i-2} = 0;
        forcedNeutralCount{i-2} = 0;
        
        maxValue = max(wholeMatrix(:,i));
        minValue = min(wholeMatrix(:,i));
        range = maxValue - minValue;
        discrete_size = range / (num_discrete);
        boundaries{i-2} = [minValue:discrete_size:maxValue];
        boundary_count = size(boundaries{i-2},2);
        if boundary_count == 0
            partitionLabelsInitial{i-2} = zeros(1, num_discrete);
            continue
        end
        
        currentNormalPartitions = zeros(1, boundary_count);
        currentOutlierPartitions = zeros(1, boundary_count);
        currentPartitionLabels = zeros(1, boundary_count);
        
        partitionLabelsInitial{i-2} = currentPartitionLabels;
        
        current_boundary = boundaries{i-2};
        
        isConflict = false;
        normalizedNormalSum = 0;
        normalizedNormalCount = 0;
        normalizedOutlierSum = 0;
        normalizedOutlierCount = 0;
        for j=1:size(current_boundary,2)
            if j == size(current_boundary,2)
                currentNormalPartitions(j) = sum(normalMatrix(:,i) >= current_boundary(j));
                currentOutlierPartitions(j) = sum(outlierMatrix(:,i) >= current_boundary(j));
            else
                currentNormalPartitions(j) = sum(normalMatrix(:,i) >= current_boundary(j) & normalMatrix(:,i) < current_boundary(j+1));
                currentOutlierPartitions(j) = sum(outlierMatrix(:,i) >= current_boundary(j) & outlierMatrix(:,i) < current_boundary(j+1));
            end
            
            if currentNormalPartitions(j) > 0 && currentOutlierPartitions(j) > 0
                isConflict = true;
                conflictCount{i-2} = conflictCount{i-2} + 1;
                currentPartitionLabels(j) = CONFLICT_PARTITION;
                continue;
            end
            
            if currentNormalPartitions(j) > 0 
                currentPartitionLabels(j) = NORMAL_PARTITION;
            end
            if currentOutlierPartitions(j) > 0
                currentPartitionLabels(j) = OUTLIER_PARTITION;
            end
        end

		numNormalPartition = sum(currentPartitionLabels==NORMAL_PARTITION);
		numOutlierPartition = sum(currentPartitionLabels==OUTLIER_PARTITION);

		expectedNormalPerPartition = normalSize / numNormalPartition;
		expectedOutlierPerPartition = outlierSize / numOutlierPartition;

        for j=1:size(current_boundary,2)
			if currentPartitionLabels(j) == CONFLICT_PARTITION
				if respectUserSelectedAnomaly
					relativeNormal = currentNormalPartitions(j) / normalSize;
					relativeOutlier = currentOutlierPartitions(j) / outlierSize;
					if relativeNormal > relativeOutlier
						currentPartitionLabels(j) = NORMAL_PARTITION;
					elseif relativeOutlier > relativeNormal
						currentPartitionLabels(j) = OUTLIER_PARTITION;
					else
						currentPartitionLabels(j) = 0;
					end
				else
					currentPartitionLabels(j) = 0;
				end
			end
		end
        
        for j=1:size(current_boundary,2)
            if currentPartitionLabels(j) == NORMAL_PARTITION
                normalizedNormalSum = normalizedNormalSum + ( (current_boundary(j) - minValue) / range );
                normalizedNormalCount = normalizedNormalCount + 1;
            elseif currentPartitionLabels(j) == OUTLIER_PARTITION
                normalizedOutlierSum = normalizedOutlierSum + ( (current_boundary(j) - minValue) / range );
                normalizedOutlierCount = normalizedOutlierCount + 1;
            end
        end
        normalizedNormalAverage{i-2} = normalizedNormalSum / normalizedNormalCount;
        normalizedOutlierAverage{i-2} = normalizedOutlierSum / normalizedOutlierCount;
        
        markForNeutral = zeros(size(currentPartitionLabels));
        for j=1:(size(currentPartitionLabels,2)-1)
            currentPartition = currentPartitionLabels(j);
            if currentPartition == 0
                continue
            end
            for k=(j+1):size(currentPartitionLabels,2)
                if (currentPartitionLabels(k) > 0)
                    if (currentPartitionLabels(k) ~= currentPartition)
                        markForNeutral(j) = 1;
                        markForNeutral(k) = 1;
                    end
                    break;
                end
            end
        end
        
        forcedNeutralCount{i-2} = sum(markForNeutral);
        
        partitionLabelsInitial{i-2} = currentPartitionLabels;
        
        normalCount = sum(currentPartitionLabels == NORMAL_PARTITION);
        outlierCount = sum(currentPartitionLabels == OUTLIER_PARTITION);
             
        for j=1:size(markForNeutral,2)
            if (markForNeutral(j) == 1)
                if (currentPartitionLabels(j) == NORMAL_PARTITION) && (normalCount > 1)
                    currentPartitionLabels(j) = 0;
                elseif (currentPartitionLabels(j) == OUTLIER_PARTITION) && (outlierCount > 1)
                    currentPartitionLabels(j) = 0;
                end
            end
        end
        partitionLabelsAfterReset{i-2} = currentPartitionLabels;
        
        normalCount = sum(currentPartitionLabels == NORMAL_PARTITION);
        outlierCount = sum(currentPartitionLabels == OUTLIER_PARTITION);
        
        if (normalCount == 0 && outlierCount > 0)
            normalMean = mean(normalMatrix(:,i));
            for j=1:size(current_boundary,2)
                if j == size(current_boundary,2)
                    if (normalMean >= current_boundary(j))
                        currentPartitionLabels(j) = NORMAL_PARTITION;
                    	break
                    end
                else
                    if (normalMean >= current_boundary(j) & normalMean < current_boundary(j+1))
                        currentPartitionLabels(j) = NORMAL_PARTITION;
                        break
                    end
                end
            end
        end
        
        markForNeutral = zeros(size(currentPartitionLabels));
        for j=1:size(currentPartitionLabels,2)
            if (currentPartitionLabels(j) == 0)
               
                distanceToNormal = num_discrete * 2 * outlier_multiplier;
                distanceToOutlier = num_discrete * 2 * outlier_multiplier;
                k=j-1;
                while k >= 1
                    if currentPartitionLabels(k) == NORMAL_PARTITION
                        if distanceToNormal > abs(k-j)
                            distanceToNormal = abs(k-j);
                        end
                        break
                    end
                    if currentPartitionLabels(k) == OUTLIER_PARTITION
                        if distanceToOutlier > abs(k-j)
                            distanceToOutlier = abs(k-j) * outlier_multiplier;
                        end
                        break
                    end
                    k = k - 1;
                end

                k=j+1;
                while k <= size(currentPartitionLabels,2)
                    if currentPartitionLabels(k) == NORMAL_PARTITION
                        if distanceToNormal > abs(k-j)
                            distanceToNormal = abs(k-j);
                        end
                        break
                    end
                    if currentPartitionLabels(k) == OUTLIER_PARTITION
                        if distanceToOutlier > abs(k-j)
                            distanceToOutlier = abs(k-j) * outlier_multiplier;
                        end
                        break
                    end
                    k = k + 1;
                end
                
                if distanceToNormal < distanceToOutlier
                    markForNeutral(j) = NORMAL_PARTITION;
                elseif distanceToOutlier < distanceToNormal
                    markForNeutral(j) = OUTLIER_PARTITION;
                end
                
            end
        end
        
        for j=1:size(currentPartitionLabels,2)
            if (currentPartitionLabels(j) == 0)
                currentPartitionLabels(j) = markForNeutral(j);
            end
        end
        
        normalPartitions{i-2} = currentNormalPartitions;
        outlierPartitions{i-2} = currentOutlierPartitions;       
        partitionLabels{i-2} = currentPartitionLabels;
        normalAverage{i-2} = mean(normalMatrix(:,i));
        outlierAverage{i-2} = mean(outlierMatrix(:,i));
    end
    
    predicates = {};
    
    for i=1:size(partitionLabels, 2)
        
        predicates{i,1} = -1;
        predicates{i,3} = 0;
        if isempty(partitionLabels{i}) || sum(partitionLabels{i}==OUTLIER_PARTITION) == 0
            continue
        end
        
        partitions = partitionLabels{i};
        boundary = boundaries{i};
        
        if size(boundary,2) == 0
            continue
        end
        
        predicateName = fieldNames{i+2};
        predicateString = '';
        predicateCount = 0;
        lower = inf;
        upper = inf;
        
        for j=1:size(partitions,2)-1
           
           if j == 1 && partitions(j) == OUTLIER_PARTITION
               predicateCount = predicateCount + 1;
           end
           if partitions(j) ~= OUTLIER_PARTITION && partitions(j+1) == OUTLIER_PARTITION
               if ~isempty(predicateString) 
                   predicateString = sprintf('%s OR %s', predicateString, sprintf('> %.6f', boundary(j+1)));
             
               else
                   predicateString = sprintf('> %.6f', boundary(j+1));
               
               end
               lower = boundary(j+1);
               predicateCount = predicateCount + 1;
           elseif partitions(j) == OUTLIER_PARTITION && partitions(j+1) ~= OUTLIER_PARTITION
               if ~isempty(predicateString) 
                   predicateString = sprintf('%s and ', predicateString);
                   
               end
               predicateString = sprintf('%s%s', predicateString, sprintf('< %.6f', boundary(j+1)));
               upper = boundary(j+1);
               predicateCount = predicateCount + 1;
           end
        end
        
        if ~isempty(predicateString)
            predicateString = sprintf('%s %s', predicateName, predicateString);
        end
        
        predicates{i, 1} = predicateCount;
        predicates{i, 2} = predicateString;
        predicates{i, 3} = abs(normalizedNormalAverage{i} - normalizedOutlierAverage{i});
        predicates{i, 4} = lower;
        predicates{i, 5} = upper;
        predicates{i, 6} = predicateName;
        predicates{i, 7} = normalAverage{i};
        predicates{i, 8} = outlierAverage{i};
        
    end

    filtered_predicates = {};
    count = 1;
    sorted_predicates = sortrows(predicates, [1 -3]);
    effect = {};
    effectCount = 1;

    for j=1:size(sorted_predicates,1)
        if (sorted_predicates{j,1} <= 0)
            continue
        end
        count=count+1;
        
		if (sorted_predicates{j,1} <= 2) && (sorted_predicates{j,3} > normalized_diff_threshold)
			effect{effectCount, 1} = sorted_predicates{j,6};
			effect{effectCount, 2} = [sorted_predicates{j,4} sorted_predicates{j,5}];
			effect{effectCount, 4} = sorted_predicates{j,7};
			effect{effectCount, 5} = sorted_predicates{j,8};
			effect{effectCount, 3} = (sorted_predicates{j,8} - sorted_predicates{j,7}) / sorted_predicates{j,7} * 100;
			filtered_predicates(effectCount,:) = effect(effectCount,:);
			effectCount = effectCount + 1;
		end
    end

	predicates = sortrows(filtered_predicates, -3);
    
    causalModels = loadCausalModel(model_directory);
    causeRank = cell(size(causalModels,2), 2);
        
    % Let's try causal model analysis
    for i=1:size(causalModels,2)
        cause = causalModels{i}.cause;
        effectPredicates = causalModels{i}.predicates;
        
        coveredOutlierRatioAverage = 0;
		coveredNormalRatioAverage = 0;
        precisionAverage = 0;
        recallAverage = 0;
        for j=1:size(effectPredicates,1)
%             effectPredicate = effectPredicates{j};
            field = effectPredicates{j, 1};
            predicate = effectPredicates{j, 2};
            
            
            fieldIndex = find(ismember(fieldNames, field));
            if isempty(fieldIndex)
                disp(sprintf('the field: %s not found!', field))
            else
%                 disp(sprintf('the field: %s, index = %d', field, fieldIndex))
                actualIndex = fieldIndex - 2;
                if size(boundaries{actualIndex}, 2) == 0
                    continue
                end
                currentPartition = partitionLabelsInitial{actualIndex};
                partitionBoundaries = boundaries{actualIndex};
				normalPartitionCount = 0;
                outlierPartitionCount = 0;
                coveredPartitionCount = 0;
                coveredNormalCount = 0;
        
                for k=1:size(currentPartition,2)
                    if currentPartition(k) == OUTLIER_PARTITION
                        outlierPartitionCount = outlierPartitionCount + 1;
                        for p=1:size(predicate,1)
                            lower = predicate(p, 1);
                            upper = predicate(p, 2);
                            if lower ~= inf && lower > partitionBoundaries(k)
                                continue
                            elseif upper ~= inf && k ~= size(currentPartition,2) && upper <= partitionBoundaries(k+1)
                                continue
                            end
%                             [actualIndex lower upper partitionBoundaries(k)]
                            coveredPartitionCount = coveredPartitionCount + 1;
                            break
                        end
                    elseif currentPartition(k) == NORMAL_PARTITION
						normalPartitionCount = normalPartitionCount + 1;
                        for p=1:size(predicate,1)
                            lower = predicate(p, 1);
                            upper = predicate(p, 2);
                            if lower ~= inf && lower > partitionBoundaries(k)
                                continue
                            elseif upper ~= inf && k ~= size(currentPartition,2) && upper <= partitionBoundaries(k+1)
                                continue
                            end
%                             [actualIndex lower upper partitionBoundaries(k)]
                            coveredNormalCount = coveredNormalCount + 1;
                            break
                        end
                    end
                end
%                 disp(sprintf('\tfor field %s: outlierTotal = %d, covered = %d, ratio covered = %.3f', field, outlierPartitionCount, coveredPartitionCount, ...
%                     coveredPartitionCount / outlierPartitionCount * 100))
                ratio = (coveredPartitionCount / outlierPartitionCount);
                if isnan(ratio)
                    ratio = 0;
                end
                coveredOutlierRatioAverage = coveredOutlierRatioAverage + ratio;

                ratio = (coveredNormalCount / normalPartitionCount);
                if isnan(ratio)
                    ratio = 0;
                end
                coveredNormalRatioAverage = coveredNormalRatioAverage + ratio;
                
                ratio = (coveredPartitionCount / (coveredNormalCount + coveredPartitionCount));
                if isnan(ratio)
                    ratio = 0;
                end
                precisionAverage = precisionAverage + ratio;
            end
        end
        coveredOutlierRatioAverage = coveredOutlierRatioAverage / size(effectPredicates,1);
		coveredNormalRatioAverage = coveredNormalRatioAverage / size(effectPredicates,1);
        precisionAverage = precisionAverage / size(effectPredicates, 1);
%         disp(sprintf('for cause - %s: confidence = %.3f', cause, coveredRatioAverage))
        causeRank{i, 1} = cause;
        causeRank{i, 2} = (coveredOutlierRatioAverage - coveredNormalRatioAverage) * 100; % confidence
        causeRank{i, 3} = precisionAverage; % precision
        causeRank{i, 4} = 2 * (coveredOutlierRatioAverage * precisionAverage) / (coveredOutlierRatioAverage + precisionAverage); % f1-measure
        causeRank{i, 5} = coveredOutlierRatioAverage * 100; % recall
		causeRank{i, 6} = effectPredicates;
    end
    
%     disp ' '
%     disp '-- Cause Ranking --'
    causeRank = sortrows(causeRank, -2);
    for i=1:size(causeRank,1)
%         disp(sprintf('%d. %s: confidence = %.3f', i, causeRank{i,1}, causeRank{i,2}))
%         disp(sprintf('%d. %s (%.3f)', i, causeRank{i,1}, causeRank{i,2}))
    end
    explanation = causeRank;
end
