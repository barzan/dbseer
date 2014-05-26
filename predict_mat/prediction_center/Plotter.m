classdef Plotter < handle
    properties
        mv
        Xdata
        Xlabel
    end
    
    methods
        function obj = set.mv(self, value)
            self.mv = value;
            self.Xdata = 1:1:value.numberOfObservations-1;
            self.Xlabel = 'Time (seconds)';
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotIndividualCoreUsageUser(this)
            Xdata = this.Xdata;
            Ydata = this.mv.cpu_user;
            Xlabel = this.Xlabel;
            Ylabel = 'Individual core usr usage';
            legends = {'Core with MySQL'};
        end
        
        function [title legends Xdata Ydata Xlabel Ylabel] = plotRowsChangedPerWriteMB(this)
            mv = this.mv;
            if isfield(mv, 'dbmsChangedRows')
                temp = [mv.dbmsChangedRows mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            elseif isfield(mv, 'dbmsTotalWritesMB')
                temp = [mv.dbmsTotalWritesMB mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            else
                temp = [mv.dbmsLogWritesMB mv.dbmsPageWritesMB mv.osNumberOfSectorWrites];
            end
            temp = sortrows(temp, 1);
            Xdata = temp(:,1);
            Ydata = temp(:,2:end);
            title = 'Rows changed vs. written data (MB)';
            Xlabel = '# Rows Changed';
            Ylabel = 'Written data (MB)';
            legends = {'MySQL total IO', 'MySQL log IO', 'MySQL data IO', 'System physical IO'};
        end
    end
end