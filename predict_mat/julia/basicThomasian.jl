function basicThomasian(givenT, givenJ, givenF, givenK, givenRegions, givenD, givenS0, givenS, givenG)
	#%realistic is the implementation of the "On a more realistic lock
	#%contention model and its analysis" paper from ICDE 1994
	T_total = givenT; #% the requested TPS
	J = givenJ; #%  of transaction classes, e.g. in TPC-C, J=5
	f = givenF; #% frequency matrix for each class
	K = givenK; #% the number of locks requested in each transaction class
	regions = givenRegions; #% number of database regions, perhaps should be equal to the number of tables?! 
	D = givenD; #% number of data items in the i'th DB region
	maxK = Max(K)
	S = givenS; #% S_jn is the processing of the n'th step of a transaction of class C_j
	S0 = givenS0; #% Barzan assumes that everything that precedes the first lock is constant time for all transactions
	g = givenG; #%g_jni is the probability that a tran type j access the i'the database region in its n'th step
	cores = Inf;
	
	R = 0;
	M_total = 0;
	Vp = 0;
	V = 0;
	Pcon = 0;
	L = 0;


	if length(f)!=J || length(K)!=J || length(D)!= regions || size(S,1) != J || size(S,2) != maxK
		error("Invalid input arguments");
	end

	for j=1:J
		for n=1:K[j]
			if abs(sum(g[j,n,:]) - 1.0) > 1e-7
				j
				n
				#%g[j,n,:]
				why=sum(g[j,n,:])
				error("The access pattern g is malformed");
			end
		end
	end

	K_total = dot(vec(f),vec(K));

	D_total = sum(D);

	U = zeros(convert(Int,J),convert(Int,maxK)); #% initialization
	for j=1:J
		U[j,1:K[j]] = 0;
	end


	deltaW=1000; W = 1000;
	iter = 0;

	ClientsNum = 160;

	while abs(deltaW)> 0.001 && !isnan(W) && W!=Inf && iter <=100 #% repetetive process
		iter = iter + 1;
		Scum = cumsum(S,2);
		Ucum = cumsum(U,2);

		B = sum(U',1); #%3.2: B_j=delay of tran type j due to blocking

		B_total = dot(vec(f),vec(B)); #%3.1: B_total=mean blocking time over all trans classes

		R = S0 + sum(S'+U',1); #%3.1: R_j=latency of tran type J

		beta = Div(B,R); #%3.12: beta_j=fraction of time trans of type J are blocked=fraction of trans type J that are blocked 

		L = zeros(convert(Int,J),convert(Int,regions)); #%3.6: L_ji= of locks held by trans of type j in DBR i
		Lp = zeros(convert(Int,J),convert(Int,regions)); #%3.6: Lp_ji=locks held by blocked trans of type j in DBR i
		buf = zeros(size(U))
		for i=1:regions 
			gJK = cumsum(g[:,:,i],2);
			gJKshifted = gJK - g[:,:,i];
			buf = U+0;
			for j=1:length(gJKshifted)
				if gJKshifted[j] == 0
					buf[j] = 0
				end
			end
			tempDot = zeros(size(buf,1),1)
			for j= 1:size(buf,1)
				for k=1:size(buf,2)
					tempDot[j,1]=tempDot[j,1]+buf[j,k]*gJKshifted[j,k]
				end
				#tempDot[j,1] = dot(vec(buf[j,:]),vec(gJKshifted[j,:]))
			end
			Lp[:,i] = (1./ R') .* tempDot
			tempDot2 = zeros(size(S,1),1)
			for j= 1:size(S,1)
				for k=1:size(S,2)
					tempDot2[j,1]=tempDot2[j,1]+S[j,k]*gJK[j,k]
				end
				#tempDot2[j,1] = dot(vec(S[j,:]),vec(gJK[j,:]))
			end
			L[:,i] = (1./ R') .* tempDot2 + Lp[:,i];
		end

		R_total = dot(vec(f), vec(R)); #%3.2: R=mean delay over all trans classes
		M_total = T_total * R_total; #%3.3: M_total=total number of transactions in the system
		if 1==0
			if (M_total>ClientsNum)
				M_total = ClientsNum;
				newR_total = M_total / T_total;
				scaleR = newR_total / R_total;
				U = U * scaleR;
				S0 = S0 * scaleR;
				S = S * scaleR;
				R = S0 + sum(S',1) + sum(U',1);
				R_total = dot(vec(f), vec(R));
			end
		end

	    	if((typeof(T_total) <: Array) && size(f) == size(T_total))
			T = f * T_total;
		elseif(typeof(T_total) <: Array)
			T = f .* T_total[1];
		elseif(typeof(T_total) <: Number)
			T = f .*  T_total;
		end

		#%3.3: T_j=TPS of trans type j
		M = T .* R; #%3.3: M_j=# of trans type j in the system 
		#Julia: change from / to ./
		beta_total = dot(vec(beta), vec(M ./ M_total)); #%3.11: beta_total=fraction of time that trans are blocked in the system, or fraction of blocked trans
		N = zeros(convert(Int,J),convert(Int,J),convert(Int,regions)); #%3.5: N_jli= of exclusive locks held by transations of type l in DBR i, (when considering a trans of type j)
		for j=1:J
			for i=1:regions
				N[:,j,i] = M[j] * L[j,i];
				if M[j]>=1
					N[j,j,i] = (M[j]-1) * L[j,i];
				else
					N[j,j,i] = 0;
				end
			end
		end
	    
		P = zeros(convert(Int,J),convert(Int,maxK),convert(Int,J),convert(Int,regions)); #%3.4: P_jnli=prob of a conflict upon n'th lock request of tran type j with a tran of type l in DBR i
		for j=1:J
			for n=1:K[j]
				for jj=1:J
					for i=1:regions
					    P[j,n,jj,i] = Div(g[j,n,i] * N[j,jj,i], D[i]);
					end
				end
			end
		end

		Pcon = zeros(1, convert(Int,J)); #%3.12: Pcon_j=prob of lock conflict PER LOCK request for transactions of type j
		for j=1:J
			Pcon[j] = Div(sum(P[j,:,:,:]) , K[j]);
		end

		Pcon_total = dot(vec(Div(f .* K , K_total)), vec(Pcon)); #%3.13: Pcon_total=prob of lock conflict per lock request

		rhop = Div(Lp, L); #%3.9: rhop_ji=prob of a lock conflict with a blocked trans of type j which requested a lock in DBR i
	    
		Q = zeros(convert(Int,J), convert(Int,regions)); #%3.11: Q_ji=prob of lock conflict by trans of type j requesting a lock in DBR i
		for j=1:J
			for i=1:regions
				Q[j,i] = (1/K[j]) * sum(P[j,:,:,i]);
			end
		end

		rho = Div(sum(rhop .* Q) , sum(Q)); #%3.10: rho=prob that a requested lock is held by a blocked trans when there is a lock conflict
	    
		H = zeros(1, convert(Int,regions)); #% the normalization constant
		for i=1:regions
			H[i] = sum (f .* sum((S.*cumsum(g[:,:,i],2))',1));
		end
	    
		Vp = zeros(1, convert(Int,regions)); #%3.6: Vp_i= mean waiting time w.r.t. the active transactions in DBR i
		for i=1:regions
			for j=1:J
				tempJ = 0;
				for n=1:K[j]
					tempK = 0;
					for m=1:n
						if Ucum[j,n]==Inf
							buf = Inf;
							if Ucum[j,K[j]] < Ucum[j,n]
								error("Something went wrong with a running sum!");
							end
						else
							buf = Ucum[j,K[j]]-Ucum[j,n];
						end
						tempK = tempK + bmult(g[j,m,i], (Scum[j,K[j]]-Scum[j,n]+S[j,n] + buf) );
					end
					tempJ = tempJ + S[j,n] * tempK;
				end
				Vp[i] = Vp[i] + bmult(f[j], tempJ);
			end
		end    
		Vp = Div(Vp, H);
	    
		Vp_total = 0; #%3.7: Vp_total=mean waiting time w.r.t. active transactions in all DBRs
		for j=1:J
			tempI = 0;
			for i=1:regions
				tempI = tempI + bmult(Vp[i], sum(P[j,:,:,i]));
			end
			Vp_total = Vp_total + Div(bmult(f[j], tempI), Pcon[j]);
		end    
		Vp_total = Div(Vp_total , K_total);
	    
		V = Vp + Div(Vp_total* rho*(1+rho) , (2*(1-rho)^2)); #%3.8: V_i=blocking time when transactions encounter a lock conflict in DBR i
	    
		old_W = W;
		W = 0; #%3.14: W=mean waiting time per lock conflict
		for j=1:J
			tempI = 0;
			for i=1:regions
				buf = sum(P[j,:,:,i]);
				if buf!=0; 
					tempI = tempI + V[i]*buf;
				end
			end
			W = W + Div(bmult(f[j], tempI) , Pcon[j]);
		end    
		W = Div(W , K_total);
	    
		deltaW = W - old_W;

		#%finally
		for j=1:J
			for n=1:K[j]
				tempI = 0;
				for i=1:regions
					tempI = tempI + bmult(V[i], sum(P[j,n,:,i]));
				end
				U[j,n] = tempI+0;  #%3.2: U_jn=mean delay incurred by trans type j when they encounter a lock conflict at step n 
			end
		end

		if typeof(M_total) <: Array
			if M_total[1] * (1-beta_total) > cores
				S0 = givenS0 * M_total[1] * (1-beta_total) /cores;
				S = givenS .* M_total[1] * (1-beta_total) /cores;
			end
		else
			if M_total * (1-beta_total) > cores
				S0 = givenS0 * M_total * (1-beta_total) /cores;
				S = givenS .* M_total * (1-beta_total) /cores;
			end
		end
	end

	#%fprintf(1,'iter deltaW  R W T V=');
	#%[iter deltaW R W T V]
	#%U

	#%R
	#%T
	#%M_total
	
	return R,T_total,M_total,Vp,V,W,U,Pcon,L

end
