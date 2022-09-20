# What Is It?

Small app + desktop server to forward my phone notifications into [XSOverlay](https://store.steampowered.com/app/1173510/XSOverlay/), which is a fantastic tool for placing + using your desktops + windows in VR, as overlays. When I'm in VR I mirror my phone onto my PC using (the also fantastic) [scrcpy](https://github.com/Genymobile/scrcpy), and then use that in VR by XSOverlay. This is great if I need to quickly respond to a message, but I need the notification from my phone to tell me someone messaged me in the first place.

# How Does This Work?

This API was recently added to support popping up notifications in XSOverlay: [Notifications API](https://xiexe.github.io/XSOverlayDocumentation/#/NotificationsAPI). It allows pushing them by UDP to a specific port, but only from localhost. So this project has two parts:

 1. A tiny little server that runs on your PC, written using [ktor](https://ktor.io/) (because it's such an easy way to create a little HTTP server). This will forward notifications it is sent onto XSOverlay's UDP port.
 2. A little Android app that will listen to notifications on the phone and send them to that server. You need to grant it permission to read notifications for that to work. The app has one screen that allows changing a few options, which should be pretty self-explanatory.

# Installation / Usage
Right now, open the project in IntelliJ IDEA / Android Studio and you'll see two run configurations, one for the server and one for the app. The app one has some arguments specified that initialise certain values (host, port, whether to enable).

# Not a developer?
Sorry, that's all I've done so far. I might get around to packaging it up a bit but I really just made this for my own use.

# Criticisms / Requests
I hacked this together because I use XSOverlay and was excited to see that API added, and wanted to make use of it right away. It's rough, but it's tiny, and does what I wanted. I'm sharing it here in case it's of use to anyone else. Free to take, modify and use as you wish. You accept all responsibility in doing so. I have since patched it up a bit and may yet do so more, just for the practice (eg. XML UI + Activity boilerplate to Compose).
