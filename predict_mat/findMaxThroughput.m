function [xMaxThroughput yMaxThroughput] = findMaxThroughput(SubmittedTransactions)
    sm1 = 1000; %1000: lower part, 5000:middle
    sm2 = 10;
    mslope = 10; %lower this number sooner it declares a max throughput!
    
    sTPS = smooth(SubmittedTransactions, sm1);
    [Y I] = localmax(sTPS', 1, false);
    xMaxThroughput = I(find(diff(smooth(I,sm2))<mslope, 1, 'first') + 1);
    yMaxThroughput = sTPS(xMaxThroughput);

end

