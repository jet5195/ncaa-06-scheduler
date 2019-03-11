# ncaa-06-scheduler
Program that allows you to edit the schedule for NCAA Football 06 (and possibly other NCAA PS2 games)
Currently, this program takes in 2 excel files (demos are in the source/main/resources folder, you will need to download these separately as a file picker is now used to select your files).

1. My_Custom_Conferences: this file contains the data for every FBS school, their conference, their rivals, etc
2. SCHED: This is the schedule exported from NCAA Football. 

This program creates a new schedule file that can be converted to a csv file and then imported into PS2 NCAA Football dynasties at the beginning of seasons. (Pre-season is the only time I have attempted to modify the schedule)
If you want to use this program, you should modify the My_Custom_Conferences file to be set to your conferences and also export your schedule from your dynasty.

How to use?
1. Download the ncaa_06_scheduler_jar folder. Make sure you store both the batch file and the jar in the same folder
2. Double click the batch file and it should run.
3. You will probably see errors about log4j not being found, I haven't figured out how to get it to work in a jar. You may have to hit enter to bypass these, but don't worry. These errors are not important.

OR
1. Clone everything to your IDE.
2. Run the application there.

