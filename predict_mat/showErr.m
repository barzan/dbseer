function errString = showErr(name, predictions, actual)
[rel_err abs_err rel_diff discrete_rel_error weka_rel_err] = myerr(predictions, actual);

errString = horzcat(name,'(',num2str(rel_err*100),'%,dis=',num2str(discrete_rel_error*100),'%,abs=',num2str(abs_err),')');

end

