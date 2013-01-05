tc = load('../data/expr5/fine_grained_deverticalization/coefs-7200--0--0--0-50-2000_trans_count');
tcrough = load('../data/expr5/fine_grained_deverticalization/coefs-7200--0--0--0-50-2000_rough_trans_count');

%plot((tc-tcrough)/mean(tc)*100);
plot(sum((tc-tcrough)')'/sum(mean(tc))*100);
ylabel('error (%)');
xlabel('time (sec)');
title('Percentage error for non-fractional transaction counting');
grid;

avgthroughput = sum(mean(tc));
abserr = max(sum((tc-tcrough)'));
fprintf(1,'\nAbsolute max error: %4.4f transactions in 1 sec window (running at %6.2f tps)\n',abserr,avgthroughput);

meanerr = sum(mean((tc-tcrough)));
fprintf(1,'\nAbsolute mean error: %4.4f transactions (The error is almost unbiased)\n ',meanerr);


set(gcf,'Color','w');
