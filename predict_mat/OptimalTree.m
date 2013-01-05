function R=OptimalTree(T,V,Header)
% function R=OptimalTree(T,V,Header)
% finds the optimal model based on an iteration of the Min Num of Instance
% Copyright : Gerald Corzo
% 2007

LocalRunTime=zeros(1,27);
RMSE=zeros(1,27);

Tstart=tic;
for i=3:30
    Temp=RunWeka(T,V,3,i);%[Header ' MinNumberInstances=' num2str(i) ]);
    RMSE(i-2)=Temp.Error.RMSE;
    LocalRunTime(i-2)=toc(Tstart);
end

[R.MinRMSE I]=min(RMSE);
R.T=RunWeka(T,T,3,I);%['MinNumberInstances' num2str(I+2) ' - RMSE = ' num2str(R.MinRMSE)]);
% R.V=RunWeka(T,V,3,I);
R.V=TestWeka(V,3,num2str(I));

R.I=I+3;
plot(RMSE,'b.');
saveas(gcf,Header);
close
R.LocalRunTime=LocalRunTime(I);