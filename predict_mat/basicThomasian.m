function [R T_total M_total Vp V W U Pcon L] = basicThomasian(givenT, givenJ, givenF, givenK, givenRegions, givenD, givenS0, givenS, givenG)
%realistic is the implementation of the "On a more realistic lock
%contention model and its analysis" paper from ICDE 1994
T_total = givenT; % the requested TPS
J = givenJ; % # of transaction classes, e.g. in TPC-C, J=5
f = givenF; % frequency matrix for each class
K = givenK; % the number of locks requested in each transaction class
regions = givenRegions; % number of database regions, perhaps should be equal to the number of tables?! 
D = givenD; % number of data items in the i'th DB region
maxK = max(K);
S = givenS; % S_jn is the processing of the n'th step of a transaction of class C_j
S0 = givenS0; % Barzan assumes that everything that precedes the first lock is constant time for all transactions
g = givenG; %g_jni is the probability that a tran type j access the i'the database region in its n'th step

cores = Inf;

if length(f)~=J || length(K)~=J || length(D)~= regions || size(S,1) ~= J || size(S,2) ~= maxK
    error('Invalid input arguments');

end

for j=1:J
    for n=1:K(j)
        if abs( sum(g(j,n,:)) - 1.0) > 1e-7
            j
            n
            %g(j,n,:)
            why=sum(g(j,n,:))
            error('The access pattern g is malformed');
        end
    end
end

K_total = dot(f,K);
D_total = sum(D);

U = zeros(J,maxK); % initialization
for j=1:J
    U(j,1:K(j)) = 0;
end


deltaW=1000; W = 1000;
iter = 0;

ClientsNum = 160;

