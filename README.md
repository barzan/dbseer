# DBSeer

DBSeer is a historical research prototype for predicting database resource use and performance from workload measurements. It was developed in support of the DBSeer research project; it is preserved here for reproducibility and study, not as a currently maintained production database-management tool.

## Historical status and supported stack

The checked-in installation guide describes the stack on which this version was developed and tested: MATLAB R2007b+ or GNU Octave 4.0.0+, Julia 0.3.10+, Java 7+, and a Linux server running `dstat`. Its supported historical targets are MySQL, MariaDB, and PostgreSQL. Modern MATLAB/Octave, Julia, Java, DBMS, and operating-system versions have not been qualified by this repository.

The repository includes a historical middleware copy and a GUI front end. The current middleware project is referenced from the [installation guide](INSTALL.md); do not assume that the bundled historical middleware supports current systems.

## Setup and build

Read [INSTALL.md](INSTALL.md) before building or running DBSeer. It contains the documented dependencies, the Ant command for building `dbseer_front_end`, configuration guidance, and the GUI launch command. The repository also includes the historical user and Docker usage guides referenced there.

MATLAB helpers required by the public release are vendored in [`common_mat`](common_mat). No separate shared-helper checkout is required.

## Citing DBSeer

```bibtex
@inproceedings{mozafari2013dbseer,
  title     = {DBSeer: Resource and Performance Prediction for Building a Next Generation Database Cloud},
  author    = {Mozafari, Barzan and Curino, Carlo and Madden, Samuel},
  booktitle = {CIDR},
  year      = {2013}
}
```

## License

DBSeer is released under the [Apache License 2.0](LICENSE). Third-party components retain their own notices and licenses.
