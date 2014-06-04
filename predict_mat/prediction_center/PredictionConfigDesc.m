classdef PredictionConfigDesc < handle

    properties
        dir
        signature
        tranTypes
        startIdx
        endIdx
        maxThroughputIdx
        io_conf
        lock_conf
    end
    
    methods
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
    end % end methods
    
end % end classdef