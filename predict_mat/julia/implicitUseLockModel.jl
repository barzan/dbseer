function implicitUseLockModel(lockType,conf,data)
	
	Val = ""
	if lockType == "waitTime"
		Val = useLockModel([0.125 0.0001 conf], data, "TPCC").TimeSpentWaiting
	elseif lockType == "numberOfLocks"
		Val = useLockModel([0.125 0.0001 conf], data, "TPCC").LocksBeingHeld
	elseif lockType == "numberOfConflicts"
		Val = useLockModel([0.125 0.0001 conf], data, "TPCC").totalWaits
	else
		error(string("Invalid lockType:", lockType))
	end
	return Val
end
