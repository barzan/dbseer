close all;
figure

Ypredicted =   0.000000031927 .* Callez1(:,1).*Callez1(:,2) ...
    - 0.000128888666 .* Mallez1(:,Com_delete) ...
    + 0.00000180394 .* Mallez1(:,Innodb_row_lock_time)  ...
    + 0.000003876117 .* Mallez1(:,Innodb_rows_updated) ...
    - 0.081137780296 .* log(Mallez1(:,Innodb_log_writes)) ...
    + 0.530226260608;

Yactual = Lallez1(:, 1);

subplot(2,2,1);
plot(Yactual,'b');
hold on;
plot(Ypredicted, 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (entire data)');
grid;

subplot(2,2,2);
plot(Yactual(1:1000),'b');
hold on;
plot(Ypredicted(1:1000), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (zoomed in)');
grid;

subplot(2,2,3);
plot(Yactual(36000:36500),'b');
hold on;
plot(Ypredicted(36000:36500), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (easiest workload)');
grid;


subplot(2,2,4);
plot(Yactual(34900:35000),'b');
hold on;
plot(Ypredicted(34900:35000), 'r');

l = legend('actual latency','predicted latency (closed formula)');
ylabel('Latency (sec)');
xlabel('Time');
title('Latency Prediction (zoomed in)');
grid;