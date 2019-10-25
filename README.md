# PowerTunnel
Simple, scalable, cross-platform and effective solution against government censorship

Designed for use in Russia, but can used but can be used in other countries.

###### PowerTunnel in action
![PowerTunnel in action: UI](https://raw.githubusercontent.com/krlvm/PowerTunnel/master/ui.png "User Interface")
![PowerTunnel in action: Logs](https://raw.githubusercontent.com/krlvm/PowerTunnel/master/log.png "Logs")

### What is it
Nowadays many governments introducing censorship in the Internet and situation with Internet freedom becomes worse every day. DPI - Deep Packet Inspection systems - helps them do this.

This force the people to use ways to bypass locks like Proxy and VPN, that send your Internet traffic to the third parties and may be unstable.

PowerTunnel is active DPI circumvention utility, that works only on your PC and don't send your traffic to third-party servers.

### How it works?
PowerTunnel establishes a HTTP/HTTPS-proxy on the your PC and directs your traffic through it.

On the proxy PowerTunnel throws out DPI-redirects to the ISP stub and uses methods like modifying `Host` header in a HTTP request and fragmentation of a HTTPS packet, keeping your HTTPS data encrypted.

## How I can get it?
You can compile yourself or download prepared binary from the `Releases` tab.

## Setup
At first, you need Java 7 and later installed on your PC.

Download `.jar` from the `Releases` tab and launch it manually for starting server on `127.0.0.1:8085` or execute `java -jar PowerTunnel.jar [IP] [PORT]` in the terminal.

Then go to the your system settings and set global proxy to address, that will be shown after launching the program.

If you're using Windows 10 I highly recommend you to set settings for don't use proxy for `activity.windows.com`.

## I did everything, but it does not work
Yes, you should fill `government-blacklist.txt` file for working. Google around the Internet and found list of blocked websites in your country, then copy them in the file. They shouldn't contain `http://`, `https://` or `www.`.

If you're living in Russia, you can download government blacklist from https://antizapret.prostovpn.org/domains-export.txt

## Still not working
It's sad but true - your ISP using another blocking method, that PowerTunnel cannot bypass right now.

If you have offers, improvements or find bug you can leave an issue.

## Dependencies
We're using patched LittleProxy (`org.littleshoot:littleproxy:1.1.2`, https://github.com/adamfisk/LittleProxy) as Proxy Server.

We're using SwingDPI (https://github.com/krlvm/SwingDPI) for scaling UI on a HiDPI screens.