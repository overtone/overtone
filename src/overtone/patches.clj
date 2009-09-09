(ns overtone.patches
  (:refer [clojure.core :rename {"filter" fltr}))

(def wave
  {:sine      0
   :square    1
   :saw       2
   :revsaw    3
   :triangle  4
   :pulse1    5
   :pulse2    6
   :noise     7
   :pinknoise 8})

(def filter
  {:lowpass  0 
   :bandpass 1 
   :highpass 2 
   :formant  3 ; sub only
})

(def sub
  {:orb
   {:mainvolume 1 :poly 3 
    :freqa 2 :freqb 1.501 
    :cutoff 0.6 :resonance 0.3 
    :ftype 2 
    :typea 2 :volumea 1 
    :attacka 0 :decaya 0.1 :sustaina 0.1 :releasea 0.5 
    :typeb 2 :volumeb 1
    :attackb 0 :decayb 0.1 :sustainb 0.1 :releaseb 0.5          
    :volumef 0.2
    :attackf 0.2 :decayf 0.2 :sustainf 0.1 :releasef 0.5 
    :lfodepth 0.5 :lfofreq 0.1 
    :slidea 0.02 :slideb 0.05 
    :distort 0.7 :crushfreq 0 :crushbits 0}
  :lazer-bug
   {:poly 3 :mainvolume 1
    :freqa 1 :freqb 0.501 :cutoff 0.2 :resonance 0.1 :ftype 2
    :typea 8 :attacka 0 :decaya 0.5 :sustaina 0 :releasea 0 :volumea 1 
    :typeb 8 :attackb 0 :decayb 0.2 :sustainb 0 :releaseb 0 :volumeb 1
    :attackf 0 :decayf 0.6 :sustainf 0 :releasef 0 :volumef 1
    :lfodepth 0 :lfofreq 5 :crushfreq 0 :crushbits 0
    :slidea 0 :slideb 0.2 :distort 0.7
    }})

(def drum 
  {:high-filtered
   {:kickfreqdecay 0.1 :kickdecay 0.5 :kickfreqvolume 2 :kickfreq 0.1
    :hat1decay 0.02 :hat1volume 2 :hat1cutoff 0.01 :hat1resonance 0.4
    :hat2decay 0.02 :hat2volume 2 :hat2cutoff 0.4 :hat2resonance 0.1
    :snaredecay 0.01 :snarevolume 2 :distort 0.5 
    :snareftype 0 :snarefilterattack 0 :snarefilterdecay 0.1 
    :snarefiltersustain 0 :snarefilterrelease 0 :snarefiltervolume 1
    :snarecutoff 0.2 :snareresonance 0.4
    :crushfreq 2 :crushbits 3.5 :poly 1 :mainvolume 1}
   })

