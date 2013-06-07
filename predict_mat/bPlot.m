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

