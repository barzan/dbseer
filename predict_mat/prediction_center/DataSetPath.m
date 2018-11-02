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

classdef DataSetPath < handle

    properties
        name
        path
        header_path
        monitor_path
        trans_count_path
        avg_latency_path
        percentile_latency_path
        statement_stat_path
    end

    %properties (SetAccess='private', GetAccess='public')
        %header
        %monitor
        %averageLatency
        %percentileLatency
        %transactionCount
        %statementStat
        %diffedMonitor
    %end

    properties (SetAccess='private', GetAccess='public')
        statReady = false;
    end

    methods

        % property accessors set statReady to false
        function set.header_path(this, value)
            this.header_path = value;
            this.statReady = false;
        end

        function set.path(this, value)
            this.path = value;
            this.statReady = false;
        end

        function set.monitor_path(this, value)
            this.monitor_path = value;
            this.statReady = false;
        end

        function set.trans_count_path(this, value)
            this.trans_count_path = value;
            this.statReady = false;
        end

        function set.avg_latency_path(this, value)
            this.avg_latency_path = value;
            this.statReady = false;
        end

        function set.percentile_latency_path(this, value)
            this.percentile_latency_path = value;
            this.statReady = false;
        end

        % function set.tranTypes(obj, this)
        %     this.tranTypes = obj;
        %     statReady = false;
        % end

        function set.statement_stat_path(this, value)
            this.statement_stat_path = value;
            this.statReady = false;
        end



        function this = setStruct(this, paramStruct)
            % propertyList = properties(this);
            propertyList = fieldnames(this);
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
            % propertyList = properties(this);
            propertyList = fieldnames(this);
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
