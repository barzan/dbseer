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

classdef DataSet < handle

    properties
        datasets
        tranTypes
        use_entire = true;
        startIdx
        endIdx
        %maxThroughputIdx
    end

    properties (SetAccess='private', GetAccess='public')
        header
        monitor
        averageLatency
        percentileLatency
        transactionCount
        statementStat
        diffedMonitor
    end

    properties (SetAccess='private', GetAccess='public')
        statReady = false;
    end

    methods

        % property accessors set statReady to false
        function set.datasets(this, value)
            this.datasets = value;
            this.statReady = false;
        end

        function set.use_entire(this, value)
            this.use_entire = value;
            this.statReady = false;
        end

        function set.startIdx(this, value)
            this.startIdx = value;
            this.statReady = false;
        end

        function set.endIdx(this, value)
            this.endIdx = value;
            this.statReady = false;
        end

        function value = get.header(this)
            if this.statReady
                value = this.header;
            else
                value = [];
            end
        end

        %function value = get.monitor(this)
            %if this.statReady
                %value = this.monitor;
            %else
                %value = [];
            %end
        %end

        %function value = get.averageLatency(this)
            %if this.statReady
                %value = this.averageLatency;
            %else
                %value = [];
            %end
        %end

        %function value = get.percentileLatency(this)
            %if this.statReady
                %value = this.percentileLatency;
            %else
                %value = [];
            %end
        %end

        %function value = get.transactionCount(this)
            %if this.statReady
                %value = this.transactionCount;
            %else
                %value = [];
            %end
        %end

        %function value = get.statementStat(this)
            %if this.statReady
                %value = this.statementStat;
            %else
                %value = [];
            %end
        %end

        %function value = get.diffedMonitor(this)
            %if this.statReady
                %value = this.diffedMonitor;
            %else
                %value = [];
            %end
        %end

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

        function loadStatistics(this)
			if this.use_entire
				[this.header this.monitor this.averageLatency this.percentileLatency this.transactionCount this.diffedMonitor] = ...
					load_stats2(this.datasets, 0, 0, true, this.tranTypes);
			else
				[this.header this.monitor this.averageLatency this.percentileLatency this.transactionCount this.diffedMonitor] = ...
					load_stats2(this.datasets, this.startIdx, this.endIdx, false, this.tranTypes);
			end
            this.statReady = true;
        end
    end % end methods

end % end classdef
