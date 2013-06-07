function [M L C dM] = applyGroupingPolicy(groupingStrategy, M, L, C, dM)
overallTime = tic;

assert(~isfield(groupingStrategy, 'groupParams') || ~isfield(groupingStrategy, 'groups'), ...
                                        'Error: You cannot provide both the groups and the grouping parameters'); 

if isfield(groupingStrategy, 'groupParams') || isfield(groupingStrategy, 'groups') % we need to do grouping
    temp = [M L C dM];
    n1 = size(M,2); n2 = size(L, 2); n3 = size(C, 2); n4 = size(dM, 2);
    
    if isfield(groupingStrategy, 'groupParams') % run some clustering algorithm to find the groups
        assert(~isfield(groupingStrategy.groupParams, 'allowedRelativeDiff') || ~isfield(groupingStrategy.groupParams, 'nClusters'), ...
                                                'Either give allowedRelativeDiff (for GroupByAvg) or give nClusters (for BetterGroupByAvg) but not both!');
        if groupingStrategy.groupParams.groupByTPSinsteadOfIndivCounts %group by TPS only!
            TPS = sum(C,2);
            if isfield(groupingStrategy.groupParams, 'nClusters') && ~isfield(groupingStrategy.groupParams, 'allowedRelativeDiff')
                [temp freqs] = BetterGroupByAvg([TPS temp], 1, groupingStrategy.groupParams.nClusters, groupingStrategy.groupParams.minFreq, groupingStrategy.groupParams.minTPS, groupingStrategy.groupParams.maxTPS);
            elseif isfield(groupingStrategy.groupParams, 'allowedRelativeDiff')
                [temp freqs] = GroupByAvg([TPS temp], 1, groupingStrategy.groupParams.allowedRelativeDiff, groupingStrategy.groupParams.minFreq, groupingStrategy.groupParams.minTPS, groupingStrategy.groupParams.maxTPS);
            else
                error(['You can only specify either nClusters or allowedRelativeDiff!' valueToString(groupingStrategy.groupParams)]);
            end
            temp = temp(:,2:end); % to get rid of TPS
            
        else % group by individual transaction counts
            if isfield(groupingStrategy.groupParams, 'byWhichTranTypes')
                key = (n1+n2)+groupingStrategy.groupParams.byWhichTranTypes;
            else
                key = (n1+n2)+(1:n3);
            end
            
            if isfield(groupingStrategy.groupParams, 'nClusters') && ~isfield(groupingStrategy.groupParams, 'allowedRelativeDiff') 
                [temp freqs] = BetterGroupByAvg(temp, key, groupingStrategy.groupParams.nClusters, groupingStrategy.groupParams.minFreq, groupingStrategy.groupParams.minTPS, groupingStrategy.groupParams.maxTPS);
            elseif isfield(groupingStrategy.groupParams, 'allowedRelativeDiff')
                [temp freqs] = GroupByAvg(temp, key, groupingStrategy.groupParams.allowedRelativeDiff, groupingStrategy.groupParams.minFreq, groupingStrategy.groupParams.minTPS, groupingStrategy.groupParams.maxTPS);
            else
                error(['You can only specify either nClusters or allowedRelativeDiff!' valueToString(groupingStrategy.groupParams)]);
            end
        end
    else % then the actual grouping has been provided!
        nGroups = size(groupingStrategy.groups, 1);
        grouped = zeros(nGroups, size(temp,2));
        for g=1:nGroups
            lbound = groupingStrategy.groups(g,1);
            rbound = groupingStrategy.groups(g,2);
            grouped(g,:) = mean(temp(lbound:rbound,:));
        end
        temp = grouped;
    end
    % apply the grouping     
    M = temp(:,1:n1);
    L = temp(:,(n1+1):(n1+n2));
    C = temp(:,(n1+n2+1):(n1+n2+n3));
    dM = temp(:,(n1+n2+n3+1):(n1+n2+n3+n4));
end

%otherwise no grouping is required and the output will be identical to the
%input due to their identical variable names
elapsed = toc(overallTime);
fprintf(1,'applyGroupingPolicy time=%f\n', elapsed);

end

