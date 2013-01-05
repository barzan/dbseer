overTimeDist = load('err-b12t1000');
%overTimeDist = load('err-i2t1-10000');

totalDist = overTimeDist(end,:);
%overTimeDist = diff(overTimeDist);


figure('Name', 'b12t2-10000');

subplot(2,2,1);

plot(totalDist,'.');
%axis([0 160 0 15000]);
xlabel('Thread (Client) id');
ylabel('Total # of transactions sent');

subplot(2,2,2);

plot(overTimeDist);
xlabel('Time (seconds)');
ylabel('TPS per each thread');

subplot(2,2,3);

plot(overTimeDist(:,1:10));
xlabel('Time (seconds)');
ylabel('TPS per each thread (plotted for 10 threads)');


subplot(2,2,4);

plot(overTimeDist(:,10:11));
xlabel('Time (seconds)');
ylabel('TPS per each thread (plotted for 5 threads)');

