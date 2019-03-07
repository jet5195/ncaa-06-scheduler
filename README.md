# ncaa-06-scheduler
Program that allows you to edit the schedule for NCAA Football 06 (and possibly other NCAA PS2 games)
Currently, this program takes in 2 excel files (both bundled in the source/main/resources folder).
1. My_Custom_Conferences: this file contains the data for every FBS school, their conference, their rivals, etc
2. SCHED: This is the schedule exported from NCAA Football. 

This program creates a new schedule file that can be converted to a csv file and then imported into PS2 NCAA Football dynasties at the beginning of seasons. (Pre-season is the only time I have attempted to modify the schedule)
If you want to use this program, you should modify the My_Custom_Conferences file to be set to your conferences and also export your schedule from your dynasty.
Right now, both of these files need to be in the resources folder with specific names. I plan to change this, but for now it's easier for me to just modify the String that tells the program what file to look for.
