function [mix] = getMixture(mv)

start_col = 1;
numRow = size(mv.clientIndividualSubmittedTrans, 1);
totalTransactionCount = zeros(numRow, mv.numOfTransType(1));

for i=1:size(mv.numOfTransType, 2)
    end_col = start_col + mv.numOfTransType(i) - 1;
    totalTransactionCount = totalTransactionCount + mv.clientIndividualSubmittedTrans(:,start_col:end_col);
    end_col = start_col + mv.numOfTransType(i) - 1;
end
total = sum(sum(totalTransactionCount,2), 1);
mix = sum(totalTransactionCount, 1) ./ total;

end % end function
