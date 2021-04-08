# What's This?

A quick little project I created to forward my phone notifications into [XSOverlay](https://store.steampowered.com/app/1173510/XSOverlay/),  which is a fantastic tool for placing + using your desktops + windows in VR, as overlays. When I'm in VR I mirror my phone onto my PC using (the also fantastic) [scrcpy](https://github.com/Genymobile/scrcpy), and then use that in VR by XSOverlay. This is great if I need to quickly respond to a message, but I need the notification from my phone to tell me someone messaged me in the first place.

# How Does This Work?

This API was recently added to support popping up notifications in XSOverlay: [Notifications API](https://xiexe.github.io/XSOverlayDocumentation/#/NotificationsAPI). It allows pushing them by UDP to a specific port, but only from localhost. So this project has two parts:

 1. A tiny little server that runs on your PC, written using [ktor](https://ktor.io/) (because it's such an easy way to create a little HTTP server). This will forward notifications it is sent onto XSOverlay's UDP port.
 2. A little Android app that will listen to notifications on the phone and send them to that server. You need to grant it permission to read notifications for that to work. That's buried in your phone Settings somewhere, with a big ol' warning about privacy when you grant it. This won't actually see any notifications and thus won't do anything until you do grant it. When you do it'll forward them to the server you specify (enter your PC's name/IP). The app has one screen that allows changing a few options, which should be pretty self-explanatory.

# How to use?
Right now, open the project in IntelliJ IDEA / Android Studio and you'll see two run configurations, one for the server and one for the app. The app one has some arguments specified that initialise certain values (host, port, whether to enable).

# But I'm not a developer
Sorry, that's all I've done so far. I might get around to packaging it up a bit but I really just made this for my own use, and thinking about how to properly package the server and app and install the app and all that... great effort, no benefit to me...

# This sucks, your code sucks, you should have...
I hacked this together because I really love XSOverlay and was excited to see that API added, and wanted to make use of it right away. It's rough, but it's tiny, and does what I wanted. Then I figured maybe someone else might like to make use of it too. Free to take, modify and use as you wish. You accept all responsibility in doing so.