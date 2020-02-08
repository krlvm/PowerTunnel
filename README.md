<div align="center">
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/libertytunnel/images/logo.png" height="192px" width="192px" />
<br><h1>LibertyTunnel</h1><br>
A lightweight edition of <a href="https://github.com/krlvm/PowerTunnel">PowerTunnel</a> - simple, scalable, cross-platform and effective solution against government censorship
<br><br>
<a href="https://github.com/krlvm/PowerTunnel/blob/master/LICENSE"><img src="https://img.shields.io/github/license/krlvm/PowerTunnel?style=flat-square" alt="License"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases"><img src="https://img.shields.io/github/v/release/krlvm/PowerTunnel?style=flat-square" alt="Latest release"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases"><img src="https://img.shields.io/github/downloads/krlvm/PowerTunnel/total?style=flat-square" alt="Downloads"/></a>
<a href="https://github.com/krlvm/PowerTunnel/wiki"><img src="https://img.shields.io/badge/help-wiki-yellow?style=flat-square" alt="Help on the Wiki"/></a>
</div>

### What is it
LibertyTunnel is a lightweight and debloated version of <a href="https://github.com/krlvm/PowerTunnel">PowerTunnel</a>, focused only on anti-censorship and speed.

Nowadays Internet censorship is introducing in many countries: governments analyze and block traffic to this sites using DPI - Deep Packet Inspection systems, forcing you using circumvention utilities like VPN, for example. That approach have many disadvantages, most noticeable - connection speed slowdown. In addition, these services cannot guarantee work stability and your data confidence.

LibertyTunnel is active DPI circumvention utility, that works only on your PC and don't send your traffic to third-party servers, respecting your privacy and do not slowing down your internet connection.

LibertyTunnel is more than a regular anti-censorship utility - using it, you can monitor your traffic and block any connection, advertising, for example.

### How does it work?
LibertyTunnel establishes a transparent proxy server on your PC and directs your traffic through it, applying DPI circumvention tricks.

## How can I get it?
You have to compile a binary yourself - you need Java JDK 7+

### Setup
The installation process, mostly, is identical to the PowerTunnel installation process and described in detail [on the Wiki](https://github.com/krlvm/PowerTunnel/wiki/Installation).

The only difference is that you have to launch LibertyTunnel via command line/terminal.

### Doesn't work
Most likely your ISP blocked the website you need by IP address, so only encrypted tunnel (VPN/Tor) can help you.

Also, you can try launching LibertyTunnel with `-full-chunking` argument.

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
Your ISP using another blocking method, that LibertyTunnel cannot bypass right now.

Also, you can try launching the program with `-full-chunking` argument.

If you have offers, improvements or find bug you can leave an issue.

## Dependencies
* [LittleProxy](https://github.com/adamfisk/LittleProxy) with some [patches](https://github.com/krlvm/PowerTunnel/tree/libertytunnel/src/org/littleshoot/proxy/impl) - proxy server

Base PowerTunnel version is 1.7.2.
