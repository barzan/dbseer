config='7200-10-10-10-10-2000';
%header_index;
coef = horzcat('/Users/sina/expr5/deverticalized/coefs-', config, '_rough_trans_count');
monitor=horzcat('/Users/sina/expr5/raw/monitor-', config, '.csv');
C = load(coef);
C = C(2:7200,:);

M=load(monitor);
hold off;
plot(sum(C'), 'g');
hold on;
%plot(M(:,Com_commit)+100, 'r');
plot(M(3+1199:end,Com_commit)+200, 'b');


