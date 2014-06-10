classdef GroupParameters < handle

    properties
        minFreq
        minTPS
        maxTPS
        groupByTPSinsteadOfIndivCounts
        byWhichTranTypes
        allowedRelativeDiff
        nClusters
        groups
        use_group
    end % end properties
    
    methods
        function set.allowedRelativeDiff(obj, value)
            if value < 0 || value > 1
                error('allowedRelativeDiff must be between 0 and 1.');
            else
                obj.allowedRelativeDiff = value;
            end
        end
        
        function this = setStruct(this, paramStruct)
            propertyList = properties(this);
            for i = 1:length(propertyList)
                prop = propertyList(i);
                prop = prop{1};
                propertyName = num2str(prop);
                if isfield(paramStruct, propertyName)
                    this = setfield(this, propertyName, getfield(paramStruct, propertyName));
                end
            end
        end
        
        function groupStruct = getStruct(this)
            if ~isempty(this.use_group) && this.use_group==1
                groupStruct = struct('groups', groups);
            else
                groupStruct = struct();
                propertyList = properties(this);
                for i = 1:length(propertyList)
                    prop = propertyList(i);
                    prop = prop{1};
                    propertyName = num2str(prop);
                    if ~isempty(getfield(this,propertyName))
                        groupStruct = setfield(groupStruct, propertyName, getfield(this, propertyName));
                    end
                end
                if ~isempty(fieldnames(groupStruct))
                    groupStruct = struct('groupParams', groupStruct);
                else
                    groupStruct = struct();
                end
            end
        end
        
    end % end methods
end