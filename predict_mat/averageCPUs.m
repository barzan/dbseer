function [ avgCPU ] = averageCPUs(matrix, startingIndex, lastIndex, increment)
    idx = startingIndex;
    avgCPU = matrix[:,idx];
    while 
        idx = idx + 6;
        avgCPU = avgCPU + matrix[:,idx];         
    end
    avgCPU = avgCPU ./ 16.0;

end

