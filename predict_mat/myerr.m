function [rel_err abs_err rel_diff discrete_rel_error weka_rel_err] = myerr( predictions, actualdata )
rel_err = zeros(1,size(predictions,2));
abs_err = zeros(1,size(predictions,2));
rel_diff = zeros(1,size(predictions,2));
discrete_rel_error = zeros(1,size(predictions,2));
weka_rel_err = zeros(1,size(predictions,2));

% This is useful for values such as CPU usage or number of pages flushed!

epsilon = 0; % anything below epsilon will be treated as "effectively zero"
maxPenalty = 1;

for i=1:size(predictions,2)
    act = actualdata(:,i);
    pre = predictions(:,i);
    dact = round(act);
    dpre = round(pre);
    
    abs_err(i) = mean(abs(pre-act));
    
    avgAct = mean(act);
    weka_rel_err(i) = sum(abs(pre-act)) / sum(abs(avgAct-act));
    
    err1 = abs(pre-act) ./ abs(act);
    err1(act<=epsilon) = maxPenalty;
    err1(act==pre) = 0;
    err2 = abs(pre-act) ./ abs(pre);
    err2(pre<=epsilon) = maxPenalty;
    err2(act==pre) = 0;
    
    rel_err(i) = mean(err1);
    rel_diff(i) = mean(max(err1, err2));
    
    err3 = abs(dpre-dact) ./ abs(dact);
    err3(dact<=epsilon) = maxPenalty;
    err3(dact==dpre) = 0;
    
    discrete_rel_error(i) = mean(err3);
    
end



end

