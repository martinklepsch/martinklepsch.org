---
layout: post
title: Blocking Spammers Within Your Network
categories: draft
---
Julius Werner: naja, du musst auf hubble alle ausgehenden pakete auf tcp port 25 die nicht von domino
kommen sperren

Martin: damit hätten wir aber nicht das ssh problem gelöst oder?
Martin: ja, das finde ich schon raus...

Julius Werner: nein, aber damit könnte von anderen ips und aus dem
tower-netz heraus keiner mehr spammen

Julius Werner: und dann musst du auf domino noch mit iptables sicher
stellen, dass nur prozesse unter dem debian-exim user auf port 25 senden dürfen

Julius Werner: mit der änderung auf hubble wär ich übrigens vorsichtig,
das solltest du vielleicht lieber mal im tower probieren... sonst musst du, falls du etwas
falsch machst, vielleicht morgen früh in den tower hetzen

