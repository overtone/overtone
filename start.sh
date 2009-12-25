#!/bin/sh

killall -9 jackd
jackd -t 5000 -d alsa --rate 44100 --softmode --inchannels 2 --outchannels 2 &
echo "play" | jack_transport

killall -9 pulseaudio
pulseaudio -nF ~/.jack.pa --start
