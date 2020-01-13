# [PowerTunnel](https://github.com/krlvm/PowerTunnel): LibertyTunnel
Simple, scalable, cross-platform and effective solution against government censorship

LibertyTunnel is just a regular anti-censorship utility - if you want to control your traffic too, use [PowerTunnel](https://github.com/krlvm/PowerTunnel)

Designed for use in Russia, but can be used in other countries.

LibertyTunnel is PowerTunnel with accent on speed and simplicity, that's achieving by rejecting UI, traffic filter and additional output. Nothing extra.

PowerTunnel Wiki: https://github.com/krlvm/PowerTunnel/wiki/

### What is it
Nowadays many governments introducing censorship in the Internet and situation with Internet freedom becomes worse every day. DPI - Deep Packet Inspection systems - helps them do this.

This force the people to use ways to bypass locks like Proxy and VPN, that send your Internet traffic to the third parties and may be unstable.

LibertyTunnel is active DPI circumvention utility, that works only on your PC and don't send your traffic to third-party servers, respecting your privacy and isn't slowing down your internet connection.

### How it works?
LibertyTunnel establishes a HTTP/HTTPS-proxy on the your PC and directs your traffic through it.

More details on the Wiki: https://github.com/krlvm/PowerTunnel/wiki/How-it-works%3F

## How I can get it?
You have to compile a binary yourself

## Setup
LibertyTunnel setup is identical with PowerTunnel's, please, look at the Wiki: https://github.com/krlvm/PowerTunnel/wiki/Installation

## Launch arguments
You can specify a few params through the CLI:

```
java -jar LibertyTunnel.jar
-help - displays help
-full-chunking - enables chunking the whole packets
-mix-host-case - enables 'Host' header case mix (unstable)
-send-payload [length] - method to bypass HTTP blocking, 21 is recommended
-chunk-size [Size] - sets chunk size, minimum is 1, default is 5
-ip [IP Address]
-port [Port]
```

## Not working
Your ISP using another blocking method, that PowerTunnel cannot bypass right now.

Also, you can try launching the program with `-full-chunking` argument.

If you have offers, improvements or find bug you can leave an issue.

## Dependencies
We're using patched [LittleProxy](https://github.com/adamfisk/LittleProxy) as a Proxy Server.

Base PowerTunnel version is 1.7.2.
