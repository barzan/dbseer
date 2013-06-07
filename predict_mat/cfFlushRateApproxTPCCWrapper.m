function [ output_args ] = cfFlushRateApproxTPCCWrapper( io_conf,transCounts )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

conf = struct('io_conf',io_conf,'workloadName','TPCC');
output_args = cfFlushRateApprox(conf,transCounts);

end

