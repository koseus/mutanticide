To apply mutation testing, create a bash script or edit one of the existing scripts.

At the top of a script, there are directory definitions. These directories are the following:
BACKUP_BENCHMARK: a copy of SUT
TEMP_BENCHMARK: another copy of SUT
LAB_PATH: path to Mutanticide on the system
REPORT_PATH: folder in which reports will be created
CLASSPATH: classes to include for successful test execution. If there are more than one, they must be sepearated by a semicolon.

After these definitions are saved, run the script on a bash terminal. Every class in the SUT will be passed to Mutanticide for mutant generation. Then, each mutant is tested against the test suite, and a report file is created for it under REPORT_PATH.

After the script terminates, mutation score can be computed. When in REPORT_PATH, run the following commands:

ll | wc -l
grep -L "DEAD" *

The first command gives mutant count (call this M), the second gives living mutant count (call this L). Mutation score can be computed by (M - L) / M.
