function cl = getConcurrencyLevel(conf, counts)

allPredictions  = useLockModel(conf.lock_conf, counts, conf.workloadName);
if allPredictions.TimeSpentWaiting < 1e-5
    cl = 0; % as if there's not that many people in the system!
else
    cl = allPredictions.M_total;
end

end

