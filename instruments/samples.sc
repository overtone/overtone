s.boot;

(
SynthDef("play-mono", {| out = 0, bufnum = 0 |
        Out.ar(out, Pan2.ar( 
                PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), doneAction:2),
                0)
            )
        }).store;
        )

SynthDef("play-stereo", {| out = 0, bufnum = 0 |
        Out.ar(out, Pan2.ar( 
                PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), doneAction:2),
                0)
            )
        }).store;
        )

b = Buffer.read(s, "/home/rosejn/foo.wav"); // remember to free the buffer later.


c = Synth("play-mono", ["bufnum", 1]);
s.sendMsg("/s_new", "play-mono", x = s.nextNodeID, 1, 2);

// allocate a disk i/o buffer
s.sendMsg("/b_alloc", 0, 65536, 1);

// open an input file for this buffer, leave it open
s.sendMsg("/b_read", 0, "/home/rosejn/foo.wav", 0, 65536, 0, 1);


s.sendMsg("/b_close", 0); // close the file (very important!)

// again 
// don't need to reallocate and Synth is still reading
s.sendMsg("/b_read", 0, "/home/rosejn/foo.wav", 0, 0, 0, 1);

s.sendMsg("/n_free", x); // stop reading

s.sendMsg("/b_close", 0); // close the file.

s.sendMsg("/b_free", 0); // frees the buffer

