function [dirtyPages flushRate] = steadyDF(D, RowsChanged)
dirtyPages = 0;
prevFlushRate = 1e+20;
flushRate = 0;

bestC = [2027811.6708307797 2000000.0000000000 1.0000000000 0.8346540331 76443.8419762718 78408.0000000000 D]; %t12345-memless-long

while abs(prevFlushRate-flushRate) > 0.01
    prevFlushRate = flushRate;
    
    dirtyPages = dirtyPages + mapRowsToPages(D-dirtyPages, RowsChanged.*(D-dirtyPages)/D) - flushRate; 
    flushRate = estimateFlushRate(bestC, RowsChanged);
end


end

