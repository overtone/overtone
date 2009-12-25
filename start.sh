#!/bin/sh

killall -9 jackd
jackd -t 5000 -d alsa --rate 44100 --softmode --inchannels 0 --outchannels 2 &
echo "play" | jack_transport
#jack_connect system:capture_1 system:playback_1
#jack_connect system:capture_2 system:playback_2

killall -9 pulseaudio
pulseaudio -nF ~/.jack.pa --start
