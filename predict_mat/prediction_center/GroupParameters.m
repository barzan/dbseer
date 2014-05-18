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
        use_group = 0;
    end % end properties
    
    methods
        function set.allowedRelativeDiff(obj, value)
            if value < 0 || value > 1
                error('allowsRelativeDiff must be between 0 and 1.');
            else
                obj.allowsRelativeDiff = value;
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
            if this.use_group > 0
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
            end
        end
        
    end % end methods
end