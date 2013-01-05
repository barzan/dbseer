%latency
hold off;
hold on; 
%plot(hist(bL12(:,1), unique(bL12(:,1))), 'g');
%plot(hist(bL14(:,1), unique(bL14(:,1))), 'r');
%plot(hist(bL15(:,1), unique(bL15(:,1))), 'b');

plot(hist(bL12(:,1), 10), 'g');
plot(hist(bL14(:,1), 10), 'r');
plot(hist(bL15(:,1), 10), 'b');



