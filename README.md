# Schedulo
#### also (formerly) known as sjsapp

Android app to check St. John's School website from mobile

Pulls in `htmltextview` 3rd party dep, all other deps are support libs

Build system: I use `gradle` but build system is not included in this repo. Feel free to build however you want, but it's not my problem

What mostly works:
- schedule page
- assignments page
- assignments sorting (by due, assigned, class)
- assignment details view
- assignment links
- assignment downloads
- changing assignment status (to do, in progress, completed)
- profile page
- settings page
- log in validation
- storing credentials

What sometimes doesn't work:
- nav pullout menu sometimes doesn't show the right highlight
- assignment status changer let's user change "graded" assignment status - very rare
- profile page hangs on populating imageview

What needs to be done:
- option for notifying before next class
- assignments view by day (still considering)
