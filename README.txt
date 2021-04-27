This tool automates the booking for BoulderWorld.

Requirements:
- have Chrome v.88 or v. 89 or v.90 installed (to check version in Chrome: dots menu >> Help >> About Google Chrome)
- have Java JRE installed

1. Modify example.json file to fit your data
2. Run the jar from the console: java -jar ./boulderworld_autobooker.jar
3. Copy content of autobooker_drivers to requested path and hit enter
4. Follow the instructions

Notes:
- bookings are opened on Mondays 12 PM (for the following week's weekdays), and Saturdays 12 PM (for the following weekend).
- this bot is mostly compatible for bookings on weekdays (rules for weekends have not been implemented yet)
- run the bot on the Monday of the previous week of your slot (otherwise it will refuse to run)
- the bot will sleep until 11:59AM. It will then try to find your slot. If it doesn't find it, it will refresh and try again (as long as you don't stop it)