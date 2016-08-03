%Simulation definitions
prog_time           = 400;      %Number of hours of programming per stage
writing_test_time   = 100;      %Number of hours of writing test per stage
executing_test_time = 100;      %Number of hours of executing test per stage
quality             = 0.05;     %Final quality (%bugs)
max_lines           = 1.08e5;   %Number of codes in the program


%Create empty program and variables for storing the results
p                   = program;
hours               = 0;
prog_length         = [];
debugging_time      = [];
hour_steps          = [];
programming_time    = [];
quality_track       = [];

clear tests

while (p.lines < max_lines) %hours < 8000,    
    %Program for ones hour
    p       = programming(p, prog_time); %Regular programming
    %p       = programming(p, prog_time, 'lines_per_hour', 15, 'bugs_per_line', 0.01*300/356); %Pair programming
    hours   = hours + prog_time;
    
    %Write tests
    new_tests   = write_tests(p, writing_test_time, 'percent_coverage_old', 0.05, 'time_to_execute', 2, 'time_to_write', 10);
    if ~exist('tests', 'var')
        tests   = new_tests;
    else
        tests(end+1:end+length(new_tests)) = new_tests;
    end
    hours       = hours + writing_test_time;

    %Execute tests
    [bugs_found, p, tests, actual_execution_time]  = execute_tests(p, tests, executing_test_time, 'only_new_tests', 0);
    hours                   = hours + actual_execution_time;

    %Debugging
    [time_to_execute, p]    = debugging (p, bugs_found);
    hours                   = hours + time_to_execute;
    debugging_time(end+1)   = time_to_execute;
    
    %Store simulation data
    prog_length(end+1)      = p.lines;
    hour_steps(end+1)       = hours;
    programming_time(end+1) = prog_time;
    quality_track(end+1)    = (1-mean([p.bugs(:).fixed]));
end

%Keep fixing bugs until desired quality is reached
while (1-mean([p.bugs(:).fixed])) > quality
    new_tests   = write_tests(p, writing_test_time, 'max_line', p.lines, 'percent_coverage_old', 0.1, 'time_to_execute', 2, 'time_to_write', 10);
    hours       = hours + writing_test_time;
    [bugs_found, p, tests, actual_execution_time]  = execute_tests(p, new_tests, executing_test_time);
    hours                   = hours + actual_execution_time;
    [time_to_execute, p]    = debugging (p, bugs_found);
    hours                   = hours + time_to_execute;
    debugging_time(end+1)   = time_to_execute;
    
    prog_length(end+1)      = p.lines;
    hour_steps(end+1)       = hours;
    programming_time(end+1) = prog_time;
    quality_track(end+1)    = (1-mean([p.bugs(:).fixed]));
    
end

%Plot the results
figure(1)
hold on
plot(hour_steps, prog_length)
xlabel('Project time [hours]')
ylabel('Number of program lines')

figure(2)
hold on
plot(hour_steps, debugging_time)
xlabel('Project time [hours]')
ylabel('Total debugging time [hours]')

figure(3)
hold on
plot(hour_steps, quality_track)
xlabel('Project time [hours]')
ylabel('Fraction of unfixed bugs')

