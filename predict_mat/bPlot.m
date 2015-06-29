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

function ph1 = bPlot(varargin)
% Barzan's plot

if mod(length(varargin), 2)~=0
    error ('Number of input parameters should be even');
end

seenTheFirst = false;

for i=1:length(varargin)/2
    value = varargin{2*i-1};
    caption = varagin{2*i};
    
    ph1 = plot(Xdata(:), mv.clientTotalSubmittedTrans,'kd');
    hold all;
    legend('-DynamicLegend');
    for i=1:size(mv.clientIndividualSubmittedTrans, 2)
        plot(Xdata(:), mv.clientIndividualSubmittedTrans(:,i), nextPlotStyle, 'DisplayName', ['# Transactions ' num2str(i)]);        
    end

    if isfield(mv, 'dbmsRollbackHandler')
        plot(Xdata(:), mv.dbmsRollbackHandler, nextPlotStyle, 'DisplayName', 'dbmsRollbackHandler');
    end    
    plot(Xdata(:), mv.dbmsCommittedCommands, nextPlotStyle, 'DisplayName', 'dbmsCommittedCommands');
    plot(Xdata(:), mv.dbmsRolledbackCommands, nextPlotStyle, 'DisplayName', 'dbmsRolledbackCommands');    
end
    
end

