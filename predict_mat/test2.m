% i: isolated, b:binary, a:all possible mixtures
[iC1 iCC1 iL1 iM1] = loadAligned('processed','i1t1-2500',1201,72);
iB1 = [iC1 iCC1 iL1 iM1];
[iC2 iCC2 iL2 iM2] = loadAligned('processed','i2t1-10000',1201,72);
iB2 = [iC2 iCC2 iL2 iM2];
[bC12 bCC12 bL12 bM12] = loadAligned('processed','b12t2-10000',1201,72);
bB12 = [bC12 bCC12 bL12 bM12];
[aC12 aCC12 aL12 aM12] = loadAligned('processed','b12t1000',1201,72);
aB12 = [aC12 aCC12 aL12 aM12];

tranHead = 'Tran1,Tran2,Tran3,Tran4,Tran5';
tran2Head = 'Tran12,Tran13,Tran14,Tran15,Tran23,Tran24,Tran25,Tran34,Tran35,Tran45';
latHead = 'Latency1,Latency2,Latency3,Latency4,Latency5';
monHead = monitorHeaderGen(1:1:437);
monNoIndivCpuHead = monitorHeaderGen([1:1:(cpu1_usr-1) (cpu16_siq+1):1:437]);

totalHead = horzcat(tranHead,',',tran2Head,monHead,',latency1');
totalHeadnoind = horzcat(tranHead,',',tran2Head,monNoIndivCpuHead,',latency1');

produceWekaFile(totalHead,[iC1 iCC1 iM1 iL1(:,1)],'weka/iB1.csv');
produceWekaFile(totalHeadnoind,[iC1 iCC1 iM1(:,[1:1:(cpu1_usr-1) (cpu16_siq+1):1:437]) iL1(:,1)],'weka/iB1-noind.csv');



