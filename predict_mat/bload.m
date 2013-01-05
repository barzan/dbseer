function [ matrix ] = bload(filename, headerlines)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

cmd = horzcat('tail -n +',num2str(headerlines+1), ' ', filename, ' > ', filename, '_temp');
system(cmd);
matrix = load(horzcat(filename,'_temp'));
system(horzcat('rm ',filename,'_temp'));

matrix = [matrix(:,1:2) matrix(:,4:end)]; % in order to skip the additional fake part of the system date field!
end


