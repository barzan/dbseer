function prepareLockModel(initialize, conf, counts, workloadName)
	if !isdefined(:oldConfigForPrepareLockModel)
		global oldConfigForPrepareLockModel = ""
	else 
		global oldConfigForPrepareLockModel
	end

	if !isdefined(:oldWorkloadNameForPrepareLockModel)
		global oldWorkloadNameForPrepareLockModel = ""
	else
		global oldWorkloadNameForPrepareLockModel
	end

	if !isdefined(:JForPrepareLockModel)
		global JForPrepareLockModel = 1
	else
		global JForPrepareLockModel
	end

	if !isdefined(:KForPrepareLockModel)
		global KForPrepareLockModel = []
	else
		global KForPrepareLockModel
	end

	if !isdefined(:regionsForPrepareLockModel)
		global regionsForPrepareLockModel = 1
	else
		global regionsForPrepareLockModel
	end

	if !isdefined(:DForPrepareLockModel)
		global DForPrepareLockModel = []
	else
		global DForPrepareLockModel
	end

	if !isdefined(:SForPrepareLockModel)
		global SForPrepareLockModel = []
	else
		global SForPrepareLockModel
	end

	if !isdefined(:gForPrepareLockModel)
		global gForPrepareLockModel = []
	else
		global gForPrepareLockModel
	end

	if !isdefined(:readRowsForPrepareLockModel)
		global readRowsForPrepareLockModel = []
	else
		global readRowsForPrepareLockModel
	end

	if !isdefined(:updatedRowsForPrepareLockModel)
		global updatedRowsForPrepareLockModel = []
	else
		global updatedRowsForPrepareLockModel
	end


	#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	if workloadName == "LOCK1"
		beginCost = conf[1];
		commitCost = conf[2];
	elseif workloadName == "TPCC"
		beginCost = conf[1];
		interLockInterval = conf[2];
		DomainMultiplier =  conf[3]; #%0.0000000100;
		costMultiplier= conf[4];
	else
		error(string("Unknown workloadName: ", workloadName));
	end
	nano = 1000000000.0;
	#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	if initialize==1 || !isequal(conf, oldConfigForPrepareLockModel) || (workloadName != oldWorkloadNameForPrepareLockModel)
		oldConfigForPrepareLockModel = conf;
		oldWorkloadNameForPrepareLockModel = workloadName;
		if workloadName == "LOCK1"
			JForPrepareLockModel = 1; #%  of transaction classes, e.g. in TPC-C, J=5
			f = [1.0]; #% frequency matrix for each class
			KForPrepareLockModel = [2]; #% the number of locks requested in each transaction class
			regionsForPrepareLockModel = 1; #% number of database regions, perhaps should be equal to the number of tables?! 
			DForPrepareLockModel = [1024]; #% number of data items in the i'th DB region
			maxK = Max(vec(KForPrepareLockModel));
			SForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel),convert(Int,maxK)); #% S_jn is the processing of the n'th step of a transaction of class C_j
			S0 = beginCost; #% Barzan assumes that everything that precedes the first lock is constant time for all transactions
			SForPrepareLockModel[1,1] = 0.001;
			SForPrepareLockModel[1,2] = 0.001 + commitCost;

			gForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel),convert(Int,maxK),convert(Int,regionsForPrepareLockModel));
			gForPrepareLockModel[1,:,1] = 1.0;
		elseif workloadName == "TPCC"
			JForPrepareLockModel = 5;
			f = [0.2 0.2 0.2 0.2 0.2];
			KForPrepareLockModel = [136 8 6 10 153]; #% info for transaction type 4 is not accurate!
			regionsForPrepareLockModel = 9;
			customer=1; district=2; history=3; item=4; new_order=5; oorder=6; order_line=7; stock=8; warehouse=9;
			DForPrepareLockModel = zeros(1, convert(Int,regionsForPrepareLockModel));
			DForPrepareLockModel[customer] = 960000; DForPrepareLockModel[district] = 320; DForPrepareLockModel[history] = 4390440; #% specs says 960000
			DForPrepareLockModel[item]=100000; DForPrepareLockModel[new_order]=42;#%or 0? or 32*9000
			DForPrepareLockModel[oorder] = 5255647; #% according to specs this should be 960000!
			DForPrepareLockModel[order_line]=32*300000;#%not sure about this one!
			DForPrepareLockModel[stock]=3200000; DForPrepareLockModel[warehouse]=32;

			maxK = Max(vec(KForPrepareLockModel));
			SForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel),convert(Int,maxK));
			S0 = beginCost + dot(vec([2096  0.1*6403+0.9*3608 1753 1543 4170]), vec(f)) / nano;
			SForPrepareLockModel[1,1:136] = [interLockInterval 727780 609558 564452 604045 666491 574574 736047 568746 730061 563094 722544 563341 715844 565713 734241 562731 727079 570391 727549 562944 723694 569319 720982 571238 729174 repmat([interLockInterval], 1, 99) 2305666 repmat([interLockInterval], 1, 9) 5241200];
			SForPrepareLockModel[2,1:8] = 0.1*[589150 623830 590536 644205 946331 647169 797044 3018040] + 0.9*[586701 618534 588888 643966 950617 0 700665 3515563];    
			SForPrepareLockModel[3,1:6] = [952166 77273 repmat([interLockInterval], 1, 3) 7834434];
			SForPrepareLockModel[4,1:10] = [640781 626863 621894 617058 617356 615745 613847 614119 615625 3111715];
			SForPrepareLockModel[5,1:153] = [591071 repmat([interLockInterval], 1, 151) 8500096];

			S0 = beginCost + (S0-beginCost) * costMultiplier;
			SForPrepareLockModel = (SForPrepareLockModel/nano) * costMultiplier;

			readRowsForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel), convert(Int,regionsForPrepareLockModel)); #%b_ji= the probability that 
			updatedRowsForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel), convert(Int,regionsForPrepareLockModel));

			gForPrepareLockModel = zeros(convert(Int,JForPrepareLockModel),convert(Int,maxK),convert(Int,regionsForPrepareLockModel));
			gForPrepareLockModel[1,1,customer] = 1.0;
			gForPrepareLockModel[1,2,warehouse] = 1.0;
			gForPrepareLockModel[1,3,district] = 1.0;
			gForPrepareLockModel[1,4,new_order] = 1.0;
			gForPrepareLockModel[1,5,district] = 1.0;
			gForPrepareLockModel[1,6,oorder] = 1.0;
			gForPrepareLockModel[1,[7  9 11 13 15 17 19 21 23 25],item] = 1.0;
			gForPrepareLockModel[1,[8 10 12 14 16 18 20 22 24 26],stock] = 1.0;
			gForPrepareLockModel[1,27:126,order_line] = 1.0;
			gForPrepareLockModel[1,127:136,stock] = 1.0;
			readRowsForPrepareLockModel[1,customer] = 1;
			readRowsForPrepareLockModel[1,warehouse] = 1;
			updatedRowsForPrepareLockModel[1,district] = 2;
			readRowsForPrepareLockModel[1,new_order] = 1; #% insert
			readRowsForPrepareLockModel[1,oorder] = 1; #%insert
			readRowsForPrepareLockModel[1,item] = 10;
			updatedRowsForPrepareLockModel[1,stock] = 10; #% I only count them when they get the locks with "select for update"
			readRowsForPrepareLockModel[1,order_line] = 100; #% inserts
			gForPrepareLockModel[2,1,warehouse] = 1.0;
			gForPrepareLockModel[2,2,warehouse] = 1.0;
			gForPrepareLockModel[2,3,district] = 1.0;
			gForPrepareLockModel[2,4,district] = 1.0;
			gForPrepareLockModel[2,5,customer] = 1.0;
			gForPrepareLockModel[2,6,customer] = 1.0;
			gForPrepareLockModel[2,7,customer] = 1.0;
			gForPrepareLockModel[2,8,history] = 1.0; #% the trace says table 't' which I guess means 'history'
			readRowsForPrepareLockModel[2,warehouse] = 1;
			updatedRowsForPrepareLockModel[2,warehouse] = 1;
			readRowsForPrepareLockModel[2,district] = 1;
			updatedRowsForPrepareLockModel[2,district] = 1;
			readRowsForPrepareLockModel[2,customer] = 2;
			updatedRowsForPrepareLockModel[2,customer] = 1;
			readRowsForPrepareLockModel[2,history] = 1; #% insert    
			gForPrepareLockModel[3,1,customer] = 1.0;
			gForPrepareLockModel[3,2,oorder] = 1.0;
			gForPrepareLockModel[3,3:6,order_line] = 1.0;
			readRowsForPrepareLockModel[3,customer] = 1;
			readRowsForPrepareLockModel[3,oorder] = 1;
			readRowsForPrepareLockModel[3,order_line] = 4;
		    
			#% For transaction type 4:
			#% with probability (659+665) / (659+665+10774) = 0.11:
			#%    w_new_order_1 -> r_oorder_1 ->  w_oorder_1 -> w_order_line_5 ->  r_order_line_5 -> w_customer_1 -> 
			#% with prob 1:
			#%    r_new_order_10 
			tempA = 0.11;      
			pBranch = 1-(1-tempA)^0.1;
			gForPrepareLockModel[4,1:10,new_order] = 1.0 - 3*pBranch;
			readRowsForPrepareLockModel[4,new_order] = 10; #% I have to yse number of rows as a probability instead of a simple count! this way I can also account for other types of transaction 4    
			updatedRowsForPrepareLockModel[4, new_order] = tempA/(1.0 - 3*pBranch); #% to make sure in the end we update the new_order w prob 0.11 too!            
			gForPrepareLockModel[4,1:10,oorder] = pBranch; #% this way the overall prob that tran type 4 accesses oorder adds up to tempA !
			readRowsForPrepareLockModel[4,oorder] = 1;
			updatedRowsForPrepareLockModel[4,oorder] = 1;
			gForPrepareLockModel[4,1:10,order_line] = pBranch;
			updatedRowsForPrepareLockModel[4,order_line] = 5; 
			readRowsForPrepareLockModel[4,order_line] = 5;
			gForPrepareLockModel[4,1:10,customer] = pBranch;
			updatedRowsForPrepareLockModel[4,customer] = 1;

			#%w_new_order_1 w_oorder_1 w_order_line_5 w_customer_1

			gForPrepareLockModel[5,1,district] = 1.0;
			gForPrepareLockModel[5,2:152,order_line] = 1.0;
			gForPrepareLockModel[5,153,stock] = 1.0;
			readRowsForPrepareLockModel[5,district] = 1;
			readRowsForPrepareLockModel[5,order_line] = 151;
			readRowsForPrepareLockModel[5,stock] = 1;    

		else
			error(string("Unknown workloadName: ", workloadName));
		end
	end #% end of initialization
	#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	#% This is the main part!

	f = counts;
	tps = sum(f,2);
	if length(tps) == 1
		tps = tps[1]
	end
	
	#Julia: change from / to ./
	f = f ./ tps;

	S0 = beginCost + (dot(vec([2096  0.1*6403+0.9*3608 1753 1543 4170]), vec(f)) / nano) * costMultiplier;

	b = zeros(1, convert(Int,regionsForPrepareLockModel));
	for i=1:regionsForPrepareLockModel
		sm = (readRowsForPrepareLockModel[:,i]+updatedRowsForPrepareLockModel[:,i]);
		for j=1:length(sm)
			if sm[j] == 0
				sm[j] = 123456
			end
		end
		b[i] = dot(vec(f), vec((updatedRowsForPrepareLockModel[:,i] ./ sm)'));
	end
	
	for j=1:length(b)
		if b[j] == 0
			b[j] = 0.0000000100 #%DomainMultiplier;
		end
	end
	DForPrepareLockModel = DForPrepareLockModel ./ (1- (1-b.*b)); #% when we do have exclusive locks we should not shrink all the tables!

	DForPrepareLockModel = DForPrepareLockModel * DomainMultiplier;
	#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	tps_ = tps;
	J_ = JForPrepareLockModel;
	f_ = f;
	K_ = KForPrepareLockModel;
	regions_ = regionsForPrepareLockModel; 
	D_ = DForPrepareLockModel;
	S0_ = S0;
	S_ = SForPrepareLockModel;
	g_ = gForPrepareLockModel;
	return tps_,J_,f_,K_,regions_,D_,S0_,S_,g_
end
