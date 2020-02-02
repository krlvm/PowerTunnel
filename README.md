# PowerTunnel
![License](https://img.shields.io/github/license/krlvm/PowerTunnel?style=flat-square)
![Latest release](https://img.shields.io/github/v/release/krlvm/PowerTunnel?style=flat-square)
![Downloads](https://img.shields.io/github/downloads/krlvm/PowerTunnel/total?style=flat-square)
![Help](https://img.shields.io/badge/help-wiki-yellow?style=flat-square)

Simple, scalable, cross-platform and effective solution against government censorship
<p align="center">
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/images/logo.png" />
<br>
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/images/ui.png" alt="PowerTunnel User Interface" />
</p>

### What is it
Nowadays Internet censorship is introducing in many countries: governments analyze and block traffic to this sites using DPI - Deep Packet Inspection systems, forcing you using circumvention utilities like VPN, for example. That approach have many disadvantages, most noticeable - connection speed slowdown. In addition, these services cannot guarantee work stability and your data confidence.

PowerTunnel is active DPI circumvention utility, that works only on your PC and don't send your traffic to third-party servers, respecting your privacy and do not slowing down your internet connection.

PowerTunnel is more than a regular anti-censorship utility - using it, you can monitor your traffic and block any connection, advertising, for example.

### How does it work?
PowerTunnel establishes a transparent proxy server on your PC and directs your traffic through it, applying DPI circumvention tricks.

## How can I get it?
You can compile a binary yourself or download prepared binary [here](https://github.com/krlvm/PowerTunnel/releases).

## Setup
Please, look at the Wiki: https://github.com/krlvm/PowerTunnel/wiki/Installation

## User interface
You can monitor network activity, block and whitelist websites through Java Swing-based user interface when console mode is off or through PowerTunnel Monitor - a new Web-based user interface (v1.7+), available from fake URL http://powertunnelmonitor.info if you aren't disabled it using argument `-disable-web-ui`.

## Launch arguments
You can specify a few params through the CLI:

```
java -jar PowerTunnel.jar
-help - displays help
-start - starts server right after load
-console - console mode, without UI
-full-chunking - enables chunking the whole packets
-mix-host-case - enables 'Host' header case mix (unstable)
-send-payload [length] - method to bypass HTTP blocking, 21 is recommended
-chunk-size [Size] - sets chunk size, minimum is 1, default is 5
-ip [IP Address]
-port [Port]
-with-web-ui [appendix] - enables Web UI at http://powertunnelmonitor[appendix].info
-disable-native-lf - disables native L&F (when UI enabled)
-disable-ui-scaling - disables UI scaling (when UI enabled)
-disable-updater - disables the update notifier
-debug
```

## Doesn't work
Most likely your ISP blocked the website you need by IP address, so only encrypted tunnel (VPN/Tor) can help you.

Also, you can try launching PowerTunnel with `-full-chunking` argument.

## Spin-off projects
* [Invader](https://github.com/krlvm/Invader) - an effective MITM utility and script injector
* [LibertyTunnel](https://github.com/krlvm/PowerTunnel/tree/libertytunnel) - a lightweight edition of PowerTunnel 

## Dependencies
* [LittleProxy](https://github.com/adamfisk/LittleProxy) with some patches - proxy server
* [SwingDPI](https://github.com/krlvm/SwingDPI) - HiDPI scaling
