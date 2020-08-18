<div align="center">
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/images/logo.png" height="192px" width="192px" />
<br><h1>PowerTunnel</h1><br>
Simple, scalable, cross-platform and effective solution against government censorship
<!-- That does not mean the battle is finished -->
<!--<h3><b>Please, read <a href="https://gist.github.com/krlvm/76595f2fec7e23cf5e20f8ccfa43997a">important announcement</a></b></h3>-->

<a href="https://t.me/powertunnel_dpi">Telegram channel</a>
<br>
<a href="https://github.com/krlvm/PowerTunnel-Android">Check out the Android version!<a/>
<br><br>
<a href="https://github.com/krlvm/PowerTunnel/blob/master/LICENSE"><img src="https://img.shields.io/github/license/krlvm/PowerTunnel?style=flat-square" alt="License"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases/latest"><img src="https://img.shields.io/github/v/release/krlvm/PowerTunnel?style=flat-square" alt="Latest release"/></a>
<a href="https://github.com/krlvm/PowerTunnel/releases"><img src="https://img.shields.io/github/downloads/krlvm/PowerTunnel/total?style=flat-square" alt="Downloads"/></a>
<a href="https://github.com/krlvm/PowerTunnel/wiki"><img src="https://img.shields.io/badge/help-wiki-yellow?style=flat-square" alt="Help on the Wiki"/></a>
<br>
<img src="https://raw.githubusercontent.com/krlvm/PowerTunnel/master/images/ui.png" alt="PowerTunnel User Interface" />
</div>

### What is it
Nowadays Internet censorship is introducing in many countries: governments analyze and block traffic to this sites using DPI - Deep Packet Inspection systems, forcing you using circumvention utilities like VPN, for example. That approach have many disadvantages, most noticeable - connection speed slowdown. In addition, these services cannot guarantee work stability and your data confidence.

PowerTunnel is active DPI circumvention utility, that works only on your PC and don't send your traffic to third-party servers, respecting your privacy and do not slowing down your internet connection.

PowerTunnel is more than a regular anti-censorship utility - using it, you can monitor your traffic and block any connection, advertising, for example.

### How does it work?
PowerTunnel establishes a transparent proxy server on your PC and directs your traffic through it, applying DPI circumvention tricks.

