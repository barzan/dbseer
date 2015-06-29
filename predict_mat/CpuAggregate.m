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

function [CpuUser CpuSys CpuIdle CpuWai CpuHiq CpuSiq] = CpuAggregate( monitorMatrix)
header_aligned;

cpu_usr_indexes = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr]; % cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];
cpu_sys_indexes = [cpu1_sys cpu2_sys cpu3_sys cpu4_sys cpu5_sys cpu6_sys cpu7_sys cpu8_sys]; % cpu9_sys cpu10_sys cpu11_sys cpu12_sys cpu13_sys cpu14_sys cpu15_sys cpu16_sys];
cpu_idl_indexes = [cpu1_idl cpu2_idl cpu3_idl cpu4_idl cpu5_idl cpu6_idl cpu7_idl cpu8_idl]; % cpu9_idl cpu10_idl cpu11_idl cpu12_idl cpu13_idl cpu14_idl cpu15_idl cpu16_idl];
cpu_wai_indexes = [cpu1_wai cpu2_wai cpu3_wai cpu4_wai cpu5_wai cpu6_wai cpu7_wai cpu8_wai]; % cpu9_wai cpu10_wai cpu11_wai cpu12_wai cpu13_wai cpu14_wai cpu15_wai cpu16_wai];
cpu_hiq_indexes = [cpu1_hiq cpu2_hiq cpu3_hiq cpu4_hiq cpu5_hiq cpu6_hiq cpu7_hiq cpu8_hiq]; % cpu9_hiq cpu10_hiq cpu11_hiq cpu12_hiq cpu13_hiq cpu14_hiq cpu15_hiq cpu16_hiq];
cpu_siq_indexes = [cpu1_siq cpu2_siq cpu3_siq cpu4_siq cpu5_siq cpu6_siq cpu7_siq cpu8_siq]; % cpu9_siq cpu10_siq cpu11_siq cpu12_siq cpu13_siq cpu14_siq cpu15_siq cpu16_siq];

    CpuUser = mean(monitorMatrix(:,cpu_usr_indexes)')';
    CpuSys =  mean(monitorMatrix(:,cpu_sys_indexes)')';
    CpuIdle = mean(monitorMatrix(:,cpu_idl_indexes)')';
    CpuWai= mean(monitorMatrix(:,cpu_wai_indexes)')';
    CpuHiq = mean(monitorMatrix(:,cpu_hiq_indexes)')';
    CpuSiq = mean(monitorMatrix(:,cpu_siq_indexes)')';    
end

