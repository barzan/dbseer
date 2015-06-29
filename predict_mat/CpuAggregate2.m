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

function [CpuUser CpuSys CpuIdle CpuWai CpuHiq CpuSiq] = CpuAggregate2(monitorMatrix, header)

% TODO: figure out whether or not multi-threading is ON !!
cpu_usr_indexes = header.metadata.cpu_usr;
cpu_sys_indexes = header.metadata.cpu_sys;
cpu_idl_indexes = header.metadata.cpu_idl;
cpu_wai_indexes = header.metadata.cpu_wai;
cpu_hiq_indexes = header.metadata.cpu_hiq;
cpu_siq_indexes = header.metadata.cpu_siq;

    CpuUser = mean(monitorMatrix(:,cpu_usr_indexes), 2);
    CpuSys =  mean(monitorMatrix(:,cpu_sys_indexes), 2);
    CpuIdle = mean(monitorMatrix(:,cpu_idl_indexes), 2);
    CpuWai= mean(monitorMatrix(:,cpu_wai_indexes), 2);
    CpuHiq = mean(monitorMatrix(:,cpu_hiq_indexes), 2);
    CpuSiq = mean(monitorMatrix(:,cpu_siq_indexes), 2);    
end

