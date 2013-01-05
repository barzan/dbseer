function [M L C dM] = applyGroupingPolicy(config, M, L, C, dM)

assert(~isfield(config, 'groupParams') || ~isfield(config, 'groups'), ...
                                        'Error: You cannot provide both the groups and the grouping parameters'); 

if isfield(config, 'groupParams') || isfield(config, 'groups') % we need to do grouping
    temp = [M L C dM];
    n1 = size(M,2); n2 = size(L, 2); n3 = size(C, 2); n4 = size(dM, 2);
    
    if isfield(config, 'groupParams') % run some clustering algorithm to find the groups
        assert(~isfield(config.groupParams, 'allowedRelativeDiff') || ~isfield(config.groupParams, 'nClusters'), ...
                                                'Either give allowedRelativeDiff (for GroupByAvg) or give nClusters (for BetterGroupByAvg) but not both!');
        if config.groupParams.groupByTPSinsteadOfIndivCounts %group by TPS only!
            TPS = sum(C,2);
            if isfield(config.groupParams, 'nClusters') && ~isfield(config.groupParams, 'allowedRelativeDiff')
                [temp freqs] = BetterGroupByAvg([TPS temp], 1, config.groupParams.nClusters, config.groupParams.minFreq, config.groupParams.minTPS, config.groupParams.maxTPS);
            elseif isfield(config.groupParams, 'allowedRelativeDiff')
                [temp freqs] = GroupByAvg([TPS temp], 1, config.groupParams.allowedRelativeDiff, config.groupParams.minFreq, config.groupParams.minTPS, config.groupParams.maxTPS);
            else
                error(['You can only specify either nClusters or allowedRelativeDiff!' valueToString(config.groupParams)]);
            end
            temp = temp(:,2:end); % to get rid of TPS
            
        else % group by individual transaction counts
            if isfield(config.groupParams, 'byWhichTranTypes')
                key = (n1+n2)+config.groupParams.byWhichTranTypes;
            else
                key = (n1+n2)+(1:n3);
            end
            
            if isfield(config.groupParams, 'nClusters') && ~isfield(config.groupParams, 'allowedRelativeDiff') 
                [temp freqs] = BetterGroupByAvg(temp, key, config.groupParams.nClusters, config.groupParams.minFreq, config.groupParams.minTPS, config.groupParams.maxTPS);
            elseif isfield(config.groupParams, 'allowedRelativeDiff')
                [temp freqs] = GroupByAvg(temp, key, config.groupParams.allowedRelativeDiff, config.groupParams.minFreq, config.groupParams.minTPS, config.groupParams.maxTPS);
            else
                error(['You can only specify either nClusters or allowedRelativeDiff!' valueToString(config.groupParams)]);
            end
        end
    else % then the actual grouping has been provided!
        nGroups = size(config.groups, 1);
        grouped = zeros(nGroups, size(temp,2));
        for g=1:nGroups
            lbound = config.groups(g,1);
            rbound = config.groups(g,2);
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

end

