function models = loadCausalModel(model_directory)

    modelFiles = dir([model_directory '/*.mat']);
    models = {};
    for i=1:length(modelFiles)
        modelFile = [model_directory '/' modelFiles(i).name];
        model = load(modelFile);
        model = model.model;
        
        isCombined = false;
        % combine model with same cause
        for j=1:length(models)
            if strcmp(models{j}.cause, model.cause)
                current_predicates = models{j}.predicates;
                incoming_predicates = model.predicates;
                newPredicateIndex = [];

                % check each predicate
                for k=1:size(incoming_predicates,1)
                    idx = find(strcmp(current_predicates(:,1), incoming_predicates(k,1)));
                    if idx > 0
                        current_pred = current_predicates{idx,2};
                        incoming_pred = incoming_predicates{k,2};
						current_lb = current_pred(1);
						current_ub = current_pred(2);
						incoming_lb = incoming_pred(1);
						incoming_ub = incoming_pred(2);
						new_lb = inf;
						new_ub = inf;
						if current_lb == inf && incoming_lb ~= inf
							new_lb = incoming_lb;
						elseif current_lb ~= inf && incoming_lb == inf
							new_lb = current_lb; 
						elseif current_lb ~= inf && incoming_lb ~= inf
							if current_lb < incoming_lb
								new_lb = current_lb;
							else
								new_lb = incoming_lb;
							end
						end
						if current_ub == inf && incoming_ub ~= inf
							new_ub = incoming_ub;
						elseif current_ub ~= inf && incoming_ub == inf
							new_ub = current_ub; 
						elseif current_ub ~= inf && incoming_ub ~= inf
							if current_ub > incoming_ub
								new_ub = current_ub;
							else
								new_ub = incoming_ub;
							end
						end
						if new_lb == inf && new_ub == inf
							continue;
						end
                        %new_pred = vertcat(current_pred, incoming_pred);
                        new_pred = [new_lb new_ub];
                        models{j}.predicates{idx,2} = new_pred;
                        models{j}.predicates{idx,3}(end+1) = incoming_predicates{k,3};
                        models{j}.predicates{idx,4}(end+1) = incoming_predicates{k,4};
                        models{j}.predicates{idx,5}(end+1) = incoming_predicates{k,5};
                        newPredicateIndex(end+1) = idx;
                        isCombined = true;
                    end
                end
                
                models{j}.predicates = models{j}.predicates(newPredicateIndex, :);
            end
        end
        
        if ~isCombined 
            models{end+1} = model;
        end
    end
end
