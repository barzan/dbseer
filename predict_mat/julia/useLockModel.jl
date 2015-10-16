function useLockModel(conf, counts, workloadName)
	tps,J,f,K,regions,D,S0,S,g = prepareLockModel(1, conf, counts[1,:], workloadName);
	nRows = size(counts, 1);
	all_R =zeros(nRows, convert(Int,J));
	all_T_total =zeros(nRows, 1);
	all_M_total =zeros(nRows, 1);
	all_Vp =zeros(nRows, 9);
	all_V =zeros(nRows, 9);
	all_W =zeros(nRows, 1);
	all_Pcon =zeros(nRows, convert(Int,J));
	all_totalWaits =zeros(nRows, convert(Int,J));
	all_TimeSpentWaiting =zeros(nRows, convert(Int,J));
	all_LocksBeingHeld =zeros(nRows, convert(Int,J));

	for row=1:nRows
		tps,J,f,K,regions,D,S0,S,g = prepareLockModel(1, conf, counts[row,:], workloadName);
		filter = f ./ f;
		for j=1:length(filter)
			if isnan(filter[j])
				filter[j] = 0;
			end
		end
	    
		R,T_total,M_total,Vp,V,W,U,Pcon,L = basicThomasian(tps, J, f, K, regions, D, S0, S, g);
		#% R_j=latency of tran type J
		#% T_total = givenT; % the requested TPS
		#% M_total=total number of transactions in the system
		#% Vp_i= mean waiting time w.r.t. the active transactions in DBR i
		#% V_i=blocking time when transactions encounter a lock conflict in DBR i
		#% W=mean waiting time per lock conflict
		#% U_jn=mean delay incurred by trans type j when they encounter a lock conflict at step n 
		#% Pcon_j=prob of lock conflict PER LOCK request for transactions of type j
		#% L_ji= of locks held by trans of type j in DBR i

		all_R[row,:]=R .* filter;
		if(length(T_total)==1) 
			T_total=T_total[1]
		end
		all_T_total[row,:]= T_total;
		if(length(M_total)==1) 
			M_total=M_total[1]
		end
		all_M_total[row,:] = M_total;
		all_Vp[row,:] = Vp;
		all_V[row,:] = V;
		if(length(W)==1) 
			W=W[1]
		end
		all_W[row,:] = W;
		all_Pcon[row,:] = Pcon .* filter;
		all_totalWaits[row,:] = Pcon .* K .* f * T_total[1] .* filter;
		all_TimeSpentWaiting[row,:] = Pcon .* K .* f * T_total[1] * W .* filter;

		all_LocksBeingHeld[row, :] = sum((f * T_total[1]) * L, 2)[1];
	end

	all_totalWaits2 = zeros(size(all_totalWaits,1),1)
	all_TimeSpentWaiting2 = zeros(size(all_TimeSpentWaiting,1),1)
	all_LocksBeingHeld2 = zeros(size(all_LocksBeingHeld,1),1)
	for i=1:size(all_totalWaits,1)
		all_totalWaits2[i] = dot(vec(all_totalWaits[i,:]),vec(counts[i,:]))
	end
	for i=1:size(all_TimeSpentWaiting,1)
		all_TimeSpentWaiting2[i] = dot(vec(all_TimeSpentWaiting[i,:]),vec(counts[i,:]))
	end
	for i=1:size(all_LocksBeingHeld,1)
		all_LocksBeingHeld2[i] = dot(vec(all_LocksBeingHeld[i,:]),vec(counts[i,:]))
	end

	return typeOfPredictions(all_R,all_T_total,all_M_total,all_Vp, all_V,all_W, all_Pcon,  all_totalWaits2,all_TimeSpentWaiting2,all_LocksBeingHeld2)

end