while abs(deltaW)> 0.001 && ~isnan(W) && W~=Inf && iter <=100 % repetetive process
    iter = iter + 1;
    Scum = cumsum(S,2);
    Ucum = cumsum(U,2);
    
    B = sum(U'); %3.2: B_j=delay of tran type j due to blocking
    B_total = dot(f,B); %3.1: B_total=mean blocking time over all trans classes
    
    R = S0 + sum(S'+U'); %3.1: R_j=latency of tran type J
        
    beta = div(B,R); %3.12: beta_j=fraction of time trans of type J are blocked=fraction of trans type J that are blocked 
    
    L = zeros(J,regions); %3.6: L_ji=# of locks held by trans of type j in DBR i
    Lp = zeros(J,regions); %3.6: Lp_ji=#locks held by blocked trans of type j in DBR i
    for i=1:regions;   
        gJK = cumsum(g(:,:,i),2);
        gJKshifted = gJK - g(:,:,i);
        buf = U;
        buf(gJKshifted==0)=0;
        Lp(:,i) = (1./ R') .* dot(buf, gJKshifted, 2);
        L(:,i) = (1./ R') .* dot(S, gJK, 2) + Lp(:,i);
    end
    
    R_total = dot(f, R); %3.2: R=mean delay over all trans classes
    M_total = T_total * R_total; %3.3: M_total=total number of transactions in the system
    if 1==0
    if (M_total>ClientsNum)
        M_total = ClientsNum;
        newR_total = M_total / T_total;
        scaleR = newR_total / R_total;
        U = U * scaleR;
        S0 = S0 * scaleR;
        S = S * scaleR;
        R = S0 + sum(S') + sum(U');
        R_total = dot(f, R);
    end
    end
    
    T = f * T_total; %3.3: T_j=TPS of trans type j
    M = T .* R; %3.3: M_j=# of trans type j in the system 
    beta_total = dot(beta, M / M_total); %3.11: beta_total=fraction of time that trans are blocked in the system, or fraction of blocked trans

    N = zeros([J J regions]); %3.5: N_jli=# of exclusive locks held by transations of type l in DBR i, (when considering a trans of type j)
    for j=1:J
        for i=1:regions
            N(:,j,i) = M(j) * L(j,i);
            if M(j)>=1
                N(j,j,i) = (M(j)-1) * L(j,i);
            else
                N(j,j,i) = 0;
            end
        end
    end
    
    P = zeros([J maxK J regions]); %3.4: P_jnli=prob of a conflict upon n'th lock request of tran type j with a tran of type l in DBR i
    for j=1:J
        for n=1:K(j)
            for jj=1:J
                for i=1:regions
                    P(j,n,jj,i) = div(g(j,n,i) * N(j,jj,i), D(i));
                end
            end
        end
    end
    
    Pcon = zeros(1, J); %3.12: Pcon_j=prob of lock conflict PER LOCK request for transactions of type j
    for j=1:J
        Pcon(j) = div(sum(sum(sum(P(j,:,:,:)))) , K(j));
    end
    
    Pcon_total = dot(div(f .* K , K_total), Pcon); %3.13: Pcon_total=prob of lock conflict per lock request

    rhop = div(Lp, L); %3.9: rhop_ji=prob of a lock conflict with a blocked trans of type j which requested a lock in DBR i
    
    Q = zeros(J, regions); %3.11: Q_ji=prob of lock conflict by trans of type j requesting a lock in DBR i
    for j=1:J
        for i=1:regions
            Q(j,i) = (1/K(j)) * sum(sum(P(j,:,:,i)));
        end
    end

    rho = div(sum(sum(rhop .* Q)) , sum(sum(Q))); %3.10: rho=prob that a requested lock is held by a blocked trans when there is a lock conflict
    
    H = zeros(1, regions); % the normalization constant
    for i=1:regions
       H(i) = sum (f .* sum((S.*cumsum(g(:,:,i),2))') );
    end
    
    Vp = zeros(1, regions); %3.6: Vp_i= mean waiting time w.r.t. the active transactions in DBR i
    for i=1:regions
       for j=1:J
          tempJ = 0;
          for n=1:K(j)
             tempK = 0;
             for m=1:n
                if Ucum(j,n)==Inf
                    buf = Inf;
                    if Ucum(j,K(j)) < Ucum(j,n)
                        error('Something went wrong with a running sum!');
                    end
                else
                    buf = Ucum(j,K(j))-Ucum(j,n);
                end
                tempK = tempK + bmult(g(j,m,i), (Scum(j,K(j))-Scum(j,n)+S(j,n) + buf) );
             end
             tempJ = tempJ + S(j,n) * tempK;
          end
          Vp(i) = Vp(i) + bmult(f(j), tempJ);
       end
    end    
    Vp = div(Vp, H);
    
    Vp_total = 0; %3.7: Vp_total=mean waiting time w.r.t. active transactions in all DBRs
    for j=1:J
        tempI = 0;
        for i=1:regions
            tempI = tempI + bmult(Vp(i), sum(sum(P(j,:,:,i))));
        end
        Vp_total = Vp_total + div(bmult(f(j), tempI), Pcon(j));
    end    
    Vp_total = div(Vp_total , K_total);
    
    V = Vp + div(Vp_total* rho*(1+rho) , (2*(1-rho)^2)); %3.8: V_i=blocking time when transactions encounter a lock conflict in DBR i
    
    old_W = W;
    W = 0; %3.14: W=mean waiting time per lock conflict
    for j=1:J
        tempI = 0;
        for i=1:regions
            buf = sum(sum(P(j,:,:,i)));
            if buf~=0; 
                tempI = tempI + V(i)*buf;
            end
        end
        W = W + div(bmult(f(j), tempI) , Pcon(j));
    end    
    W = div(W , K_total);
    
    deltaW = W - old_W;
    
    %finally
    for j=1:J
        for n=1:K(j)
            tempI = 0;
            for i=1:regions
                tempI = tempI + bmult(V(i), sum(P(j,n,:,i)));
            end
            U(j,n) = tempI;  %3.2: U_jn=mean delay incurred by trans type j when they encounter a lock conflict at step n 
        end
    end
    
    if M_total * (1-beta_total) > cores
        S0 = givenS0 * M_total * (1-beta_total) /cores;
        S = givenS .* M_total * (1-beta_total) /cores;
    end
end

%fprintf(1,'iter deltaW  R W T V=');
%[iter deltaW R W T V]
%U

%R
%T
%M_total


end

