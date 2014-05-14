function [xMaxThroughput yMaxThroughput] = findMaxThroughput(SubmittedTransactions)
    sm1 = 1000; %1000: lower part, 5000:middle
    sm2 = 10;
    mslope = 10; %lower this number sooner it declares a max throughput!
    
    sTPS = DoSmooth(SubmittedTransactions, sm1);
    %sTPS = smooth(SubmittedTransactions, sm1);
    %sTPS = SubmittedTransactions;
    if isOctave
        I = vl_localmax(sTPS');
    else
        [Y I] = localmax(sTPS', 1, false);
    end

    xMaxThroughput = I(find(diff(DoSmooth(I,sm2))<mslope, 1, 'first') + 1);
    yMaxThroughput = sTPS(xMaxThroughput);

end

