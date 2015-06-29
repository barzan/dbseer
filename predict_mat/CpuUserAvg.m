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

function [ CpuAvg CpuSysAvg CpuIdleAvg] = CpuUserAvg( monitorMatrix)
header_aligned;

cpu_usr_indexes = [cpu1_usr cpu2_usr cpu3_usr cpu4_usr cpu5_usr cpu6_usr cpu7_usr cpu8_usr]; % cpu9_usr cpu10_usr cpu11_usr cpu12_usr cpu13_usr cpu14_usr cpu15_usr cpu16_usr];
cpu_sys_indexes = [cpu1_sys cpu2_sys cpu3_sys cpu4_sys cpu5_sys cpu6_sys cpu7_sys cpu8_sys]; % cpu9_sys cpu10_sys cpu11_sys cpu12_sys cpu13_sys cpu14_sys cpu15_sys cpu16_sys];
cpu_idl_indexes = [cpu1_idl cpu2_idl cpu3_idl cpu4_idl cpu5_idl cpu6_idl cpu7_idl cpu8_idl]; % cpu9_idl cpu10_idl cpu11_idl cpu12_idl cpu13_idl cpu14_idl cpu15_idl cpu16_idl];

    CpuAvg = mean(monitorMatrix(:,cpu_usr_indexes)')';
    CpuSysAvg =  mean(monitorMatrix(:,cpu_sys_indexes)')';
    CpuIdleAvg = mean(monitorMatrix(:,cpu_idl_indexes)')';
end