PowerTunnel never decrypts your traffic, all code is open-source.\
You should install Root CA only in case you enabled [some specific options](https://github.com/krlvm/PowerTunnel/wiki/SNI-Tricks).

## How can I get it?
You can compile a binary yourself or download prepared binary [here](https://github.com/krlvm/PowerTunnel/releases).

### Setup
The installation process is described in detail [on the Wiki](https://github.com/krlvm/PowerTunnel/wiki/Installation).

### Doesn't work
Try to disable chunking mode and enable SNI Spoil.

Most likely your ISP blocked the website you need by IP address, so only encrypted tunnel (VPN/Tor) can help you.

## User interface
You can monitor network activity, block and whitelist websites through Java Swing-based user interface when console mode is off or through [PowerTunnel Monitor](https://github.com/krlvm/PowerTunnel/wiki/PowerTunnel-Monitor).

## Configuring PowerTunnel
### DNS over HTTPS
PowerTunnel supports DNS over HTTPS (DoH) - secure and fast DNS protocol.

DoH servers tested with PowerTunnel:
* Google (recommended):
  * https://dns.google/dns-query
  * https://8.8.8.8/dns-query
* Cloudflare - https://cloudflare-dns.com/dns-query (unstable)

You can test if you're using DoH [here](http://www.whatsmydnsserver.com/). If you have problems with DoH on PowerTunnel try disabling DNSSec mode.

### From User Interface
![User Interface for configuring the program](https://raw.githubusercontent.com/krlvm/PowerTunnel/master/images/options.png "User Interface for configuring the program")

Hover over option to get more info
### Launch arguments
You can specify some params that will override settings through CLI:

```
$ java -jar PowerTunnel.jar -help

Available arguments:
 -help                                display help
 -start                               starts server right after load
 -console                             console mode, without UI
 -government-blacklist-from [URL]     automatically fill government blacklist from URL
 -use-dns-sec                         enables DNSSec mode with the Google DNS servers
 -use-dns-server [URL]                overrides DNS settings (DNS over HTTPS supported)
 -disallow-invalid-packets            HTTP packets without Host header will be thrown out (unrecommended)
 -disable-chunking                    HTTPS: disables packet chunking (fragmentation)
 -full-chunking                       HTTPS: enables chunking the whole packets (requires chunking enabled)
 -chunk-size [size]                   HTTPS: sets size of one chunk
 -sni-trick [trick]                   HTTPS: enable SNI tricks: 1 - spoil, 2 - erase, 3 - fake; (requires Root CA installation)
 -sni-trick-fake-host [host]          HTTPS: host that will used with 'fake' SNI Trick
 -line-break-get                      HTTP:  inserts a line break before 'GET' method
 -space-after-get                     HTTP:  inserts a space after 'GET' method
 -apply-http-https                    HTTP:  apply enabled HTTP tricks to HTTPS
 -mix-host-case                       HTTP:  enables 'Host' header value case mix
 -complete-mix-host-case              HTTP:  complete 'Host' header value case mix
 -disable-mix-host-header-case        HTTP:  disables 'Host' header case mix
 -disable-dot-after-host-header       HTTP:  disables dot after host header
 -send-payload [length]               HTTP:  sends payload to bypass blocking, 21 is recommended
 -ip [IP Address]                     sets IP Address
 -port [Port]                         sets port
 -enable-journal                      enables PowerTunnel journal (when UI enabled)
 -enable-logs                         enables PowerTunnel logs (when UI enabled)
 -enable-log-to-file                  enables PowerTunnel logger and log file
 -with-web-ui [appendix]              enables Web UI at http://powertunnelmonitor[appendix].info
 -disable-auto-proxy-setup            disables auto proxy setup (supported OS: Windows)
 -enable-proxy-pac                    enables generation of PAC file on startup
 -auto-proxy-setup-win-ie             auto proxy setup using IE instead of native API on Windows
 -full-output-mirroring               fully mirrors system output to the log
 -set-scale-factor [n]                sets DPI scale factor (for testing purposes)
 -disable-tray                        disables tray icon
 -disable-native-lf                   disables native L&F (when UI enabled)
 -disable-ui-scaling                  disables UI scaling (when UI enabled)
 -disable-updater                     disables the update notifier
 -debug                               enables debug
```

## Spin-off projects
* [PowerTunnel for Android](https://github.com/krlvm/PowerTunnel-Android) - an Android port
* [Invader](https://github.com/krlvm/Invader) - an effective MITM utility and script injector
* [LibertyTunnel](https://github.com/krlvm/PowerTunnel/tree/libertytunnel) - a lightweight edition of PowerTunnel 

## Dependencies
* [LittleProxy](https://github.com/adamfisk/LittleProxy) - proxy server
  * Replaced with a [forked version](https://github.com/mrog/LittleProxy) since v1.11
* [LittleProxy-MITM](https://github.com/ganskef/LittleProxy-mitm) - LittleProxy SSL extension
* [dnsjava](https://github.com/dnsjava/dnsjava) - DNS library, DoH realization
* [dnssecjava](https://github.com/ibauersachs/dnssecjava) - DNSSec realization for dnsjava
* [DNSSEC4J](https://github.com/adamfisk/DNSSEC4J) - DNSSec realization for LittleProxy
* [Java Native Access](https://github.com/java-native-access/jna) - library for accessing system API
* [SwingDPI](https://github.com/krlvm/SwingDPI) - High DPI scaling

### Credits
* [blockcheck](https://github.com/ValdikSS/blockcheck)